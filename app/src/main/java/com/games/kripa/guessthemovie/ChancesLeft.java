package com.games.kripa.guessthemovie;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kripa on 23/7/2016.
 */

public class  ChancesLeft extends View {
    private List<String> mData = new ArrayList<String>();
    private boolean mShowLabelTextStrike = false;
    private float mTextWidth = 0.0f;
    private float mTextHeight = 0.0f;
    private float mTextX = 0.0f;
    private float mTextY = 0.0f;
    private float startX = 0.0f;
    private float startY = 0.0f;
    private float endX = 0.0f;
    private float endY = 0.0f;
    private float slope = 0.0f;
    private float intercept = 0.0f;
    private boolean flag;
    private String mLabelText;
    private Paint mTextPaint, mStrikePaint;
    private int mTextColor, mStrikeColor;
    private float mStatusBarHeight = 0.0f;
    public ChancesLeft(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ChancesLeft,
                0, 0
        );

        try {
            mShowLabelTextStrike = a.getBoolean(R.styleable.ChancesLeft_showLabelTextStrike, false);
            mTextHeight =  a.getDimension(R.styleable.ChancesLeft_labelHeight, 0.0f);
            mTextWidth =  a.getDimension(R.styleable.ChancesLeft_labelWidth, 0.0f);
            mTextColor = a.getColor(R.styleable.ChancesLeft_labelColor, 0xff000000);
            mStrikeColor = a.getColor(R.styleable.ChancesLeft_strikeColor, 0xff000000);
            mTextX = a.getDimension(R.styleable.ChancesLeft_labelTextX, 0.0f);
            mLabelText = a.getString(R.styleable.ChancesLeft_labelText);
            mStatusBarHeight = getStatusBarHeight();
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

        init();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void init() {
        // Set up the paint for the label text
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        if (mTextHeight == 0) {
            mTextHeight = mTextPaint.getTextSize();
        } else {
            mTextPaint.setTextSize(mTextHeight);
        }

        mStrikePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrikePaint.setColor(mStrikeColor);
        mStrikePaint.setStrokeWidth(5);
        setWillNotDraw(false);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(mLabelText, mTextX, mTextY, mTextPaint);
        // Draw the label text
        if (getShowTextStrike()) {
            canvas.drawLine(startX, startY, endX, endY, mStrikePaint);
        }
    }

    public void getCoordinates(){
        startX = 0;
        startY = 0;
        endX = startX + getTextWidth();
        endY = startY + getTextHeight();
    }

    public float getTextWidth() {
        return mTextWidth;
    }

    public void setTextWidth(float textWidth) {
        mTextWidth = textWidth;
        invalidate();
    }

    public float getTextHeight() {
        return mTextHeight;
    }

    public void setTextHeight(float textHeight) {
        mTextHeight = textHeight;
        invalidate();
    }

    public boolean getShowTextStrike () {
        return mShowLabelTextStrike;
    }

    public void setShowText(boolean showLabelTextStrike) {
        mShowLabelTextStrike = showLabelTextStrike;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTextX = 5.0f;
        mTextY = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

    public void drawStrike(){
        getCoordinates();
        slope = (endY - startY)/(endX-startX);
        intercept = endY - (slope * endX);
        ValueAnimator animation = ValueAnimator.ofFloat(startX, endX);
        animation.setDuration(1000);
        animation.start();
       animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
           @Override
           public void onAnimationUpdate(ValueAnimator animation) {
               Float value = (Float) animation.getAnimatedValue();
               endX = value;
               endY = slope * value + intercept;
               invalidate();
           }
       });
    }
}

