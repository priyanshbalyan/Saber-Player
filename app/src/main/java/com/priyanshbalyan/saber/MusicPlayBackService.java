package com.priyanshbalyan.saber;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Priyansh on 22-Oct-16.
 */

public class MusicPlayBackService extends Service
{
    MediaPlayer mp ;

    List<String> musictitles,musicpaths,artistnames ;
    String albumname, albumart;

    int pos=0;
    Boolean mstart = true,shuffle=false;
    Random rand ;
    String controller = "";

    Notification status;
    RemoteViews views,bigViews ;

    private final IBinder musicbind = new MusicBinder();

    private ServiceCallbacks servicecallbacks ;
    Timer seekbarupdater ;

    @Override
    public IBinder onBind(Intent p1)
    {
        // TODO: Implement this method
        return musicbind;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // TODO: Implement this method
        if(intent != null)
            switch(intent.getAction()){
                case Utilities.ACTION.START_FOREGROUND_ACTION :
                    //Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
                    break;

                case Utilities.ACTION.PREV_ACTION :
                    //Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show();
                    prevTrack();
                    break;

                case Utilities.ACTION.PLAY_ACTION :
                    //Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show();
                    if(mp.isPlaying()){
                        mp.pause();
                        views.setImageViewResource(R.id.status_bar_play, R.drawable.play);
                        bigViews.setImageViewResource(R.id.bigstatus_bar_play, R.drawable.play);
                        startForeground(Utilities.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
                    }
                    else {
                        mp.start();
                        views.setImageViewResource(R.id.status_bar_play, R.drawable.pause);
                        bigViews.setImageViewResource(R.id.bigstatus_bar_play, R.drawable.pause);
                        startForeground(Utilities.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
                    }
                    break;

                case Utilities.ACTION.NEXT_ACTION :
                    //Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show();
                    nextTrack();
                    break;

                case Utilities.ACTION.STOP_FOREGROUND_ACTION :
                    Toast.makeText(this, "Playback Stopped", Toast.LENGTH_SHORT).show();
                    stopForeground(true);
                    mp.stop();
                    mp.release();
                    seekbarupdater.cancel();
                    servicecallbacks.finishActivity();
                    stopSelf();
            }
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        // TODO: Implement this method
        super.onCreate();
        pos=0;

        rand = new Random();
        mp = new MediaPlayer();
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
            @Override
            public void onPrepared(MediaPlayer p1){
                // TODO: Implement this method
                mp.start();
                servicecallbacks.playerReady(mp.getDuration(), musictitles.get(pos), artistnames.get(pos));
                seekbarupdater= new Timer();
                seekbarupdater.scheduleAtFixedRate(new TimerTask(){
                    @Override
                    public void run()
                    {
                        // TODO: Implement this method
                        try{ servicecallbacks.updateSeekbar(mp.getCurrentPosition(),mp.isPlaying()); }
                        catch(IllegalStateException e){}
                    }
                }, 0, 1000);

            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer p1){
                // TODO: Implement this method
                nextTrack();
            }
        });
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener(){
            @Override
            public boolean onError(MediaPlayer p1, int p2, int p3){
                // TODO: Implement this method
                Toast.makeText(getApplicationContext(), "Oops! Something happened.",Toast.LENGTH_SHORT).show();
                nextTrack();
                return false;
            }
        });

        PhoneStateListener phonestatelistener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if(state == TelephonyManager.CALL_STATE_RINGING){
                    if(mp != null) if(mp.isPlaying()) mp.pause();
                }else if(state == TelephonyManager.CALL_STATE_IDLE){}
                else if(state == TelephonyManager.CALL_STATE_OFFHOOK){
                    if(mp != null) if(mp.isPlaying()) mp.pause();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager mgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        if(mgr != null){
            mgr.listen(phonestatelistener,PhoneStateListener.LISTEN_CALL_STATE);
        }

    }

    public void setList(List<AlbumsData> ad, int position){
        musicpaths = ad.get(position).mpia;
        musictitles = ad.get(position).mnia;
        artistnames = ad.get(position).maia;
        albumname = ad.get(position).albumname;
        albumart = ad.get(position).albumart;

        //Toast.makeText(getApplicationContext(),musicpaths[0],Toast.LENGTH_SHORT).show();
    }

    public void setTrack(int position){
        pos = position;
        PlayMusic();
    }

