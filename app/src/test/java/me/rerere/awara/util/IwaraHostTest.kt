package me.rerere.awara.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IwaraHostTest {
    @Test
    fun acceptsApexAndSubdomains() {
        assertTrue(isTrustedIwaraHost("iwara.tv"))
        assertTrue(isTrustedIwaraHost("api.iwara.tv"))
        assertTrue(isTrustedIwaraHost("I.IWARA.TV."))
    }

    @Test
    fun rejectsLookalikeAndUnrelatedDomains() {
        assertFalse(isTrustedIwaraHost("iwara.tv.example.com"))
        assertFalse(isTrustedIwaraHost("fake-iwara.tv"))
        assertFalse(isTrustedIwaraHost("example.com"))
    }
}
