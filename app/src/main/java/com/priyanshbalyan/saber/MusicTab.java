package com.priyanshbalyan.saber;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Priyansh on 21-Oct-16.
 */

public class MusicTab extends Fragment implements MyItemClickListener, MyItemLongClickListener {
    Cursor ca, cm;

    String[] a = {MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.LAST_YEAR, MediaStore.Audio.Albums.ALBUM_ART};
    String[] m = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST};

    int albumnameindex, artistnameindex, musicnameindex, musicpathindex, durationindex;
    int height,width ;

    RecyclerView rva;
    RVAlbumsAdapter rvalbumsadapter;

    Bitmap[] albumartlist;
    AlbumArtLoader albumartloader;

    List<AlbumsData> ad;
    boolean tracklistopen = false;
    int albumposition = 0;

    LinearLayout albumdetaillayout;
    SquareImageView albumart;
    TextView albumdetails;

    @Override
    public void onDestroyView() {
        // TODO: Implement this method
        super.onDestroyView();
        if (!albumartloader.isCancelled())
            albumartloader.cancel(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: Implement this method
        View view = inflater.inflate(R.layout.musictab, container, false);

        ca = getActivity().managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, a, null, null, MediaStore.Audio.Albums.ALBUM + " ASC");
        cm = getActivity().managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, m, null, null, MediaStore.Audio.Media.ALBUM + " ASC");

        albumnameindex = cm.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
        artistnameindex = cm.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        musicnameindex = cm.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        musicpathindex = cm.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        durationindex = cm.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

        albumdetaillayout = (LinearLayout) view.findViewById(R.id.musictabalbumdetail);
        albumart = (SquareImageView) view.findViewById(R.id.ivmusictabalbumart);
        albumdetails = (TextView) view.findViewById(R.id.tvmusictabalbumdetails);

        albumdetaillayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((MainActivity) getActivity()).getCurrentTrackPosition() == -1)
                    ((MainActivity) getActivity()).MusicPlayback(ad, albumposition, 0);
                else if (((MainActivity) getActivity()).getCurrentTrackPosition() == (int) albumdetaillayout.getTag() && ((MainActivity) getActivity()).getCurrentAlbumPosition() == albumposition)
                    ((MainActivity) getActivity()).expandDrawer();
                else
                    ((MainActivity) getActivity()).MusicPlayback(ad, albumposition, 0);
            }
        });

        rva = (RecyclerView) view.findViewById(R.id.rvalbum);
        rva.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        rva.setHasFixedSize(true);

        albumsdatasorter();

        rvalbumsadapter = new RVAlbumsAdapter(getActivity().getApplicationContext());
        rva.setAdapter(rvalbumsadapter);
        rvalbumsadapter.setOnItemClickListener(this);
        rvalbumsadapter.setOnLongClickListener(this);

        albumartlist = new Bitmap[ad.size()];
        albumartloader = new AlbumArtLoader();
        albumartloader.execute();

        return view;
    }

    @Override
    public void onItemClick(View v, int position) {
        // TODO: Implement this method
        try {
            if (tracklistopen) {
                albumdetaillayout.setTag(position);
                if (((MainActivity) getActivity()).getCurrentTrackPosition() == -1)
                    ((MainActivity) getActivity()).MusicPlayback(ad, albumposition, position);
                else if (((MainActivity) getActivity()).getCurrentTrackPosition() == position && ((MainActivity) getActivity()).getCurrentAlbumPosition() == albumposition) {
                    ((MainActivity) getActivity()).expandDrawer();
                    //Toast.makeText(getActivity().getApplicationContext(),"Currently playing this track Atul hutiye.",Toast.LENGTH_SHORT).show();
                } else
                    ((MainActivity) getActivity()).MusicPlayback(ad, albumposition, position);
                //Toast.makeText(getActivity().getApplicationContext(), albumposition +" "+position,Toast.LENGTH_SHORT).show();
            } else {
                albumposition = position;
                albumdetaillayout.setVisibility(View.VISIBLE);

                if (albumartlist[albumposition] != null)
                    albumart.setImageBitmap(albumartlist[albumposition]);
                else
                    albumart.setImageResource(R.drawable.defaultalbumimage);

                albumdetails.setText(ad.get(albumposition).albumname + "\n" + ad.get(albumposition).artistname + "\n" + ad.get(albumposition).mpia.size() + " Track");
                if (ad.get(albumposition).mpia.size() > 1)
                    albumdetails.setText(albumdetails.getText().toString() + "s");
                if (ad.get(albumposition).year != null)
                    albumdetails.setText(albumdetails.getText().toString() + " • " + ad.get(albumposition).year);

                rva.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                RVTracksAdapter rvtracksadapter = new RVTracksAdapter(getActivity().getApplicationContext(), position, ad);
                rva.setAdapter(rvtracksadapter);
                rvtracksadapter.setOnItemClickListener(this);

                tracklistopen = true;
            }
        }catch(Exception e){}
    }

    public void closeTrackList() {
        tracklistopen = false;
        albumdetaillayout.setVisibility(View.GONE);
        rva.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        rva.setAdapter(rvalbumsadapter);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        // TODO: Implement this method
        Utilities.albumOptions(getActivity(), ad, position);
    }

    public class RVAlbumsAdapter extends RecyclerView.Adapter<RVAlbumsAdapter.ItemAlbumsViewHolder> {
        Context context;
        MyItemClickListener clicklistener;
        MyItemLongClickListener longclicklistener;

        RVAlbumsAdapter(Context c) {
            this.context = c;
        }

        public void setOnItemClickListener(MyItemClickListener mylistener) {
            this.clicklistener = mylistener;
        }

        public void setOnLongClickListener(MyItemLongClickListener mylongclicklistener) {
            this.longclicklistener = mylongclicklistener;
        }

        @Override
        public int getItemCount() {
            // TODO: Implement this method
            return ad.size();
        }

        @Override
        public ItemAlbumsViewHolder onCreateViewHolder(ViewGroup vg, int p2) {
            // TODO: Implement this method
            View v = LayoutInflater.from(vg.getContext()).inflate(R.layout.musiclistitemfull, vg, false);
            ItemAlbumsViewHolder iavh = new ItemAlbumsViewHolder(v, clicklistener, longclicklistener);
            return iavh;
        }

        @Override
        public void onBindViewHolder(ItemAlbumsViewHolder iavh, int i) {
            // TODO: Implement this method
            iavh.albumname.setText(ad.get(i).albumname);
            iavh.artistname.setText(ad.get(i).artistname);

            if (albumartlist[i] != null)
                iavh.albumart.setImageBitmap(albumartlist[i]);
            else
                iavh.albumart.setImageResource(R.drawable.defaultalbumimage);

        }

        public class ItemAlbumsViewHolder extends RecyclerView.ViewHolder {

            MyItemClickListener myclicklistener;
            MyItemLongClickListener mylongclicklistener;

            TextView albumname, artistname;
            SquareImageView albumart;

            ItemAlbumsViewHolder(View v, MyItemClickListener clicklistener, MyItemLongClickListener longclicklicklistener) {
                super(v);

                albumname = (TextView) v.findViewById(R.id.tvalbumname);
                artistname = (TextView) v.findViewById(R.id.tvartistname);
                albumart = (SquareImageView) v.findViewById(R.id.ivalbumthumb);

                artistname.setTextColor(Utilities.getcolor(getActivity().getApplicationContext(),false));

                this.myclicklistener = clicklistener;
                this.mylongclicklistener = longclicklicklistener;

                width = albumart.getMeasuredWidth();
                height = albumart.getMeasuredHeight();

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View p1) {
                        // TODO: Implement this method
                        if (myclicklistener != null)
                            myclicklistener.onItemClick(p1, getPosition());
                    }
                });
                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View p1) {
                        // TODO: Implement this method
                        if (mylongclicklistener != null)
                            mylongclicklistener.onItemLongClick(p1, getPosition());
                        return true;
                    }
                });

            }
        }

    }

    private class AlbumArtLoader extends AsyncTask<Object, Void, Bitmap> {

        int position;

        public AlbumArtLoader() {

        }

        @Override
        protected Bitmap doInBackground(Object[] p1) {
            // TODO: Implement this method

                for (int i = 0; i < ad.size(); i++) {
                    final int j = i;
                    String albumpath = ad.get(i).albumart;
                    Bitmap b;
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4 ;
                        b = BitmapFactory.decodeFile(albumpath,options);
                    } catch (Exception e) {
                        b = null;
                    }
                    if (b != null)
                        albumartlist[i] = b;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: Implement this method
                            rvalbumsadapter.notifyItemChanged(j);
                        }
                    });

                }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO: Implement this method
        }
    }

    public void albumsdatasorter() {

        String comparestring = "Album Name";
        boolean flag = false;
        int pos = -1, flagpos = 0;

        AlbumsData adderclass;
        ad = new ArrayList<>();

        for (int i = 0; i < cm.getCount(); i++) {
            cm.moveToPosition(i);
            String aname = cm.getString(albumnameindex); //getting folder name from path

            if (!comparestring.equals(aname)) {
                comparestring = aname;
                for (int j = 0; j < ad.size(); j++) {
                    if (aname.equals(ad.get(j).albumname)) {    //to check if item is already in the list
                        flag = true;  //album is already in list
                        flagpos = j;
                        break;
                    } else
                        flag = false;
                }
                if (!flag) {     //if folder name is not already in list
                    pos++;
                    ca.moveToPosition(pos);
                    adderclass = new AlbumsData();
                    adderclass.albumname = aname;
                    for (int k = 0; k < ca.getCount(); k++) {
                        ca.moveToPosition(k);
                        if (aname.equals(ca.getString(ca.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)))) {
                            adderclass.artistname = ca.getString(ca.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
                            adderclass.albumart = ca.getString(ca.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                            adderclass.year = ca.getString(ca.getColumnIndexOrThrow(MediaStore.Audio.Albums.LAST_YEAR));
                            break;
                        }
                    }
                    ad.add(adderclass);
                }
            }

            String ar = cm.getString(artistnameindex);
            if(ar.contains("<unknown>") || (ar == null) || (ar.compareToIgnoreCase("<unknown>") == 0) || (ar.compareToIgnoreCase("") == 0))
                ar = "Unknown Artist" ;

            if (aname.equals(ad.get(pos).albumname)) {  //if current path album is same as class albumname at pos
                ad.get(pos).mpia.add(cm.getString(musicpathindex));  //Adding music path to generated albumsdata class
                ad.get(pos).mnia.add(cm.getString(musicnameindex));
                ad.get(pos).maia.add(ar);
                ad.get(pos).mdia.add(cm.getString(durationindex));
            } else if (aname.equals(ad.get(flagpos).albumname)) {
                ad.get(flagpos).mpia.add(cm.getString(musicpathindex));  //Adding music paths to generated AlbumsData class
                ad.get(flagpos).mnia.add(cm.getString(musicnameindex));
                ad.get(flagpos).maia.add(ar);
                ad.get(flagpos).mdia.add(cm.getString(durationindex));
            }
        }

    }

    public void theme(){
        TextView v = (TextView)getActivity().findViewById(R.id.tvmusictabalbumdetails);
        v.setTextColor(Utilities.getcolor(getActivity().getApplicationContext(),false));
        rvalbumsadapter.notifyItemRangeChanged(0,ad.size());
    }
}
