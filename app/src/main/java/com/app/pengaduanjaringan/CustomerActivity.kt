package com.app.pengaduanjaringan

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import com.app.pengaduanjaringan.databinding.ActivityCustomerBinding
import com.app.pengaduanjaringan.pengaduan.PengaduanActivity
import com.app.pengaduanjaringan.user.UserModel
import com.app.pengaduanjaringan.utils.Constant
import com.app.pengaduanjaringan.utils.MainViewmodel
import com.google.firebase.messaging.FirebaseMessaging

class CustomerActivity : AppCompatActivity() {
    val TAG ="CustomerActivityTAG"
    lateinit var binding:ActivityCustomerBinding
    lateinit var myViewModel: MainViewmodel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myViewModel = vita.with(VitaOwner.Multiple(this)).getViewModel<MainViewmodel>()

        myViewModel.getMyProfile(Constant.getIdUser(this)!!)
            .observe(this){
                binding.linearProfil.setOnClickListener (object :View.OnClickListener{
                    override fun onClick(p0: View?) {
                        Constant.showMyProfileDialog(
                            this@CustomerActivity,
                            layoutInflater,
                            it,
                            myViewModel
                        )
                    }
                })
            }
        binding.imageBtnLogout.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setCancelable(true)
            builder.setTitle("Konfirmasi")
            builder.setMessage("Apakah Yakin Akan Logout")
            builder.setPositiveButton(
                "OK"
            ) { dialog, which ->
              Constant.setIdUser(null, this)
                Constant.setRole(0, this)
                startActivity(Intent(this, LoginActivity::class.java))
            }
            builder.setNegativeButton(
                android.R.string.cancel
            ) { dialog, which ->


            }
            val dialog = builder.create()
            dialog.show()
        }

        myViewModel.getDataUser().addSnapshotListener { value, error ->
            if (value != null) {
                for (document in value) {
                    val user = document.toObject(UserModel::class.java)
                    user.id = document.id
                    if (user.id==Constant.getIdUser(this)) {
                        binding.txtUsername.text = "Selamat datang, "+ user.username
                        binding.txtName.text = user.nama
                        binding.txtEmail.text = user.email
                        break
                    }
                }
            }
        }

        FirebaseMessaging.getInstance().unsubscribeFromTopic(getString(R.string.subscribe_key))
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d(TAG, msg)
              //  Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
    }
    var doubleBackToExitPressedOnce = false

    @SuppressLint("NewApi")
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(homeIntent)
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Tap sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    fun toPengaudanCustomer(view: View) {
        startActivity(Intent(this, PengaduanActivity::class.java))
    }
}