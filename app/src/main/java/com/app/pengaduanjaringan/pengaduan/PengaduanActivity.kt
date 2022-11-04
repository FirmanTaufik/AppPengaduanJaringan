package com.app.pengaduanjaringan.pengaduan

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import com.app.pengaduanjaringan.R
import com.app.pengaduanjaringan.databinding.ActivityPengaduanBinding
import com.app.pengaduanjaringan.databinding.DialogDetailPengaduanBinding
import com.app.pengaduanjaringan.databinding.FormAddPengaduanBinding
import com.app.pengaduanjaringan.user.UserModel
import com.app.pengaduanjaringan.utils.Constant
import com.app.pengaduanjaringan.utils.MainViewmodel
import java.util.*


class PengaduanActivity : AppCompatActivity(), PengaduanAdapter.CallBack {
    val TAG ="PengaduanActivityTAG"
    lateinit var binding: ActivityPengaduanBinding
    lateinit var pengaduanAdapter: PengaduanAdapter
    lateinit var myViewModel: MainViewmodel
    lateinit var data:ArrayList<PengaduanModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengaduanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myViewModel = vita.with(VitaOwner.Multiple(this)).getViewModel<MainViewmodel>()
        setSupportActionBar(binding.toolbar)
        binding.recyclerView.layoutManager =LinearLayoutManager(this)
        data= ArrayList()
        val startDate = 946744680000L
        val endDate = 32529128244000L
        myViewModel.getDataPengaduan(startDate, endDate) .observe(this){
            data.clear()
            data.addAll(it)
            pengaduanAdapter = PengaduanAdapter(this, data,myViewModel)
            binding.recyclerView.adapter = pengaduanAdapter
            pengaduanAdapter.setCallBack(this)
            pengaduanAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        if (Constant.getRole(this)==1) {
            menu.findItem(R.id.add).isVisible = false
        }

        if (Constant.getRole(this)==2) {
            menu.findItem(R.id.print).isVisible = false
        }
        menu.findItem(R.id.acion_search).isVisible=false
        menu.findItem(R.id.filter).isVisible = false
        menu.findItem(R.id.print).isVisible = false

        menu.findItem(R.id.search).isVisible = true
        return true
    }

    override fun onClick(position: Int) {
        val d = data[position]
        val sd =   PengaduanModel(
        null,d.idUser,
        d.tanggal,   d.judul,  d.isi, d.alamat,  d.status, true )
        myViewModel.editDataPengaduan(sd, d.id)
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val layoutView  = DialogDetailPengaduanBinding.inflate(layoutInflater)
        dialogBuilder.setView(layoutView.root)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.getWindow()?.getAttributes()?.windowAnimations = R.style.DialogAnimation
        alertDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        layoutView.txtIsi.text = d.isi
        layoutView.txtAlamat.text=d.alamat
        myViewModel. getDataUser().addSnapshotListener { value, error ->
            if (value != null) {
                for (document in value) {
                    val user = document.toObject(UserModel::class.java)
                    user.id = document.id
                    if (user.id==d.idUser) {
                        layoutView.txtName.text =user.nama
                        break
                    }
                }
            }
        }

        layoutView.txtJudul.text =d.judul
        layoutView.txtTanggal.text =Constant.convertToyyyMMdd(d.tanggal!!)
        if (Constant.getRole(this)==2) {
            if (d.status!=0) layoutView.btnMore.visibility= View.GONE

        }else{
            if (d.status==2) layoutView.btnMore.visibility= View.GONE
        }
        layoutView.btnMore.setOnClickListener {
            val popupMenu = PopupMenu(this, layoutView.btnMore)
            if (Constant.getRole(this)==2) {
                popupMenu.menu.add("Edit")
                popupMenu.menu.add("Hapus")
                popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                        alertDialog.dismiss()
                        if (menuItem.getTitle().equals("Edit")) {
                            showDialogEditPengaduan(d)
                        } else {
                            val builder = AlertDialog.Builder(this@PengaduanActivity)
                            builder.setCancelable(true)
                            builder.setTitle("Konfirmasi")
                            builder.setMessage("Apakah Yakin Akan Menghapus")
                            builder.setPositiveButton(
                                "OK"
                            ) { dialog, which ->
                                myViewModel.deletePengaduan(d.id!!)
                                    .addOnSuccessListener {
                                        Constant.showToast(this@PengaduanActivity,"sukses di hapus")
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
            }else{
                popupMenu.menu.add("Proses")
                popupMenu.menu.add("Selesai")
                popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
                        if (menuItem != null) {
                            if (menuItem.getTitle().equals("Proses")) {
                                val pengaduan = PengaduanModel(
                                    null,
                                    Constant.getIdUser(this@PengaduanActivity)!!,
                                    sd.tanggal,
                                    sd.judul,
                                    sd.isi,sd.alamat,1, true
                                )
                                myViewModel.editDataPengaduan(pengaduan, d.id)
                                    .addOnSuccessListener {
                                        alertDialog.cancel()
                                        Constant.showToast(this@PengaduanActivity,"sukses menyimpan")
                                    }
                            } else{

                                val pengaduan = PengaduanModel(
                                    null,
                                    Constant.getIdUser(this@PengaduanActivity)!!,
                                    sd.tanggal,
                                    sd.judul,
                                    sd.isi,sd.alamat,2, true
                                )
                                myViewModel.editDataPengaduan(pengaduan, d.id)
                                    .addOnSuccessListener {
                                        alertDialog.cancel()
                                        Constant.showToast(this@PengaduanActivity,"sukses menyimpan")
                                    }
                            }
                        }
                        return false
                    }

                })

            }

            popupMenu.show()
        }
        alertDialog.show();
    }

