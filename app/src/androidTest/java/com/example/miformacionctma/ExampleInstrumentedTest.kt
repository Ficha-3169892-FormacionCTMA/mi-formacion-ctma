package com.example.miformacionctma

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        // Cada sabor agrega su sufijo (.dev, .stage), por eso se compara con el applicationId generado
        assertEquals(BuildConfig.APPLICATION_ID, appContext.packageName)
        assertTrue(appContext.packageName.startsWith("com.example.miformacionctma"))
    }
}