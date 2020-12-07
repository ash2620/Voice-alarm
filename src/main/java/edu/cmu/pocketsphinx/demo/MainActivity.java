/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package edu.cmu.pocketsphinx.demo;

import android.Manifest;
import android.app.Activity;
//import android.content.Context;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
//import android.media.Ringtone;
//import android.media.RingtoneManager;
//import android.net.Uri;
//import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.PersistableBundle;
//import android.support.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
//import android.widget.TextView;
//import android.widget.Toast;


import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

//import edu.cmu.pocketsphinx.Assets;
//import edu.cmu.pocketsphinx.Hypothesis;
//import edu.cmu.pocketsphinx.RecognitionListener;
//import edu.cmu.pocketsphinx.SpeechRecognizer;
//import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

//import static android.widget.Toast.makeText;

public class MainActivity extends Activity
    {
        Button b;
        File f;
        Scanner sc;
        FileWriter fw;
        FileReader fr;
        Context c;
        EditText e;
        MyDBHandler dbHandler;
        TextView t;
        Context context;
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            c = this;
            try {
                DisplayKeyword();
            }catch (Exception e){

            }
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO} , 1);
                return;
            }
        }

        public void StartService(View view){
            b = (Button) findViewById(R.id.button2);
            if (b.getText().toString().equalsIgnoreCase(getString(R.string.start))) {
                stopService(new Intent(MainActivity.this , MyService.class));
                startService(new Intent(MainActivity.this, MyService.class));
                b.setText("Stop");
            }
            else{
                stopService(new Intent(MainActivity.this , MyService.class));
                b.setText("Start");
            }
        }

        public void ChangeKeyword(View v){
            String a = "";
            String filepath = c.getFilesDir().getPath().toString()+"/keyword.txt";
            f = new File(c.getFilesDir() , "keyword.txt");
            e = (EditText) findViewById(R.id.editText);
            t = (TextView) findViewById(R.id.textView);
            String keyword = e.getText().toString();
            if(!(e.getText().toString().equals("")))
                try {
                    FileOutputStream fos = openFileOutput("keyword.txt", Context.MODE_PRIVATE);
                    fos.write(keyword.getBytes());

                }catch (IOException e){
                    e.printStackTrace();
                }
            DisplayKeyword();
            e.setText("");
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();

        }
        private void DisplayKeyword(){
            try{
                FileInputStream fis = c.openFileInput("keyword.txt");
                InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
                StringBuilder stringBuilder = new StringBuilder();
                try{
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line = reader.readLine();
                    stringBuilder.append(line).append('\n');
                    // while (line != null) {
                    //   stringBuilder.append(line).append('\n');
                    // line = reader.readLine();
                    //}
                }catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    t.setText("Keyphrase is "+stringBuilder.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        /* Named searches allow to quickly reconfigure the decoder */
    /*
    private static final String KWS_SEARCH = "wakeup";
    //private static final String FORECAST_SEARCH = "forecast";
    //private static final String DIGITS_SEARCH = "digits";
    //private static final String PHONE_SEARCH = "phones";
    //private static final String MENU_SEARCH = "menu";
    Context mContext;
    /* Keyword we are looking for to activate menu */
    /*
    private static final String KEYPHRASE = "Hello";

    /* Used to handle permission request */
    /*
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mContext = getApplicationContext();
        // Prepare the data for UI
        captions = new HashMap<>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        //captions.put(MENU_SEARCH, R.string.menu_caption);
        //captions.put(DIGITS_SEARCH, R.string.digits_caption);
        //captions.put(PHONE_SEARCH, R.string.phone_caption);
        //captions.put(FORECAST_SEARCH, R.string.forecast_caption);
        setContentView(R.layout.main);
        ((TextView) findViewById(R.id.caption_text))
                .setText("Preparing the recognizer");

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<PocketSphinxActivity> activityReference;
        SetupTask(PocketSphinxActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                ((TextView) activityReference.get().findViewById(R.id.caption_text))
                        .setText("Failed to init recognizer " + result);
            } else {
                activityReference.get().switchSearch(KWS_SEARCH);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                new SetupTask(this).execute();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    /*
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.trim().equalsIgnoreCase(KEYPHRASE)){
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            Ringtone ringtone = RingtoneManager.getRingtone(mContext, uri);
            ringtone.play();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ringtone.stop();

        }



        else
            ((TextView) findViewById(R.id.result_text)).setText(text);


    }

    /**
     * This callback is called when we stop the recognizer.
     */
    /*
    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) findViewById(R.id.result_text)).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            if (text.trim().equalsIgnoreCase("Hello")) {
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                Ringtone ringtone = RingtoneManager.getRingtone(mContext, uri);
                ringtone.play();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ringtone.stop();
            }
        }
    }


    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    /*
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);

        //String caption = getResources().getString(captions.get(searchName));
        //((TextView) findViewById(R.id.caption_text)).setText(caption);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
       // recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        // Create grammar-based search for selection between demos
       /* File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

        // Create grammar-based search for digit recognition
        File digitsGrammar = new File(assetsDir, "digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);

        // Create language model search
        File languageModel = new File(assetsDir, "weather.dmp");
        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);

        // Phonetic search
        File phoneticModel = new File(assetsDir, "en-phone.dmp");
        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);


        */
/*
    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

 */
}
