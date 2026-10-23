package com.linguaflow.app

import com.linguaflow.app.domain.util.AuthValidator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun testUsernameValidation() {
        assertNotNull("Menos de 3 caracteres debe fallar", AuthValidator.validateUsername("ab"))
        assertNull("3 caracteres válidos debe pasar", AuthValidator.validateUsername("abc"))
        assertNull("Nombre normal debe pasar", AuthValidator.validateUsername("FelipeDuran"))
        assertNotNull("Más de 25 caracteres debe fallar", AuthValidator.validateUsername("a".repeat(26)))
    }

    @Test
    fun testPasswordValidationRequirements() {
        // Contraseña corta
        assertNotNull(AuthValidator.validatePassword("Ab1!"))

        // Sin mayúscula
        assertNotNull(AuthValidator.validatePassword("password123!"))

        // Sin minúscula
        assertNotNull(AuthValidator.validatePassword("PASSWORD123!"))

        // Sin carácter especial
        assertNotNull(AuthValidator.validatePassword("Password123"))

        // Válida con todos los criterios
        assertNull(AuthValidator.validatePassword("LinguaFlow2026!"))
    }

    @Test
    fun testPasswordCriteriaBreakdown() {
        val criteria = AuthValidator.checkPasswordCriteria("Flow2026!")
        assertTrue(criteria.hasMinLength)
        assertTrue(criteria.hasUpperCase)
        assertTrue(criteria.hasLowerCase)
        assertTrue(criteria.hasSpecialChar)
        assertTrue(criteria.isValid)

        val invalidCriteria = AuthValidator.checkPasswordCriteria("simple")
        assertTrue(invalidCriteria.hasMinLength)
        assertFalse(invalidCriteria.hasUpperCase)
        assertTrue(invalidCriteria.hasLowerCase)
        assertFalse(invalidCriteria.hasSpecialChar)
        assertFalse(invalidCriteria.isValid)
    }
}
