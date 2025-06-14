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
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import android.view.Menu
import android.view.MenuItem
import android.content.res.Configuration

import com.example.quantitycalendar.api.HolidayApiService
import com.example.quantitycalendar.model.HolidayItem
import com.example.quantitycalendar.model.HolidayResponse
import retrofit2.Retrofit
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import retrofit2.converter.jackson.JacksonConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import com.fasterxml.woodstox.WstxInputFactory
import com.fasterxml.woodstox.WstxOutputFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedDate: LocalDate? = null
    private val holidays = mutableMapOf<LocalDate, HolidayItem>()

    companion object {
        private const val BASE_URL = "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfo/"
        private const val SERVICE_KEY = "vadVqUFa6685ZGne6KMnuX7bg2E%2Bpi7omEguo3UKao7II4nTqAE9PFW5TgUaWUo12oMmVWAJuFXNmtw%2Fe4HEZw%3D%3D"

        private val xmlMapper = XmlMapper(WstxInputFactory(), WstxOutputFactory()).apply {
            enable(SerializationFeature.INDENT_OUTPUT)
            registerKotlinModule()
        }

        private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create(xmlMapper))
            .build()

        val holidayApiService: HolidayApiService = retrofit.create(HolidayApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 저장된 테마 설정 로드
        val sharedPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val savedTheme = sharedPrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedTheme)

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
                            when (data.date.dayOfWeek) {
                                java.time.DayOfWeek.SUNDAY -> container.textView.setTextColor(getColor(R.color.day_text_weekend_sunday)) // 일요일
                                else -> container.textView.setTextColor(getColor(R.color.day_text_color)) // 월요일 포함 나머지 요일
                            }

                            // 공휴일 표시 로직 추가
                            val holiday = holidays[data.date]
                            if (holiday != null) {
                                container.textView.text = "${data.date.dayOfMonth}\n${holiday.dateName}"
                                container.textView.setTextColor(getColor(R.color.day_text_weekend_sunday)) // 공휴일은 일요일과 같은 색상으로 표시
                                container.textView.textSize = 10f // 글자 크기 조절
                            } else {
                                container.textView.textSize = 16f // 기본 글자 크기
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
                            container.textView.setTextColor(getColor(R.color.day_text_other_month_color))
                            container.selectedView.visibility = View.GONE
                            container.textView.setOnClickListener(null)
                        }
                    }
                }
            }

            monthHeaderBinder = object : com.kizitonwose.calendar.view.MonthHeaderFooterBinder<MonthHeaderContainer> {
                override fun create(view: View) = MonthHeaderContainer(view)
                override fun bind(container: MonthHeaderContainer, data: com.kizitonwose.calendar.core.CalendarMonth) {
                    // `calendar_month_header_layout.xml`에는 동적으로 바인딩할 텍스트 뷰가 없으므로,
                    // 여기서는 추가적인 작업이 필요하지 않습니다.
                }
            }

            monthScrollListener = { month ->
                title = "${month.yearMonth.year}년 ${month.yearMonth.monthValue}월"
                // 월이 변경될 때마다 공휴일 정보 가져오기
                fetchHolidays(month.yearMonth.year, month.yearMonth.monthValue)
            }
        }

        binding.calendarView.setup(
            firstMonth,
            lastMonth,
            WeekFields.of(firstDayOfWeek, 1).firstDayOfWeek
        )
        binding.calendarView.scrollToMonth(currentMonth)

        // 초기 달력 로드 시 현재 월의 공휴일 정보 가져오기
        fetchHolidays(currentMonth.year, currentMonth.monthValue)
    }

    private fun setupButtons() {
        binding.addDeliveryButton.setOnClickListener {
            // 목록탭을 누르면 라이트모드로 전환
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            // SharedPreferences에 라이트 모드 설정 저장 (앱 재실행 시에도 유지)
            val sharedPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            sharedPrefs.edit().putInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO).apply()

            Toast.makeText(this, "수량 추가하기", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val toggleThemeItem = menu?.findItem(R.id.action_toggle_theme)
        if (toggleThemeItem != null) {
            val currentNightMode = AppCompatDelegate.getDefaultNightMode()
            if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                toggleThemeItem.title = "라이트 모드 전환"
            } else {
                toggleThemeItem.title = "다크 모드 전환"
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_theme -> {
                val currentNightMode = AppCompatDelegate.getDefaultNightMode()
                val newNightMode = if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    MODE_NIGHT_NO
                } else {
                    MODE_NIGHT_YES
                }

                // SharedPreferences에 테마 설정 저장
                val sharedPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                sharedPrefs.edit().putInt("theme_mode", newNightMode).apply()

                // 테마 적용
                AppCompatDelegate.setDefaultNightMode(newNightMode)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchHolidays(year: Int, month: Int) {
        val monthString = String.format("%02d", month) // 월을 두 자리 숫자로 포맷 (예: 1 -> 01)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = holidayApiService.getHolidays(SERVICE_KEY, year, monthString)
                if (response.isSuccessful) {
                    val holidayResponse = response.body()
                    holidayResponse?.body?.items?.item?.let { holidayList ->
                        // 기존 공휴일 데이터를 해당 월에 대해 초기화
                        holidays.entries.removeAll { it.key.monthValue == month && it.key.year == year }
                        holidayList.forEach { holidayItem ->
                            holidayItem.locdate?.let { locdate ->
                                // YYYYMMDD 형식의 locdate를 LocalDate로 변환
                                val extractedYear = locdate / 10000
                                val extractedMonth = (locdate % 10000) / 100
                                val day = locdate % 100
                                val date = LocalDate.of(extractedYear, extractedMonth, day)
                                holidays[date] = holidayItem
                            }
                        }
                        withContext(Dispatchers.Main) {
                            binding.calendarView.notifyCalendarChanged() // 달력 전체를 새로고침
                        }
                    }
                } else {
                    // 에러 응답 처리
                    val errorBody = response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "공휴일 정보 로드 실패: $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "공휴일 정보 로드 중 오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
} 