package com.example.cleantrack.util

object ContactSupportConstants {

    // Issue Categories
    const val CATEGORY_APP = "App"
    const val CATEGORY_LOGIN_TECH = "Login & Technical Issues"
    const val CATEGORY_SERVICE = "Service-Related Issues"
    const val CATEGORY_PAYMENT = "Payments & Billing"
    const val CATEGORY_ACCOUNT = "Account & Profile"
    const val CATEGORY_LOCATION = "Location & Map"
    const val CATEGORY_FEEDBACK = "Feedback & Others"

    val ISSUE_CATEGORIES = listOf(
        CATEGORY_APP,
        CATEGORY_LOGIN_TECH,
        CATEGORY_SERVICE,
        CATEGORY_PAYMENT,
        CATEGORY_ACCOUNT,
        CATEGORY_LOCATION,
        CATEGORY_FEEDBACK
    )

    // User Type
    const val USER_REGISTERED = "REGISTERED"
    const val USER_GUEST = "GUEST"
}
