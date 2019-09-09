package com.kozyrev.jotdown_room.CustomViews;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class WrapContentHeightViewPager extends ViewPager {

    public WrapContentHeightViewPager(Context context){
        super(context);
    }

    public WrapContentHeightViewPager(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){

        int mode = MeasureSpec.getMode(heightMeasureSpec);

        if(mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int height = 0;
            for(int i = 0; i< getChildCount(); i++){
                View child = getChildAt(i);
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int childMeasuredHeight = child.getMeasuredHeight();
                if(childMeasuredHeight > height){
                    height = childMeasuredHeight;
                }
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
