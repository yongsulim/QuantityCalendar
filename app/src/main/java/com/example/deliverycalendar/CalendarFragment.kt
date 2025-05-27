// 캘린더 프래그먼트

package com.example.deliverycalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

class CalendarFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private var selectedDate: LocalDate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        calendarView = view.findViewById(R.id.calendarView)

        // 캘린더 설정
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        // 날짜 선택 처리
        class DayViewContainer(view: View) : CalendarView.DayViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectedDate = day.date
                        calendarView.notifyDateChanged(day.date)
                        loadDeliveryData(day.date)
                    }
                }
            }
        }

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                container.textView.text = day.date.dayOfMonth.toString()
                
                if (day.owner == DayOwner.THIS_MONTH) {
                    container.textView.setTextColor(Color.BLACK)
                    // 선택된 날짜 표시
                    if (selectedDate == day.date) {
                        container.textView.setBackgroundResource(R.drawable.selected_day_background)
                    } else {
                        container.textView.background = null
                    }
                } else {
                    container.textView.setTextColor(Color.GRAY)
                    container.textView.background = null
                }
            }
        }
    }

    private fun loadDeliveryData(date: LocalDate) {
        // TODO: 선택된 날짜의 배송 데이터를 로드하는 로직 구현
    }
} 