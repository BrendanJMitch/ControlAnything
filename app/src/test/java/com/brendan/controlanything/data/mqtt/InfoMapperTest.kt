package com.brendan.controlanything.data.mqtt

import com.brendan.controlanything.domain.model.ControlDef
import com.brendan.controlanything.domain.model.OutputDef
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InfoMapperTest {

    private fun parse(json: String) = Json.decodeFromString<InfoMessage>(json).toDeviceInfo()

    @Test
    fun `parses device metadata`() {
        val info = parse(
            """
            {
              "device_id": "esp32-01",
              "device_name": "Test Rover",
              "project_id": "test_project",
              "schema_hash": "abc123",
              "controls": [],
              "outputs": []
            }
            """.trimIndent(),
        )
        assertEquals("esp32-01", info.deviceId)
        assertEquals("Test Rover", info.deviceName)
        assertEquals("test_project", info.projectId)
        assertEquals("abc123", info.schemaHash)
    }

    @Test
    fun `parses a toggle control`() {
        val info = parse(controlsPayload("""{"topic": ["lights"], "display_name": "Lights", "type": "bool", "widget": {"type": "toggle"}}"""))
        assertEquals(listOf(ControlDef.Toggle("lights", "Lights")), info.controls)
    }

    @Test
    fun `parses a button control`() {
        val info = parse(controlsPayload("""{"topic": ["horn"], "display_name": "Horn", "type": "bool", "widget": {"type": "button"}}"""))
        assertEquals(listOf(ControlDef.Button("horn", "Horn")), info.controls)
    }

    @Test
    fun `parses a slider control with explicit min and max`() {
        val info = parse(
            controlsPayload(
                """{"topic": ["speed"], "display_name": "Speed", "type": "float", "widget": {"type": "slider", "min": -2.0, "max": 2.0}}""",
            ),
        )
        assertEquals(listOf(ControlDef.Slider("speed", "Speed", -2f, 2f)), info.controls)
    }

    @Test
    fun `slider falls back to 0 to 1 when min and max are omitted`() {
        val info = parse(controlsPayload("""{"topic": ["speed"], "display_name": "Speed", "type": "float", "widget": {"type": "slider"}}"""))
        assertEquals(listOf(ControlDef.Slider("speed", "Speed", 0f, 1f)), info.controls)
    }

    @Test
    fun `parses a joystick's two topics by position`() {
        val info = parse(
            controlsPayload(
                """{"topic": ["drive_x", "drive_y"], "display_name": "Drive", "type": "float", "widget": {"type": "joystick"}}""",
            ),
        )
        assertEquals(listOf(ControlDef.Joystick("drive_x", "drive_y", "Drive")), info.controls)
    }

    @Test
    fun `joystick with only one topic is dropped`() {
        val info = parse(
            controlsPayload(
                """{"topic": ["drive_x"], "display_name": "Drive", "type": "float", "widget": {"type": "joystick"}}""",
            ),
        )
        assertTrue(info.controls.isEmpty())
    }

    @Test
    fun `unknown widget type is dropped instead of failing the parse`() {
        val info = parse(
            controlsPayload(
                """{"topic": ["mystery"], "display_name": "Mystery", "type": "float", "widget": {"type": "dial"}}""",
            ),
        )
        assertTrue(info.controls.isEmpty())
    }

    @Test
    fun `parses numeric readout and led indicator outputs`() {
        val info = parse(
            outputsPayload(
                """{"topic": ["battery"], "display_name": "Battery", "type": "float", "widget": {"type": "numeric_readout"}}""",
                """{"topic": ["status"], "display_name": "Status", "type": "bool", "widget": {"type": "led_indicator"}}""",
            ),
        )
        assertEquals(
            listOf(OutputDef.NumericReadout("battery", "Battery"), OutputDef.LedIndicator("status", "Status")),
            info.outputs,
        )
    }

    @Test
    fun `entry with an empty topic list is dropped`() {
        val info = parse(controlsPayload("""{"topic": [], "display_name": "Lights", "type": "bool", "widget": {"type": "toggle"}}"""))
        assertTrue(info.controls.isEmpty())
        assertNull(info.controls.firstOrNull())
    }

    private fun controlsPayload(vararg controls: String) = """
        {
          "device_id": "esp32-01",
          "device_name": "Test Rover",
          "project_id": "test_project",
          "schema_hash": "abc123",
          "controls": [${controls.joinToString(",")}],
          "outputs": []
        }
    """.trimIndent()

    private fun outputsPayload(vararg outputs: String) = """
        {
          "device_id": "esp32-01",
          "device_name": "Test Rover",
          "project_id": "test_project",
          "schema_hash": "abc123",
          "controls": [],
          "outputs": [${outputs.joinToString(",")}]
        }
    """.trimIndent()
}
