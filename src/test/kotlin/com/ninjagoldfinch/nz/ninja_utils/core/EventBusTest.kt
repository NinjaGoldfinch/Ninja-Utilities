package com.ninjagoldfinch.nz.ninja_utils.core

import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests the internal event bus publish-subscribe system.
 *
 * Tests verify the bus's behavioral contract:
 * - Events reach matching subscribers
 * - Type filtering works correctly
 * - Errors in one handler don't break others
 *
 * Each test uses unique event types to avoid cross-test interference,
 * since EventBus is a global singleton with no public clear method.
 */
class EventBusTest {

    // Unique event types per test group to avoid interference
    data class DeliveryEvent(val value: String)
    data class MultiSubEvent(val value: String)
    data class NoSubEvent(val value: String)
    data class FilterA(val value: String)
    data class FilterB(val number: Int)
    data class ErrorEvent1(val value: String)

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    @Nested
    inner class BasicDelivery {
        @Test
        fun `subscriber receives posted event`() {
            val received = mutableListOf<DeliveryEvent>()
            EventBus.subscribe<DeliveryEvent> { received.add(it) }
            EventBus.post(DeliveryEvent("hello"))
            assertEquals(1, received.size)
            assertEquals("hello", received[0].value)
        }

        @Test
        fun `multiple subscribers all receive event`() {
            val received = mutableListOf<MultiSubEvent>()
            EventBus.subscribe<MultiSubEvent> { received.add(it) }
            EventBus.subscribe<MultiSubEvent> { received.add(it) }
            EventBus.post(MultiSubEvent("test"))
            assertEquals(2, received.size)
        }

        @Test
        fun `no subscribers does not throw`() {
            EventBus.post(NoSubEvent("nobody listening"))
        }
    }

    @Nested
    inner class TypeFiltering {
        @Test
        fun `subscriber only receives matching event type`() {
            val receivedA = mutableListOf<FilterA>()
            val receivedB = mutableListOf<FilterB>()
            EventBus.subscribe<FilterA> { receivedA.add(it) }
            EventBus.subscribe<FilterB> { receivedB.add(it) }

            EventBus.post(FilterA("a"))
            assertEquals(1, receivedA.size)
            assertTrue(receivedB.isEmpty())

            EventBus.post(FilterB(42))
            assertEquals(1, receivedA.size)
            assertEquals(1, receivedB.size)
        }
    }

    @Nested
    inner class ErrorHandling {
        @Test
        fun `error in one handler does not prevent others from running`() {
            var secondHandlerRan = false
            EventBus.subscribe<ErrorEvent1> { throw RuntimeException("oops") }
            EventBus.subscribe<ErrorEvent1> { secondHandlerRan = true }
            EventBus.post(ErrorEvent1("test"))
            assertTrue(secondHandlerRan)
        }
    }
}
