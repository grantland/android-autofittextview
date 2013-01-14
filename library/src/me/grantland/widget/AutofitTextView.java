package me.grantland.widget;

import me.grantland.autofittextview.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * A TextView that resizes it's text to be no larger than the width of the view.
 * 
 * @author Grantland Chew <grantlandchew@gmail.com>
 */
public class AutofitTextView extends TextView {

	private static final String TAG = "me.grantland.widget.AutoFitTextView";
	private static final boolean SPEW = false;

	// Minimum size of the text in pixels
	private static final int DEFAULT_MIN_TEXT_SIZE = 8; // px
	// How precise we want to be when reaching the target textWidth size
	private static final float PRECISION = 0.5f;

	// Attributes
	private float mMinTextSize;
	private float mMaxTextSize;
	private float mPrecision;
	private Paint mPaint;
	/** Flag to invalidate the the text size. */
	private boolean mLayoutDirty;
	/** The width which was used to refit the text. */
	private int mLastRefitWidth;

	public AutofitTextView(Context context) {
		super(context);
		init();
	}
	
    public AutofitTextView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	
        init();
		initAttrs(context, attrs);
    }

	public AutofitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
		initAttrs(context, attrs);
    }

    /**
     * Initializes the attributes from the xml.
     * @param context		the context
     * @param attrs			the xml layout attributes
     */
	private void initAttrs(Context context, AttributeSet attrs) {
		if (attrs != null) {
			final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutofitTextView, 0, 0);
			final int minSize = ta.getDimensionPixelSize(R.styleable.AutofitTextView_minTextSize, -1);
			final int maxSize = ta.getDimensionPixelSize(R.styleable.AutofitTextView_maxTextSize, -1);
			final float precision = ta.getFloat(R.styleable.AutofitTextView_precision, -1.0f);
			if (minSize != -1) {
				setMinTextSize(minSize);
			}
			if (maxSize != -1) {
				setMaxTextSize(maxSize);
			}
			if (precision != -1.0f) {
				setPrecision(precision);
			}
			ta.recycle();
		}
	}

	private void init() {
		mMinTextSize = DEFAULT_MIN_TEXT_SIZE;
		mMaxTextSize = getTextSize();
		mPrecision = PRECISION;
		mPaint = new Paint();
		mLayoutDirty = true;
		
		mPaint.setColor(0x00ffff);
		// these settings are pretty much a given, the code assumes that they are set. So lets make sure.
		setLines(1);
		setMaxLines(1);
		setSingleLine();
		setEllipsize(TruncateAt.END);
	}

	// Getters and Setters

	public float getMinTextSize() {
		return mMinTextSize;
	}

	public void setMinTextSize(int minTextSize) {
		mMinTextSize = minTextSize;
	}

	public float getMaxTextSize() {
		return mMaxTextSize;
	}

	public void setMaxTextSize(int maxTextSize) {
		mMaxTextSize = maxTextSize;
	}

	public float getPrecision() {
		return mPrecision;
	}

	public void setPrecision(float precision) {
		mPrecision = precision;
	}

	/**
	 * Re size the font so the specified text fits in the text box assuming the
	 * text box is the specified width.
	 */
	private void refitText(String text, int width) {
		if (width > 0) {
			Context context = getContext();
			Resources r = Resources.getSystem();

			int targetWidth = width - getPaddingLeft() - getPaddingRight();
			float newTextSize = mMaxTextSize;
			float high = mMaxTextSize;
			float low = 0;

			if (context != null) {
				r = context.getResources();
			}

			mPaint.set(getPaint());
			mPaint.setTextSize(newTextSize);

			if (mPaint.measureText(text) > targetWidth) {
				newTextSize = getTextSize(r, text, targetWidth, low, high);

				if (newTextSize < mMinTextSize) {
					newTextSize = mMinTextSize;
				}
			}

			setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
		}
	}

	// Recursive binary search to find the best size for the text
	private float getTextSize(Resources resources, String text,
			float targetWidth, float low, float high) {
		float mid = (low + high) / 2.0f;

		mPaint.setTextSize(mid);//TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mid, resources.getDisplayMetrics()));
		float textWidth = mPaint.measureText(text);

		if (SPEW)
			Log.d(TAG, "low=" + low + " high=" + high + " mid=" + mid
					+ " target=" + targetWidth + " width=" + textWidth);

		if ((high - low) < mPrecision) {
			return low;
		} else if (textWidth > targetWidth) {
			return getTextSize(resources, text, targetWidth, low, mid);
		} else if (textWidth < targetWidth) {
			return getTextSize(resources, text, targetWidth, mid, high);
		} else {
			return mid;
		}
	}

	@Override
	protected void onTextChanged(final CharSequence text, final int start,
			final int lengthBefore, final int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		setTextSize(TypedValue.COMPLEX_UNIT_PX, getMaxTextSize());
		mLayoutDirty = true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w != oldw) {
			mLayoutDirty = true;
		}
	}

//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		final int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
//		refitTextIfNecessary(parentWidth);
//	}

	/**
	 * Resizes the text size so that it can fit into the given area. The actual refit operation will only be 
	 * executed when it is truly necessary (layout is dirty due to text or size changes).
	 * @param parentWidth	the available width of the parent in pixels
	 */
	private void refitTextIfNecessary(int parentWidth) {
		if (mLayoutDirty || parentWidth != mLastRefitWidth) {
			mLastRefitWidth = parentWidth;
			mLayoutDirty = false;
			refitText(getText().toString(), parentWidth);
//			if (BuildConfig.DEBUG) {
//				Log.d(TAG, "refitting text: " + getText() + " size: " + getTextSize() + " width: " + parentWidth);
//			}
//		} else if (BuildConfig.DEBUG) {
//			Log.d(TAG, "not refitting text " + getText() + " size: " + getTextSize());
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		refitTextIfNecessary(right - left);
		super.onLayout(changed, left, top, right, bottom);
	}
}
