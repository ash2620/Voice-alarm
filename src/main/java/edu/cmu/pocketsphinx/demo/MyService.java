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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;

import androidx.annotation.Nullable;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

public class MyService extends Service implements
        RecognitionListener {
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    //private static final String FORECAST_SEARCH = "forecast";
    //private static final String DIGITS_SEARCH = "digits";
    //private static final String PHONE_SEARCH = "phones";
    //private static final String MENU_SEARCH = "menu";
    Context mContext;
    /* Keyword we are looking for to activate menu */
    MyDBHandler dbHandler;
    private static String KEYPHRASE;
    boolean play = false;
    AudioManager audio;
    Context c;
    private SpeechRecognizer recognizer;
    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    Ringtone ringtone;
    //private HashMap<String, Integer> captions;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mContext = getApplicationContext();
        c = this;
        ringtone = RingtoneManager.getRingtone(mContext, uri);
        play = false;
        Log.d("Result" , "yay");


        //captions.put(MENU_SEARCH, R.string.menu_caption);
        //captions.put(DIGITS_SEARCH, R.string.digits_caption);
        //captions.put(PHONE_SEARCH, R.string.phone_caption);
        //captions.put(FORECAST_SEARCH, R.string.forecast_caption);


        new SetupTask(this).execute();
        Log.d("Result" , "2");
        return super.onStartCommand(intent, flags, startId);
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<MyService> activityReference;
        SetupTask(MyService activity) {
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
            activityReference.get().switchSearch(KWS_SEARCH);
        }
    }


    @Override
    public void onDestroy() {
        play = false;
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        super.onDestroy();
        mContext = this;

        ringtone.stop();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        c = this;
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //ringtone = RingtoneManager.getRingtone(mContext, uri);
        if (hypothesis == null)
            return;
            if(!audio.isMusicActive()) {
            String text = hypothesis.getHypstr();
            Log.d("Resylt",text);
            Log.d("Result", text +" on partial result");
            int c =0;
            String test="";
            text= text.trim();
            for (int i = 0 ; i < text.length() ; i++){
                if (c==1){
                    break;
                }
                else {
                    if (text.charAt(i)==' ' || text.charAt(i)=='\n'){
                        c++;
                    }
                    else {
                        test+=text.charAt(i);
                    }
                }

            }
            Log.d("Result" , test);

          if (text.trim().equalsIgnoreCase(KEYPHRASE.trim()) && !play) {
              ringtone.play();
              play = true;
                /*try {
                    Thread.sleep(9000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ringtone.stop();
                 */
          }
        }
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
       /* if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            Log.d("Result" , text+" onresult");
            if (text.trim().equalsIgnoreCase(KEYPHRASE)) {
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

        */


    }


    @Override
    public void onBeginningOfSpeech() {

    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        Log.d("Result" , "3");
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
        c = this;
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
                KEYPHRASE = stringBuilder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (KEYPHRASE.split(" ").length>1) {
            recognizer = SpeechRecognizerSetup.defaultSetup()
                    .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                    .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                    .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                    .setBoolean("remove_noise", true)
                    .setKeywordThreshold(1e-27f)
                    .getRecognizer();
            recognizer.addListener(this);
        }
        else{
            recognizer = SpeechRecognizerSetup.defaultSetup()
                    .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                    .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                    .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                    .setBoolean("remove_noise", true)
                    .setKeywordThreshold(1e-10f)
                    .getRecognizer();
            recognizer.addListener(this);
        }

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        recognizer.startListening(KWS_SEARCH);
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

    }

    @Override
    public void onError(Exception error) {
        Log.getStackTraceString(error);
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Restarting the service if it is removed.
        PendingIntent service =
                PendingIntent.getService(getApplicationContext(), new Random().nextInt(),
                        new Intent(getApplicationContext(), MyService.class), PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);
        super.onTaskRemoved(rootIntent);
    }
}
