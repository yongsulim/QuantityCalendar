package com.example.quantitycalendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.quantitycalendar.databinding.ActivityMainBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCalendar()
        setupButtons()
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(12)
        val lastMonth = currentMonth.plusMonths(12)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        binding.calendarView.apply {
            dayBinder = object : MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    container.textView.text = data.date.dayOfMonth.toString()
                    
                    // 날짜 스타일 설정
                    when (data.position) {
                        DayPosition.MonthDate -> {
                            container.textView.alpha = 1f
                            
                            // 주말 색상 설정
                            when (data.date.dayOfWeek.value) {
                                7 -> container.textView.setTextColor(getColor(android.R.color.holo_blue_dark)) // 토요일
                                1 -> container.textView.setTextColor(getColor(android.R.color.holo_red_dark))  // 일요일
                                else -> container.textView.setTextColor(getColor(android.R.color.black))
                            }

                            // 선택된 날짜 표시
                            container.selectedView.visibility = if (data.date == selectedDate) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }

                            container.textView.setOnClickListener {
                                if (selectedDate != data.date) {
                                    selectedDate = data.date
                                    binding.calendarView.notifyDateChanged(data.date)
                                    
                                    // 이전에 선택된 날짜가 있다면 해당 날짜도 다시 그리기
                                    selectedDate?.let { date ->
                                        binding.calendarView.notifyDateChanged(date)
                                    }
                                }
                                Toast.makeText(
                                    this@MainActivity,
                                    "선택된 날짜: ${data.date}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        DayPosition.InDate, DayPosition.OutDate -> {
                            container.textView.alpha = 0.3f
                            container.textView.setTextColor(getColor(android.R.color.darker_gray))
                            container.selectedView.visibility = View.GONE
                            container.textView.setOnClickListener(null)
                        }
                    }
                }
            }

            monthScrollListener = { month ->
                title = "${month.yearMonth.year}년 ${month.yearMonth.monthValue}월"
            }
        }

        binding.calendarView.setup(
            firstMonth,
            lastMonth,
            WeekFields.of(firstDayOfWeek, 1).firstDayOfWeek
        )
        binding.calendarView.scrollToMonth(currentMonth)
    }

    private fun setupButtons() {
        binding.addDeliveryButton.setOnClickListener {
            Toast.makeText(this, "수량 추가하기", Toast.LENGTH_SHORT).show()
        }
    }
} 