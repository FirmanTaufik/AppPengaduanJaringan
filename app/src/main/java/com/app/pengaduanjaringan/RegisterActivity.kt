package com.app.pengaduanjaringan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import com.app.pengaduanjaringan.databinding.ActivityRegisterBinding
import com.app.pengaduanjaringan.user.UserModel
import com.app.pengaduanjaringan.utils.Constant
import com.app.pengaduanjaringan.utils.MainViewmodel

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    lateinit var myViewModel: MainViewmodel
    lateinit var data: ArrayList<UserModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data = ArrayList()
        myViewModel = vita.with(VitaOwner.Multiple(this)).getViewModel()
        myViewModel.getDataUser().addSnapshotListener { value, error ->
            if (value != null) {
                data.clear()
                for (document in value) {
                    val user = document.toObject(UserModel::class.java)
                    user.id = document.id
                    if (user.role==2){
                        data.add(user)
                    }
                }
            }
        }
        binding.btnRegister.setOnClickListener {
            saveDatabase()
        }
    }

    private fun saveDatabase() {
        if (binding.etName.length() == 0) {
            binding.etName.error = "Tidak boleh kosong"
            return
        }

        if (binding.etUsername.length() == 0) {
            binding.etUsername.error = "Tidak boleh kosong"
            return
        }
        if (binding.etPassword.length() == 0) {
            binding.etPassword.error = "Tidak boleh kosong"
            return
        }

        if (binding.etEmail.length() == 0) {
            binding.etEmail.error = "Tidak boleh kosong"
            return
        }

        val isAvailable = myViewModel.checkAvailableUsername(
            binding.etUsername.text.toString(),
            data
        )
        if (isAvailable) {
            val ue = UserModel(
                binding.etName.text.toString(),
                binding.etEmail.text.toString(),
                binding.etUsername.text.toString(),
                binding.etPassword.text.toString(),
                2,null, false
            )
            myViewModel.postDataUser(ue).addOnSuccessListener {
                Constant.showToast(this, "sukses register")
                finish()
            }
        } else {
            Constant.showToast(this, "username sudah ada yang pakai")
        }
    }
}