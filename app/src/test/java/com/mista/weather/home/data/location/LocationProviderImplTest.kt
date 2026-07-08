package com.mista.weather.home.data.location

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LocationProviderImplTest {

    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(ContextCompat::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun `returns null without touching the system location service when permission is not granted`() = runTest {
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED

        val provider = LocationProviderImpl(context)

        val result = provider.getCurrentLocation()

        assertNull(result)
        verify(exactly = 0) { context.getSystemService(Context.LOCATION_SERVICE) }
    }
}
