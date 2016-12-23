package com.priyanshbalyan.saber;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.app.AlertDialog;
import android.text.Html;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Priyansh on 21-Oct-16.
 */

public class Utilities {
    public interface ACTION
    {
        public static String MAIN_ACTION = "com.priyanshbalyan.saber.action.main";
        public static String INIT_ACTION = "com.priyanshbalyan.saber.action.init";
        public static String PREV_ACTION = "com.priyanshbalyan.saber.action.prev";
        public static String PLAY_ACTION = "com.priyanshbalyan.saber.action.play";
        public static String NEXT_ACTION = "com.priyanshbalyan.saber.action.next";
        public static String START_FOREGROUND_ACTION = "com.priyanshbalyan.saber.action.startforeground";
        public static String STOP_FOREGROUND_ACTION = "com.priyanshbalyan.saber.action.stopforeground";
    }

    public interface NOTIFICATION_ID
    {
        public static int FOREGROUND_SERVICE = 101 ;
    }

    public static String textDuration(int duration)
    {
        if (duration < 3600000)
            return String.format("%02d:%02d", duration / 60000, (duration / 1000) % 60);
        else
            return String.format("%d:%02d:%02d", duration / 3600000, (duration / 60000) % 60 , (duration / 1000) % 60);
    }


    public static void circularReveal(View v)
    {
        if(Build.VERSION.SDK_INT >= 21) {
            int cx = v.getMeasuredWidth() / 2;
            int cy = v.getMeasuredHeight() / 2;
            int radius = (int) Math.hypot(v.getWidth(), v.getHeight()) / 2;

            Animator circularreveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);

            if (v.getVisibility() == View.INVISIBLE) {
                v.setVisibility(View.VISIBLE);
                try{ circularreveal.start(); }catch(Exception e){}
            }
        }else
            v.setVisibility(View.VISIBLE);

    }


    public static void circularUnreveal(final View v)
    {
        if(Build.VERSION.SDK_INT >= 21) {
            int cx = v.getMeasuredWidth() / 2;
            int cy = v.getMeasuredHeight() / 2;
            int radius = (int) Math.hypot(v.getWidth(), v.getHeight()) / 2;

            Animator circularunreveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, radius, 0);
            circularunreveal.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    v.setVisibility(View.INVISIBLE);
                }
            });
            try{ circularunreveal.start(); }catch(Exception e){}
        }else
            v.setVisibility(View.INVISIBLE);
    }

    public static void showAboutDialog(final Context c)
    {
        AlertDialog d = new AlertDialog.Builder(c).create();
        d.setTitle("Who developed this App?");
        d.setMessage("Developed by Priyansh Balyan \n");
        d.setButton("FINE", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                // TODO: Implement this method
            }
        });

        d.setButton2("Linkedin", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                // TODO: Implement this method
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://in.linkedin.com/in/priyansh-balyan-461707127"));
                c.startActivity(i);
            }
        });

        d.setButton3("Rate the App!!", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent ri = new Intent(Intent.ACTION_VIEW);
                ri.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.priyanshbalyan.saber"));
                c.startActivity(ri);
            }
        });
        d.show();
    }

    public static void showOptionsDialog(final Context c, final VideoFolders vf)
    {
        //Toast.makeText(getApplicationContext(), "Function called", Toast.LENGTH_SHORT).show();
        List<String> s = new ArrayList<>();
        s.add("Play");
        s.add("Info");
        // s.add("item2");
        AlertDialog.Builder b = new AlertDialog.Builder(c) ;
        b.setAdapter(new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, s), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                // TODO: Implement this method
                switch (p2)
                {
                    case 0 : playFolder(c, vf);
                        break;

                    case 1 : infoDialog(c,vf);
                        break;
                }
            }
        });
        AlertDialog d = b.create();
        d.setTitle("Options");

        d.show();
    }

    public static void playFolder(Context c, VideoFolders vf){
        Intent i = new Intent(c,FullScreenPlayback.class);
        i.setAction(Utilities.ACTION.MAIN_ACTION);
        i.putExtra("videopaths",vf.vpif.toArray(new String[0]));
        i.putExtra("videonames",vf.vnif.toArray(new String[0]));
        i.putExtra("pos",0);
        i.putExtra("currentduration",0);
        i.putExtra("continueflag",true);
        c.startActivity(i);
    }

    public static void renameDialog(final Context c, final VideoFolders vf)
    {
        AlertDialog.Builder rd = new AlertDialog.Builder(c);
        final EditText edittext = new EditText(c);
        rd.setMessage("Enter New name");
        rd.setTitle(vf.foldername + " will be renamed.");
        rd.setView(edittext);
        rd.setPositiveButton("Rename", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                // TODO: Implement this method
                String editedname = edittext.getText().toString();
                File file = new File(vf.vpif.get(0));
                File folderfile = new File(file.getParent());
                File newfile = new File(folderfile.getParent() + "/" + editedname);
                folderfile.renameTo(newfile);
                Toast.makeText(c, "Folder renamed.", Toast.LENGTH_SHORT).show();
                //videostab.itemchangenotifier();
            }
        });
        rd.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                // TODO: Implement this method
            }
        });
        rd.show();
    }

    public static void infoDialog(Context c, VideoFolders vf)
    {
        AlertDialog id = new AlertDialog.Builder(c).create();
        id.setTitle(vf.foldername + " Info");

        File file = new File(vf.vpif.get(0));
        File parentfile = new File(file.getParent());

        String details = "Location : " + parentfile.getAbsolutePath()
                + "\nSize : " + vf.foldersize / 1024 + " KB"
                + "\nContains : " + vf.vpif.size() + " Videos"
                ;

        id.setMessage(details);
        id.show();

		/*List<String> s = new ArrayList<>();
		 s.add("item1");
		 s.add("item2");
		 AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this) ;
		 b.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, s),
		 new DialogInterface.OnClickListener(){
		 @Override
		 public void onClick(DialogInterface p1, int p2)
		 {
		 // TODO: Implement this method
		 }
		 });
		 AlertDialog d = b.create();
		 d.setTitle("Options");

		 d.show();*/
    }

    public static void deleteDialog(final Context c, final VideoFolders vf)
    {
        AlertDialog deld = new AlertDialog.Builder(c).create();
        deld.setTitle("Are you sure you want to delete " + vf.foldername + " ?");
        deld.setMessage(vf.vpif.size() + " videos will be deleted.");
        /*deld.setButton("YES", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                // TODO: Implement this method
                File file = new File(vf.vpif.get(0));
                File parentfile = new File(file.getParent());
                boolean deleted = parentfile.delete();
                if (deleted)
                    Toast.makeText(c, "Folder deleted.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(c, "Unable to delete.", Toast.LENGTH_SHORT).show();
                //videostab.itemchangenotifier();

            }
        });
        deld.setButton2("NO", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                // TODO: Implement this method
            }
        });*/
        deld.show();
    }

    public static void albumOptions(final Context c, final List<AlbumsData> ad, final int apos){
        List<String> a = new ArrayList<>();
        a.add("Play Album");
      //  a.add("Add to Now Playing");
        a.add("Details");

        AlertDialog.Builder b = new AlertDialog.Builder(c);
        b.setAdapter(new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, a), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                // TODO: Implement this method
                switch(p2){
                    case 0: ((MainActivity)c).MusicPlayback(ad,apos,0);
                        break;

                   // case 1: //((MainActivity)c).addToMusicPlayback(apos);
                    //    break;

                    case 1: AlertDialog info = new AlertDialog.Builder(c).create();
                        info.setTitle(ad.get(apos).albumname);
                        info.setMessage("Artist : "+ad.get(apos).artistname+
                                "\nYear : "+ad.get(apos).year+
                                "\nTracks : "+ad.get(apos).mpia.size());
                        info.show();
                        break;
                }
            }
        });
        AlertDialog d = b.create();
        b.setTitle("Options");
        b.show();
    }


    public static int getcolor(Context c, boolean dark){
        SharedPreferences prefs = c.getSharedPreferences("Prefs",0);
        int t = prefs.getInt("Theme",0);
        //0 Crimson Red, 1 Jet Black, 2 Blue Azure, 3 Fuchsia, 4 Purple Orchid. 5 Classic Teal, 6 Burning Amber
        if(!dark) {
            switch (t) {
                case 0: return Color.parseColor("#e53935");
                case 1: return Color.parseColor("#000000");
                case 2: return Color.parseColor("#2196f3");
                case 3: return Color.parseColor("#f06292");
                case 4: return Color.parseColor("#ab47bc");
                case 5: return Color.parseColor("#009688");
                case 6: return Color.parseColor("#ff8f00");
            }
        }else {
            switch (t) {
                case 0: return Color.parseColor("#d32f3f");
                case 1: return Color.parseColor("#000000");
                case 2: return Color.parseColor("#1e88e5");
                case 3: return Color.parseColor("#ec407a");
                case 4: return Color.parseColor("#9c27b0");
                case 5: return Color.parseColor("#00897b");
                case 6: return Color.parseColor("#ff6f00");
            }
        }
        return Color.parseColor("#000000");
    }
}
