package ess.imu_logger.app.markdownViewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.markdown4j.Markdown4jProcessor;

import ess.imu_logger.app.ApplicationSettings;
import ess.imu_logger.app.R;

public abstract class MarkdownViewerActivity extends Activity {
    private final static String TAG = MarkdownViewerActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_screen);


        initializeWebView(markdownFile());
    }

    protected abstract String markdownFile();

    private void initializeWebView(String file) {
        String html = "404";
        WebView wv = (WebView) findViewById(R.id.webView);
        StringBuilder sb = new StringBuilder();

        try {
            InputStream is = getAssets().open("html/layout.html");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
            is.close();
            br.close();
            html = sb.toString();

            sb = new StringBuilder();
            is = getAssets().open("markdown/" + file + ".md");
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null)
                sb.append(line + "\n");
            is.close();
            br.close();
            String md = sb.toString();

            html = html.replace("%%content%%", new Markdown4jProcessor().process(md));


        } catch (IOException e) {
            e.printStackTrace();
        }


        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wv.loadDataWithBaseURL("file:///android_asset/html/", html, null, "utf8", "file:///android_asset/html/help.html");
        //wv.loadUrl("file:///android_asset/html/help.html");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_markdown_viewer, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ApplicationSettings.class);
            startActivity(intent);
        } else if (id == R.id.action_help) {
            Intent intent = new Intent(this, HelpScreen.class);
            startActivity(intent);
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutScreen.class);
            startActivity(intent);
        } else if (id == R.id.action_introduction) {
            Intent intent = new Intent(this, IntroductionScreen.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
