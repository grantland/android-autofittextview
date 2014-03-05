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
     * Sets the property of this field (singleLine, to automatically resize the text to fit its constraints.
     */
    public void setSizeToFit() {
        setSizeToFit(true);
    }

    /**
     * If true, the text will automatically be resized to fit its constraints; if false, it will
     * act like a normal TextView.
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
     * @param unit The desired dimension unit.
     * @param minSize The desired size in the given units.
     *
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
     *
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

        if (mMaxLines <= 0) {
            // Don't auto-size since there's no limit on lines.
            return;
        }

        String text = getText().toString();
        int targetWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        if (targetWidth > 0) {
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

            if ((mMaxLines == 1 && mPaint.measureText(text) > targetWidth)
                    || getLineCount(text, mPaint, size, targetWidth, displayMetrics) > mMaxLines) {
                size = getTextSize(text, mPaint, targetWidth, mMaxLines, low, high, mPrecision,
                        displayMetrics);
            }

            if (size < mMinTextSize) {
                size = mMinTextSize;
            }

            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    /**
     * Recursive binary search to find the best size for the text
     */
    private static float getTextSize(String text, TextPaint paint,
                                     float targetWidth, int maxLines,
                                     float low, float high, float precision,
                                     DisplayMetrics displayMetrics) {
        float mid = (low + high) / 2.0f;
        int lineCount = 1;
        StaticLayout layout = null;

        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mid,
                displayMetrics));

        if (maxLines != 1) {
            layout = new StaticLayout(text, paint, (int)targetWidth, Layout.Alignment.ALIGN_NORMAL,
                    1.0f, 0.0f, true);
            lineCount = layout.getLineCount();
        }

        if (SPEW) Log.d(TAG, "low=" + low + " high=" + high + " mid=" + mid +
                " target=" + targetWidth + " maxLines=" + maxLines + " lineCount=" + lineCount);

        if (lineCount > maxLines) {
            return getTextSize(text, paint, targetWidth, maxLines, low, mid, precision,
                    displayMetrics);
        }
        else if (lineCount < maxLines) {
            return getTextSize(text, paint, targetWidth, maxLines, mid, high, precision,
                    displayMetrics);
        }
        else {
            float maxLineWidth = 0;
            if (maxLines == 1) {
                maxLineWidth = paint.measureText(text);
            } else {
                for (int i = 0; i < lineCount; i++) {
                    if (layout.getLineWidth(i) > maxLineWidth) {
                        maxLineWidth = layout.getLineWidth(i);
                    }
                }
            }

            if ((high - low) < precision) {
                return low;
            } else if (maxLineWidth > targetWidth) {
                return getTextSize(text, paint, targetWidth, maxLines, low, mid, precision,
                        displayMetrics);
            } else if (maxLineWidth < targetWidth) {
                return getTextSize(text, paint, targetWidth, maxLines, mid, high, precision,
                        displayMetrics);
            } else {
                return mid;
            }
        }
    }

    private static int getLineCount(String text, TextPaint paint, float size, float width,
                                    DisplayMetrics displayMetrics) {
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, size,
                displayMetrics));
        StaticLayout layout = new StaticLayout(text, paint, (int)width,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        return layout.getLineCount();
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
        if (w != oldw) {
            refitText();
        }
    }
}
