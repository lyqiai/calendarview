package com.river.calendar.adapter

import android.content.Context
import androidx.core.util.forEach
import com.river.calendar.RangeMonthView
import java.util.*

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class RangeCalendarPickerViewAdapter(var minDate: Calendar, var maxDate: Calendar) : CalendarPickerViewAdapter<RangeMonthView>(minDate, maxDate), RangeMonthView.OnDayRangeChangedListener {
    private var mSelectedStartDate: Date? = null
    private var mSelectedEndDate: Date? = null
    private var mOnDayRangeChangedListener: RangeMonthView.OnDayRangeChangedListener? = null

    override fun createMonthView(context: Context): RangeMonthView {
        return RangeMonthView(context).apply {
            setSelectedDate(mSelectedStartDate, mSelectedEndDate)
            setOnDayRangeChangedListener(this@RangeCalendarPickerViewAdapter)
        }
    }

    override fun onDayRangeChanged(view: RangeMonthView, start: Date?, end: Date?) {
        mSelectedStartDate = start
        mSelectedEndDate = end
        mItems.forEach { key, value ->
            value.calendar.setSelectedDate(mSelectedStartDate, mSelectedEndDate)
        }
        mOnDayRangeChangedListener?.onDayRangeChanged(view, start, end)
    }

    /**
     * 日期范围选择回调
     * @param listener OnDayRangeChangedListener?
     */
    fun setOnDayRangeChangedListener(listener: RangeMonthView.OnDayRangeChangedListener?) {
        mOnDayRangeChangedListener = listener
    }

    fun setRange(min: Calendar, max: Calendar) {
        mMinDate = min
        mMaxDate = max
        notifyDataSetChanged()
    }
}