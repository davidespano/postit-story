package com.vuforia.samples.VuforiaSamples.app.PostItNote;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import android.util.Log;

import com.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.vuforia.samples.VuforiaSamples.R;
import com.vuforia.samples.VuforiaSamples.ui.ActivityList.ActivityLauncher;
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuInterface;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by alessiosperoni on 12/09/16.
 */
public class Help extends Activity implements View.OnClickListener, SampleAppMenuInterface {

    private WebView mAboutWebText;
    private Button mStartButton;
    private TextView mAboutTextTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        Bundle extras = getIntent().getExtras();
        //String webText = extras.getString("HELP_TEXT");
        String webText;
        webText = "raw/hlp_about.html";
        //webText = "";
        mAboutWebText = (WebView) findViewById(R.id.help_text);

        AboutWebViewClient aboutWebClient = new AboutWebViewClient();
        mAboutWebText.setWebViewClient(aboutWebClient);

        String aboutText = "";
        /*try
        {
            //InputStream is = getAssets().open(webText);
            InputStream is = getAssets().open("hlp_about.html");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null)
            {
                aboutText += line;
            }
        } catch (IOException e)
        {
            Log.e("HELP", "About html loading failed");
        }
        */
        aboutText="<html>\n" +
                "<head>\n" +
                "    <meta name = \"viewport\" content = \"user-scalable=no\">\n" +
                "    <meta name=\"apple-mobile-web-app-capable\" content=\"yes\"/>\n" +
                "    <style type=\"text/css\">\n" +
                "        html {\n" +
                "            -webkit-text-size-adjust: none; /* Never autoresize text */\n" +
                "            word-wrap:break-word; /* break the word in an another line if it's too long */\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "\t<div style=\"font-family:Helvetica; font-size:14px; line-height:18px; margin: 20px auto;\">\n" +
                "        <div style=\"padding-left:15px; padding-right:15px;\">\n" +
                "\n" +
                "            <p>Grazie a dei codici QR sarà possibile visualizzare il fumetto con all'interno la parola descrittiva dell'oggetto qul quale verrà apposto il marker QR.</p>\n" +
                "            \n" +
                "            <p style=\"margin-bottom:3px;\"><strong>Funzioni chiave</strong></p>\n" +
                "            \n" +
                "            <ul style=\"padding-left:16px;margin-top:0px;\">\n" +
                "                <li>L'applicazione è in grado di riconoscere contemporaneamente più marker.</li>\n" +
                "            </ul>\n" +
                "\n" +
                "            <p style=\"margin-bottom:3px;\"><strong>Credits:</strong></p>\n" +
                "            <p style=\"margin-top:0px;\"><a href=\"www.google.com\">Post-Is Note</a></p>\n" +
                "\n" +
                "            <p style=\"margin-bottom:3px;\"><strong>Istruzioni</strong></p>\n" +
                "            <ul style=\"padding-left:16px;margin-top:0px;\">\n" +
                "                <li>Punta la telecamera verso il marker</li>\n" +
                "                <li>Tocca una volta lo schermo per mettere a fuoco</li>\n" +
                "                <li>Tocca la parola per ascoltare la pronuncia</li>\n" +
                "                <li>Tocca due volte lo schermo per accedere al menu</li>\n" +
                "            </ul>\n" +
                "            \n" +
                "            <p><a href=\"www.google.com\">Termini ed utilizzo</a></p>\n" +
                "            \n" +
                "            <p>&copy; Nessuno per ora</p>\n" +
                "\n" +
                "        </div>\n" +
                "\t</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>\t\n";
        mAboutWebText.loadData(aboutText, "text/html", "UTF-8");

        mStartButton = (Button) findViewById(R.id.button_OK);
        mStartButton.setOnClickListener(this);

        mAboutTextTitle = (TextView) findViewById(R.id.help_title);
        //mAboutTextTitle.setText(extras.getString("HELP_TITLE"));
        mAboutTextTitle.setText("Help");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.button_OK:
                finish();
                break;
        }
    }

    @Override
    public boolean menuProcess(int command) {
        {
            boolean result = true;

            switch (command) {
                case -1:
                    finish();
                    break;

            }

            return result;
        }
    }
    private class AboutWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }
}
