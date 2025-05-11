package com.example.quantitycalendar

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.quantitycalendar.databinding.ActivityMainBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.DayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val today = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewModel 연결
        val db = AppDatabase.getDatabase(applicationContext)
        val viewModel: MainViewModel by viewModels {
            MainViewModelFactory(db.deliveryDao())
        }

        // ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Calendar 설정
        val currentMonth = YearMonth.now()
        val firstDayOfWeek = java.time.DayOfWeek.SUNDAY

        binding.calendarView.setup(
            currentMonth,
            currentMonth.plusMonths(1),
            firstDayOfWeek
        )

        binding.calendarView.scrollToMonth(currentMonth)

        // dayView layout 설정 (res/layout/calendar_day.xml)
        binding.calendarView.dayViewResource = R.layout.calendar_day

        // DayBinder 연결
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView: TextView = view.findViewById(R.id.dayText)
        }

        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
            }
        }
    }
}
