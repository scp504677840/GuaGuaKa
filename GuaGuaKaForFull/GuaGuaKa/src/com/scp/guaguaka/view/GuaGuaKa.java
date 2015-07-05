package com.scp.guaguaka.view;

import com.scp.guaguaka.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class GuaGuaKa extends View {
	private Canvas mCanvas;// 画布
	private Bitmap mBitmap;// 图片
	private Bitmap mOutterBitmap;// 输出背景图
	private Paint mOutterPaint;// 输出画笔
	private Path mPath;// 用户绘制的路径
	private int mLastX;// 记录用户上一次触摸的X坐标
	private int mLastY;// 记录用户上一次触摸的Y坐标
	// 原本View需要的变量
	private Bitmap bitmap;// 背景图
	private Paint mBackPaint;// 绘制文本的画笔
	private Rect mTextBound;// 绘制一个矩形，记录刮奖信息文本的宽和高
	private String mText;// 文本
	private int mTextSize;// 文字字体大小
	private int mTextColor;// 文本颜色
	// 如果用户擦除区域大于60%那么我们就不绘制路径以及涂层
	// 解决该属性引起的并发问题
	// 使用volatile关键字进行修饰，从而保证它在被子线程修改后主线程还能对它的一个可见性
	private volatile boolean isComplete;

	public GuaGuaKa(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
		TypedArray typedArray = null;
		try {
			// 通过TypedArray来获取我们的自定义属性
			typedArray = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.GuaGuaKa, defStyleAttr, 0);
			int count = typedArray.getIndexCount();
			for (int i = 0; i < count; i++) {
				int attr = typedArray.getIndex(i);
				switch (attr) {
				case R.styleable.GuaGuaKa_text:
					mText = typedArray.getString(attr);
					break;
				case R.styleable.GuaGuaKa_textColor:
					mTextColor = typedArray.getColor(attr,
							Color.parseColor("#000000"));
					break;
				case R.styleable.GuaGuaKa_textSize:
					mTextSize = (int) typedArray.getDimension(attr, TypedValue
							.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22,
									getResources().getDisplayMetrics()));
					break;
				}
			}
		} finally {
			if (typedArray != null) {
				typedArray.recycle();
			}
		}

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
		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.hyomin2);
		mOutterBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.guaguaka);
		mText = "谢谢惠顾";
		mTextBound = new Rect();
		mBackPaint = new Paint();
		// 转换为22sp
		mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				22, getResources().getDisplayMetrics());
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
		// 设置绘制获奖信息的画笔
		setBackPaint();
		// mCanvas.drawColor(Color.parseColor("#c0c0c0"));
		mCanvas.drawRoundRect(new RectF(0, 0, height, width), 30, 30,
				mOutterPaint);
		mCanvas.drawBitmap(mOutterBitmap, null, new Rect(0, 0, width, height),
				null);
	}

	/**
	 * 设置绘制path画笔的一些属性
	 */
	private void setOutPaint() {
		// mOutterPaint.setColor(Color.RED);// 画笔颜色
		mOutterPaint.setColor(Color.parseColor("#c0c0c0"));
		mOutterPaint.setAntiAlias(true);// 抗锯齿
		mOutterPaint.setDither(true);// 防抖动
		mOutterPaint.setStrokeJoin(Paint.Join.ROUND);// 设置连接方式为圆角
		mOutterPaint.setStrokeCap(Paint.Cap.ROUND);// 设置画笔笔刷类型
		// mOutterPaint.setStyle(Style.STROKE);// 设置画笔的填充方式为描边
		mOutterPaint.setStyle(Style.FILL);
		mOutterPaint.setStrokeWidth(20);// 设置画笔的一个宽度
	}

	/**
	 * 绘制获奖信息的画笔
	 */
	private void setBackPaint() {
		mBackPaint.setColor(mTextColor);// 画笔颜色
		mBackPaint.setAntiAlias(true);// 抗锯齿
		mBackPaint.setDither(true);// 防抖动
		mBackPaint.setStyle(Style.FILL);// 设置画笔的填充方式为实心
		mBackPaint.setTextSize(mTextSize);// 设置文本字体大小
		// 获得当前画笔绘制文本的宽和高
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
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
			new Thread(mRunnable).start();
			break;

		}
		invalidate();
		return true;
	}

	/**
	 * 异步计算用户擦除的面积
	 */
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			int w = getWidth();
			int h = getHeight();
			// 擦除区域的大小
			float wipeArea = 0;
			// 控件区域总共的像素值
			float totalArea = w * h;
			Bitmap bitmap = mBitmap;// 涂层区域绘制在我们的bitmap上
			// 获取bitmap的所有像素信息
			int[] mPixels = new int[w * h];
			bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);

			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					int index = i + j * w;
					if (mPixels[index] == 0) {
						wipeArea++;
					}
				}
			}

			if (wipeArea > 0 && totalArea > 0) {
				int percent = (int) (wipeArea * 100 / totalArea);
				System.out.println(percent);
				// 如果擦除面积大于60%
				if (percent > 60) {
					// 清楚掉涂层区域
					isComplete = true;
					// 因为在子线程中，不能使用invalidate(),必须使用postInvalidate()
					postInvalidate();
				}
			}

		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		// canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2,
				getHeight() / 2 + mTextBound.height() / 2, mBackPaint);
		if (isComplete) {
			if (mListener != null) {
				mListener.complete();
			}
		}
		if (!isComplete) {
			// 我们没有用系统的Canvas绘图
			drawPath();
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
	}

	private void drawPath() {
		mOutterPaint.setStyle(Style.STROKE);
		// 我们在绘制路径之前设置我们的mOutterPaint
		mOutterPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
		mCanvas.drawPath(mPath, mOutterPaint);
	}

	public void setText(String mText) {
		this.mText = mText;
		// 获得当前画笔绘制文本的宽和高
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
	}

	/**
	 * 刮刮卡刮完的一个回调
	 * 
	 * @author 宋春鹏
	 *
	 */
	public interface OnGuaGuaKaCompleteListener {
		void complete();
	}

	private OnGuaGuaKaCompleteListener mListener;

	public void setOnGuaGuaKaCompleteListener(
			OnGuaGuaKaCompleteListener mListener) {
		this.mListener = mListener;
	}

}
