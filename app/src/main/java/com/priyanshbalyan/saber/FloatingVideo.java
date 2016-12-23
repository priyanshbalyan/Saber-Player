package com.priyanshbalyan.saber;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Priyansh on 21-Oct-16.
 */

public class FloatingVideo extends Service implements View.OnClickListener,View.OnTouchListener,SeekBar.OnSeekBarChangeListener
{

    WindowManager wm ;
    LayoutInflater inflater ;
    ImageView vplay,vnext,vprev,gofull,resize,close ;
    FrameLayout fl,floatingmediapanel, flfloatingplayback ;
    VideoView v ;
    SeekBar sb ;

    String[] videopaths,videonames ;
    int pos, currentposition, initx, inity, sw, h,w, autohide=0, intresize=0;
    float touchx, touchy ;

    TextView videotext;

    Boolean boolresize=false;

    final WindowManager.LayoutParams vparams =
            new WindowManager.LayoutParams(
                    500,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);


    @Override
    public IBinder onBind(Intent p1)
    {
        // TODO: Implement this method
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        // TODO: Implement this method
        super.onStart(intent, startId);
        videopaths = intent.getStringArrayExtra("videopaths");
        videonames = intent.getStringArrayExtra("videonames");
        pos = intent.getIntExtra("pos", 0);
        currentposition = intent.getIntExtra("currentduration", 0);

        v.setVideoPath(videopaths[pos]);
        v.seekTo(currentposition);
        v.start();
    }

    @Override
    public void onCreate()
    {
        // TODO: Implement this method
        super.onCreate();

        wm = (WindowManager)getSystemService(WINDOW_SERVICE);

        inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        fl = (FrameLayout)inflater.inflate(R.layout.floatingvideo, null);

        vparams.gravity = Gravity.TOP | Gravity.LEFT;
        vparams.x = 0 ;
        vparams.y = 100;
        vparams.width = (wm.getDefaultDisplay().getWidth() * 80) / 100 ;

        v = (VideoView)fl.findViewById(R.id.vfloatplayback);
        sb = (SeekBar)fl.findViewById(R.id.sbfloatingvideo);
        vplay = (ImageView)fl.findViewById(R.id.ibfloatplay);
        vnext = (ImageView)fl.findViewById(R.id.ibfloatnext);
        vprev = (ImageView)fl.findViewById(R.id.ibfloatprev);
        gofull = (ImageView)fl.findViewById(R.id.ibgofull);
        resize = (ImageView)fl.findViewById(R.id.ibresize);
        close = (ImageView)fl.findViewById(R.id.ibclose);
        videotext = (TextView)fl.findViewById(R.id.tvfloatingvideo);
        videotext.setSelected(true);

        floatingmediapanel = (FrameLayout)fl.findViewById(R.id.flfloatingcontrols);
        flfloatingplayback = (FrameLayout)fl.findViewById(R.id.flfloatingplayback);

        flfloatingplayback.setOnClickListener(this);
        vplay.setOnClickListener(this);
        vnext.setOnClickListener(this);
        vprev.setOnClickListener(this);
        gofull.setOnClickListener(this);
        resize.setOnClickListener(this);
        close.setOnClickListener(this);

        flfloatingplayback.setOnTouchListener(this);

        sb.setOnSeekBarChangeListener(this);

        v.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
            @Override
            public void onPrepared(MediaPlayer p1)
            {
                // TODO: Implement this method
                sw = wm.getDefaultDisplay().getWidth();
                sb.setMax(v.getDuration());
                videotext.setText(videonames[pos]);

                h= vparams.height;
                w = vparams.width;

                Timer t = new Timer() ;
                t.scheduleAtFixedRate(new TimerTask(){
                    @Override
                    public void run()
                    {
                        // TODO: Implement this method
                        try {
                            sb.setProgress(v.getCurrentPosition());
                        }catch(Exception e){}

                        autohide++ ;
                        if(autohide == 4){
                            int mediumAnimDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
                            floatingmediapanel.animate().alpha(0f).setDuration(mediumAnimDuration).setListener(new AnimatorListenerAdapter(){
                                public void onAnimationEnd(Animator animator){
                                    floatingmediapanel.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }, 0, 1000);
            }
        });

        v.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer p1)
            {
                // TODO: Implement this method
                if (pos < videopaths.length - 1)
                {
                    pos++ ;
                    v.setVideoPath(videopaths[pos]);
                    v.start();
                }
                else
                    Toast.makeText(getApplicationContext(), "Playback Ended", Toast.LENGTH_LONG).show();
                stopSelf();
            }
        });

        v.setOnErrorListener(new MediaPlayer.OnErrorListener(){
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Toast.makeText(getApplicationContext(), "Oops! Something happened.", Toast.LENGTH_SHORT).show();
                if (pos < videopaths.length - 1)
                {
                    pos++ ;
                    v.setVideoPath(videopaths[pos]);
                    v.start();
                }
                else
                    Toast.makeText(getApplicationContext(), "Playback Ended", Toast.LENGTH_LONG).show();
                stopSelf();

                return true;
            }
        });

