package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SensitivityLevel
import com.example.data.repository.ProtectionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TouchGuard", appName)
  }

  @Test
  fun `test protection repository settings and incident recording`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = ProtectionRepository.getInstance(context)

    repo.updateSettings { it.copy(sensitivity = SensitivityLevel.HIGH, actionCloseAppReturnHome = true) }
    val currentSettings = repo.settings.value
    assertEquals(SensitivityLevel.HIGH, currentSettings.sensitivity)
    assertTrue(currentSettings.actionCloseAppReturnHome)

    val id = repo.recordIncident(
        reason = "Test Motion Trigger",
        sensorReading = "ΔAcc: 3.5 m/s²",
        actionTaken = "Closed Running App & Returned to Home",
        alarmTriggered = false
    )
    assertTrue(id > 0)

    val count = repo.incidentCount.first()
    assertTrue(count >= 1)
  }
}
