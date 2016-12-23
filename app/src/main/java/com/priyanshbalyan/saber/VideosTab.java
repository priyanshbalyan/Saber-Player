package com.priyanshbalyan.saber;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Priyansh on 21-Oct-16.
 */

public class VideosTab extends Fragment implements MyItemClickListener,MyItemLongClickListener{

    Cursor c ;
    RecyclerView rv ;
    RVFolderAdapter rvfolderadapter ;

    int vpathindex,vnameindex,vidindex,vresindex ;
    List<String> foldernames ;

    Bitmap[] bitmaplist ;
    ThumbnailLoader thumbloader ;
    List<VideoFolders> vf ;
    String[] vp,vn,vr ;
    long[] vid ;

    String[] p = {MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID, MediaStore.Video.Media.HEIGHT} ;

    public VideosTab(){

    }

    @Override
    public void onDestroyView()
    {
        // TODO: Implement this method
        super.onDestroyView();
        if(!thumbloader.isCancelled())
            thumbloader.cancel(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.videostab, container, false);
        rv = (RecyclerView)view.findViewById(R.id.rvfolder);
        rv.setLayoutManager(new GridLayoutManager(getActivity(),1));
        rv.setHasFixedSize(true);

        c = getActivity().managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, p, null, null, MediaStore.Video.Media.DISPLAY_NAME + " ASC");

