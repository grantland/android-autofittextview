package me.grantland.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import me.grantland.autofittextview.R;

/**
 * A TextView that resizes it's text to be no larger than the width of the view.
 *
 * @author Grantland Chew <grantlandchew@gmail.com>
 * @author David R. Bild <drbild@willbild.com>
 */
public class AutofitTextView extends TextView {

    private static final String TAG = "AutoFitTextView";

    // Minimum size of the text in pixels
    private static final int DEFAULT_MIN_TEXT_SIZE = 8; //sp
    // How precise we want to be when reaching the target textWidth size
    private static final float PRECISION = 0.5f;

    // Attributes
    private boolean mSizeToFit;
    private int mMaxLines;
    private float mTextSize;
    private float mMinTextSize;
    private float mPrecision;
    private TextUtils.TruncateAt mEllipsize;

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
        if (mSizeToFit) {
            super.setEllipsize(null);
        } else {
            super.setEllipsize(mEllipsize);
        }
        requestLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTextAppearance(Context context, int resid) {
        super.setTextAppearance(context, resid);
        setRawTextSize(super.getTextSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getTextSize() {
        return mTextSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
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
        super.setTextSize(unit, size);
    }

    private void setRawTextSize(float size) {
        mTextSize = size;
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
            requestLayout();
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
            requestLayout();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLines(int lines) {
        super.setLines(lines);
        mMaxLines = lines;
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
        mMaxLines = maxLines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEllipsize(TextUtils.TruncateAt where) {
        super.setEllipsize(where);
        mEllipsize = where;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (mSizeToFit) {
            // Superclass TextView will not requestLayout() when both height and width are fixed,
            // but we need need layout to compute the new font size.
            requestLayout();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mSizeToFit) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int maxLines = getMaxLines();

        // Cannot adjust font size to fit if the desired view size is unbounded
        if (widthMode == MeasureSpec.UNSPECIFIED ||
            (heightMode == MeasureSpec.UNSPECIFIED &&
             (maxLines <= 0 || maxLines == Integer.MAX_VALUE))) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // Find the maximum font size that allows the text to fit in the desired bounds.
        // We recursively ask the superclass TextView to measure itself with the given width
        // constraint but an unbounded height (and lines), decreasing the font size until the
        // resulting height (and/or maxLines) meets the given constraint.
        final int targetHeight = MeasureSpec.getSize(heightMeasureSpec);
        final int parentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        // Save state
        if (maxLines > 0) {
            super.setMaxHeight(Integer.MAX_VALUE);
        }
        super.setEllipsize(null);
        // Find correct font size
        final float size = findTextSize(1.0f, mTextSize, targetHeight, maxLines, widthMeasureSpec,
                                        parentHeightMeasureSpec);

        // Restore state
        super.setEllipsize(mEllipsize);
        if (maxLines > 0) {
            super.setMaxLines(maxLines);
        }

        // Measure with correct font size
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Recursive binary search to find the best size for the text.
     */
    private float findTextSize(float low, float high, int targetHeight, int targetLines,
                               int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        if ((high - low) < mPrecision) {
            return low;
        }

        float mid = (low + high) / 2.0f;

        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mid);
        super.onMeasure(parentWidthMeasureSpec, parentHeightMeasureSpec);

        final int height = getMeasuredHeight();
        final int lineCount = getLineCount();

        if (tooTall(targetHeight, height, targetLines, lineCount)) {
            return findTextSize(low, mid, targetHeight, targetLines, parentWidthMeasureSpec,
                                parentHeightMeasureSpec);
        } else if (tooShort(targetHeight, height, targetLines, lineCount)) {
            return findTextSize(mid, high, targetHeight, targetLines, parentWidthMeasureSpec,
                                parentHeightMeasureSpec);
        } else {
            return mid;
        }
    }

    private static boolean tooTall(int targetHeight, int measuredHeight, int targetLines,
                                   int lineCount) {
        return (targetHeight > 0 && measuredHeight > targetHeight) ||
               (targetLines > 0 && lineCount > targetLines);
    }

    private static boolean tooShort(int targetHeight, int measuredHeight, int targetLines,
                                    int lineCount) {
        return Implication(targetHeight > 0, measuredHeight < targetHeight) && Implication(
                targetLines > 0, lineCount <= targetLines);
    }

    /**
     * material implication (p -> q)
     */
    private static boolean Implication(boolean p, boolean q) {
        return !p || q;
    }
}
