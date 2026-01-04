package com.allmoviedatabase.movielibrary.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // --- State Management ---
    // Yükleniyor mu? (ProgressBar için)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Kayıt Başarılı mı? (Navigation için)
    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    // Genel Hata Mesajı (Toast için)
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // --- Validation Hataları (Hangi input hatalı?) ---
    private val _validationErrors = MutableLiveData<Map<String, String>>()
    val validationErrors: LiveData<Map<String, String>> get() = _validationErrors

    fun registerUser(
        email: String,
        pass: String,
        confirmPass: String,
        nickname: String,
        birthDate: Long?,
        gender: String,
        isTermsAccepted: Boolean
    ) {
        // 1. Validasyon Kontrolü
        if (!validateInputs(email, pass, confirmPass, nickname, birthDate, isTermsAccepted)) {
            return
        }

        // 2. İşlem Başlıyor
        _isLoading.value = true

        // 3. Firebase Auth
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    saveUserToFirestore(userId, email, nickname,gender, birthDate!!)
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                if (e.message?.contains("email", true) == true) {
                    _validationErrors.value = mapOf("email" to "Bu e-posta zaten kullanımda.")
                } else {
                    _errorMessage.value = e.localizedMessage
                }
            }
    }

    private fun saveUserToFirestore(userId: String, email: String, nickname: String,gender: String, birthDate: Long) {
        val newUser = User(
            userId = userId,
            email = email,
            nickname = nickname,
            gender = gender,
            birthDate = birthDate
        )

        firestore.collection("users").document(userId).set(newUser)
            .addOnSuccessListener {
                sendVerificationMail()
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMessage.value = "Veritabanı hatası: ${e.localizedMessage}"
            }
    }

    private fun sendVerificationMail() {
        auth.currentUser?.sendEmailVerification()
            ?.addOnSuccessListener {
                auth.signOut() // Otomatik girişi engelle
                _isLoading.value = false
                _registerSuccess.value = true // Fragment'a "işlem tamam" sinyali
            }
            ?.addOnFailureListener { e ->
                _isLoading.value = false
                _errorMessage.value = "Mail gönderilemedi: ${e.localizedMessage}"
            }
    }

    private fun validateInputs(
        email: String, pass: String, confirmPass: String,
        nickname: String, birthDate: Long?, isTermsAccepted: Boolean
    ): Boolean {
        val errors = mutableMapOf<String, String>()

        if (nickname.isBlank()) errors["nickname"] = "Takma ad boş olamaz."
        if (birthDate == null) errors["birthDate"] = "Lütfen yaşınızı seçiniz."

        if (email.isBlank()) {
            errors["email"] = "E-posta boş olamaz."
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors["email"] = "Geçerli bir e-posta giriniz."
        }

        val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[-/@#$%^&+=!,._])(?=\\S+$).{6,}$")

        if (pass.isBlank()) {
            errors["password"] = "Şifre boş olamaz."
        } else if (!passwordPattern.matcher(pass).matches()) {
            errors["password"] = "Şifre en az: 1 Büyük, 1 Küçük, 1 Rakam ve 1 Özel Karakter içermelidir."
        }

        if (confirmPass.isBlank()) {
            errors["confirmPass"] = "Şifre tekrarı boş olamaz."
        } else if (pass != confirmPass) {
            errors["confirmPass"] = "Şifreler uyuşmuyor."
        }

        if (!isTermsAccepted) {
            _errorMessage.value = "Lütfen kullanım koşullarını kabul edin."
            return false // Hata map'ine değil, genel toast mesajına gönderdik
        }

        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            return false
        }

        return true
    }
}