        vpathindex = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        vnameindex = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
        vidindex = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        vresindex = c.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT);

        videofoldersorter();

        rvfolderadapter = new RVFolderAdapter(getActivity().getApplicationContext());
        rv.setAdapter(rvfolderadapter);
        rvfolderadapter.setOnItemClickListener(this);
        rvfolderadapter.setOnItemLongClickListener(this);

        bitmaplist = new Bitmap[vf.size()] ;
        thumbloader = new ThumbnailLoader();
        thumbloader.execute();
        return view ;
    }

    @Override
    public void onItemClick(View v, int position)
    {
        // TODO: Implement this method
        //Toast.makeText(getActivity().getApplicationContext(), "item "+position, Toast.LENGTH_SHORT).show();
        vp = new String[vf.get(position).vpif.size()] ;
        vn = new String[vf.get(position).vnif.size()] ;
        vr = new String[vf.get(position).vrif.size()] ;
        vid = new long[vf.get(position).vidif.size()] ;

        for(int i=0 ; i<vf.get(position).vpif.size() ; i++){
            vp[i] = vf.get(position).vpif.get(i);
            vn[i] = vf.get(position).vnif.get(i);
            vr[i] = vf.get(position).vrif.get(i);
            vid[i] = vf.get(position).vidif.get(i);
        }

        Intent intent = new Intent(getActivity().getApplicationContext(), FolderVideos.class);
        intent.putExtra("videopaths",vp);
        intent.putExtra("videonames",vn);
        intent.putExtra("videores",vr);
        intent.putExtra("videoid",vid);
        intent.putExtra("foldername",vf.get(position).foldername);


        startActivity(intent);

    }

    @Override
    public void onItemLongClick(View view, final int position)
    {
        // TODO: Implement this method
        //Toast.makeText(getActivity().getApplicationContext(),"long click",Toast.LENGTH_SHORT).show();
        Utilities.showOptionsDialog(getActivity(), vf.get(position));
    }

    public void itemchangenotifier(){
        Toast.makeText(getActivity().getApplicationContext(), "Fragment Function called", Toast.LENGTH_SHORT).show();
        videofoldersorter();
        rvfolderadapter.notifyDataSetChanged();
    }

    public void videofoldersorter(){

        String comparestring = "Folder Name" ;
        boolean flag=false ;
        int pos=-1,flagpos=0;

        VideoFolders adderclass;
        vf = new ArrayList<>();

        for(int i=0 ; i<c.getCount() ; i++){
            c.moveToPosition(i);
            File file = new File(c.getString(vpathindex));
            File parentfile = new File(file.getParent());
            String fname = parentfile.getName();  //getting folder name from path

            if(!comparestring.equals(fname)){
                comparestring = fname ;
                for(int j=0 ; j<vf.size() ; j++)
                {
                    if(fname.equals(vf.get(j).foldername)){    //to check if item is already in the list
                        flag = true ;  //folder is already in list
                        flagpos = j;
                        break ;
                    }else
                        flag = false ;
                }
                if(!flag){     //if folder name is not already in list
                    pos++ ;
                    adderclass = new VideoFolders();
                    adderclass.foldername = fname ;
                    vf.add(adderclass);
                }
            }

            String filename = file.getName();
            filename.replaceAll("/(.*)\\.[^.]+$/","") ;

            if(fname.equals(vf.get(pos).foldername)){  //if current path folder is same as class foldername at pos
                vf.get(pos).vpif.add(c.getString(vpathindex));  //Adding video paths to generated video folder class
                vf.get(pos).vrif.add(c.getString(vresindex));
                vf.get(pos).vidif.add(c.getLong(vidindex));
                vf.get(pos).vnif.add(filename);
                vf.get(pos).foldersize += file.length();

            }else if(fname.equals(vf.get(flagpos).foldername)){
                vf.get(flagpos).vpif.add(c.getString(vpathindex));
                vf.get(flagpos).vnif.add(filename);
                vf.get(flagpos).vrif.add(c.getString(vresindex));
                vf.get(flagpos).vidif.add(c.getLong(vidindex));
                vf.get(flagpos).foldersize += file.length();
            }
        }

    }

    public class RVFolderAdapter extends RecyclerView.Adapter<RVFolderAdapter.ItemFolderViewHolder>
    {
        Context context ;
        MyItemClickListener listener ;
        MyItemLongClickListener mylongclicklistener ;

        RVFolderAdapter(Context context){
            this.context = context;
        }

        public void setOnItemClickListener(MyItemClickListener mylistener){
            this.listener = mylistener ;
        }

        public void setOnItemLongClickListener(MyItemLongClickListener mylongclicklistener){
            this.mylongclicklistener = mylongclicklistener ;
        }

        @Override
        public int getItemCount()
        {
            // TODO: Implement this method
            return vf.size() ;
        }

        @Override
        public ItemFolderViewHolder onCreateViewHolder(ViewGroup vg, int i)
        {
            // TODO: Implement this method
            View v = LayoutInflater.from(vg.getContext()).inflate(R.layout.folderlistitem,vg,false);
            ItemFolderViewHolder ifvh = new ItemFolderViewHolder(v,listener,mylongclicklistener);

            return ifvh ;
        }

        @Override
        public void onBindViewHolder(ItemFolderViewHolder ifvh, int i)
        {
            // TODO: Implement this method
            String fname = vf.get(i).foldername;
            String fdetails = vf.get(i).vpif.size() + " Video";
            if(vf.get(i).vpif.size() > 1)
                fdetails += "s";
            fdetails += " • " + VideoFolders.filesize(vf.get(i).foldersize) ;

            ifvh.foldername.setText(fname);
            ifvh.folderdetails.setText(fdetails);

            if(bitmaplist[i] != null)
                ifvh.vfolderthumb.setImageBitmap(bitmaplist[i]);
        }

        public class ItemFolderViewHolder extends RecyclerView.ViewHolder{
            TextView foldername, folderdetails ;
            CustomImageView vfolderthumb ;
            MyItemClickListener mylistener ;
            MyItemLongClickListener mylongclicklistener ;

            ItemFolderViewHolder(View v, MyItemClickListener listener,MyItemLongClickListener longclicklistener){
                super(v);
                foldername = (TextView)v.findViewById(R.id.tvfoldername);
                folderdetails = (TextView)v.findViewById(R.id.tvfolderdetails);
                vfolderthumb = (CustomImageView)v.findViewById(R.id.ivfolderthumb);

                folderdetails.setTextColor(Utilities.getcolor(getActivity().getApplicationContext(),false));

                this.mylistener = listener;
                this.mylongclicklistener = longclicklistener ;
                v.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View p1)
                    {
                        // TODO: Implement this method
                        if(mylistener != null){
                            mylistener.onItemClick(p1, getPosition());
                        }
                    }
                });


                v.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View p1)
                    {
                        // TODO: Implement this method
                        if(mylongclicklistener != null){
                            mylongclicklistener.onItemLongClick(p1,getPosition());
                        }
                        return true ;
                    }
                });


            }
        }

    }


    private class ThumbnailLoader extends AsyncTask<Object,Void,Bitmap>
    {

        int position ;

        public ThumbnailLoader(){

        }

        @Override
        protected Bitmap doInBackground(Object[] p1)
        {
            // TODO: Implement this method

            for(int i=0 ; i<vf.size() ; i++){
                final int j = i ;
                long vcolumnid = vf.get(i).vidif.get(0);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                Bitmap b ;
                try{ b = MediaStore.Video.Thumbnails.getThumbnail(getActivity().getContentResolver(), vcolumnid, MediaStore.Video.Thumbnails.MINI_KIND, options); }
                catch(Exception e){b=null;}

                if(b != null)
                    bitmaplist[i] = b ;
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run()
                    {
                        // TODO: Implement this method
                        rvfolderadapter.notifyItemChanged(j);
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

    public void theme(){
        rvfolderadapter.notifyItemRangeChanged(0,vf.size());
    }
}
