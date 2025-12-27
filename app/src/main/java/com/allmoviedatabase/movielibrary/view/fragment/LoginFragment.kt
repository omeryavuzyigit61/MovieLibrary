package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        // --- OTOMATİK GİRİŞ KONTROLÜ ---
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Kullanıcıyı yenile (mail onayı sonradan yapılmış olabilir)
            currentUser.reload().addOnCompleteListener {
                if (currentUser.isEmailVerified) {
                    // Mail onaylı, direkt ana sayfaya git
                    navigateToMovieList()
                } else {
                    // Oturum açık ama mail onaysız, kullanıcıyı uyar ve çıkış yap
                    // Toast.makeText(requireContext(), "Lütfen önce mail adresinizi doğrulayın.", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
        }
        // --------------------------------

        // Giriş Yap Butonu
        binding.btnLogin.setOnClickListener {
            if (validateLoginInputs()) {
                performLogin()
            }
        }

        // Kayıt Ol Ekranına Git
        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validateLoginInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.ilEmail.error = null
        binding.ilPassword.error = null

        if (email.isEmpty()) {
            binding.ilEmail.error = "E-posta giriniz."
            return false
        }
        if (password.isEmpty()) {
            binding.ilPassword.error = "Şifre giriniz."
            return false
        }
        return true
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null && user.isEmailVerified) {
                    // Başarılı ve Onaylı
                    navigateToMovieList()
                } else {
                    // Şifre doğru ama mail onaylanmamış
                    Toast.makeText(requireContext(), "Giriş yapabilmek için e-posta adresinizi doğrulamanız gerekmektedir.", Toast.LENGTH_LONG).show()
                    auth.signOut() // Güvenlik için tekrar atıyoruz
                }
            }
            .addOnFailureListener { exception ->
                // Hatalı şifre veya kullanıcı yok
                Toast.makeText(requireContext(), "Giriş Başarısız: Bilgilerinizi kontrol edin.", Toast.LENGTH_LONG).show()
            }
    }

    private fun navigateToMovieList() {
        findNavController().navigate(R.id.action_loginFragment_to_movieListFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}