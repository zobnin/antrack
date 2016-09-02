package org.antrack.app.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Leo on 2015/09/03
 */
public class RecyclerViewAnim extends RecyclerView {
    private boolean mScrollable;
    private boolean mFirstRun = true;

    public RecyclerViewAnim(Context context) {
        this(context, null);
    }

    public RecyclerViewAnim(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewAnim(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScrollable = false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return !mScrollable || super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        // Workaround for not redraw animation second time
        if (!mFirstRun) {
            return;
        }

        for (int i = 0; i < getChildCount(); i++) {
            animate(getChildAt(i), i);

            if (i == getChildCount() - 1) {
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScrollable = true;
                        mFirstRun = false;
                    }
                }, i * 100);
            }
        }
    }

    private void animate(View view, final int pos) {
        view.animate().cancel();
        view.setTranslationY(100);
        view.setAlpha(0);
        view.animate().alpha(1.0f).translationY(0).setDuration(300).setStartDelay(pos * 100);
    }
}

