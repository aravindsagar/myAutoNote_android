package paradigm.shift.myautonote.util;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by aravind on 12/6/17.
 */

public class TypeWriter extends android.support.v7.widget.AppCompatTextView {

    public static interface OnAnimationEndListener {
        void onAnimationEnd(View v);
    }

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 150; // in ms
    private OnAnimationEndListener onAnimationEndListener;

    public TypeWriter(Context context) {
        super(context);
    }

    public TypeWriter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TypeWriter setOnAnimationEndListener(OnAnimationEndListener onAnimationEndListener) {
        this.onAnimationEndListener = onAnimationEndListener;
        return this;
    }

    private Handler mHandler = new Handler();

    private Runnable characterAdder = new Runnable() {

        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));

            if (mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            } else if (onAnimationEndListener != null) {
                onAnimationEndListener.onAnimationEnd(TypeWriter.this);
            }
        }
    };

    public void animateText(CharSequence txt) {
        mText = txt;
        mIndex = 0;

        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public TypeWriter setCharacterDelay(long m) {
        mDelay = m;
        return this;
    }
}
