## MonthView

月份展示控件

| dayTextSize | 字号 |
| --- | --- |
| dayTextColor | 字色 |
| padDayTextColor | 补充日期字色 |
| daySelectedTextColor | 选中字色 |
| dayDisabledTextColor | 禁止字色 |
| daySelectedBackgroundColor | 选中背景色 |
| dayHighlightBackgroundColor | 点击按压色 |
| verticalOffset | 垂直距离 |
| fullDay | 是否显示补充日期 |

该类为抽象类,根据UI自行实现对应

```kotlin
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
```

对外方法

| fun setDate(date: Date) | 设置展示月份 |
| --- | --- |
| fun getMonthYearLabel(): String | 获取展示月份 |
| fun setDayTextSize(textSize: Float) | 设置day字体大小 |
| fun setDayTextColor(color: Int) | 设置day字体颜色 |
| fun setDaySelectedTextColor(color: Int) | 设置day字体选中颜色 |
| fun setDayDisabledTextColor(color: Int) | 设置day字体禁用颜色 |
| fun setDaySelectedBackgroundColor(color: Int) | 设置day选中背景颜色 |
| fun setDayHighlightBackgroundColor(color: Int) | 设置day按压背景颜色 |
| fun setFirstDayOfWeek(weekStart: Int) | 设置周几为一周的第一天 |
| fun setFullDay(fullDay: Boolean) | 设置是否填充日期 |
| fun year(): Int | 获取年份 |
| fun month(): Int | 获取月份 |

## SingleMonthView

MonthView的实现类，实现了单个点击选中效果

## RangeMonthView

MonthView的实现类，实现了时间范围选中效果

## CalendarPickerView

组装MonthView的控件，通过DatePickerViewAdapter设置数据。对外方法

| fun setAdapter(adapter: DatePickerViewAdapter<*>) | 设置日历适配器 |
| --- | --- |
| fun nextMonth() | 跳转下一个月 |
| fun prevMonth() | 跳转上一个月 |
| fun canGoNextMonth(): Boolean | 是否可以跳转下一个月 |
| fun canGoPrevMonth(): Boolean | 是否可以跳转上一个月 |
| fun getMonthView(): MonthView? | 获取月份控件 |
| fun getWeekView(): WeekView | 获取星期控件 |
| fun setFirstDayOfWeek(weekStart: Int) | 设置周几为第一天 |
| fun setOnMonthChangeListener(listener: OnMonthChangedListener) | 设置日历月份监听器 |

## CalendarPickerViewAdapter<VIEW : MonthView>

DatePickerView的适配器，需要重写的方法

| override fun createMonthView(context: Context): VIEW  | 返回需要的 |
| --- | --- |

## SingleCalendarPickerViewAdapter

单选日期适配的实现

## RangeCalendarPickerViewAdapter

范围日期适配的实现

## Demo
布局文件
```xml
    <com.river.calendar.CalendarPickerView
        android:id="@+id/month_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
```
代码
```kotlin
        val adapter = SingleCalendarPickerViewAdapter(Calendar.getInstance(), Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
        })
        adapter.setOnDayClickListener {v, d->

        }
        findViewById<CalendarPickerView>(R.id.month_view).setAdapter(adapter)
```

## 集成
根目录build.gradle添加：

```groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
}
```

模块build.gradl添加：

```groovy
dependencies {
        implementation 'com.github.lyqiai:wheelview:0.0.1'
}
```