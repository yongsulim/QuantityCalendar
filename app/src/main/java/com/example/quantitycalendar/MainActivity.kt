package com.example.quantitycalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.CalendarView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import com.example.quantitycalendar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val today = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)
        val viewModel: MainViewModel by viewModels {
            MainViewModelFactory(db.deliveryDao())
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentMonth = YearMonth.now()
        val firstDayOfWeek = java.time.DayOfWeek.SUNDAY

        binding.calendarView.setup(
            currentMonth,
            currentMonth.plusMonths(1),
            firstDayOfWeek
        )

        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.dayBinder = object : com.kizitonwose.calendar.view.ViewContainer(binding.calendarView) {
            override fun bind(day: CalendarDay) {
                val textView = findViewById<TextView>(R.id.dayText)
                textView.text = day.date.dayOfMonth.toString()
            }
        }
    }
}
