package me.grantland.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AutofitTextView extends TextView {
    private static final String TAG = "AutoFitTextView";

    private static final int DEFAULT_MIN_TEXT_SIZE = 4; //dp

    //Attributes
    private float minTextSize;
    private Paint mPaint;

    public AutofitTextView(Context context) {
        super(context);
        init();
    }

    public AutofitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        minTextSize = DEFAULT_MIN_TEXT_SIZE;
        mPaint = new Paint();
    }

    /* Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    //TODO binary search
    private void refitText(String text, int textWidth) {
        if (textWidth > 0) {
            Context context = getContext();
            Resources r = Resources.getSystem();

            int availableWidth = textWidth - getPaddingLeft() - getPaddingRight();
            float trySize = getTextSize();

            if (context != null) {
                r = context.getResources();
            }
            mPaint.set(getPaint());

            while ((trySize > minTextSize) && (mPaint.measureText(text) > availableWidth)) {
                trySize -= 1;

                if (trySize <= minTextSize) {
                    trySize = minTextSize;
                    break;
                }

                mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, trySize, r.getDisplayMetrics()));
            }

            setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
        }
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(getText().toString(), w);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        //int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        refitText(getText().toString(), parentWidth);
    }

    //Getters and Setters
    public float getMinTextSize() {
        return minTextSize;
    }

    public void setMinTextSize(int minTextSize) {
        this.minTextSize = minTextSize;
    }
}
