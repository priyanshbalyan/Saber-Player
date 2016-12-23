package com.priyanshbalyan.saber;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Priyansh on 21-Oct-16.
 */

public class CustomImageView extends ImageView
{
    public CustomImageView(final Context context){
        super(context);
    }

    public CustomImageView(final Context context, final AttributeSet attrs){
        super(context,attrs);
    }

    public CustomImageView(final Context context, final AttributeSet attrs, final int defStyle){
        super(context,attrs,defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // TODO: Implement this method
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredwidth = getMeasuredWidth();
        int measuredheight = (int)9*measuredwidth/16 ;

        setMeasuredDimension(measuredwidth,measuredheight);
    }
}
