package com.brendan.controlanything.domain.model

sealed class ControlDef {
    abstract val topic: String
    abstract val displayName: String

    data class Toggle(
        override val topic: String,
        override val displayName: String,
        val defaultValue: Boolean = false,
    ) : ControlDef()

    data class Button(
        override val topic: String,
        override val displayName: String,
        val mode: ButtonMode = ButtonMode.STATE,
    ) : ControlDef()

    data class Slider(
        override val topic: String,
        override val displayName: String,
        val min: Float,
        val max: Float,
        val defaultValue: Float,
        val orientation: SliderOrientation = SliderOrientation.HORIZONTAL,
    ) : ControlDef()

    data class Joystick(
        val topicX: String,
        val topicY: String,
        override val displayName: String,
    ) : ControlDef() {
        // Stable identity for this control - joysticks have two wire topics but one widget.
        override val topic: String get() = topicX
    }
}

/** How a button reports presses: a one-shot pulse on press, on release, or the held-down state on both. */
enum class ButtonMode { RISING, FALLING, STATE }

enum class SliderOrientation { HORIZONTAL, VERTICAL }
