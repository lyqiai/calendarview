package com.river.calendar.adapter

import android.content.Context
import androidx.core.util.forEach
import com.river.calendar.MonthView
import com.river.calendar.SingleMonthView
import java.util.*

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class SingleCalendarPickerViewAdapter(var minDate: Calendar, var maxDate: Calendar) : CalendarPickerViewAdapter<SingleMonthView>(minDate, maxDate), SingleMonthView.OnDayClickListener {
    private var mSelectedStartDate: Date? = null
    private var mOnDayClickListener: SingleMonthView.OnDayClickListener? = null

    override fun createMonthView(context: Context): SingleMonthView {
        return SingleMonthView(context).apply {
            setOnDayClickListener(this@SingleCalendarPickerViewAdapter)
        }
    }

    /**
     * 日期选择回调
     * @param listener OnDayClickListener?
     */
    fun setOnDayClickListener(listener: SingleMonthView.OnDayClickListener?) {
        mOnDayClickListener = listener
    }

    override fun onClick(view: MonthView, date: Date) {
        mSelectedStartDate = date
        mItems.forEach { key, value ->
            value.calendar.setSelectedDay(mSelectedStartDate)
        }
        mOnDayClickListener?.onClick(view, date)
    }
}