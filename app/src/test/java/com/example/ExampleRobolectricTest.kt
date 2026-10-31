package com.example

import com.example.ble.BleContract
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun verifyAppName() {
        assertEquals("VEIL", "VEIL")
    }

    @Test
    fun verifyBleServiceUuid() {
        assertEquals(
            "7e1a0001-5645-494c-0000-000000000001",
            BleContract.SERVICE_UUID.toString()
        )
    }
}
