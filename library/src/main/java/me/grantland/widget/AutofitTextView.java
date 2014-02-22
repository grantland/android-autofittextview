package me.grantland.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.text.TextUtils;
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
    private static final int DEFAULT_MIN_TEXT_SIZE = 8; //sp
    // How precise we want to be when reaching the target textWidth size
    private static final float PRECISION = 0.5f;

    // Attributes
    private float mMinTextSize;
    private float mMaxTextSize;
    private float mPrecision;
    private Paint mPaint;

    public AutofitTextView(Context context) {
        super(context);
        init(context);
    }

    public AutofitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        mMinTextSize = scaledDensity * DEFAULT_MIN_TEXT_SIZE;
        mPrecision = PRECISION;
        mPaint = new Paint();
        setRawTextSize(super.getTextSize());
        //TODO allow multiple lines and ellipsize settings
        setMaxLines(1);
        setEllipsize(TextUtils.TruncateAt.END);
    }

    // Getters and Setters

    @Override
    public float getTextSize() {
        return mMaxTextSize;
    }

    @Override
    public void setTextSize(int unit, float size) {
        Context context = getContext();
        Resources r = Resources.getSystem();

        if (context != null) {
            r = context.getResources();
        }

        setRawTextSize(TypedValue.applyDimension(unit, size, r.getDisplayMetrics()));
    }

    private void setRawTextSize(float size) {
        if (size != mMaxTextSize) {
            mMaxTextSize = size;
            refitText();
        }
    }

    public float getMinTextSize() {
        return mMinTextSize;
    }

    public void setMinTextSize(int minTextSize) {
        if (minTextSize != mMinTextSize) {
            mMinTextSize = minTextSize;
            refitText();
        }
    }

    public float getPrecision() {
        return mPrecision;
    }

    public void setPrecision(float precision) {
        if (precision != mPrecision) {
            mPrecision = precision;
            refitText();
        }
    }

    /**
     * Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    private void refitText() {
        String text = getText().toString();
        int targetWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        if (targetWidth > 0) {
            Context context = getContext();
            Resources r = Resources.getSystem();

            float size = mMaxTextSize;
            float high = size;
            float low = 0;

            if (context != null) {
                r = context.getResources();
            }

            mPaint.set(getPaint());
            mPaint.setTextSize(size);

            if (mPaint.measureText(text) > targetWidth) {
                size = getTextSize(r, text, targetWidth, low, high, mPrecision, mPaint);

                if (size < mMinTextSize) {
                    size = mMinTextSize;
                }
            }

            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    // Recursive binary search to find the best size for the text
    private static float getTextSize(Resources resources, String text, float targetWidth,
                                     float low, float high, float precision, Paint paint) {
        float mid = (low + high) / 2.0f;

        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mid, resources.getDisplayMetrics()));
        float textWidth = paint.measureText(text);

        if (SPEW) Log.d(TAG, "low=" + low + " high=" + high + " mid=" + mid + " target=" + targetWidth + " width=" + textWidth);

        if ((high - low) < precision) {
            return low;
        }
        else if (textWidth > targetWidth) {
            return getTextSize(resources, text, targetWidth, low, mid, precision, paint);
        }
        else if (textWidth < targetWidth) {
            return getTextSize(resources, text, targetWidth, mid, high, precision, paint);
        }
        else {
            return mid;
        }
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start,
                                 final int lengthBefore, final int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        refitText();
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            refitText();
        }
    }
}
