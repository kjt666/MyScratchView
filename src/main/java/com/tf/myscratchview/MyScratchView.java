package com.tf.myscratchview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * 目标图像是我们手指在屏幕上的轨迹形成的图像
 * 源图像就是刮刮卡的封面图像
 * 可以设置MyScratchView的显示模式为文本或者图像。
 */
public class MyScratchView extends View {
    /**
     * 画布
     */
    private Canvas mCanvas;
    /**
     * 画笔
     */
    private Paint mPaint;
    private Paint mPathPaint;
    /**
     * 手指路径
     */
    private Path mPath;
    /**
     * 目标图像
     */
    private Bitmap mDstBmp;
    /**
     * 源图像
     */
    private Bitmap mSrcBmp;
    private int mSrcBmpId;
    /**
     * 礼物图片
     */
    private Bitmap mGiftBmp;
    private int mGiftBmpId;
    /**
     * 礼物文字
     */
    private String mGiftText;
    private int mGiftTextSize;
    private int mGiftTextColor;
    /**
     * 内容区显示类型、有图片和文本类型。
     */
    private String mViewTypeStr;
    private ViewType mViewType;

    public MyScratchView(Context context) {
        this(context, null);
    }

    public MyScratchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyScratchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyScratchView);
        mViewTypeStr = ta.getString(R.styleable.MyScratchView_viewType);
        mSrcBmpId = ta.getResourceId(R.styleable.MyScratchView_scratchSrc, R.mipmap.guaguaka);
        //判断类型，根据类型选择初始化那些数据
        if (mViewTypeStr == null || "text".equals(mViewTypeStr)) {
            mViewType = ViewType.TEXT_MODE;
            //文本设置
            mGiftText = ta.getString(R.styleable.MyScratchView_giftText);
            mGiftTextSize = (int) ta.getDimension(R.styleable.MyScratchView_giftTextSize, sp2px(18));
            mGiftTextColor = ta.getColor(R.styleable.MyScratchView_giftTextColor, 0xff000000);
            //对画笔进行文本设置
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setTextSkewX(-0.25f);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setTextSize(mGiftTextSize);
            mPaint.setColor(mGiftTextColor);
            if (mGiftText == null || mGiftText.isEmpty()) {
                mGiftText = "谢谢惠顾！";
            }
        } else if ("src".equals(mViewTypeStr)) {
            mViewType = ViewType.SRC_MODE;
            mGiftBmpId = ta.getResourceId(R.styleable.MyScratchView_giftSrc, R.mipmap.gift);
            mGiftBmp = BitmapFactory.decodeResource(getResources(), mGiftBmpId, null);
        }
        ta.recycle();
        //源图像和目标图像是必须的
        mSrcBmp = BitmapFactory.decodeResource(getResources(), mSrcBmpId, null);
        mDstBmp = Bitmap.createBitmap(mSrcBmp.getWidth(), mSrcBmp.getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mDstBmp);
        mPath = new Path();
        mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPathPaint.setColor(Color.RED);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mPathPaint.setStrokeWidth(60);
        mPathPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas.drawPath(mPath, mPathPaint);
        if (mViewType == ViewType.SRC_MODE) {
            canvas.drawBitmap(mGiftBmp, 0, 0, mPaint);
        } else {
            canvas.drawText(mGiftText, getMeasuredWidth() / 2, getMeasuredHeight() / 2, mPaint);
        }
        int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), mPaint, Canvas.ALL_SAVE_FLAG);
        canvas.drawBitmap(mDstBmp, 0, 0, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        canvas.drawBitmap(mSrcBmp, 0, 0, mPaint);
        mPaint.setXfermode(null);
        canvas.restoreToCount(layerId);
    }

    private float mEndX, mEndY, mPreX, mPreY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(event.getX(), event.getY());
                mPreX = event.getX();
                mPreY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mEndX = (mPreX + event.getX()) / 2;
                mEndY = (mPreY + event.getY()) / 2;
                mPath.quadTo(mPreX, mPreY, mEndX, mEndY);
                mPreX = event.getX();
                mPreY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        postInvalidate();
        return true;
    }

    /**
     * 为控件选择合适的宽度
     *
     * @param widthMeasureSpec
     * @return
     */
    public int measureWidth(int widthMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int result = 0;
        if (mode == MeasureSpec.EXACTLY) {
            result = width;
        } else if (mode == MeasureSpec.AT_MOST) {
            result = mGiftBmp == null ? mSrcBmp.getWidth() : Math.max(mSrcBmp.getWidth(), mGiftBmp.getWidth());
        }
        return result;
    }


    /**
     * 为控件选择合适的高度
     *
     * @param heightMeasureSpec
     * @return
     */
    public int measureHeight(int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int result = 0;
        if (mode == MeasureSpec.EXACTLY) {
            result = height;
        } else if (mode == MeasureSpec.AT_MOST) {
            result = mGiftBmp == null ? mSrcBmp.getHeight() : Math.max(mSrcBmp.getHeight(), mGiftBmp.getHeight());
        }
        return result;
    }

    /**
     * dp转sp
     *
     * @return
     */
    protected int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    /**
     * 控件奖品内容显示类型
     */
    public enum ViewType {
        TEXT_MODE,
        SRC_MODE;
    }

    /**
     * 代码设置显示类型
     *
     * @param viewType
     */
    public void setViewType(ViewType viewType) {
        this.mViewType = viewType;
    }

    /**
     * 设置文本模式下显示的奖品文字颜色
     *
     * @param color
     */
    public void setTextColor(int color) {
        mPaint.setColor(color);
    }

    public void setTextSize(int size) {
        mPaint.setTextSize(sp2px(size));
    }

    /**
     * 设置文本模式下显示的奖品文字
     *
     * @param str
     */
    public void setGiftText(String str) {
        this.mGiftText = str;
    }

    /**
     * 设置图像模式的图片
     *
     * @param src
     */
    public void setGiftSrc(int src) {
        this.mGiftBmpId = src;
        mGiftBmp = BitmapFactory.decodeResource(getResources(), mGiftBmpId, null);
    }

    /**
     * 设置刮刮卡图层图片
     *
     * @param src
     */
    public void setScratchSrc(int src) {
        this.mSrcBmpId = src;
        mSrcBmp = BitmapFactory.decodeResource(getResources(), mSrcBmpId, null);
    }
}
