package me.grantland.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import me.grantland.autofittextview.R;

/**
 * A TextView that resizes it's text to be no larger than the width of the view.
 *
 * @author Grantland Chew <grantlandchew@gmail.com>
 */
public class AutofitTextView extends TextView {

    private static final String TAG = "AutoFitTextView";
    private static final boolean SPEW = false;

    // Minimum size of the text in pixels
    private static final int DEFAULT_MIN_TEXT_SIZE = 8; //sp
    // How precise we want to be when reaching the target textWidth size
    private static final float PRECISION = 0.5f;

    // Attributes
    private boolean mSizeToFit;
    private int mMaxLines;
    private float mMinTextSize;
    private float mMaxTextSize;
    private float mPrecision;
    private TextPaint mPaint;

    public AutofitTextView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AutofitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AutofitTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        boolean sizeToFit = true;
        int minTextSize = (int) scaledDensity * DEFAULT_MIN_TEXT_SIZE;
        float precision = PRECISION;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.AutofitTextView,
                    defStyle,
                    0);
            sizeToFit = ta.getBoolean(R.styleable.AutofitTextView_sizeToFit, sizeToFit);
            minTextSize = ta.getDimensionPixelSize(R.styleable.AutofitTextView_minTextSize,
                                                   minTextSize);
            precision = ta.getFloat(R.styleable.AutofitTextView_precision, precision);
            ta.recycle();
        }

        mPaint = new TextPaint();
        setSizeToFit(sizeToFit);
        setRawTextSize(super.getTextSize());
        setRawMinTextSize(minTextSize);
        setPrecision(precision);
    }

    // Getters and Setters

    /**
     * @return whether or not the text will be automatically resized to fit its constraints.
     */
    public boolean isSizeToFit() {
        return mSizeToFit;
    }

    /**
     * Sets the property of this field (singleLine, to automatically resize the text to fit its
     * constraints.
     */
    public void setSizeToFit() {
        setSizeToFit(true);
    }

    /**
     * If true, the text will automatically be resized to fit its constraints; if false, it will act
     * like a normal TextView.
     *
     * @param sizeToFit
     */
    public void setSizeToFit(boolean sizeToFit) {
        mSizeToFit = sizeToFit;
        refitText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getTextSize() {
        return mMaxTextSize;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * @return the minimum size (in pixels) of the text size in this AutofitTextView
     */
    public float getMinTextSize() {
        return mMinTextSize;
    }

    /**
     * Set the minimum text size to a given unit and value. See TypedValue for the possible
     * dimension units.
     *
     * @param unit    The desired dimension unit.
     * @param minSize The desired size in the given units.
     * @attr ref me.grantland.R.styleable#AutofitTextView_minTextSize
     */
    public void setMinTextSize(int unit, float minSize) {
        Context context = getContext();
        Resources r = Resources.getSystem();

        if (context != null) {
            r = context.getResources();
        }

        setRawMinTextSize(TypedValue.applyDimension(unit, minSize, r.getDisplayMetrics()));
    }

    /**
     * Set the minimum text size to the given value, interpreted as "scaled pixel" units. This size
     * is adjusted based on the current density and user font size preference.
     *
     * @param minSize The scaled pixel size.
     * @attr ref me.grantland.R.styleable#AutofitTextView_minTextSize
     */
    public void setMinTextSize(int minSize) {
        setMinTextSize(TypedValue.COMPLEX_UNIT_SP, minSize);
    }

    private void setRawMinTextSize(float minSize) {
        if (minSize != mMinTextSize) {
            mMinTextSize = minSize;
            refitText();
        }
    }

    /**
     * @return the amount of precision used to calculate the correct text size to fit within it's
     * bounds.
     */
    public float getPrecision() {
        return mPrecision;
    }

    /**
     * Set the amount of precision used to calculate the correct text size to fit within it's
     * bounds. Lower precision is more precise and takes more time.
     *
     * @param precision The amount of precision.
     */
    public void setPrecision(float precision) {
        if (precision != mPrecision) {
            mPrecision = precision;
            refitText();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLines(int lines) {
        super.setLines(lines);
        mMaxLines = lines;
        refitText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxLines() {
        return mMaxLines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        if (maxLines != mMaxLines) {
            mMaxLines = maxLines;
            refitText();
        }
    }

    /**
     * Re size the font so the specified text fits in the text box assuming the text box is the
     * specified width.
     */
    private void refitText() {
        if (!mSizeToFit) {
            return;
        }

        if (mMaxTextSize == 0) {
            // Don't resize until a max font size is available.
            return;
        }

        int targetWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int targetHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        if (targetWidth > 0 || targetHeight > 0) {
            Context context = getContext();
            Resources r = Resources.getSystem();
            DisplayMetrics displayMetrics;

            float size = mMaxTextSize;
            float high = size;
            float low = 0;

            if (context != null) {
                r = context.getResources();
            }
            displayMetrics = r.getDisplayMetrics();

            mPaint.set(getPaint());
            mPaint.setTextSize(size);

            size = getTextSize(low, high, targetWidth, targetHeight, displayMetrics);

            if (size < mMinTextSize) {
                size = mMinTextSize;
            }

            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    /**
     * Recursive binary search to find the best size for the text
     */
    private float getTextSize(float low, float high, int targetWidth, int targetHeight,
                              DisplayMetrics displayMetrics) {
        float mid = (low + high) / 2.0f;
        int lineCount = -1;
        int height = -1;
        int width = -1;
        StaticLayout layout;

        layout = getLayout(mid, targetWidth, displayMetrics);
        lineCount = layout.getLineCount();
        width = layout.getWidth();
        height = layout.getHeight();

        if (SPEW) Log.d(TAG, "low=" + low + " high=" + high + " mid=" + mid + " targetWidth=" +
                             targetWidth + " targetHeight=" + targetHeight + " maxLines=" +
                             mMaxLines + " lineCount=" + lineCount);

        if (mMaxLines > 0) {
            // We have a maxLines constraint
            if (lineCount > mMaxLines) {
                // Too many lines; try smaller font
                return getTextSize(low, mid, targetWidth, targetHeight, displayMetrics);
            } else if (lineCount < mMaxLines) {
                // More lines available; try larger font
                return getTextSize(mid, high, targetWidth, targetHeight, displayMetrics);
            }
        }


        // Max lines constraint is met; now satisfy width and constraints, if any
        if (targetWidth > 0 && targetHeight > 0) {
            if (width > targetWidth || height > targetHeight) {
                // Too wide or too tall; try smaller font
                if ((high - low) < mPrecision)
                    return low;
                else
                    return getTextSize(low, mid, targetWidth, targetHeight, displayMetrics);
            } else {
                // More width and height available; try larger font
                if ((high - low) < mPrecision)
                    return low;
                else
                    return getTextSize(mid, high, targetWidth, targetHeight, displayMetrics);
            }
        } else {
            return mid;
        }
    }

    private StaticLayout getLayout(float size, int width, DisplayMetrics displayMetrics) {
        mPaint.setTextSize(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, size, displayMetrics));
        return new StaticLayout(getText(), mPaint, width, Layout.Alignment.ALIGN_CENTER,
                                1.0f, 0.0f, true);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start,
                                 final int lengthBefore, final int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        refitText();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            refitText();
        }
    }
}
