package com.brendan.controlanything.domain.grid

data class GridPosition(
    val col: Int,
    val row: Int,
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
)
