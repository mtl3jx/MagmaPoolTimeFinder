package neopets.mluansing.com.magmapooltimefinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private static final int REFRESH_INTERVAL = 1; // TODO: change to 5 mins
    private static final String URL_TO_RELOAD = "http://www.neopets.com/magma/pool.phtml";
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ssZ", Locale.US);

    // TODO: allow custom setting of timer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRunning) {
                    Toast.makeText(MainActivity.this, "Turning off auto-refresh timer", Toast.LENGTH_SHORT).show();

                    enableAutoRefreshTimer();

                    fab.setImageResource(R.drawable.ic_start);
                    isRunning = !isRunning;
                } else {
                    Toast.makeText(MainActivity.this, "Starting auto-refresh timer for every 5 mins", Toast.LENGTH_SHORT).show();

                    disableAutoRefreshTimer();
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

        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // screenshot after webpage loaded
                takeScreenshot();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableAutoRefreshTimer() {
        // set auto-refresh timer for every 5 mins
        webView.loadUrl(URL_TO_RELOAD);

        reloadWebView();
    }

    public void reloadWebView() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Reloading web page at " +  getTimestamp());

                webView.loadUrl(URL_TO_RELOAD);

                // reschedule next timer
                reloadWebView();
            }
        }, REFRESH_INTERVAL * 60 * 1000);
    }

    private void takeScreenshot() {
        // generate filename with timestamp
        String timestamp = getTimestamp();

        Log.d(TAG, "Taking screenshot at " +  timestamp);

        // store screenshot
        Bitmap b = Screenshot.takeScreenshotOfRootView(webView);
        storeScreenshot(b, timestamp);
    }

    public void storeScreenshot(Bitmap bitmap, String filename) {
//        ATTEMPT 1
//        String path = Environment.getExternalStorageDirectory().toString() + "/" + filename;
//        OutputStream out = null;
//        File imageFile = new File(path);
//
//        try {
//            out = new FileOutputStream(imageFile);
//            // choose JPEG format
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            out.flush();
//        } catch (FileNotFoundException e) {
//            Log.e(TAG, "FileNotFoundException" + e.getMessage());
//        } catch (IOException e) {
//            // manage exception ...
//            Log.e(TAG, "IOException " + e.getMessage());
//        } finally {
//
//            try {
//                if (out != null) {
//                    out.close();
//                }
//
//            } catch (Exception exc) {
//                Log.e(TAG, exc.getMessage());
//            }
//
//        }

//        ATTEMPT 2
//        File myDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//
//        if (!myDir.exists()) {
//            myDir.mkdirs();
//        }
//
//        File file = new File (myDir, filename);
//        if (file.exists ())
//            file.delete ();
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            out.flush();
//            out.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        ATTEMPT 3
        final String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(dirPath);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dirPath, filename);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void disableAutoRefreshTimer() {
        // remove future timers
        handler.removeCallbacksAndMessages(null);
    }

    private String getTimestamp() {
        return TIMESTAMP_FORMAT.format(new Date());
    }

}
