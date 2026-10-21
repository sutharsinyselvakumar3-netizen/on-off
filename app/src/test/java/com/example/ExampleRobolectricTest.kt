package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.LedState
import com.example.network.NodeMcuApiClient
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NodeMCU LED Controller", appName)
  }

  @Test
  fun `led state parsing`() {
    assertEquals(LedState.ON, LedState.fromString("ON"))
    assertEquals(LedState.ON, LedState.fromString("on"))
    assertEquals(LedState.ON, LedState.fromString("1"))
    assertEquals(LedState.OFF, LedState.fromString("OFF"))
    assertEquals(LedState.OFF, LedState.fromString("off"))
    assertEquals(LedState.OFF, LedState.fromString("0"))
    assertEquals(LedState.UNKNOWN, LedState.fromString("UNKNOWN"))
    assertEquals(LedState.UNKNOWN, LedState.fromString(null))
  }

  @Test
  fun `url normalization`() {
    val client = NodeMcuApiClient()
    assertEquals("http://nodemcu-led.local", client.normalizeUrl("nodemcu-led.local"))
    assertEquals("http://nodemcu-led.local", client.normalizeUrl("http://nodemcu-led.local/"))
    assertEquals("http://192.168.1.150", client.normalizeUrl("192.168.1.150"))
    assertEquals("http://192.168.4.1:8080", client.normalizeUrl("192.168.4.1:8080/"))
  }
}
