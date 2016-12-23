package com.priyanshbalyan.saber;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.priyanshbalyan.saber.MusicPlayBackService.MusicBinder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, ServiceCallbacks, MyItemClickListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    Toolbar toolbar;
    TabLayout tablayout;
    ViewPager mViewPager;
    VideosTab videostab;
    MusicTab musictab;
    RecyclerView rvtracks;
    RVTracksAdapter rvtracksadapter;

    SlidingUpPanelLayout slayout;

    List<AlbumsData> ad;
    int albumposition = 0, trackpos = 0;

    ImageButton tracklist, play, prev, next, ibshuffle;
    SquareImageView albumart;
    TextView toolbartitle, trackname, artistname, cdurationtext, tdurationtext, drawername;
    SeekBar sbmusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        videostab = new VideosTab();
        musictab = new MusicTab();

        trackname = (TextView) findViewById(R.id.tvmptrackname);
        artistname = (TextView) findViewById(R.id.tvmpartistname);
        drawername = (TextView) findViewById(R.id.tvdrawertrackname);
        cdurationtext = (TextView) findViewById(R.id.tvmpcurrentduration);
        tdurationtext = (TextView) findViewById(R.id.tvmptotalduration);
        drawername.setSelected(true);
        trackname.setSelected(true);
        artistname.setSelected(true);

        sbmusic = (SeekBar) findViewById(R.id.sbtrack);
        rvtracks = (RecyclerView) findViewById(R.id.rvtracklist);
        rvtracks.setLayoutManager(new LinearLayoutManager(this));
        rvtracks.setHasFixedSize(true);

        albumart = (SquareImageView) findViewById(R.id.ivmpalbumart);

        tracklist = (ImageButton) findViewById(R.id.ibtracklist);
        play = (ImageButton) findViewById(R.id.ibmpplay);
        prev = (ImageButton) findViewById(R.id.ibmpprev);
        next = (ImageButton) findViewById(R.id.ibmpnext);
        ibshuffle = (ImageButton) findViewById(R.id.ibshuffle);
        tracklist.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        ibshuffle.setOnClickListener(this);
        sbmusic.setOnSeekBarChangeListener(this);

        slayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                // TODO: Implement this method
                drawername.setAlpha(1 - slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                // TODO: Implement this method
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                    Utilities.circularUnreveal(rvtracks);
            }
        });

        theme();
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
        switch (id) {
            case R.id.about:
                Utilities.showAboutDialog(MainActivity.this);
                return true;
            case R.id.sendfeedback:
                Intent sf = new Intent(this, SendFeedback.class);
                startActivity(sf);
                return true;

            case R.id.theme:
                themeDialog();

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    //public static class PlaceholderFragment extends Fragment {}

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return videostab;
                case 1:
                    return musictab;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Videos";
                case 1:
                    return "Music";
            }
            return null;
        }
    }

    @Override
    public void onClick(View p1) {
        // TODO: Implement this method
        switch (p1.getId()) {
            case R.id.ibmpplay:
                if (musicSrv.ismplaying()) {
                    musicSrv.mp.pause();
                    play.setImageResource(R.drawable.play);
                } else {
                    musicSrv.mp.start();
                    play.setImageResource(R.drawable.pause);
                }
                break;

            case R.id.ibmpprev:
                musicSrv.prevTrack();
                break;

            case R.id.ibmpnext:
                musicSrv.nextTrack();
                break;

            case R.id.ibtracklist:
                if (rvtracks.getVisibility() == View.INVISIBLE)
                    Utilities.circularReveal(rvtracks);
                else
                    Utilities.circularUnreveal(rvtracks);
                break;

            case R.id.ibshuffle:
                if (musicSrv.allowShuffle())
                    ibshuffle.setImageResource(R.drawable.shuffleon);
                else
                    ibshuffle.setImageResource(R.drawable.shuffle);
                break;
        }

    }

    @Override
    public void onBackPressed() {
        // TODO: Implement this method
        if (rvtracks.getVisibility() == View.VISIBLE)
            Utilities.circularUnreveal(rvtracks);
        else if (slayout != null && slayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (musictab.tracklistopen) {
            musictab.closeTrackList();
        } else if (musicBound)
            moveTaskToBack(true);
        else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // TODO: Implement this method
        super.onDestroy();
        if (playIntent != null)
            stopService(playIntent);
    }

    public void MusicPlayback(List<AlbumsData> albumsdata, int apos, int tpos) {

        ad = albumsdata;
        albumposition = apos;
        trackpos = tpos;
        slayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

        trackname.setText(ad.get(apos).mnia.get(tpos));
        artistname.setText(ad.get(apos).maia.get(tpos));

        rvtracksadapter = new RVTracksAdapter(this, apos, ad);
        rvtracksadapter.setOnItemClickListener(this);
        rvtracks.setAdapter(rvtracksadapter);
        Bitmap bmp = BitmapFactory.decodeFile(ad.get(apos).albumart);
        if (bmp != null)
            albumart.setImageBitmap(bmp);
        else
            albumart.setImageResource(R.drawable.defaultalbumimage);

        rvtracks.getLayoutParams().height = albumart.getWidth();

        if (playIntent == null) {
            playIntent = new Intent(this, MusicPlayBackService.class);
            playIntent.setAction(Utilities.ACTION.START_FOREGROUND_ACTION);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        //pass list
        if (musicBound == true) {
            musicSrv.setList(ad, albumposition);
            musicSrv.setTrack(tpos);
            play.setImageResource(R.drawable.pause);
        }

    }

    private MusicPlayBackService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            musicSrv.setList(ad, albumposition);
            musicSrv.setTrack(trackpos);
            musicSrv.setCallbacks(MainActivity.this);
            // Toast.makeText(getApplicationContext(), "Service Connected", Toast.LENGTH_SHORT);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void playerReady(int seekmax, String name, String aname) {
        // TODO: Implement this method
        sbmusic.setMax(seekmax);
        tdurationtext.setText(Utilities.textDuration(seekmax));
        trackname.setText(name);
        drawername.setText("Now Playing • " + name + " - " + aname);
        artistname.setText(aname);

    }

    @Override
    public void updateSeekbar(final int pos, final boolean playing) {
        // TODO: Implement this method
        sbmusic.setProgress(pos);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO: Implement this method
                cdurationtext.setText(Utilities.textDuration(pos));
                if (playing)
                    play.setImageResource(R.drawable.pause);
                else
                    play.setImageResource(R.drawable.play);
            }
        });
    }

    @Override
    public void finishActivity() {
        finish();
    }

    public void addToMusicPlayback(int position) {
        if (playIntent != null) {
            musicSrv.addToPlaying(ad, position);
        }
    }

    public boolean isMusicPlaying() {
        if (playIntent != null)
            return musicSrv.ismplaying();
        else
            return false;
    }

    public void expandDrawer() {
        slayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public int getCurrentTrackPosition() {
        if (playIntent != null) {
            return musicSrv.getmppos();
        } else
            return -1;
    }

    public int getCurrentAlbumPosition() {
        if (playIntent != null)
            return albumposition;
        else
            return -1;
    }

    public void pauseplayback(){
        if(musicBound = true)
            musicSrv.mp.pause();
    }

    @Override
    public void onItemClick(View v, int position) {
        // TODO: Implement this method
        musicSrv.setTrack(position);
        Utilities.circularUnreveal(rvtracks);
    }

    @Override
    public void onProgressChanged(SeekBar p1, int p2, boolean p3) {
        // TODO: Implement this method
        if (p3)
            musicSrv.seekPlayer(p2);
    }

    @Override
    public void onStartTrackingTouch(SeekBar p1) {
        // TODO: Implement this method
    }

    @Override
    public void onStopTrackingTouch(SeekBar p1) {
        // TODO: Implement this method
    }

    public void themeDialog(){
        SharedPreferences prefs = this.getSharedPreferences("Prefs",0);
        final SharedPreferences.Editor editor = prefs.edit();

        List<String> s = new ArrayList<>();
        s.add("Crimson Red");
        s.add("Jet Black");
        s.add("Blue Azure");
        s.add("Fuchsia Pink");
        s.add("Purple Orchid");
        s.add("Classic Teal");
        s.add("Burning Amber");
        AlertDialog.Builder b = new AlertDialog.Builder(this) ;
        b.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, s), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2){
                editor.putInt("Theme",p2);
                editor.apply();
                theme();
                musictab.theme();
                videostab.theme();
            }
        });

        AlertDialog d = b.create();
        d.setTitle("Select Theme");

        d.show();

    }

    public void theme(){
        View v = findViewById(R.id.dragView);
        v.setBackgroundColor(Utilities.getcolor(this,false));
        v = findViewById(R.id.maincontrollayout);
        v.setBackgroundColor(Utilities.getcolor(this,true));
        v = findViewById(R.id.maintracklayout);
        v.setBackgroundColor(Utilities.getcolor(this,false));
        v = findViewById(R.id.toolbar);
        v.setBackgroundColor(Utilities.getcolor(this,false));
        v = findViewById(R.id.tabs);
        v.setBackgroundColor(Utilities.getcolor(this,false));
        if(Build.VERSION.SDK_INT >= 21)
            getWindow().setStatusBarColor(Utilities.getcolor(this,true));

    }
}
