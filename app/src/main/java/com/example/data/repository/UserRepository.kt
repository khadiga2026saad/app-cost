package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entities.UserEntity
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Not connected to LoginScreen; registration/authentication policy is deferred. */
class UserRepository(private val dao: UserDao) {
    suspend fun findUser(username: String): UserEntity? = dao.findUser(username)

    suspend fun createUser(username: String, email: String, password: CharArray): Long {
        require(username.isNotBlank() && email.isNotBlank() && password.isNotEmpty())
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val spec = PBEKeySpec(password, saltBytes, 210_000, 256)
        try {
            val hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            return dao.insert(
                UserEntity(
                    username = username.trim(),
                    email = email.trim(),
                    passwordHash = Base64.getEncoder().encodeToString(hash),
                    salt = Base64.getEncoder().encodeToString(saltBytes),
                    createdAt = System.currentTimeMillis()
                )
            )
        } finally {
            spec.clearPassword()
            password.fill(0.toChar())
        }
    }
}
