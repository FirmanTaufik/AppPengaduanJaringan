package com.app.pengaduanjaringan.user

import android.app.AlertDialog
import android.app.SearchManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import com.app.pengaduanjaringan.R
import com.app.pengaduanjaringan.databinding.ActivityUserBinding
import com.app.pengaduanjaringan.databinding.DialogDetailUserBinding
import com.app.pengaduanjaringan.databinding.FormAddUserBinding
import com.app.pengaduanjaringan.utils.Constant
import com.app.pengaduanjaringan.utils.MainViewmodel


class UserActivity : AppCompatActivity(), UserAdapter.CallBack {
    val TAG = "UserActivityTAG"
    lateinit var binding: ActivityUserBinding
    lateinit var userAdapter: UserAdapter
    lateinit var data: ArrayList<UserModel>
    lateinit var myViewModel: MainViewmodel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        myViewModel = vita.with(VitaOwner.Multiple(this)).getViewModel<MainViewmodel>()
        data = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(data)
        binding.recyclerView.adapter = userAdapter


        userAdapter.setCallBack(this)
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
            userAdapter.notifyDataSetChanged()
        }

    }

    override fun onClick(position: Int) {
        val user = data[position]
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val layoutView: DialogDetailUserBinding = DialogDetailUserBinding.inflate(layoutInflater)
        dialogBuilder.setView(layoutView.root)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.getWindow()?.getAttributes()?.windowAnimations = R.style.DialogAnimation
        alertDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        layoutView.txtName.text = user.nama
        layoutView.txtEmail.text = user.email
        layoutView.txtPassword.text = user.password
        layoutView.txtUsername.text = user.username

        layoutView.btnMore.setOnClickListener {
            val popupMenu = PopupMenu(this, layoutView.btnMore)

            if (user.approved == false)  {
                popupMenu.getMenu().add("Aktifkan")
            }

            popupMenu.getMenu().add("Edit")
            popupMenu.getMenu().add("Hapus")
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                    alertDialog.dismiss()
                    if (menuItem.getTitle().equals("Aktifkan")) {
                        val ue = UserModel(
                            layoutView.txtName.text.toString(),
                            layoutView.txtEmail.text.toString(),
                            layoutView.txtUsername.text.toString(),
                            layoutView.txtPassword.text.toString(),
                            2, null, true
                        )
                        myViewModel.editDataUser(ue, user.id!!).addOnSuccessListener {
                            alertDialog.cancel()
                            alertDialog.dismiss()
                            Constant.showToast(this@UserActivity, "sukses menyimpan")
                        }
                    }else   if (menuItem.getTitle().equals("Edit")) {
                        showDialogEditUser(user)
                    } else {
                        val builder = AlertDialog.Builder(this@UserActivity)
                        builder.setCancelable(true)
                        builder.setTitle("Konfirmasi")
                        builder.setMessage("Apakah Yakin Akan Menghapus")
                        builder.setPositiveButton(
                            "OK"
                        ) { dialog, which ->
                            myViewModel.deleteUser(user.id!!).addOnSuccessListener {
                                Constant.showToast(this@UserActivity, "sukses menghapus")
                            }

                        }
                        builder.setNegativeButton(
                            android.R.string.cancel
                        ) { dialog, which ->


                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
                    return false
                }
            })
        }
        alertDialog.show();
    }

    private fun showDialogEditUser(user: UserModel) {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val layoutView = FormAddUserBinding.inflate(layoutInflater)
        dialogBuilder.setView(layoutView.root)
        layoutView.edtEmail.setText(user.email)
        layoutView.edtName.setText(user.nama)
        layoutView.edtPassword.setText(user.password)
        layoutView.edtUsername.setText(user.username)
        layoutView.edtUsername.isFocusable = false
        layoutView.edtUsername.isEnabled = false

        dialogBuilder.setPositiveButton("Simpan", null)
        dialogBuilder.setNegativeButton("Cancel", null)
        val mAlertDialog: AlertDialog = dialogBuilder.create()
        mAlertDialog.setOnShowListener {
            val b: Button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            b.setOnClickListener(View.OnClickListener {
                if (layoutView.edtPassword.length() == 0) {
                    layoutView.edtPassword.error = "Tidak boleh kosong"
                    return@OnClickListener
                }
                val ue = UserModel(
                    layoutView.edtName.text.toString(),
                    layoutView.edtEmail.text.toString(),
                    layoutView.edtUsername.text.toString(),
                    layoutView.edtPassword.text.toString()
                )
                myViewModel.editDataUser(ue, user.id!!).addOnSuccessListener {
                    mAlertDialog.cancel()
                    Constant.showToast(this, "sukses menyimpan")
                }
            })
        }
        mAlertDialog.setCancelable(false)
        mAlertDialog.show();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        menu.findItem(R.id.print).setVisible(false)
        menu.findItem(R.id.filter).setVisible(false)
        val searchView: SearchView =
            MenuItemCompat.getActionView(menu.findItem(R.id.acion_search)) as SearchView
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setQueryHint("Nama User  ")

        searchView.setSubmitButtonEnabled(true)
        searchView.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                userAdapter.getFilter()?.filter(newText)
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add) {

            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            val layoutView = FormAddUserBinding.inflate(layoutInflater)
            dialogBuilder.setView(layoutView.root)
            dialogBuilder.setPositiveButton("Simpan", null)
            dialogBuilder.setNegativeButton("Cancel", null)
            val mAlertDialog: AlertDialog = dialogBuilder.create()
            mAlertDialog.setOnShowListener {
                val b: Button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                b.setOnClickListener(View.OnClickListener {
                    if (layoutView.edtUsername.length() == 0) {
                        layoutView.edtUsername.error = "Tidak boleh kosong"
                        return@OnClickListener
                    }
                    if (layoutView.edtPassword.length() == 0) {
                        layoutView.edtPassword.error = "Tidak boleh kosong"
                        return@OnClickListener
                    }

                    val isAvailable = myViewModel.checkAvailableUsername(
                        layoutView.edtUsername.text.toString(),
                        data
                    )
                    if (isAvailable) {
                        val ue = UserModel(
                            layoutView.edtName.text.toString(),
                            layoutView.edtEmail.text.toString(),
                            layoutView.edtUsername.text.toString(),
                            layoutView.edtPassword.text.toString(),
                            2
                        )
                        myViewModel.postDataUser(ue).addOnSuccessListener {
                            mAlertDialog.cancel()
                            Constant.showToast(this, "sukses menyimpan")
                        }
                    } else {
                        Constant.showToast(this, "username sudah ada yang pakai")
                    }


                })
            }
            mAlertDialog.setCancelable(false)
            mAlertDialog.show();
        }
        return super.onOptionsItemSelected(item)
    }
}