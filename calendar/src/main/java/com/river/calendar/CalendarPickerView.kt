package com.river.calendar

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.river.calendar.adapter.CalendarPickerViewAdapter
import java.util.*

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class CalendarPickerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), IOnPageChangeListener {
    private val mWeekView: WeekView
    private val mViewPager: ViewPager
    private lateinit var mAdapter: CalendarPickerViewAdapter<*>
    private var mOnMonthChangedListener: OnMonthChangedListener? = null

    init {
        orientation = VERTICAL

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DatePickerView, defStyleAttr, R.style.DefaultDatePickerViewStyle)

        mWeekView = WeekView(context)
        mWeekView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            this.bottomMargin = typedArray.getDimensionPixelSize(R.styleable.DatePickerView_weekBottomOffset, context.resources.getDimensionPixelSize(R.dimen.week_bottom_offset))
        }
        addView(mWeekView)

        mViewPager = CalendarPickerViewPager(context)
        mViewPager.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        mViewPager.addOnPageChangeListener(this)
        addView(mViewPager)

        typedArray.recycle()
    }

    /**
     * 设置日历适配器
     * @param adapter DatePickerViewAdapter
     */
    fun setAdapter(adapter: CalendarPickerViewAdapter<*>) {
        mAdapter = adapter
        mViewPager.adapter = adapter
        mViewPager.currentItem = adapter.defaultPosition()
    }

    /**
     * 跳转下一个月
     */
    fun nextMonth() {
        mViewPager.currentItem += 1
    }

    /**
     * 跳转上一个月
     */
    fun prevMonth() {
        mViewPager.currentItem -= 1
    }

    /**
     * 是否可以跳转下一个月
     * @return Boolean
     */
    fun canGoNextMonth(): Boolean {
        val total = mAdapter.count
        val current = mViewPager.currentItem

        return current < total - 1
    }

    /**
     * 是否可以跳转上一个月
     * @return Boolean
     */
    fun canGoPrevMonth(): Boolean {
        val current = mViewPager.currentItem
        return current > 0
    }


    fun getMonthView(): MonthView? {
        return mViewPager.findViewById(mViewPager.currentItem)
    }

    fun getWeekView(): WeekView {
        return mWeekView
    }

    /**
     * 设置周几为第一天
     * @param weekStart Int
     */
    fun setFirstDayOfWeek(weekStart: Int) {
        mAdapter.setFirstDayOfWeek(weekStart)
        mWeekView.setFirstDayOfWeek(weekStart)
    }

    override fun onPageSelected(position: Int) {
        mOnMonthChangedListener?.onMonthChanged(mAdapter.getCurrentCalendar(position))
    }

    /**
     * 设置日历月份监听器
     * @param listener OnMonthChangedListener
     */
    fun setOnMonthChangeListener(listener: OnMonthChangedListener) {
        mOnMonthChangedListener = listener
    }

    interface OnMonthChangedListener {
        fun onMonthChanged(calendar: Calendar)
    }

    companion object {
        const val MONTHS_IN_YEAR = 12
    }
}