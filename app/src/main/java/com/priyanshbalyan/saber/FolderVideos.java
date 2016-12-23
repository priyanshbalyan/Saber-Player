package com.priyanshbalyan.saber;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

public class FolderVideos extends AppCompatActivity implements MyItemClickListener
{

    Toolbar toolbar ;
    String[] vn,vp,vr ;
    long[] vid ;
    RecyclerView rvf ;
    RVAdapter rvadapter ;
    Bitmap[] bitmaplist ;
    ThumbnailLoader tloader ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foldervideos);

        toolbar = (Toolbar)findViewById(R.id.foldertoolbar);
        setSupportActionBar(toolbar);
        //toolbar.setHorizontalFadingEdgeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View p1)
            {
                // TODO: Implement this method
                finish();
            }
        });

        rvf = (RecyclerView)findViewById(R.id.rvvideo);
        rvf.setLayoutManager(new GridLayoutManager(this, 2));
        rvf.setHasFixedSize(true);

        vp = getIntent().getStringArrayExtra("videopaths");
        vn = getIntent().getStringArrayExtra("videonames");
        vr = getIntent().getStringArrayExtra("videores");
        vid = getIntent().getLongArrayExtra("videoid");

        String fname = getIntent().getStringExtra("foldername");
        getSupportActionBar().setTitle(fname);
        toolbar.setTitleTextColor(Color.WHITE);

        rvadapter = new RVAdapter(getApplicationContext());

        rvadapter.setOnItemClickListener(this);
        rvf.setAdapter(rvadapter);

        bitmaplist = new Bitmap[vid.length] ;
        tloader = new ThumbnailLoader();
        tloader.execute();

        theme();
    }

    @Override
    public void onItemClick(View v, int position)
    {
        // TODO: Implement this method
        Intent i = new Intent(this,FullScreenPlayback.class);
        i.setAction(Utilities.ACTION.MAIN_ACTION);
        i.putExtra("videopaths",vp);
        i.putExtra("videonames",vn);
        i.putExtra("pos",position);
        i.putExtra("currentduration",0);
        i.putExtra("continueflag",true);


        startActivity(i);
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ItemViewHolder>
    {
        Context context ;
        MyItemClickListener listener ;

        RVAdapter(Context context)
        {
            this.context = context;
        }

        public void setOnItemClickListener(MyItemClickListener mylistener)
        {
            this.listener = mylistener ;
        }

        @Override
        public int getItemCount()
        {
            // TODO: Implement this method
            return vp.length;

        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup vg, int i)
        {
            // TODO: Implement this method
            View v = LayoutInflater.from(vg.getContext()).inflate(R.layout.listitemwiththumb, vg, false);
            ItemViewHolder ivh = new ItemViewHolder(v, listener);

            return ivh ;
        }

        @Override
        public void onBindViewHolder(ItemViewHolder ivh, int i)
        {
            // TODO: Implement this method
            String name = vn[i] ;
            ivh.vname.setText(name);

            File f = new File(vp[i]);
            String res = "" ;
            if(vr[i] != null)
                res = vr[i] + "P" + " • " ;
            res += filesize(vp[i]) ;
            ivh.vres.setText(res);

            if (bitmaplist[i] != null)
                ivh.vthumb.setImageBitmap(bitmaplist[i]);
            else
                ivh.vthumb.setImageResource(R.drawable.defaultvideoimage);

        }

        public class ItemViewHolder extends RecyclerView.ViewHolder
        {
            TextView vname,vres ;
            CustomImageView vthumb ;
            MyItemClickListener mylistener ;

            ItemViewHolder(View v, MyItemClickListener listener)
            {
                super(v);
                vname = (TextView)v.findViewById(R.id.tvvideoname);
                vres = (TextView)v.findViewById(R.id.tvvideoresolution);
                vthumb = (CustomImageView)v.findViewById(R.id.ivthumb);

                this.mylistener = listener;
                v.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View p1)
                    {
                        // TODO: Implement this method
                        if (mylistener != null)
                        {
                            mylistener.onItemClick(p1, getPosition());
                        }
                    }
                });
            }
        }


    }

    private class ThumbnailLoader extends AsyncTask<Object,Void,Bitmap>
    {

        public ThumbnailLoader()
        {

        }

        @Override
        protected Bitmap doInBackground(Object[] p1)
        {
            // TODO: Implement this method
            for (int i=0 ; i < vp.length ; i++)
            {
                final int j = i ;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;

                Bitmap b ;
                try {
                    b = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), vid[i], MediaStore.Video.Thumbnails.MINI_KIND, options);
                }catch(Exception e){b=null;}
                if (b != null)
                    bitmaplist[i] = b ;
                runOnUiThread(new Runnable(){
                    @Override
                    public void run()
                    {
                        // TODO: Implement this method
                        rvadapter.notifyItemChanged(j);
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            // TODO: Implement this method
        }
    }

    public String filesize(String path){
        File f = new File(path);
        long fs = f.length();
        if(fs < 1024)
            return fs+" B";
        else if(fs < 1048576)
            return fs/1024 + " KB";
        else if(fs < 1073741824)
            return fs/1024/1024 + " MB" ;
        else
            return String.format("%.02f GB",(float)(fs/1024.0/1024.0/1024.0)) ;

    }

    public void theme(){
        View v = findViewById(R.id.foldertoolbar);
        v.setBackgroundColor(Utilities.getcolor(this,false));
        if(Build.VERSION.SDK_INT >= 21)
            getWindow().setStatusBarColor(Utilities.getcolor(this,true));
    }
}
