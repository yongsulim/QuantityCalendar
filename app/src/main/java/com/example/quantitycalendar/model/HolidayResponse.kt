package com.example.quantitycalendar.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "response")
data class HolidayResponse(
    @JacksonXmlProperty(localName = "header")
    val header: Header,
    @JacksonXmlProperty(localName = "body")
    val body: Body
)

data class Header(
    @JacksonXmlProperty(localName = "resultCode")
    val resultCode: String,
    @JacksonXmlProperty(localName = "resultMsg")
    val resultMsg: String
)

data class Body(
    @JacksonXmlProperty(localName = "items")
    val items: Items? = null, // items가 없을 수도 있으므로 nullable 처리
    @JacksonXmlProperty(localName = "numOfRows")
    val numOfRows: Int = 0,
    @JacksonXmlProperty(localName = "pageNo")
    val pageNo: Int = 0,
    @JacksonXmlProperty(localName = "totalCount")
    val totalCount: Int = 0
)

data class Items(
    @JacksonXmlProperty(localName = "item")
    val item: List<HolidayItem>? = null // item이 하나이거나 없을 수도 있으므로 List 및 nullable 처리
)

data class HolidayItem(
    @JacksonXmlProperty(localName = "dateKind")
    val dateKind: String? = null, // 특일 종류 (01: 국경일, 02: 기념일, 03: 24절기, 04: 잡절)
    @JacksonXmlProperty(localName = "dateName")
    val dateName: String? = null, // 명칭
    @JacksonXmlProperty(localName = "isHoliday")
    val isHoliday: String? = null, // 공공기관 휴일 여부 (Y: 휴일, N: 휴일 아님)
    @JacksonXmlProperty(localName = "locdate")
    val locdate: Int? = null, // 날짜 (YYYYMMDD)
    @JacksonXmlProperty(localName = "seq")
    val seq: Int? = null // 순번
) 