package neopets.mluansing.com.magmapooltimefinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // layout vars
    private WebView webView;
    private FloatingActionButton fab;

    private boolean isRunning = false;
    final Handler handler = new Handler();
    private static final int REFRESH_INTERVAL = 5;
    private static final String URL_TO_RELOAD = "http://www.neopets.com/magma/pool.phtml";
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.US);

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 121915;

    // TODO: allow custom setting of timer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeDownloadPermissions();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRunning) {
                    Toast.makeText(MainActivity.this, "Turning off auto-refresh timer", Toast.LENGTH_SHORT).show();

                    disableAutoRefreshTimer();

                    fab.setImageResource(R.drawable.ic_start);
                    isRunning = !isRunning;
                } else {
                    Toast.makeText(MainActivity.this, "Starting auto-refresh timer for every 5 mins", Toast.LENGTH_SHORT).show();

                    enableAutoRefreshTimer();

                    fab.setImageResource(R.drawable.ic_stop);
                    isRunning = !isRunning;
                }
            }
        });

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.loadUrl(URL_TO_RELOAD);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO: permission granted to store screenshots -- do nothing?
            } else {
                // user refused to grant permission
                Toast.makeText(MainActivity.this, R.string.download_permission_needed, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            Log.d(TAG, "Manually reloading web page at " + getTimestamp());

            webView.reload();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeDownloadPermissions() {
        // ask for run time permission if running sdk 23+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // need user permission

            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // can explain why permissions needed here
            }

            // ask for permission
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // TODO: permission granted to store to device -- do nothing
        }
    }

    private void enableAutoRefreshTimer() {
        // screenshot after webpage loaded
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                takeScreenshot();
            }
        });

        // set auto-refresh timer for every 5 mins
        webView.reload();

        reloadWebView();
    }

    public void reloadWebView() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Reloading web page at " + getTimestamp());

                webView.reload();

                // reschedule next timer
                reloadWebView();
            }
        }, REFRESH_INTERVAL * 60 * 1000);
    }

    private void takeScreenshot() {
        // generate filename with timestamp
        String timestamp = getTimestamp();

        Log.d(TAG, "Taking screenshot at " + timestamp);

        // store screenshot
        Bitmap b = Screenshot.takeScreenshotOfRootView(webView);
        storeScreenshot(b, timestamp);
    }

    public void storeScreenshot(Bitmap bitmap, String filename) {
        // ATTEMPT 4
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/Screenshots/MagmaPoolTimeFinder");
        myDir.mkdirs();
        File file = new File(myDir, filename + ".jpg");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tell the media scanner about the new file so that it is immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path);
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }

    private void disableAutoRefreshTimer() {
        // remove page reload listener
        webView.setWebViewClient(null);

        // remove future timers
        handler.removeCallbacksAndMessages(null);
    }

    private String getTimestamp() {
        return TIMESTAMP_FORMAT.format(new Date());
    }

}
