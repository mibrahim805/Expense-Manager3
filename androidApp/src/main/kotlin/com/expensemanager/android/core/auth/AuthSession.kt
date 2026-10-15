package com.expensemanager.android.core.auth

data class AuthUser(
    val id: Long,
    val username: String,
    val email: String,
)

data class AuthSession(
    val token: String,
    val user: AuthUser,
)

data class PasswordResetLink(val uid: String, val token: String)
