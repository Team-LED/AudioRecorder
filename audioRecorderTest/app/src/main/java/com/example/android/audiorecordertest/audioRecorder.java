package com.example.android.audiorecordertest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.jar.Manifest;

/**
 * Created by lizzi on 3/23/2018.
 */

public class audioRecorder extends AppCompatActivity {
    //Helps us find the appropriate messages when debugging
    private static final String LOG_TAG = "audioRecorder";
    //This is used to name our recorded files
    private static String fileName = null;

    //RecordButton is a class we will create
    // that extend a basic button
    private RecordButton recordButton = null;
    //Same with PlayButton
    private PlayButton playButton = null;

    //This is our audio recorder
    private MediaRecorder recorder = null;
    //And this is our player for playing the recording back
    private MediaPlayer player = null;

    //This constant is sent in our various permissions request
    //I don't know why it's 200 yet ¯\_(ツ)_/¯
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    //Set initial permission to false so we don't automatically
    //start recording
    private boolean permissionToRecordAccepted = false;
    //Haul in a permission that was added to the manifest
    private String [] permissions = {android.Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if(!permissionToRecordAccepted)
            finish();
    }

    private void onRecord(boolean start){
        if(start)
            startRecording();
        else
            stopRecording();
    }

    private void startRecording(){
        recorder = new MediaRecorder();
        //Sets up the recorder for audio as opposed to video,
        //and a few other things related to file format and encoding.
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //Does some final magic to get the recorder ready.
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording(){
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    //TODO: add a pause button for recording

    private void onPlay(boolean start){
        if(start)
            startPlaying();
        else
            stopPlaying();
    }

    //Connect a fresh MediaPlayer to our player and load it
    //with the correct file, then play it
    private void startPlaying(){
        player = new MediaPlayer();
        try{
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        }
        catch(IOException e){
            Log.e(LOG_TAG, "Couldn't load media file: " + fileName);
        }
    }

    //TODO: add a pause button for playback

    //Release resources when finished to get memory back
    private void stopPlaying(){
        player.release();
        player = null;
    }

    class RecordButton extends android.support.v7.widget.AppCompatButton{
        boolean startRecording = true;

        OnClickListener recordButtonListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(startRecording);
                if(startRecording)
                    setText("Stop Recording");
                else
                    setText("Start Recording");
                startRecording = !startRecording;
            }
        };

        public RecordButton(Context context){
            super(context);
            setText("Start Recording");
            setOnClickListener(recordButtonListener);
        }
    }

    class PlayButton extends android.support.v7.widget.AppCompatButton{
        boolean startPlaying = true;

        OnClickListener playButtonListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(startPlaying);
                if(startPlaying)
                    setText("Stop Playing");
                else
                    setText("Start Playing");
                startPlaying = !startPlaying;
            }
        };

        public PlayButton(Context context){
            super(context);
            setText("Start Playing");
            setOnClickListener(playButtonListener);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/baby's_first_save_file.3gp";

        ActivityCompat.requestPermissions(this, permissions,REQUEST_RECORD_AUDIO_PERMISSION);

        LinearLayout ll = new LinearLayout(this);
        recordButton = new RecordButton(this);
        ll.addView(recordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        playButton = new PlayButton(this);
        ll.addView(playButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));

        setContentView(ll);

    }

    //When the app enters the stopped state, releases resources
    //We probably want to change this behavior if we want
    //recording and playback to continue when the user leaves the app
    //or locks their screen
    public void onStop(){
        super.onStop();
        if(recorder != null){
            recorder.release();
            recorder = null;
        }
    }
}
