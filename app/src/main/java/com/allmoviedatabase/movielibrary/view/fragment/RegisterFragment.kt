package com.allmoviedatabase.movielibrary.view.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.databinding.FragmentRegisterBinding
import com.allmoviedatabase.movielibrary.viewmodel.RegisterViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // ViewModel bağlantısı (Hilt ile otomatik inject edilir)
    private val viewModel: RegisterViewModel by viewModels()

    // Doğum tarihini veritabanına göndermek için tuttuğumuz değişken
    private var selectedBirthDateMillis: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // --- 1. OTOMATİK HATA SİLME (UX) ---
        // Kullanıcı yazmaya başladığı an kırmızı hata kalkar
        clearErrorOnType(binding.etNickname, binding.ilNickname)
        clearErrorOnType(binding.etRegEmail, binding.ilRegEmail)
        clearErrorOnType(binding.etRegPassword, binding.ilRegPassword)
        clearErrorOnType(binding.etRegPasswordConfirm, binding.ilRegPasswordConfirm)

        // --- 2. TIKLAMA OLAYLARI ---

        // Doğum Tarihi Seçimi
        binding.etBirthDate.setOnClickListener {
            showDatePicker()
        }

        // Kayıt Ol Butonu
        binding.btnRegister.setOnClickListener {
            val selectedGenderId = binding.rgGender.checkedRadioButtonId
            var gender = ""

            if (selectedGenderId != -1) {
                val selectedRadioButton = binding.root.findViewById<RadioButton>(selectedGenderId)
                gender = selectedRadioButton.text.toString() // "Erkek" veya "Kadın" döner
            } else {
                // Hiçbiri seçilmemişse kullanıcıyı uyarabilirsin
                Toast.makeText(context, "Lütfen cinsiyet seçiniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.registerUser(
                email = binding.etRegEmail.text.toString().trim(),
                pass = binding.etRegPassword.text.toString().trim(),
                confirmPass = binding.etRegPasswordConfirm.text.toString().trim(),
                nickname = binding.etNickname.text.toString().trim(),
                birthDate = selectedBirthDateMillis,
                gender = gender,
                isTermsAccepted = binding.cbTerms.isChecked
            )
        }

        // Giriş Ekranına Dön
        binding.tvGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun observeViewModel() {
        // 1. Validation Hatalarını Dinle (Hangi kutucuk hatalıysa altına yazar)
        viewModel.validationErrors.observe(viewLifecycleOwner) { errors ->
            // Önce temizle ki eski hatalar kalmasın (gerçi yazınca siliyoruz ama güvenlik olsun)
            clearAllErrors()

            errors.forEach { (field, message) ->
                when (field) {
                    "nickname" -> setError(binding.ilNickname, message)
                    "birthDate" -> setError(binding.ilBirthDate, message)
                    "email" -> setError(binding.ilRegEmail, message)
                    "password" -> setError(binding.ilRegPassword, message)
                    "confirmPass" -> setError(binding.ilRegPasswordConfirm, message)
                }
            }
        }

        // 2. Genel Hata Mesajlarını Dinle (Toast olarak gösterilecekler)
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // 3. Başarılı Kayıt Durumunu Dinle
        viewModel.registerSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Kayıt Başarılı! Doğrulama maili gönderildi.", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }

        // 4. Yükleniyor Durumu (Butonu pasife çekip metnini değiştiriyoruz)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnRegister.isEnabled = !isLoading
            binding.btnRegister.text = if (isLoading) "KAYIT YAPILIYOR..." else "KAYIT OL"
        }
    }

    // --- YARDIMCI FONKSİYONLAR ---

    // DatePicker açar, tarihi alır, değişkene atar ve inputa yazar
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        // UX: Varsayılan olarak 18 yıl öncesini aç
        calendar.add(Calendar.YEAR, -18)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                // 1. Veritabanı için milisaniye değerini kaydet
                selectedBirthDateMillis = selectedDate.timeInMillis

                // 2. Ekrana düzgün formatta yaz
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etBirthDate.setText(format.format(selectedDate.time))

                // 3. Tarih seçildiyse varsa hatayı sil
                binding.ilBirthDate.error = null
                binding.ilBirthDate.isErrorEnabled = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Gelecek tarih seçilmesin
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    // EditText'e yazı yazılmaya başlandığı an hatayı silen fonksiyon
    private fun clearErrorOnType(editText: TextInputEditText, inputLayout: TextInputLayout) {
        editText.doOnTextChanged { text, _, _, _ ->
            if (text != null && inputLayout.isErrorEnabled) {
                inputLayout.error = null
                inputLayout.isErrorEnabled = false
            }
        }
    }

    // Hatayı set ederken animasyonlu görünüm için errorEnabled açar
    private fun setError(inputLayout: TextInputLayout, message: String) {
        inputLayout.isErrorEnabled = true
        inputLayout.error = message
    }

    // Tüm hataları manuel temizlemek için (Butona basıldığında gerekebilir)
    private fun clearAllErrors() {
        binding.ilNickname.error = null
        binding.ilBirthDate.error = null
        binding.ilRegEmail.error = null
        binding.ilRegPassword.error = null
        binding.ilRegPasswordConfirm.error = null

        binding.ilNickname.isErrorEnabled = false
        binding.ilBirthDate.isErrorEnabled = false
        binding.ilRegEmail.isErrorEnabled = false
        binding.ilRegPassword.isErrorEnabled = false
        binding.ilRegPasswordConfirm.isErrorEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}