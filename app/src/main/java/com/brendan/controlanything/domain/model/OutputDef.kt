package com.brendan.controlanything.domain.model

sealed class OutputDef {
    abstract val topic: String
    abstract val displayName: String

    data class NumericReadout(override val topic: String, override val displayName: String) : OutputDef()

    data class LedIndicator(override val topic: String, override val displayName: String) : OutputDef()
}
