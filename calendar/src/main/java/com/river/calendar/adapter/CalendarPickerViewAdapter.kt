package com.river.calendar.adapter

import android.content.Context
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.core.util.forEach
import androidx.viewpager.widget.PagerAdapter
import com.river.calendar.CalendarPickerView
import com.river.calendar.MonthView
import com.river.calendar.model.MonthViewAttr
import java.util.*

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
abstract class CalendarPickerViewAdapter<VIEW : MonthView>(var mMinDate: Calendar, var mMaxDate: Calendar, var mWeekStart: Int = Calendar.SUNDAY) : PagerAdapter(), MonthView.OnMonthViewAttrChangedListener {
    val mItems = SparseArray<ViewHolder>()
    protected var mAttr: MonthViewAttr? = null

    override fun getCount(): Int {

        val diffYear: Int = mMaxDate.get(Calendar.YEAR) - mMinDate.get(Calendar.YEAR)
        val diffMonth: Int = mMaxDate.get(Calendar.MONTH) - mMinDate.get(Calendar.MONTH)

        return diffMonth + CalendarPickerView.MONTHS_IN_YEAR * diffYear + 1
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val monthView = createMonthView(container.context)
        monthView.id = position
        monthView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = mMinDate.timeInMillis
        calendar.add(Calendar.MONTH, position)
        monthView.setFirstDayOfWeek(mWeekStart)
        monthView.setDate(calendar.time)
        mAttr?.let { monthView.setAttr(it) }
        monthView.setOnAttrChangedListener(this)
        val holder = ViewHolder(position, monthView)
        mItems.put(position, holder)
        container.addView(monthView)
        return holder
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        val holder = `object` as CalendarPickerViewAdapter<*>.ViewHolder
        return view === holder.calendar
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val holder = `object` as CalendarPickerViewAdapter<*>.ViewHolder
        container.removeView(holder.calendar)
        mItems.remove(position)
    }

    override fun getItemPosition(`object`: Any): Int {
        val holder = `object` as CalendarPickerViewAdapter<*>.ViewHolder
        var position = holder.position
        if (holder.needRefresh) {
            holder.needRefresh = false
            position = POSITION_NONE
        }
        return position
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val v = mItems[position].calendar
        return if (v != null) {
            v.getMonthYearLabel()
        } else null
    }

    override fun onAttrChanged(attr: MonthViewAttr) {
        mAttr = attr
    }

    /**
     * 设置周几为第一天
     * @param weekStart Int
     */
    fun setFirstDayOfWeek(weekStart: Int) {
        mItems.forEach { key, value ->
            value.calendar.setFirstDayOfWeek(weekStart)
        }
    }

    /**
     * 默认位置
     * @return Int
     */
    open fun defaultPosition(): Int {
        return calcMonthFromCalendar(mMinDate, Calendar.getInstance())
    }

    fun getCurrentCalendar(position: Int): Calendar {
        val view = mItems[position].calendar
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, view.year())
        calendar.set(Calendar.MONTH, view.month())

        return calendar
    }

    /**
     * 计算日期间相差月数
     * @param min Calendar
     * @param max Calendar
     * @return Int
     */
    internal fun calcMonthFromCalendar(min: Calendar, max: Calendar): Int {
        return 12 * (max.get(Calendar.YEAR) - min.get(Calendar.YEAR)) + max.get(Calendar.MONTH) - min.get(
                Calendar.MONTH
        )
    }

    abstract fun createMonthView(context: Context): VIEW

    inner class ViewHolder(
            val position: Int,
            val calendar: VIEW,
            var needRefresh: Boolean = false
    )
}