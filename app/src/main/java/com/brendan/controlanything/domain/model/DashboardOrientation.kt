package com.brendan.controlanything.domain.model

/**
 * Auto-rotate doesn't work with a grid laid out for one screen dimension, so the dashboard locks
 * the Activity to whichever orientation is chosen here instead. Saved per dashboard alongside
 * column count.
 */
enum class DashboardOrientation { PORTRAIT, LANDSCAPE }
