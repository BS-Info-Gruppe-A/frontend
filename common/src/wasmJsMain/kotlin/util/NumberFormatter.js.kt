@file:Suppress("unused", "FunctionName")
package eu.bsinfo.util

@JsName("Intl.NumberFormat")
external class NumberFormat(locale: JsString, options: JsAny) : JsAny {
    fun format(date: Double): JsString
}

@JsFun("() => ({ maximumFractionDigits: 2 })")
external fun NumberFormatterOptions(): JsAny