    private fun showDialogEditPengaduan(d: PengaduanModel) {
        var dateLongInput : Long ?=d.tanggal
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val layoutView = FormAddPengaduanBinding.inflate(layoutInflater)
        dialogBuilder.setView(layoutView.root)
        layoutView.edtIsi.setText(d.isi)
        layoutView.edtJudul.setText(d.judul)
        layoutView.edtTanggal.setText(Constant.convertToyyyMMdd(d.tanggal!!))
        layoutView.edtAlamat.setText(d.alamat)
        layoutView.edtTanggal.setOnClickListener {

            val calendar: Calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(this,
                { view, year, month, dayOfMonth ->
                    val newDate: Calendar = Calendar.getInstance()
                    newDate.set(year, month, dayOfMonth)
                    dateLongInput = newDate.timeInMillis
                    layoutView.edtTanggal.setText( Constant.convertToyyyMMdd(dateLongInput!!))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        dialogBuilder.setPositiveButton("Simpan", null)
        dialogBuilder.setNegativeButton("Cancel", null)
        val mAlertDialog: AlertDialog = dialogBuilder.create()
        mAlertDialog.setOnShowListener {
            val b: Button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            b.setOnClickListener(View.OnClickListener {
                if (layoutView.edtTanggal.length() == 0) {
                    layoutView.edtTanggal.error = "Tidak boleh kosong"
                    return@OnClickListener
                }
                if (layoutView.edtJudul.length() == 0) {
                    layoutView.edtJudul.error = "Tidak boleh kosong"
                    return@OnClickListener
                }

                if (layoutView.edtIsi.length() == 0) {
                    layoutView.edtIsi.error = "Tidak boleh kosong"
                    return@OnClickListener
                }

                if (layoutView.edtAlamat.length() == 0) {
                    layoutView.edtAlamat.error = "Tidak boleh kosong"
                    return@OnClickListener
                }

                val pengaduan = PengaduanModel(
                    null,
                    Constant.getIdUser(this)!!,
                    dateLongInput!!,
                    layoutView.edtJudul.text.toString(),
                    layoutView.edtIsi.text.toString(),
                    layoutView.edtAlamat.text.toString()

                )
                myViewModel.editDataPengaduan(pengaduan,d.id)
                    .addOnSuccessListener {
                        mAlertDialog.cancel()
                        Constant.showToast(this,"sukses menyimpan")
                    }


            })
        }
        mAlertDialog.setCancelable(false)
        mAlertDialog.show();
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId ==R.id.search) {
            Constant.dateRangePickerShow(this, object :Constant.Companion.Listener{
                override fun onSelected(startDate: Long, endDate: Long) {
                    myViewModel.getDataPengaduan(startDate, endDate) .observe(this@PengaduanActivity){
                        data.clear()
                        data.addAll(it)
                        pengaduanAdapter = PengaduanAdapter(this@PengaduanActivity, data,myViewModel)
                        binding.recyclerView.adapter = pengaduanAdapter
                        pengaduanAdapter.setCallBack(this@PengaduanActivity)
                        pengaduanAdapter.notifyDataSetChanged()
                        binding.toolbar.subtitle= Constant.convertToyyyMMdd(startDate) +" - "+Constant.convertToyyyMMdd(endDate)
                    }
                }
            })
        }
        if (item.itemId==R.id.add) {
            var dateLongInput : Long ?=0
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            val layoutView = FormAddPengaduanBinding.inflate(layoutInflater)
            dialogBuilder.setView(layoutView.root)
            layoutView.edtTanggal.setOnClickListener {

                val calendar: Calendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(this,
                    { view, year, month, dayOfMonth ->
                        val newDate: Calendar = Calendar.getInstance()
                        newDate.set(year, month, dayOfMonth)
                        dateLongInput = newDate.timeInMillis
                        layoutView.edtTanggal.setText( Constant.convertToyyyMMdd(dateLongInput!!))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.show()
            }

            dialogBuilder.setPositiveButton("Simpan", null)
            dialogBuilder.setNegativeButton("Cancel", null)
            val mAlertDialog: AlertDialog = dialogBuilder.create()
            mAlertDialog.setOnShowListener {
                val b: Button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                b.setOnClickListener(View.OnClickListener {
                    if (layoutView.edtTanggal.length() == 0) {
                        layoutView.edtTanggal.error = "Tidak boleh kosong"
                        return@OnClickListener
                    }
                    if (layoutView.edtJudul.length() == 0) {
                        layoutView.edtJudul.error = "Tidak boleh kosong"
                        return@OnClickListener
                    }

                    if (layoutView.edtIsi.length() == 0) {
                        layoutView.edtIsi.error = "Tidak boleh kosong"
                        return@OnClickListener
                    }

                    if (layoutView.edtAlamat.length() == 0) {
                        layoutView.edtAlamat.error = "Tidak boleh kosong"
                        return@OnClickListener
                    }

                    val pengaduan = PengaduanModel(
                        null,
                        Constant.getIdUser(this)!!,
                        dateLongInput!!,
                        layoutView.edtJudul.text.toString(),
                        layoutView.edtIsi.text.toString(),
                        layoutView.edtAlamat.text.toString()

                    )
                    myViewModel.postDataPengaduan(pengaduan)
                        .addOnSuccessListener {
                            Constant.postNotif(this,"Pemberitahuan", "Ada Pengaduan Baru")
                            mAlertDialog.cancel()
                            Constant.showToast(this,"sukses menyimpan")
                        }


                })
            }
            mAlertDialog.setCancelable(false)
            mAlertDialog.show();
        }
        return super.onOptionsItemSelected(item)
    }

}