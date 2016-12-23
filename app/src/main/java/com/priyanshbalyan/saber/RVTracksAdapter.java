package com.priyanshbalyan.saber;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Priyansh on 21-Oct-16.
 */

public class RVTracksAdapter extends RecyclerView.Adapter<RVTracksAdapter.ItemTracksViewHolder>
{
    Context context ;
    MyItemClickListener clicklistener ;
    MyItemLongClickListener longclicklistener ;
    int albumposition;
    List<AlbumsData> ad ;

    RVTracksAdapter(Context c, int pos, List<AlbumsData> ad)
    {
        this.context = c ;
        this.albumposition = pos ;
        this.ad = ad ;
    }

    public void setOnItemClickListener(MyItemClickListener mylistener){
        this.clicklistener = mylistener ;
    }

    public void setOnLongClickListener(MyItemLongClickListener mylongclicklistener){
        this.longclicklistener = mylongclicklistener;
    }

    @Override
    public int getItemCount()
    {
        // TODO: Implement this method
        return ad.get(albumposition).mpia.size() ;
    }

    @Override
    public ItemTracksViewHolder onCreateViewHolder(ViewGroup vg, int p2)
    {
        // TODO: Implement this method
        View v = LayoutInflater.from(vg.getContext()).inflate(R.layout.tracklistitem, vg, false);
        ItemTracksViewHolder itvh = new ItemTracksViewHolder(v, clicklistener, longclicklistener);
        return itvh;
    }

    @Override
    public void onBindViewHolder(ItemTracksViewHolder itvh, int i)
    {
        // TODO: Implement this method
        itvh.trackname.setText(ad.get(albumposition).mnia.get(i));
        itvh.artistname.setText(ad.get(albumposition).maia.get(i)+" • "+Utilities.textDuration(Integer.parseInt(ad.get(albumposition).mdia.get(i))));

    }

    public class ItemTracksViewHolder extends RecyclerView.ViewHolder{

        MyItemClickListener myclicklistener ;
        MyItemLongClickListener mylongclicklistener ;

        TextView trackname,artistname ;

        ItemTracksViewHolder(View v, MyItemClickListener clicklistener, MyItemLongClickListener longclicklicklistener){
            super(v);

            trackname = (TextView)v.findViewById(R.id.tvmusictabtrackname);
            artistname = (TextView)v.findViewById(R.id.tvmusictabartistname);

            artistname.setTextColor(Utilities.getcolor(context,false));

            this.myclicklistener = clicklistener ;
            this.mylongclicklistener = longclicklicklistener;

            v.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View p1)
                {
                    // TODO: Implement this method
                    if(myclicklistener != null)
                        myclicklistener.onItemClick(p1,getPosition());
                }
            });

            v.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View p1)
                {
                    // TODO: Implement this method
                    if(mylongclicklistener != null)
                        mylongclicklistener.onItemLongClick(p1,getPosition());
                    return true;
                }
            });

        }
    }

}