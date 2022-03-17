package com.river.calendar.model

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
data class MonthViewAttr(
    var dayTextSize: Float,
    var dayTextColor: Int = 0,
    var padDayTextColor: Int = 0,
    var daySelectedTextColor: Int = 0,
    var dayDisabledTextColor: Int = 0,
    var daySelectedBackgroundColor: Int = 0,
    var dayHighlightBackgroundColor: Int = 0,
    var verticalOffset: Int = 0,
    var fullDay: Boolean = true,
)