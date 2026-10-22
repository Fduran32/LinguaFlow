package com.linguaflow.app.domain.util

object AuthValidator {

    fun validateUsername(username: String): String? {
        val clean = username.trim()
        if (clean.length < 3) {
            return "El nombre de usuario debe tener al menos 3 caracteres."
        }
        if (clean.length > 25) {
            return "El nombre de usuario no puede exceder 25 caracteres."
        }
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.length < 6) {
            return "La contraseña debe tener al menos 6 caracteres."
        }
        if (!password.any { it.isUpperCase() }) {
            return "Debe contener al menos una letra mayúscula (A-Z)."
        }
        if (!password.any { it.isLowerCase() }) {
            return "Debe contener letras minúsculas (a-z)."
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            return "Debe incluir al menos un carácter especial (ej. @, #, $, %, !, &)."
        }
        return null
    }

    data class PasswordCriteria(
        val hasMinLength: Boolean,
        val hasUpperCase: Boolean,
        val hasLowerCase: Boolean,
        val hasSpecialChar: Boolean
    ) {
        val isValid: Boolean get() = hasMinLength && hasUpperCase && hasLowerCase && hasSpecialChar
    }

    fun checkPasswordCriteria(password: String): PasswordCriteria {
        return PasswordCriteria(
            hasMinLength = password.length >= 6,
            hasUpperCase = password.any { it.isUpperCase() },
            hasLowerCase = password.any { it.isLowerCase() },
            hasSpecialChar = password.any { !it.isLetterOrDigit() }
        )
    }
}
