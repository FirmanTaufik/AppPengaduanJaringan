package com.app.pengaduanjaringan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import com.app.pengaduanjaringan.databinding.ActivityLoginBinding
import com.app.pengaduanjaringan.user.UserModel
import com.app.pengaduanjaringan.utils.Constant
import com.app.pengaduanjaringan.utils.MainViewmodel

class LoginActivity : AppCompatActivity() {
    lateinit var binding :ActivityLoginBinding
    lateinit var myViewModel: MainViewmodel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myViewModel = vita.with(VitaOwner.Multiple(this)).getViewModel()
        if (Constant.getIdUser(this)!=null) {
            if (Constant.getRole(this)==1) {
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, CustomerActivity::class.java))
            }
        }

        binding.btnLogin.setOnClickListener {
            if (binding.etUsername.text.isEmpty()) {
                binding.etUsername.error ="username kosong"
                return@setOnClickListener
            }
            myViewModel.getDataUser().addSnapshotListener { value, error ->
                if (value != null) {
                    for (document in value) {
                        val user = document.toObject(UserModel::class.java)
                        user.id = document.id

                        if (user.username == binding.etUsername.text.toString() &&
                            user.password == binding.etPassword.text.toString()) {
                            if (user.approved == true) {
                                Constant.setIdUser(user.id!!, this)
                                Constant.setRole(user.role!!, this)
                                if (user.role==1) {
                                    startActivity(Intent(this, MainActivity::class.java))
                                }else{
                                    startActivity(Intent(this, CustomerActivity::class.java))
                                }
                            }else{
                                Constant.showToast(this,"akun belum di setujui admin")

                            }

                            return@addSnapshotListener
                        }
                    }
                }
                Constant.showToast(this,"username atau password salah")
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}