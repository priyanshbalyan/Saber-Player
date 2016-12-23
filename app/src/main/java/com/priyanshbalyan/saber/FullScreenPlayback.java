package com.priyanshbalyan.saber;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullScreenPlayback extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener
{

    VideoView v ;
    ImageButton play,prev,next,gofloat,rotatescreen ;
    SeekBar sb,sbbright ;
    ProgressBar pb ;
    TextView vtimertext,vnametext,brighttext ;

    FrameLayout mediapanel,flplayback ;

    String[] videopaths, videonames ;
    int pos=0, currentposition, autohide=0, onsaveposition;
    boolean cflag=false , remtime=false;

    Timer seekbarupdater;

    SharedPreferences prefs ;
    String prefname = "Prefs" ;

    WindowManager.LayoutParams lp;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        onsaveposition = v.getCurrentPosition();
        v.pause();
        outState.putInt("position",onsaveposition);

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(prefname,0);
        if(!prefs.getBoolean("resumedfromfloat",false))
            v.seekTo(onsaveposition);
        else{
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("resumedfromfloat",false);
            editor.apply();
        }

        hidenavigationbar();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            onsaveposition = savedInstanceState.getInt("position");
        }
        //No title Bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.fullscreenplayback);

        //brightness
        lp = getWindow().getAttributes();
        //lp.screenBrightness = 25 / 100.0f ;
        //getWindow().setAttributes(lp);

        //initialization
        v = (VideoView)findViewById(R.id.vvplaybackfull);
        play = (ImageButton)findViewById(R.id.ibplayfull);
        prev = (ImageButton)findViewById(R.id.ibprevfull);
        next = (ImageButton)findViewById(R.id.ibnextfull);
        gofloat = (ImageButton)findViewById(R.id.ibgofloat);
        rotatescreen = (ImageButton)findViewById(R.id.ibrotate);

        vtimertext = (TextView)findViewById(R.id.tvvideotimer);
        vnametext = (TextView)findViewById(R.id.tvvideonamefull);
        brighttext = (TextView)findViewById(R.id.tvbrightness);
        pb = (ProgressBar)findViewById(R.id.pbvideofull);
        sb = (SeekBar)findViewById(R.id.sbvideo);
        sbbright = (SeekBar)findViewById(R.id.sbbrightness);
        sbbright.setMax(100);
        sbbright.setProgress(25);

        mediapanel = (FrameLayout)findViewById(R.id.flcontrolpanel);
        flplayback = (FrameLayout)findViewById(R.id.flplayback);
        vtimertext.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        gofloat.setOnClickListener(this);
        rotatescreen.setOnClickListener(this);
        flplayback.setOnClickListener(this);
        sb.setOnSeekBarChangeListener(this);
        sbbright.setOnSeekBarChangeListener(this);

        //getting data from intent
        if(getIntent().getAction().equals(Utilities.ACTION.MAIN_ACTION)){
            videopaths = getIntent().getStringArrayExtra("videopaths");
            videonames = getIntent().getStringArrayExtra("videonames");
            pos = getIntent().getIntExtra("pos", 0);
            currentposition = getIntent().getIntExtra("currentduration", 0);
            cflag = getIntent().getBooleanExtra("continueflag",false);

        }else if(getIntent().getAction().equals(Intent.ACTION_VIEW)){
            videonames = new String[1] ;
            videopaths = new String[1] ;
            Uri uri = getIntent().getData();
            String path = uri.getPath();
            videopaths[0] = path ;
            File file = new File(path);
            videonames[0] = file.getName();
            Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
        }

        v.setVideoPath(videopaths[pos]);
        v.requestFocus();
        v.seekTo(currentposition);
        vnametext.setText(videonames[pos]) ;


        v.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
            @Override
            public void onPrepared(MediaPlayer p1)
            {
                // TODO: Implement this method

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                sb.setMax(v.getDuration());
                fitscreen();

                //vdurationtext.setText(v.getDuration()/60000 + ":" + (v.getDuration()/1000)%60);

                autohide=0;
                pb.animate().alpha(0f).setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime)).setListener(new AnimatorListenerAdapter(){
                    public void onAnimationEnd(Animator animator)
                    {
                        pb.setVisibility(View.GONE);
                    }
                });

                seekbarupdater = new Timer();
                seekbarupdater.scheduleAtFixedRate(new TimerTask(){
                    public void run()
                    {
                        sb.setProgress(v.getCurrentPosition());
                        autohide++;

                        runOnUiThread(new Runnable(){    //As only the original thread that created the view hierarchy can touch its views therefore we have to change textview in the main ui thread by runOnUiThread()
                            @Override
                            public void run()
                            {
                                // TODO: Implement this method
                                if(remtime)
                                    vtimertext.setText(Utilities.textDuration(v.getCurrentPosition())+" / -"+Utilities.textDuration(v.getDuration() - v.getCurrentPosition()));
                                else
                                    vtimertext.setText(Utilities.textDuration(v.getCurrentPosition())+" / "+Utilities.textDuration(v.getDuration()));

                                if(autohide==4){
                                    flplayback.performClick();
                                }
                            }
                        });
                    }
                }, 0, 1000);

                if (!v.isPlaying())
                    v.start();

                CoordinatorLayout clayout = (CoordinatorLayout)findViewById(R.id.snackbarlocation);
                prefs = getSharedPreferences(prefname,0);
                final int continuepos = prefs.getInt(videonames[pos],0);
                int brightval = prefs.getInt("brightness",25);
                sbbright.setProgress(brightval);
                if(continuepos != 0){
                    if(cflag){
                        Snackbar.make(clayout, "Continue where you left off ?", Snackbar.LENGTH_LONG).setAction("Continue", new View.OnClickListener(){
                            @Override
                            public void onClick(View p1)
                            {
                                // TODO: Implement this method
                                v.seekTo(continuepos);
                                fitscreen();
                            }
                        }).show();
                    }
                }

                Timer f = new Timer();
                f.schedule(new TimerTask(){
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fitscreen();
                            }
                        });
                    }
                }, 0, 1000);
            }

        });

        v.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer p1)
            {
                // TODO: Implement this method
                if(pos < videopaths.length - 1){
                    pos++ ;
                    videonext();
                }
                else
                    finish();
            }
        });

        v.setOnErrorListener(new MediaPlayer.OnErrorListener(){
            @Override
            public boolean onError(MediaPlayer p1, int p2, int p3)
            {
                // TODO: Implement this method
                Toast.makeText(getApplicationContext(), "Error in playing media.", Toast.LENGTH_SHORT).show();
                if(pos < videopaths.length - 1){
                    pos++ ;
                    videonext();
                }
                else
                    finish();
                return true;
            }
        });

        theme();
    }

    @Override
    public void onClick(View p1)
    {
        // TODO: Implement this method
        autohide = 0 ;
        switch (p1.getId())
        {
            case R.id.ibplayfull:
                if (v.isPlaying()){
                    v.pause();
                    play.setImageResource(R.drawable.play);
                }
                else{
                    v.start();
                    play.setImageResource(R.drawable.pause);
                }
                break;

            case R.id.ibnextfull:
                if(pos < videopaths.length - 1){
                    pos++ ;
                    videonext();
                }

                break;

            case R.id.ibprevfull:
                if (pos > 0){
                    pos-- ;
                    videonext();
                }
                else
                    v.seekTo(0);
                break;

            case R.id.ibgofloat:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                currentposition = v.getCurrentPosition();
                Intent i = new Intent(getApplicationContext(), FloatingVideo.class);
                i.putExtra("pos", pos);
                i.putExtra("videonames",videonames);
                i.putExtra("videopaths", videopaths);
                i.putExtra("currentduration", currentposition);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("resumedfromfloat",true);
                editor.apply();

                startService(i);
                finish();
                break;

            case R.id.ibrotate:
                togglenavigationbar();
                if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    next.setVisibility(View.GONE);
                }
                else{
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    next.setVisibility(View.VISIBLE);
                }
                fitscreen();
                break;

            case R.id.tvvideotimer:
                remtime=!remtime;
                if(remtime)
                    vtimertext.setText(Utilities.textDuration(v.getCurrentPosition())+" / -"+Utilities.textDuration(v.getDuration() - v.getCurrentPosition()));
                else
                    vtimertext.setText(Utilities.textDuration(v.getCurrentPosition())+" / "+Utilities.textDuration(v.getDuration()));
                break;

            case R.id.flplayback:
                if (mediapanel.getVisibility() == View.INVISIBLE){
                    vnametext.setVisibility(View.VISIBLE);
                    vnametext.animate().alpha(1f).setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime)).setListener(null);
                    Utilities.circularReveal(mediapanel);
                }
                else{
                    vnametext.animate().alpha(0f).setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime)).setListener(new AnimatorListenerAdapter(){
                        public void onAnimationEnd(Animator animator){
                            vnametext.setVisibility(View.GONE);
                        }
                    });
                    Utilities.circularUnreveal(mediapanel);
                    autohide=5;
                }

                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar p1, int p2, boolean p3)
    {
        // TODO: Implement this method
        switch(p1.getId()){
            case R.id.sbvideo:
                if (p3){
                    v.seekTo(p2);
                    brighttext.setText(Utilities.textDuration(p2));
                }
                break;

            case R.id.sbbrightness:
                if(p3){
                    lp.screenBrightness = p2 / 100.0f ;
                    getWindow().setAttributes(lp);
                    brighttext.setText("Brightness\n"+p2+"%");
                }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar p1)
    {
        // TODO: Implement this method
        autohide = 5 ;
        Utilities.circularReveal(brighttext);
    }

    @Override
    public void onStopTrackingTouch(SeekBar p1)
    {
        // TODO: Implement this method
        autohide = 0 ;
        Utilities.circularUnreveal(brighttext);
    }

    @Override
    protected void onDestroy()
    {
        // TODO: Implement this method
        super.onDestroy();
        seekbarupdater.cancel();
        //Toast.makeText(getApplicationContext(), "ondestroy called",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed()
    {
        // TODO: Implement this method
        super.onBackPressed();
        seekbarupdater.cancel();
        prefs = getSharedPreferences(prefname,0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(videonames[pos], v.getCurrentPosition());
        editor.putInt("brightness",sbbright.getProgress());
        editor.apply();
        //if(editor.commit())
        //Toast.makeText(getApplicationContext(),"continue data saved"+v.getCurrentPosition(),Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        // TODO: Implement this method
        super.onConfigurationChanged(newConfig);
        fitscreen();
    }

    public void videonext(){
        if(seekbarupdater != null)
            seekbarupdater.cancel();
        pb.setAlpha(1f);
        pb.setVisibility(View.VISIBLE);
        vnametext.setText(videonames[pos]);
        v.setVideoPath(videopaths[pos]);
    }

    public void togglenavigationbar(){
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;

        fitscreen();

        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14)
        {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16)
        {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 18)
        {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

    }

    public void hidenavigationbar(){
        fitscreen();
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14)
        {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16)
        {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 18)
        {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

    }

    public void fitscreen(){
        // Adjust the size of the video
        // so it fits on the screen
        int videoWidth = v.getWidth();
        int videoHeight = v.getHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        float screenProportion = (float) screenWidth /(float) screenHeight;
        android.view.ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (videoProportion > screenProportion) {
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            //lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            //lp.width = (int) (videoProportion * (float) screenHeight);
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        }
        v.setLayoutParams(lp);

    }

    public void theme(){
        View v = findViewById(R.id.fullscreenplaybackmediapanel);
        v.setBackgroundColor(Utilities.getcolor(this,false));
        v = findViewById(R.id.tvbrightness);
        SharedPreferences prefs = getSharedPreferences("Prefs",0);
        int t = prefs.getInt("Theme",0);
        switch(t){
            case 0 : v.setBackground(getResources().getDrawable(R.drawable.circularshapered));
                break;
            case 1 : v.setBackground(getResources().getDrawable(R.drawable.circularshapeblack));
                break;
            case 2 : v.setBackground(getResources().getDrawable(R.drawable.circularshapeblue));
                break;
            case 3 : v.setBackground(getResources().getDrawable(R.drawable.circularshapepink));
                break;
            case 4 : v.setBackground(getResources().getDrawable(R.drawable.circularshapepurple));
                break;
            case 5 : v.setBackground(getResources().getDrawable(R.drawable.circularshapeteal));
                break;
            case 6 : v.setBackground(getResources().getDrawable(R.drawable.circularshapeamber));
                break;
        }
        if(Build.VERSION.SDK_INT >= 21)
            getWindow().setStatusBarColor(Color.BLACK);
    }
}