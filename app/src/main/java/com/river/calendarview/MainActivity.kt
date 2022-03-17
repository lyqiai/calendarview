package com.river.calendarview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.river.calendar.CalendarPickerView
import com.river.calendar.adapter.SingleCalendarPickerViewAdapter
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val adapter = SingleCalendarPickerViewAdapter(Calendar.getInstance(), Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
        })
        adapter.setOnDayClickListener {v, d->

        }
        findViewById<CalendarPickerView>(R.id.month_view).setAdapter(adapter)
    }
}