package com.river.calendar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import java.util.*

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class RangeMonthView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MonthView(context, attrs, defStyleAttr) {
    private var mStartDate: Date? = null
    private var mEndDate: Date? = null
    private var mListener: OnDayRangeChangedListener? = null

    override fun allowDrawDayBackground(day: Int): Boolean {
        if (mStartDate == null && mEndDate == null) return false

        if (mEndDate == null) {
            return isStartDay(day)
        }

        return isInRangDay(day) || isStartDay(day) || isEndDay(day)
    }

    override fun onDayClickHandle(day: Int) {
        val calendar = getCalendarByDay(day)
        val date = calendar.time

        if (mStartDate == null) {
            mStartDate = date
        } else if (Calendar.getInstance().apply { time = mStartDate }
                .after(calendar) || (mStartDate != null && mEndDate != null)) {
            mStartDate = date
            mEndDate = null
        } else if (mStartDate != null && mEndDate == null && !isSameDay(
                Calendar.getInstance().apply { time = mStartDate }, day
            )
        ) {
            mEndDate = date
        }

        mListener?.onDayRangeChanged(this, mStartDate, mEndDate)
    }

    override fun drawDayBackground(
        canvas: Canvas,
        paint: Paint,
        day: Int,
        centerX: Float,
        centerY: Float
    ) {
        if (isStartDay(day)) {
            if (mEndDate == null) {
                canvas.drawCircle(centerX, centerY, mDayRadius, paint)
            } else {
                val path = Path().apply {
                    var x = centerX
                    var y = centerY - mDayRadius
                    this.moveTo(x, y)
                    x += (mCellWidth + 0.5f) / 2
                    this.lineTo(x, y)
                    y += mDayRadius * 2
                    this.lineTo(x, y)
                    x -= (mCellWidth + 0.5f) / 2
                    this.lineTo(x, y)
                    this.addArc(
                        RectF(centerX - mDayRadius,centerY - mDayRadius,centerX + mDayRadius,centerY + mDayRadius),
                        90f,
                        180f
                    )
                    this.close()
                }
                canvas.drawPath(path, paint)
            }
        }

        if (isInRangDay(day)) {
            val rect = Rect(
                (centerX - (mCellWidth + 5) / 2).toInt(),
                (centerY - mDayRadius).toInt(),
                (centerX + (mCellWidth + 5) / 2).toInt(),
                (centerY + mDayRadius).toInt()
            )
            canvas.drawRect(rect, paint)
        }

        if (isEndDay(day)) {
            val path = Path().apply {
                var x = centerX
                var y = centerY - mDayRadius
                this.moveTo(x, y)
                x -= (mCellWidth + 0.5f) / 2
                this.lineTo(x, y)
                y += mDayRadius * 2
                this.lineTo(x, y)
                x += (mCellWidth + 0.5f) / 2
                this.lineTo(x, y)

                this.addArc(
                    RectF(centerX - mDayRadius, centerY - mDayRadius, centerX + mDayRadius, centerY + mDayRadius),
                    90f,
                    -180f
                )
                this.close()
            }
            canvas.drawPath(path, paint)
        }
    }

    /**
     * day是否是选中范围开始那天
     * @param day Int
     * @return Boolean
     */
    private fun isStartDay(day: Int): Boolean {
        if (mStartDate == null) return false
        return isSameDay(Calendar.getInstance().apply { time = mStartDate }, day)
    }

    /**
     * day是否是选中范围结束那天
     * @param day Int
     * @return Boolean
     */
    private fun isEndDay(day: Int): Boolean {
        if (mEndDate == null) return false
        return isSameDay(Calendar.getInstance().apply { time = mEndDate }, day)
    }

    /**
     * day是否在选中的日期范围内
     * @param day Int
     * @return Boolean
     */
    private fun isInRangDay(day: Int): Boolean {
        if (mStartDate == null || mEndDate == null) return false

        val calendar = getCalendarByDay(day)

        return calendar.after(
            Calendar.getInstance().apply { time = mStartDate }) && calendar.before(
            Calendar.getInstance().apply { time = mEndDate })
    }

    /**
     * 判断是否同一天
     * @param calendar Calendar
     * @param day Int
     * @return Boolean
     */
    private fun isSameDay(calendar: Calendar, day: Int): Boolean {
        return calendar.get(Calendar.YEAR) == mYear && calendar.get(Calendar.MONTH) == mMonth && calendar.get(
            Calendar.DAY_OF_MONTH
        ) == day
    }

    /**
     * 根据day获取Calendar
     * @param day Int
     * @return Calendar
     */
    private fun getCalendarByDay(day: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar[mYear, mMonth] = day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    /**
     * 设置监听器
     * @param listener OnDayRangeChangedListener?
     */
    fun setOnDayRangeChangedListener(listener: OnDayRangeChangedListener?) {
        mListener = listener
    }

    /**
     * 设置选中时间范围
     * @param start Date?
     * @param end Date?
     */
    fun setSelectedDate(start: Date?, end: Date?) {
        mStartDate = start
        mEndDate = end
        invalidate()
    }

    interface OnDayRangeChangedListener {
        fun onDayRangeChanged(view: RangeMonthView, start: Date?, end: Date?)
    }
}