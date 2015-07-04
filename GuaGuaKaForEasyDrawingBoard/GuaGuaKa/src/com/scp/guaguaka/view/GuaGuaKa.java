package com.scp.guaguaka.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GuaGuaKa extends View {
	private Canvas mCanvas;// 画布
	private Bitmap mBitmap;// 图片
	private Paint mOutterPaint;// 输出画笔
	private Path mPath;// 用户绘制的路径
	private int mLastX;// 记录用户上一次触摸的X坐标
	private int mLastY;// 记录用户上一次触摸的Y坐标

	public GuaGuaKa(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public GuaGuaKa(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GuaGuaKa(Context context) {
		this(context, null);
	}

	/**
	 * 进行一些初始化操作
	 */
	private void init() {
		mOutterPaint = new Paint();
		mPath = new Path();
	}

	/**
	 * 测量控件的尺寸，当我们的View调用super.onMeasure以后，我们就可以获得控件的宽和高
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		// 初始化Bitmap
		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		// 设置绘制path画笔的一些属性，也就是橡皮擦
		setOutPaint();
	}

	/**
	 * 设置绘制path画笔的一些属性
	 */
	private void setOutPaint() {
		mOutterPaint.setColor(Color.RED);
		mOutterPaint.setAntiAlias(true);// 抗锯齿
		mOutterPaint.setDither(true);// 防抖动
		mOutterPaint.setStrokeJoin(Paint.Join.ROUND);// 设置连接方式为圆角
		mOutterPaint.setStrokeCap(Paint.Cap.ROUND);// 设置画笔笔刷类型
		mOutterPaint.setStyle(Style.STROKE);// 设置画笔的填充方式为描边
		mOutterPaint.setStrokeWidth(20);// 设置画笔的一个宽度
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastX = x;
			mLastY = y;
			mPath.moveTo(mLastX, mLastY);
			break;
		case MotionEvent.ACTION_MOVE:
			int dx = Math.abs(x - mLastX);// 获得用户横向坐标移动的绝对值
			int dy = Math.abs(y - mLastY);// 获得用户纵向坐标移动的绝对值
			// 如果横向或者纵向移动的坐标值大于3像素说明路径有可连接性(接着绘制路径)
			if (dx > 3 || dy > 3) {
				mPath.lineTo(x, y);
			}

			mLastX = x;
			mLastY = y;

			break;
		case MotionEvent.ACTION_UP:
			break;

		default:
			break;
		}
		invalidate();
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// 我们没有用系统的Canvas绘图
		drawPath();
		canvas.drawBitmap(mBitmap, 0, 0, null);
	}

	private void drawPath() {
		mCanvas.drawPath(mPath, mOutterPaint);
	}

}
