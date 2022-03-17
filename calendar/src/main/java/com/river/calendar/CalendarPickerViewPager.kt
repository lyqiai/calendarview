package com.river.calendar

import android.content.Context
import android.view.View
import androidx.viewpager.widget.ViewPager

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class CalendarPickerViewPager(context: Context) : ViewPager(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val view = findViewById<View>(currentItem)

        if (view != null) {
            view.measure(widthMeasureSpec, heightMeasureSpec)
        }
        setMeasuredDimension(measuredWidth, measureHeight(heightMeasureSpec, view))
    }

    private fun measureHeight(measureSpec: Int, view: View?): Int {
        var result = 0
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)

        if (mode == MeasureSpec.EXACTLY) {
            result = size
        } else {
            if (view != null) {
                result = view.measuredHeight
            }
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(size, result)
            }
        }

        return result
    }
}