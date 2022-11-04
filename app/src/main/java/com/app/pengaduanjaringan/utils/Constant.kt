package com.app.pengaduanjaringan.utils

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.app.pengaduanjaringan.R
import com.app.pengaduanjaringan.databinding.DialogDetailUserBinding
import com.app.pengaduanjaringan.databinding.FormAddUserBinding
import com.app.pengaduanjaringan.services.model.ModelBodyFcmTo
import com.app.pengaduanjaringan.services.model.ModelNotification
import com.app.pengaduanjaringan.user.UserModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat


class Constant {
     companion object {
         val TAG = "ConstantTAG"
         lateinit var mySharedPreferences: SharedPreferences
        private val PREF = "pref"
         private val idUser = "idUser"
         private  val role ="role"

         fun setRole(r :Int, context:Context) {
             mySharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
             val myEditor: SharedPreferences.Editor = mySharedPreferences.edit()
             myEditor.putInt(role, r)
             myEditor.commit()
             myEditor.apply()
         }

         fun getRole(context: Context): Int? {
             mySharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
             return mySharedPreferences.getInt(role, 0)
         }

         fun setIdUser(id: String?, context:Context) {
             mySharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
             val myEditor: SharedPreferences.Editor = mySharedPreferences.edit()
             myEditor.putString(idUser, id)
             myEditor.commit()
             myEditor.apply()
         }

         fun getIdUser(context: Context): String? {
             mySharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
             return mySharedPreferences.getString(idUser, null)
         }


         fun showToast(contex :Context, msg :String){
             Toast.makeText(contex, msg,Toast.LENGTH_SHORT ).show()
         }

         fun convertToyyyMMdd(date: Long): String? {
             val dateFormater = SimpleDateFormat("yyyy-MM-dd")
             return dateFormater.format(date)
         }

         fun showMyProfileDialog(
             context: Context,
             layoutInflater: LayoutInflater,
             user: UserModel,
             myViewModel: MainViewmodel
         ){
             val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
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
                 val popupMenu = PopupMenu(context, layoutView.btnMore)
                 popupMenu.getMenu().add("Edit")
                 popupMenu.show()
                 popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                     override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                         alertDialog.dismiss()
                         if (menuItem.getTitle().equals("Edit")) {
                             showDialogEditUser(context,  layoutInflater,user,myViewModel)
                         }
                         return false
                     }
                 })
             }
             alertDialog.show()
         }

         private fun showDialogEditUser(
             context: Context,
             layoutInflater: LayoutInflater,
             user: UserModel,
             myViewModel: MainViewmodel
         ) {

             val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
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
                         Constant.showToast(context, "sukses menyimpan")
                     }
                 })
             }
             mAlertDialog.setCancelable(false)
             mAlertDialog.show();
         }

         fun postNotif(context: Context, title :String, body:String){
             val notif = ModelNotification(title, body)
             val modelBody = ModelBodyFcmTo("/topics/${context.getString(R.string.subscribe_key)}", notif)
             Volley.newRequestQueue(context).add(object : StringRequest(
                 Method.POST, "https://fcm.googleapis.com/fcm/send",
                 object :Response.Listener<String>{
                     override fun onResponse(response: String?) {
                         Log.d(TAG, "onResponse: "+response)
                     }
                 },
                 object :Response.ErrorListener{
                     override fun onErrorResponse(error: VolleyError?) {

                     }
                 }) {

                 @Throws(AuthFailureError::class)
                 override fun getBody(): ByteArray? {
                     return try {
                         val jsonBody = Gson().toJson(modelBody).replace("\\u003d", "=")
                         Log.d("NP", "JSONBODY:$jsonBody")
                         jsonBody.toByteArray(charset("utf-8"))
                     } catch (e: UnsupportedEncodingException) {
                         null
                     }
                 }

                 override fun getHeaders(): MutableMap<String, String> {
                     val params: MutableMap<String, String> = HashMap()
                     params["Content-Type"] = "application/json"
                     params["Authorization"] = "key=" + context.getString(R.string.fcm_key)
                     return params
                 }

                 override fun getPostBodyContentType(): String {
                     return "application/x-www-form-urlencoded; charset=UTF-8";
                 }

                 override fun getParams(): MutableMap<String, String>? {
                     val params: MutableMap<String, String> = HashMap()
                     return params
                 }
             } ).retryPolicy = DefaultRetryPolicy( 70000,
                 70000,
                 DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

         }


         interface Listener {
             fun onSelected(startDate: Long, endDate: Long)
         }

         fun dateRangePickerShow(
             activity: FragmentActivity,
             listener: Listener
         ) {
             val materialDateBuilder =
                 MaterialDatePicker.Builder.dateRangePicker()
             materialDateBuilder.setTheme(R.style.MaterialCalendarTheme)
             materialDateBuilder.setTitleText("Pilih Tanggal")
             val materialDatePicker  = materialDateBuilder.build()
             if (!materialDatePicker.isAdded) materialDatePicker.show(
                 activity.supportFragmentManager,
                 "MATERIAL_DATE_PICKER"
             )
             materialDatePicker.addOnPositiveButtonClickListener {
                 val startDate = it.first
                 val endDate  = it.second
           //      edtTanggal.setText(materialDatePicker.getHeaderText());
                 listener.onSelected(startDate, endDate);
             }
         }
     }


}