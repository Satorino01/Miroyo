package com.example.kobayashi_satoru.miroyo;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class SquareImageView extends android.support.v7.widget.AppCompatImageView {

    private boolean mAdjustWidth;

    public SquareImageView(Context context) {
        super(context, null);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mAdjustWidth = false;
        if (attrs != null) {
            TypedArray attrsArray = context.obtainStyledAttributes(attrs, R.styleable.SquareImageView);
            mAdjustWidth = attrsArray.getBoolean(R.styleable.SquareImageView_adjust_width, false);
            attrsArray.recycle();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int sideLength = 0;
        if (mAdjustWidth) {
            // 横幅に合わせる
            sideLength = getMeasuredWidth();
        } else {
            // 縦幅に合わせる
            sideLength = getMeasuredHeight();
        }
        setMeasuredDimension(sideLength, sideLength);

    }

    public void setAdjustWidth(boolean adjustWidth) {
        mAdjustWidth = adjustWidth;
    }

}
