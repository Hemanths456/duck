package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PreferencesManager
import com.example.game.GameConstants
import com.example.model.Duck
import com.example.model.FlightDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals("Duck Shooter", appName)
    }

    @Test
    fun `preferences manager saves and retrieves best score`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)
        prefs.bestScore = 42
        assertEquals(42, prefs.bestScore)

        prefs.isSoundEnabled = false
        assertFalse(prefs.isSoundEnabled)
        prefs.isSoundEnabled = true
        assertTrue(prefs.isSoundEnabled)
    }

    @Test
    fun `duck hit detection works within tolerance`() {
        val duck = Duck(
            id = 1L,
            x = 200f,
            y = 300f,
            speedX = 150f,
            baseY = 300f,
            direction = FlightDirection.LEFT_TO_RIGHT,
            width = 100f,
            height = 80f
        )

        // Direct center hit
        assertTrue(duck.containsPoint(200f, 300f, GameConstants.TOUCH_HIT_TOLERANCE))

        // Near edge within tolerance
        assertTrue(duck.containsPoint(240f, 320f, GameConstants.TOUCH_HIT_TOLERANCE))

        // Far away miss
        assertFalse(duck.containsPoint(500f, 500f, GameConstants.TOUCH_HIT_TOLERANCE))

        // Once hit, cannot be hit again
        duck.isHit = true
        assertFalse(duck.containsPoint(200f, 300f, GameConstants.TOUCH_HIT_TOLERANCE))
    }
}
