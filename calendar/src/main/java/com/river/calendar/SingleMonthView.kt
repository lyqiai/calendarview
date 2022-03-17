package com.river.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import java.util.*

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class SingleMonthView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MonthView(context, attrs, defStyleAttr) {
    private var mOnDayClickListener: OnDayClickListener? = null

    private var mDate: Date?= null

    override fun allowDrawDayBackground(day: Int): Boolean {
        if (mDate == null) return false

        val calendar = Calendar.getInstance()
        calendar.time = mDate

        return calendar.get(Calendar.YEAR) == mYear && calendar.get(Calendar.MONTH) == mMonth && calendar.get(Calendar.DAY_OF_MONTH) == day
    }

    override fun drawDayBackground(canvas: Canvas, paint: Paint, day: Int, centerX: Float, centerY: Float) {
        canvas.drawCircle(centerX, centerY, mDayRadius, paint)
    }

    override fun onDayClickHandle(day: Int) {
        val date = Calendar.getInstance()
        date[mYear, mMonth] = day
        date.set(Calendar.HOUR_OF_DAY, 0)
        date.set(Calendar.MINUTE, 0)
        date.set(Calendar.SECOND, 0)
        date.set(Calendar.MILLISECOND, 0)

        mDate = date.time

        mOnDayClickListener?.let {
            mOnDayClickListener?.onClick(this, date.time)
        }
    }

    fun setOnDayClickListener(listener: SingleMonthView.OnDayClickListener) {
        mOnDayClickListener = listener
    }

    fun setSelectedDay(date: Date?) {
        mDate = date
        invalidate()
    }

    fun interface OnDayClickListener {
        fun onClick(view: MonthView, date: Date)
    }
}