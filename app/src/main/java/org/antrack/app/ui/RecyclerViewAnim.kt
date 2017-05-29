package org.antrack.app.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class RecyclerViewAnim @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {
    var mScrollable: Boolean = false
    var mFirstUpdate: Boolean = true

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return !mScrollable || super.dispatchTouchEvent(ev)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        // Workaround for not redraw animation on update
        if (mScrollable) {
            return
        }

        val childCount = childCount

        // If there is no children it is not update
        if (childCount > 0) {
            mFirstUpdate = false
        }

        for (i in 0..childCount - 1) {
            animate(getChildAt(i), i)

            if (i == childCount - 1) {
                handler.postDelayed({ mScrollable = true }, (i * 100 + 200).toLong())
            }
        }
    }

    private fun animate(view: View, pos: Int) {
        view.animate().cancel()
        view.translationY = 100f
        view.alpha = 0f
        view.animate().alpha(1.0f).translationY(0f).setDuration(300).startDelay = (pos * 100).toLong()
    }
}

