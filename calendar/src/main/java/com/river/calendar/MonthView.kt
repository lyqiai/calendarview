package com.river.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.ViewUtils
import com.river.calendar.model.MonthViewAttr
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min


/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
@SuppressLint("ResourceType")
abstract class MonthView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val mLocale = context.resources.configuration.locale

    private val mCalendar = Calendar.getInstance()

    //日期画笔
    private val mDayPaint = TextPaint()

    //选中画笔
    private val mDaySelectedPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    //按压背景画笔
    private val mDayHighlightPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    //日期预期高度
    private val mDesiredDayHeight =
            context.resources.getDimensionPixelSize(R.dimen.date_picker_day_height)

    //日期预期宽度
    private val mDesiredCellWidth =
            context.resources.getDimensionPixelSize(R.dimen.date_picker_day_width)

    //星期预期高度
    private val mDesiredDayOfWeekHeight =
            context.resources.getDimensionPixelSize(R.dimen.date_picker_day_of_week_height)

    private var mPaddedWidth = 0
    private var mPaddedHeight = 0
    protected var mDayHeight = 0
    protected var mCellWidth = 0
    private var mDayOfWeekStart = 0
    private var mDayOfWeekHeight = 0
    protected var mDayRadius = 0F

    //一星期第一天是星期几
    private var mWeekStart: Int = DEFAULT_WEEK_START

    //月份有几天
    private var mDaysInMonth = 0

    protected var mYear = 0
    protected var mMonth = 0

    private var mDay = -1

    private var mEnabledDayStart = 1

    private var mEnabledDayEnd = 31

    protected var mAttr: MonthViewAttr

    private val mPadStartDayList = mutableListOf<Int>()
    private val mPadEndDayList = mutableListOf<Int>()

    private var mOnAttrChangedListener: OnMonthViewAttrChangedListener? = null

    init {
        val array: TypedArray = context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorPrimary))
        val typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.MonthView,
                defStyleAttr,
                R.style.DefaultMonthViewStyle
        )

        val daySelectedBackgroundColor = typedArray.getColor(R.styleable.MonthView_daySelectedBackgroundColor, 0)
        mAttr = MonthViewAttr(
                daySelectedBackgroundColor = if (daySelectedBackgroundColor == 0) array.getColor(0, -1) else daySelectedBackgroundColor,
                dayTextSize = typedArray.getDimensionPixelSize(R.styleable.MonthView_dayTextSize, context.resources.getDimensionPixelSize(R.dimen.day_text_size)).toFloat(),
                dayTextColor = typedArray.getColor(R.styleable.MonthView_dayTextColor, context.resources.getColor(R.color.day_text_color)),
                padDayTextColor = typedArray.getColor(R.styleable.MonthView_padDayTextColor, context.resources.getColor(R.color.pad_day_text_color)),
                dayDisabledTextColor = typedArray.getColor(R.styleable.MonthView_dayDisabledTextColor, context.resources.getColor(R.color.day_disabled_text_color)),
                daySelectedTextColor = typedArray.getColor(R.styleable.MonthView_daySelectedTextColor, context.resources.getColor(R.color.day_selected_text_color)),
                dayHighlightBackgroundColor = typedArray.getColor(R.styleable.MonthView_dayHighlightBackgroundColor, context.resources.getColor(R.color.day_highlight_background_color)),
                verticalOffset = typedArray.getDimensionPixelOffset(R.styleable.MonthView_verticalOffset, context.resources.getDimensionPixelOffset(R.dimen.vertical_offset)),
                fullDay = typedArray.getBoolean(R.styleable.MonthView_fullDay, context.resources.getBoolean(R.bool.full_day))
        )

        array.recycle()
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val preferredHeight: Int =
                (mDesiredDayHeight * MAX_WEEKS_IN_MONTH + paddingTop + paddingBottom + (MAX_WEEKS_IN_MONTH - 1) * mAttr.verticalOffset)
        val preferredWidth: Int = mDesiredCellWidth * DAYS_IN_WEEK + paddingStart + paddingEnd
        val resolvedWidth = resolveSize(preferredWidth, widthMeasureSpec)
        val resolvedHeight = resolveSize(preferredHeight, heightMeasureSpec)
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val w = right - left
        val h = bottom - top
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom
        val paddedRight = w - paddingRight
        val paddedBottom = h - paddingBottom
        val paddedWidth = paddedRight - paddingLeft
        val paddedHeight = paddedBottom - paddingTop

        if (paddedWidth == mPaddedWidth || paddedHeight == mPaddedHeight) {
            return
        }

        mPaddedWidth = paddedWidth
        mPaddedHeight = paddedHeight

        val cellWidth = mPaddedWidth / DAYS_IN_WEEK
        val measuredPaddedHeight = measuredHeight - paddingTop - paddingBottom
        val scaleH = paddedHeight / measuredPaddedHeight.toFloat()

        mDayOfWeekHeight = (mDesiredDayOfWeekHeight * scaleH).toInt()
        mDayHeight = (mDesiredDayHeight * scaleH).toInt()
        mCellWidth = cellWidth
        mDayRadius = (min(mCellWidth, mDayHeight) + 1.5f) / 2
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        drawDays(canvas)
        canvas.translate(-paddingLeft.toFloat(), -paddingTop.toFloat())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = (event.x + 0.5f).toInt()
        val y = (event.y + 0.5f).toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val touchedItem: Int = getDayAtLocation(x, y)
                mDay = touchedItem
                invalidate()
                if (event.action == MotionEvent.ACTION_DOWN && touchedItem < 0) {
                    return false
                }
            }
            MotionEvent.ACTION_UP -> {
                mDay = -1
                val clickedDay: Int = getDayAtLocation(x, y)
                onDayClicked(clickedDay)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                mDay = -1
                invalidate()
            }
        }
        return true
    }

    /**
     * 绘制日期
     * @param canvas Canvas
     */
    private fun drawDays(canvas: Canvas) {
        val p: TextPaint = mDayPaint.apply {
            textSize = mAttr.dayTextSize
        }
        val rowHeight: Int = mDayHeight
        val colWidth: Int = mCellWidth


        val halfLineHeight = (p.ascent() + p.descent()) / 2f
        var rowCenter = rowHeight / 2

        if (mAttr.fullDay) {
            var col = 0
            for (day in mPadStartDayList) {
                val textWidth = p.measureText(day.toString())

                val colCenter = colWidth * col + (colWidth - textWidth) / 2

                canvas.drawText(day.toString(), colCenter, rowCenter - halfLineHeight, p.apply {
                    color = mAttr.padDayTextColor
                    textSize = mAttr.dayTextSize
                })

                col++
            }
        }

        var day = 1
        var col = findDayOffset()
        while (day <= mDaysInMonth) {
            val textWidth = p.measureText(day.toString())

            val colCenter = colWidth * col + (colWidth - textWidth) / 2

            val allowDrawDayBackground = allowDrawDayBackground(day)

            val centerX = colWidth * col + colWidth / 2f

            if (mDay == day && !allowDrawDayBackground) {
                canvas.drawCircle(centerX, rowCenter.toFloat(), mDayRadius, mDayHighlightPaint.apply {
                    color = mAttr.dayHighlightBackgroundColor
                })
            }

            if (allowDrawDayBackground) {
                drawDayBackground(canvas, mDaySelectedPaint.apply { color = mAttr.daySelectedBackgroundColor }, day, centerX, rowCenter.toFloat())
            }

            canvas.drawText(day.toString(), colCenter, rowCenter - halfLineHeight, p.apply {
                color = if (allowDrawDayBackground) mAttr.daySelectedTextColor else mAttr.dayTextColor
            })

            drawDay(canvas, day, centerX, rowCenter.toFloat())

            col++
            if (col == DAYS_IN_WEEK) {
                col = 0
                rowCenter += rowHeight + mAttr.verticalOffset
            }
            day++
        }

        if (mAttr.fullDay) {
            for (day in mPadEndDayList) {
                val textWidth = p.measureText(day.toString())

                val colCenter = colWidth * col + (colWidth - textWidth) / 2

                canvas.drawText(day.toString(), colCenter, rowCenter - halfLineHeight, p.apply {
                    color = mAttr.padDayTextColor
                    textSize = mAttr.dayTextSize
                })

                col++
                if (col == DAYS_IN_WEEK) {
                    col = 0
                    rowCenter += rowHeight + mAttr.verticalOffset
                }
            }
        }
    }

    open fun drawDay(canvas: Canvas, day: Int, centerX: Float, centerY: Float) {}

    /**
     * 是否绘制背景
     * @param day Int
     * @return Boolean
     */
    abstract fun allowDrawDayBackground(day: Int): Boolean

    /**
     * 绘制背景
     * @param canvas Canvas
     * @param day Int
     * @param centerX Float
     * @param centerY Float
     */
    abstract fun drawDayBackground(
            canvas: Canvas,
            paint: Paint,
            day: Int,
            centerX: Float,
            centerY: Float
    )

    /**
     * day点击
     * @param day Int
     */
    abstract fun onDayClickHandle(day: Int)

    /**
     * 获取本月1号后移数
     * @return Int
     */
    private fun findDayOffset(): Int {
        val offset: Int = mDayOfWeekStart - mWeekStart
        return if (mDayOfWeekStart < mWeekStart) {
            offset + DAYS_IN_WEEK
        } else offset
    }

    /**
     * 获取月份日期数
     * @param month Int
     * @param year Int
     * @return Int
     */
    private fun getDaysInMonth(month: Int, year: Int): Int {
        return when (month) {
            Calendar.JANUARY, Calendar.MARCH, Calendar.MAY, Calendar.JULY, Calendar.AUGUST, Calendar.OCTOBER, Calendar.DECEMBER -> 31
            Calendar.APRIL, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.NOVEMBER -> 30
            Calendar.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> throw IllegalArgumentException("Invalid Month")
        }
    }

    /**
     * 设置展示月份
     * @param date Date
     */
    fun setDate(date: Date) {
        val mCalendar = Calendar.getInstance()
        mCalendar.time = date
        mCalendar.set(Calendar.DAY_OF_MONTH, 1)

        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK)
        mDaysInMonth = getDaysInMonth(date.month, date.year)
        mYear = mCalendar.get(Calendar.YEAR)
        mMonth = mCalendar.get(Calendar.MONTH)
        this.mCalendar.timeInMillis = mCalendar.timeInMillis

        updatePadDay()

        invalidate()
    }

    /**
     * 获取点击事件匹配的日期
     * @param x Int
     * @param y Int
     * @return Int
     */
    @SuppressLint("RestrictedApi")
    private fun getDayAtLocation(x: Int, y: Int): Int {
        val paddedX = x - paddingLeft
        if (paddedX < 0 || paddedX >= mPaddedWidth) {
            return -1
        }
        val paddedY = y - paddingTop
        if (paddedY < 0 || paddedY >= mPaddedHeight) {
            return -1
        }

        val paddedXRtl = if (ViewUtils.isLayoutRtl(this)) {
            mPaddedWidth - paddedX
        } else {
            paddedX
        }
        val row = paddedY / (mDayHeight + mAttr.verticalOffset)
        val col: Int = paddedXRtl * DAYS_IN_WEEK / mPaddedWidth
        val index: Int = col + row * DAYS_IN_WEEK
        val day = index + 1 - findDayOffset()
        return if (!isValidDayOfMonth(day)) {
            -1
        } else day
    }

    /**
     * 点击日期
     * @param day Int
     */
    private fun onDayClicked(day: Int) {
        if (!isValidDayOfMonth(day) || !isDayEnabled(day)) {
            return
        }

        onDayClickHandle(day)
    }

    /**
     * 合法日期
     * @param day Int
     * @return Boolean
     */
    private fun isValidDayOfMonth(day: Int): Boolean {
        return day in 1..mDaysInMonth
    }

    /**
     * 日期是否可点击
     * @param day Int
     * @return Boolean
     */
    private fun isDayEnabled(day: Int): Boolean {
        return day in mEnabledDayStart..mEnabledDayEnd
    }

    /**
     * 星期是否合法
     * @param day Int
     * @return Boolean
     */
    private fun isValidDayOfWeek(day: Int): Boolean {
        return day >= Calendar.SUNDAY && day <= Calendar.SATURDAY
    }

    /**
     * 更新填充日期数据
     */
    private fun updatePadDay() {
        val calendar = Calendar.getInstance()
        calendar.time = mCalendar.time
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        var offset = findDayOffset()
        calendar.add(Calendar.DAY_OF_MONTH, -offset - 1)

        mPadStartDayList.clear()
        for (i in 0..offset) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            mPadStartDayList.add(calendar.get(Calendar.DAY_OF_MONTH))
        }

        val fullDay = MAX_WEEKS_IN_MONTH * DAYS_IN_WEEK
        val offsetEnd = fullDay - offset - mDaysInMonth

        calendar.time = mCalendar.time
        calendar.set(Calendar.DAY_OF_MONTH, mDaysInMonth)

        mPadEndDayList.clear()
        for (i in 0..offsetEnd) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            mPadEndDayList.add(calendar.get(Calendar.DAY_OF_MONTH))
        }
    }

    /**
     * label
     * @return String
     */
    fun getMonthYearLabel(): String {
        val format = DateFormat.getBestDateTimePattern(mLocale, MONTH_YEAR_FORMAT)
        val formatter = SimpleDateFormat(format, mLocale)
        return formatter.format(mCalendar.time)
    }

    /**
     * 设置day字体大小
     * @param textSize Float
     */
    fun setDayTextSize(textSize: Float) {
        mAttr.dayTextSize = textSize
        invalidate()
    }

    /**
     * 设置day字体颜色
     * @param color Int
     */
    fun setDayTextColor(color: Int) {
        mAttr.dayTextColor = color
        invalidate()
    }

    /**
     * 设置day字体选中颜色
     * @param color Int
     */
    fun setDaySelectedTextColor(color: Int) {
        mAttr.daySelectedTextColor = color
        invalidate()
    }

    /**
     * 设置day字体禁用颜色
     * @param color Int
     */
    fun setDayDisabledTextColor(color: Int) {
        mAttr.dayDisabledTextColor = color
        invalidate()
    }

    /**
     * 设置day选中背景颜色
     * @param color Int
     */
    fun setDaySelectedBackgroundColor(color: Int) {
        mAttr.daySelectedBackgroundColor = color
        invalidate()
    }

    /**
     * 设置day按压背景颜色
     * @param color Int
     */
    fun setDayHighlightBackgroundColor(color: Int) {
        mAttr.dayHighlightBackgroundColor = color
        invalidate()
    }

    /**
     * 设置周几为一周的第一天
     * @param weekStart Int
     */
    fun setFirstDayOfWeek(weekStart: Int) {
        mWeekStart = if (isValidDayOfWeek(weekStart)) {
            weekStart
        } else {
            mCalendar.firstDayOfWeek
        }
        updatePadDay()
        invalidate()
    }

    /**
     * 设置是否填充日期
     * @param fullDay Boolean
     */
    fun setFullDay(fullDay: Boolean) {
        mAttr.fullDay = fullDay
        invalidate()
    }

    /**
     * 设置attr
     * @param attr MonthViewAttr
     */
    fun setAttr(attr: MonthViewAttr) {
        mAttr = attr
        invalidate()
        mOnAttrChangedListener?.onAttrChanged(mAttr)
    }

    /**
     * 设置attr监听器
     * @param listener OnMonthViewAttrChangedListener?
     */
    fun setOnAttrChangedListener(listener: OnMonthViewAttrChangedListener?) {
        mOnAttrChangedListener = listener
        mOnAttrChangedListener?.onAttrChanged(mAttr)
    }

    fun year() = mYear

    fun month() = mMonth

    interface OnMonthViewAttrChangedListener {
        fun onAttrChanged(attr: MonthViewAttr)
    }

    companion object {
        const val DAYS_IN_WEEK = 7
        const val DEFAULT_WEEK_START = Calendar.SUNDAY
        private const val MAX_WEEKS_IN_MONTH = 6
        private const val MONTH_YEAR_FORMAT = "MMMMy"
    }
}