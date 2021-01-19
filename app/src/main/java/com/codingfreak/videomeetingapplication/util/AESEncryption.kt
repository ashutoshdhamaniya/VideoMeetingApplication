package com.codingfreak.videomeetingapplication.util

import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AESEncryption {

    fun encrypt (password : String) : String {
        val key : SecretKeySpec = generateKey("@#$%^&*(&^%$##$%^&**&^%$#@#$%^&**&^%$#@")

        val cipher : Cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE , key)

        val encryptValue = cipher.doFinal(password.toByteArray())

        return Base64.encodeToString(encryptValue , Base64.DEFAULT)
    }

    fun decrypt (value : String) : String {
        val key : SecretKeySpec = generateKey("@#$%^&*(&^%$##$%^&**&^%$#@#$%^&**&^%$#@")

        val cipher : Cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE , key)
        val decodeVal : ByteArray = Base64.decode(value , Base64.DEFAULT)
        val decVal : ByteArray = cipher.doFinal(decodeVal)

        return String(decVal)
    }

    private fun generateKey (key : String) : SecretKeySpec {
        val digest : MessageDigest = MessageDigest.getInstance("SHA-256")
        val bytes : ByteArray = key.toByteArray(StandardCharsets.UTF_8)
        digest.update(bytes , 0 , bytes.size)
        val newKey : ByteArray = digest.digest()
        return SecretKeySpec(newKey , "AES")
    }

}