    public boolean allowShuffle(){
        shuffle = !shuffle;
        if(shuffle)
            Toast.makeText(getApplicationContext(),"Shuffling Tracks",Toast.LENGTH_SHORT).show();
        return shuffle ;
    }

    public void PlayMusic(){
        try{
            mp.reset();
            mp.setDataSource(musicpaths.get(pos));

        }
        catch (SecurityException e){}
        catch (IllegalArgumentException e){}
        catch (IllegalStateException e){}
        catch (IOException e){}
        mp.prepareAsync();
        showNotification();
    }

    public void nextTrack(){
        if(shuffle){
            pos = rand.nextInt(musicpaths.size());
            PlayMusic();
        }else if(pos<musicpaths.size()-1){
            pos++ ;
            PlayMusic();
        }
    }

    public void prevTrack(){
        if(pos>0){
            pos--;
            PlayMusic();
        }
    }

    public boolean ismplaying(){
        try {
            if (mp.isPlaying())
                return true;
            else
                return false;
        }catch(IllegalStateException e){return false ;}
    }

    public int getmppos(){
        return pos ;
    }

    public void seekPlayer(int pos){
        mp.seekTo(pos);
    }

    public void addToPlaying(List<AlbumsData> ad, int p){
        musicpaths.addAll(ad.get(p).mpia);
        musictitles.addAll(ad.get(p).mnia);
        artistnames.addAll(ad.get(p).maia);
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // TODO: Implement this method
        try {
            mp.stop();
            mp.release();
            stopForeground(true);
        }catch(IllegalStateException e){}
        seekbarupdater.cancel();
        return false ;
    }

    public void setCallbacks(ServiceCallbacks callbacks){
        servicecallbacks = callbacks;
    }

    private void showNotification() {
        // Using RemoteViews to bind custom layouts into Notification
        views = new RemoteViews(getPackageName(), R.layout.status_bar);
        bigViews = new RemoteViews(getPackageName(), R.layout.status_bar_expanded);
        // showing icon in normal view instead of imageview
        //views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        //views.setViewVisibility(R.id.status_bar_album_art, View.GONE);

        //getting bitmap of imageview to be set in expanded view
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = 4 ;
        Bitmap bmp;
        try{ bmp = BitmapFactory.decodeFile(albumart);}catch(Exception e){bmp = null;}

        if(bmp != null){
            bigViews.setImageViewBitmap(R.id.bigstatus_bar_album_art,bmp);
            views.setImageViewBitmap(R.id.status_bar_album_art, bmp);
        }else{
            bigViews.setImageViewResource(R.id.bigstatus_bar_album_art,R.drawable.defaultalbumimage);
            views.setImageViewResource(R.id.status_bar_album_art, R.drawable.defaultalbumimage);
        }

        Intent nIntent = new Intent(this, MainActivity.class);
        nIntent.setAction(Utilities.ACTION.MAIN_ACTION);
        nIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,nIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent cintent = new Intent(this, MusicPlayBackService.class);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, cintent.setAction(Utilities.ACTION.PREV_ACTION), 0);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0, cintent.setAction(Utilities.ACTION.PLAY_ACTION), 0);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0, cintent.setAction(Utilities.ACTION.NEXT_ACTION), 0);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0, cintent.setAction(Utilities.ACTION.STOP_FOREGROUND_ACTION), 0);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        //views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        //views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        //views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        bigViews.setOnClickPendingIntent(R.id.bigstatus_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.bigstatus_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.bigstatus_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.bigstatus_bar_collapse, pcloseIntent);

        views.setImageViewResource(R.id.status_bar_play, R.drawable.pause);
        views.setTextViewText(R.id.status_bar_track_name, musictitles.get(pos));
        views.setTextViewText(R.id.status_bar_artist_name, artistnames.get(pos));


        bigViews.setImageViewResource(R.id.bigstatus_bar_play, R.drawable.pause);
        bigViews.setTextViewText(R.id.bigstatus_bar_track_name, musictitles.get(pos));
        bigViews.setTextViewText(R.id.bigstatus_bar_artist_name, artistnames.get(pos));
        bigViews.setTextViewText(R.id.bigstatus_bar_album_name, albumname);

        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.notificationicon;
        status.contentIntent = pendingIntent;
        startForeground(Utilities.NOTIFICATION_ID.FOREGROUND_SERVICE, status);

    }

    public class MusicBinder extends Binder {
        MusicPlayBackService getService(){
            return MusicPlayBackService.this ;
        }
    }

}