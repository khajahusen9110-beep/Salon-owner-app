package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SalonNotification
import com.example.data.model.SalonReview
import com.example.data.model.StaffEarningsSummary
import com.example.util.SalonStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SalonPart5RobolectricTest {

    @Test
    fun testLocalizationStringsForPart5() {
        // Test English strings
        assertEquals("Kamai & Stylist Earnings", SalonStrings.get("en", "earnings_title"))
        assertEquals("Salon Reviews & Feedback", SalonStrings.get("en", "reviews_title"))
        assertEquals("Notifications", SalonStrings.get("en", "notifications_title"))
        assertEquals("Mark All as Read", SalonStrings.get("en", "mark_all_read"))

        // Test Hindi strings
        assertEquals("कमाई और स्टाइलिस्ट विवरण", SalonStrings.get("hi", "earnings_title"))
        assertEquals("सैलून समीक्षाएं और फीडबैक", SalonStrings.get("hi", "reviews_title"))
        assertEquals("सूचनाएं", SalonStrings.get("hi", "notifications_title"))
        assertEquals("सभी को पढ़ा हुआ मार्क करें", SalonStrings.get("hi", "mark_all_read"))
    }


    @Test
    fun testEarningsCalculations() {
        val summaries = listOf(
            StaffEarningsSummary(
                staffName = "Aarav Sharma",
                completedCount = 8,
                revenue = 4200.0,
                commissionPercent = 20.0,
                commissionAmount = 840.0,
                noShowCount = 1,
                cancelledCount = 0
            ),
            StaffEarningsSummary(
                staffName = "Priya Patel",
                completedCount = 6,
                revenue = 3600.0,
                commissionPercent = 25.0,
                commissionAmount = 900.0,
                noShowCount = 0,
                cancelledCount = 1
            )
        )


        val totalRevenue = summaries.sumOf { it.revenue }
        val totalCompleted = summaries.sumOf { it.completedCount }
        val totalCommission = summaries.sumOf { it.commissionAmount }
        val totalNoShows = summaries.sumOf { it.noShowCount }

        assertEquals(7800.0, totalRevenue, 0.01)
        assertEquals(14, totalCompleted)
        assertEquals(1740.0, totalCommission, 0.01)
        assertEquals(1, totalNoShows)
    }

    @Test
    fun testSalonReviewModelAndReplyStatus() {
        val reviewWithReply = SalonReview(
            id = "r1",
            salonId = "salon-1",
            customerName = "Rahul Verma",
            rating = 5.0,
            comment = "Exceptional haircut!",
            ownerReply = "Thank you Rahul! Looking forward to your next visit."
        )

        val reviewPendingReply = SalonReview(
            id = "r2",
            salonId = "salon-1",
            customerName = "Anita Singh",
            rating = 4.0,
            comment = "Great service, slight wait time.",
            ownerReply = null
        )

        assertNotNull(reviewWithReply.ownerReply)
        assertFalse(reviewWithReply.ownerReply.isNullOrBlank())

        assertTrue(reviewPendingReply.ownerReply.isNullOrBlank())
    }

    @Test
    fun testNotificationsModelAndReadState() {
        val unreadNotification = SalonNotification(
            id = "n1",
            userId = "u1",
            type = "new_booking",
            title = "New Booking Received",
            body = "Sneha Kapur booked Hair Spa for tomorrow 11:00 AM",
            isRead = false
        )

        val readNotification = unreadNotification.copy(isRead = true)

        assertFalse(unreadNotification.isRead)
        assertTrue(readNotification.isRead)
        assertEquals("new_booking", unreadNotification.type)
    }
}
