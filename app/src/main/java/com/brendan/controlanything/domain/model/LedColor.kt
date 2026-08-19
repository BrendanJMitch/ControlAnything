package com.brendan.controlanything.domain.model

/**
 * A curated palette of well-known, bright colors for the LED indicator widget. [argb] is a
 * plain Long (0xAARRGGBB) rather than a UI-framework Color, keeping the domain model
 * Compose-free - the widget layer converts it at render time.
 */
enum class LedColor(val argb: Long) {
    RED(0xFFF44336),
    GREEN(0xFF4CAF50),
    BLUE(0xFF2196F3),
    YELLOW(0xFFFFEB3B),
    ORANGE(0xFFFF9800),
    PURPLE(0xFF9C27B0),
    CYAN(0xFF00BCD4),
    MAGENTA(0xFFFF00FF),
    PINK(0xFFFF4081),
    LIME(0xFFCDDC39),
    TEAL(0xFF009688),
    AMBER(0xFFFFC107),
    INDIGO(0xFF3F51B5),
    VIOLET(0xFF7C4DFF),
    WHITE(0xFFFFFFFF),
}
