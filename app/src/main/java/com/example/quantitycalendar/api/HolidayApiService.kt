package com.example.quantitycalendar.api

import com.example.quantitycalendar.model.HolidayResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HolidayApiService {
    @GET("getHoliDeInfo")
    suspend fun getHolidays(
        @Query("ServiceKey", encoded = true) serviceKey: String,
        @Query("solYear") solYear: Int,
        @Query("solMonth") solMonth: String,
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("pageNo") pageNo: Int = 1,
        @Query("_type") type: String = "xml"
    ): Response<HolidayResponse>
} 