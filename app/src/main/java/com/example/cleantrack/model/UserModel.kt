package com.example.cleantrack.model

data class UserModel(
    val email: String = "",
    val fullname: String = "",
    val number: String = "",
    val role: String = "USER",
    val userId: String = "",
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "email" to email,
            "fullname" to fullname,
            "number" to number,
            "role" to role,
            "userId" to userId,
        )

    }
}
