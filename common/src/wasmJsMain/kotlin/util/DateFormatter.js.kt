@file:Suppress("unused", "FunctionName")
package eu.bsinfo.util

external class Date(time: Double) : JsAny

@JsName("Intl.DateTimeFormat")
external class DateTimeFormat(locale: JsString, options: JsAny) : JsAny {
    fun format(date: Date): JsString
}

@JsFun("() => ({ year: 'numeric', month: 'long', day: 'numeric' })")
external fun DateFormatterOptions(): JsAny
