package com.quantumd.whatismypurpose;

import android.app.Activity;
import android.os.Bundle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements RecognitionListener {

    private TextView expression;
    private TextView returnedText;
    private ImageView button;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private final String ANSWER = "Oh my god :(";
    private Purpose purpose = new Purpose();


    //socket code
    private Button btnConn;
    private TextView txvIP;
    private EditText etIP;
    private String serverIP;
    public Socket socket;
    private static final int SERVERPORT = 5000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        expression = (TextView) findViewById(R.id.textView);
        returnedText = (TextView) findViewById(R.id.textView2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        button = (ImageView) findViewById(R.id.imageView);
        //socket code
        btnConn = (Button) findViewById(R.id.button_conn);
        txvIP = (TextView) findViewById(R.id.ip_txv);
        etIP = (EditText) findViewById(R.id.ip_txt);




        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);


        //socket code

        btnConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("EVENT", "Button pressed " + btnConn.getText().toString());
                if(btnConn.getText().toString().equals("CONNECT")) {

                    try {
                        serverIP = etIP.getText().toString();
                        new Thread(new ClientThread()).start();
                    } catch (Exception e) {
                        Log.i("ERROR", "SOCKET FAILED TO SEND DATA " + e.getLocalizedMessage());
                    }
                }else{
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    txvIP.setText("IP address: ");
                    etIP.setVisibility(View.VISIBLE);
                    btnConn.setText("CONNECT");
                }
            }
        });

    }

    public void startSpeech(View v) {
        expression.setText("What is my purpose?");
        returnedText.setText("Speak now");
        progressBar.setVisibility(View.VISIBLE);
        button.setEnabled(false);
        progressBar.setIndeterminate(true);
        button.setVisibility(View.INVISIBLE);
        returnedText.setVisibility(View.VISIBLE);
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "Resume");


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.INVISIBLE);
        button.setVisibility(View.VISIBLE);
        button.setEnabled(true);
        returnedText.setVisibility(View.INVISIBLE);
        expression.setText("What is my purpose?");
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        returnedText.setText(errorMessage);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        //String text = "";
        for (String result : matches) {
            //text += result + "\n";
            if (purpose.youPassButter(result)) {
                expression.setText(ANSWER);
                try {
                    if(socket.isConnected()) {
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                                true);
                        out.println(ANSWER);
                    }else{
                        Toast.makeText(MainActivity.this, "Data can not be sent, try connecting again", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.i("ERROR", "ERROR sending data");
                }
                break;

            }
        }
        //returnedText.setText(text);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public void updateActivity(final String ip)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                etIP.setVisibility(View.INVISIBLE);
                txvIP.setText("Connected to: " + ip);
                btnConn.setText("DISCONNECT");

            }
        });
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIP);
                socket = new Socket(serverAddr, SERVERPORT);
                updateActivity(serverIP);
            } catch (UnknownHostException e1) {
                Log.i("ERROR", "Error findding host" + e1.getStackTrace());

            } catch (IOException e1) {
                Log.i("ERROR", "Error on thread" + e1.getStackTrace());
            }
        }
    }

}
