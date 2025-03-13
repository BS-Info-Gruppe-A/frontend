@file:Suppress("unused", "FunctionName")
package eu.bsinfo.util

@JsName("Intl.NumberFormat")
external class NumberFormat(locale: JsString, options: JsAny) : JsAny {
    constructor(locale: JsString)
    fun format(date: Double): JsString
}

@JsFun("() => ({ minimumFractionDigits: 1, maximumFractionDigits: 2 })")
external fun NumberFormatterOptions(): JsAny