        wm.addView(fl, vparams);


    }

    @Override
    public void onClick(View p1)
    {
        // TODO: Implement this method
        autohide=0;
        switch (p1.getId()){
            case R.id.ibfloatprev:
                if (pos > 0){
                    pos--;
                    v.setVideoPath(videopaths[pos]);
                    v.start();
                }
                else
                    v.seekTo(0);
                break;

            case R.id.ibfloatplay:
                if (v.isPlaying()){
                    v.pause();
                    vplay.setImageResource(R.drawable.play);
                }
                else{
                    v.start();
                    vplay.setImageResource(R.drawable.pause);
                }
                break;

            case R.id.ibfloatnext:
                if(pos < videopaths.length-1){
                    pos++ ;
                    v.setVideoPath(videopaths[pos]);
                    v.start();
                }
                break;

            case R.id.ibgofull:
                Intent i = new Intent(getApplicationContext(), FullScreenPlayback.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setAction(Utilities.ACTION.MAIN_ACTION);
                i.putExtra("videopaths", videopaths);
                i.putExtra("videonames", videonames);
                i.putExtra("pos", pos);
                i.putExtra("currentduration", v.getCurrentPosition());
                i.putExtra("continueflag",false);

                Toast.makeText(getApplicationContext(), "Loading..", Toast.LENGTH_SHORT).show();
                //wm.removeView(fl);
                startActivity(i);
                stopSelf();
                break;

            case R.id.flfloatingplayback:
                int mediumAnimDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
                if(floatingmediapanel.getVisibility() == View.GONE){
                    floatingmediapanel.setAlpha(0f);
                    floatingmediapanel.setVisibility(View.VISIBLE);
                    floatingmediapanel.animate().alpha(1f).setDuration(mediumAnimDuration).setListener(null);
                }
                else{
                    floatingmediapanel.animate().alpha(0f).setDuration(mediumAnimDuration).setListener(new AnimatorListenerAdapter(){
                        public void onAnimationEnd(Animator animator){
                            floatingmediapanel.setVisibility(View.GONE);
                        }
                    });
                    autohide=5;
                }
                break;

            case R.id.ibresize:
                vprev.setVisibility(View.VISIBLE);
                vnext.setVisibility(View.VISIBLE);
                    switch(intresize){
                        case 0 : intresize++;
                            vparams.width = WindowManager.LayoutParams.MATCH_PARENT;
                            wm.updateViewLayout(fl,vparams);
                            Toast.makeText(getApplicationContext(),"100%",Toast.LENGTH_SHORT).show();
                            break;

                        case 1 : intresize++ ;
                            vprev.setVisibility(View.GONE);
                            vnext.setVisibility(View.GONE);
                            vparams.width = (wm.getDefaultDisplay().getWidth() * 50) / 100 ;
                            wm.updateViewLayout(fl,vparams);
                            Toast.makeText(getApplicationContext(),"50%",Toast.LENGTH_SHORT).show();
                            break;

                        case 2 : intresize=0;
                            vparams.width = (wm.getDefaultDisplay().getWidth() * 80) / 100 ;
                            wm.updateViewLayout(fl,vparams);
                            Toast.makeText(getApplicationContext(),"80%",Toast.LENGTH_SHORT).show();
                            break;
                    }
                break;

            case R.id.ibclose:
                v.stopPlayback();
                stopSelf();
                break;
        }
    }

    @Override
    public boolean onTouch(View p1, MotionEvent e)
    {
        // TODO: Implement this method
        switch (p1.getId())
        {
            case R.id.flfloatingplayback:
                switch (e.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        initx = vparams.x ;
                        inity = vparams.y ;
                        touchx = e.getRawX() ;
                        touchy = e.getRawY() ;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        vparams.x = initx + (int)(e.getRawX() - touchx);
                        vparams.y = inity + (int)(e.getRawY() - touchy);

                        wm.updateViewLayout(fl, vparams);
                        break;

                    case MotionEvent.ACTION_UP:

                        break;
                }
                return false;

		/*	case R.id.ibresize:
				autohide=0;
				float videoproportion = (float)v.getWidth() / (float)v.getHeight() ;
				android.view.ViewGroup.LayoutParams lp = v.getLayoutParams();
				switch (e.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						initx = vparams.width;
						inity = vparams.height;
						touchx = e.getRawX();
						touchy = e.getRawY();
						break;

					case MotionEvent.ACTION_MOVE:
						vparams.width = initx + (int)(e.getRawX() - touchx);
						vparams.height = inity + (int)(e.getRawY() - touchy) ;


						lp.width = WindowManager.LayoutParams.MATCH_PARENT;
						lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
						v.setLayoutParams(lp);
						if (vparams.width <= sw && vparams.width > 220 && vparams.height > 10){
							wm.updateViewLayout(fl, vparams);
						}

				vparams.width  = WindowManager.LayoutParams.WRAP_CONTENT ;
				vparams.geight = WindowManager.LayoutParams.WRAP_CONTENT ;
						widthtext.setText(vparams.width+" "+vparams.height);
						break;

					case MotionEvent.ACTION_UP:
						if (vparams.width >= sw - 20)
						{
							vparams.width = sw ;
							v.layout(sw, sw, sw, sw) ;
							wm.updateViewLayout(fl, vparams) ;
						}
						break;
				}
				return false;
				*/
        }
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar p1, int p2, boolean p3)
    {
        // TODO: Implement this method
        if (p3)
            v.seekTo(p2);
    }

    @Override
    public void onStartTrackingTouch(SeekBar p1)
    {
        // TODO: Implement this method
        autohide=5;
    }

    @Override
    public void onStopTrackingTouch(SeekBar p1)
    {
        // TODO: Implement this method
        autohide=0;
    }

    @Override
    public void onDestroy()
    {
        // TODO: Implement this method
        super.onDestroy();
        v.stopPlayback();
        wm.removeView(fl);
        stopSelf();
    }


}