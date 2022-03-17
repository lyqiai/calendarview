package com.river.calendar

import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import java.text.DateFormatSymbols
import java.util.*

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
class WeekView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val mLocale = context.resources.configuration.locale

    //一星期第一天是星期几
    private var mWeekStart: Int = MonthView.DEFAULT_WEEK_START

    //星期画笔
    private val mDayOfWeekPaint = TextPaint().apply {
        textSize =
            context.resources.getDimensionPixelSize(R.dimen.date_picker_day_font_size).toFloat()
    }

    //星期预期高度
    private val mDesiredDayOfWeekHeight = context.resources.getDimensionPixelSize(R.dimen.date_picker_day_of_week_height)

    //日期预期宽度
    private val mDesiredCellWidth = context.resources.getDimensionPixelSize(R.dimen.date_picker_day_width)

    //星期数据
    private val mDayOfWeekLabels = arrayOfNulls<String>(7)

    private var mDayOfWeekHeight = 0
    private var mCellWidth = 0
    private var mPaddedWidth = 0
    private var mPaddedHeight = 0

    init {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.WeekView, defStyleAttr, R.style.DefaultWeekViewStyle)
        mDayOfWeekPaint.textSize = typeArray.getDimensionPixelSize(R.styleable.WeekView_weekTextSize, context.resources.getDimensionPixelSize(R.dimen.week_text_size)).toFloat()
        mDayOfWeekPaint.color = typeArray.getColor(R.styleable.WeekView_weekTextColor, context.resources.getColor(R.color.week_text_color))
        val bgColor = typeArray.getColor(R.styleable.WeekView_backgroundColor, context.resources.getColor(R.color.week_background_color))
        setBackgroundColor(bgColor)
        typeArray.recycle()

        updateDayOfWeekLabels()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val preferredHeight: Int = (mDesiredDayOfWeekHeight + paddingTop + paddingBottom)
        val preferredWidth: Int = mDesiredCellWidth * MonthView.DAYS_IN_WEEK + paddingStart + paddingEnd
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

        val cellWidth: Int = mPaddedWidth / MonthView.DAYS_IN_WEEK
        val measuredPaddedHeight = measuredHeight - paddingTop - paddingBottom
        val scaleH = paddedHeight / measuredPaddedHeight.toFloat()

        mDayOfWeekHeight = (mDesiredDayOfWeekHeight * scaleH).toInt()
        mCellWidth = cellWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        drawDaysOfWeek(canvas)
        canvas.translate(-paddingLeft.toFloat(), -paddingTop.toFloat())
    }

    /**
     * 绘制星期
     * @param canvas Canvas
     */
    private fun drawDaysOfWeek(canvas: Canvas) {
        val p: TextPaint = mDayOfWeekPaint
        val rowHeight: Int = mDayOfWeekHeight
        val colWidth = mCellWidth

        val halfLineHeight = (p.ascent() + p.descent()) / 2f
        val rowCenter = rowHeight / 2

        for (col in 0 until MonthView.DAYS_IN_WEEK) {
            val label: String = mDayOfWeekLabels[col]!!
            val textWidth = p.measureText(label)
            val colCenter = colWidth * col + (colWidth - textWidth) / 2
            canvas.drawText(label, colCenter, rowCenter - halfLineHeight, p)
        }
    }

    /**
     * 更新星期数据
     */
    private fun updateDayOfWeekLabels() {
        val tinyWeekdayNames: Array<String> = DateFormatSymbols(mLocale).shortWeekdays
        for (i in 0 until MonthView.DAYS_IN_WEEK) {
            mDayOfWeekLabels[i] = tinyWeekdayNames[(mWeekStart + i - 1) % MonthView.DAYS_IN_WEEK + 1]
        }
    }

    /**
     * 设置星期字体大小
     * @param textSize Float
     */
    fun setWeekTextSize(textSize: Float) {
        mDayOfWeekPaint.textSize = textSize
        invalidate()
    }

    /**
     * 设置星期字体颜色
     * @param color Int
     */
    fun setWeekTextColor(color: Int) {
        mDayOfWeekPaint.color = color
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
            MonthView.DEFAULT_WEEK_START
        }
        updateDayOfWeekLabels()
        invalidate()
    }

    /**
     * 星期是否合法
     * @param day Int
     * @return Boolean
     */
    private fun isValidDayOfWeek(day: Int): Boolean {
        return day >= Calendar.SUNDAY && day <= Calendar.SATURDAY
    }
}