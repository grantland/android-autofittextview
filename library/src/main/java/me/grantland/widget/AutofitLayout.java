package me.grantland.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.WeakHashMap;

/**
 * A {@link ViewGroup} that re-sizes the text of it's children to be no larger than the width of the
 * view.
 *
 * @attr ref R.styleable.AutofitTextView_autofitWidthEnabled
 * @attr ref R.styleable.AutofitTextView_autofitHeightEnabled
 * @attr ref R.styleable.AutofitTextView_minTextSize
 * @attr ref R.styleable.AutofitTextView_precision
 */
public class AutofitLayout extends FrameLayout {

    private boolean mIsAutofitWidthEnabled;
    private boolean mIsAutofitHeightEnabled;
    private float mMinTextSize;
    private float mPrecision;
    private WeakHashMap<View, AutofitHelper> mHelpers = new WeakHashMap<View, AutofitHelper>();

    public AutofitLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AutofitLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AutofitLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        boolean autofitWidthEnabled = true;
        boolean autofitHeightEnabled = false;
        int minTextSize = -1;
        float precision = -1;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.AutofitTextView,
                    defStyle,
                    0);
            autofitWidthEnabled = ta.getBoolean(R.styleable.AutofitTextView_autofitWidthEnabled,
                    autofitWidthEnabled);
            autofitHeightEnabled = ta.getBoolean(R.styleable.AutofitTextView_autofitHeightEnabled,
                    autofitHeightEnabled);
            minTextSize = ta.getDimensionPixelSize(R.styleable.AutofitTextView_minTextSize,
                    minTextSize);
            precision = ta.getFloat(R.styleable.AutofitTextView_precision, precision);
            ta.recycle();
        }

        mIsAutofitWidthEnabled = autofitWidthEnabled;
        mIsAutofitHeightEnabled = autofitHeightEnabled;
        mMinTextSize = minTextSize;
        mPrecision = precision;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        TextView textView = (TextView) child;
        AutofitHelper helper = AutofitHelper.create(textView);
        helper.setAutofitWidthEnabled(mIsAutofitWidthEnabled);
        helper.setAutofitHeightEnabled(mIsAutofitHeightEnabled);
        if (mPrecision > 0) {
            helper.setPrecision(mPrecision);
        }
        if (mMinTextSize > 0) {
            helper.setMinTextSize(TypedValue.COMPLEX_UNIT_PX, mMinTextSize);
        }
        mHelpers.put(textView, helper);
    }

    /**
     * Returns the {@link AutofitHelper} for this child View.
     */
    public AutofitHelper getAutofitHelper(TextView textView) {
        return mHelpers.get(textView);
    }

    /**
     * Returns the {@link AutofitHelper} for this child View.
     */
    public AutofitHelper getAutofitHelper(int index) {
        return mHelpers.get(getChildAt(index));
    }
}