package com.app.pengaduanjaringan.pengaduan

import android.content.Context
import android.content.Intent
import android.graphics.Color.green
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import com.app.pengaduanjaringan.R
import com.app.pengaduanjaringan.databinding.ListItemPengaduanBinding
import com.app.pengaduanjaringan.pengaduan.PengaduanModel
import com.app.pengaduanjaringan.user.UserModel
import com.app.pengaduanjaringan.utils.Constant
import com.app.pengaduanjaringan.utils.MainViewmodel

class PengaduanAdapter(
    val context: Context,
    var data: ArrayList<PengaduanModel>,
    var myViewModel: MainViewmodel
):
    RecyclerView.Adapter<PengaduanAdapter.ViewHolder>() {
    val dataListfull = data
    private val TAG = "PengaduanAdapterTAG"

    private var mCallBack: CallBack? = null
    interface CallBack {
        fun onClick(position: Int )
    }

    fun setCallBack(callBack: CallBack) {
        mCallBack = callBack
    }

    fun getFilter(): Filter? {
        return object : Filter() {
            protected override fun performFiltering(charSequence: CharSequence): FilterResults? {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    data = dataListfull
                } else {
                    val filteredList  : ArrayList<PengaduanModel> = ArrayList()
                    if (dataListfull != null) {
                        for (row in dataListfull) {
                            if (row.judul?.toLowerCase()!!.contains(charString)
                                || row.judul!!.startsWith(charString)
                                || row.judul!!.toUpperCase().contains(charString)
                                || row.judul!!.contains(charString)) {
                                filteredList.add(row)
                            }
                        }
                    }
                    data = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = data
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                data = filterResults.values as ArrayList<PengaduanModel>
                notifyDataSetChanged()
            }
        }
    }

    inner  class ViewHolder(val binding: ListItemPengaduanBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PengaduanAdapter.ViewHolder {
        val binding = ListItemPengaduanBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(data[position]){
                binding.txtTanggal.text = this.tanggal?.let { Constant.convertToyyyMMdd(it) }
                binding.txtJudul.text = this.judul
                if (Constant.getRole(context)==1) {
                    if (!this.isRead) binding.image.visibility=View.VISIBLE
                }

                when (this.status) {
                    0 ->{
                        binding.txtStatus.text ="Menunggu Respon"
                        binding.txtStatus.setBackgroundColor(context.getColor(R.color.blue))
                    }
                    1 ->{
                        binding.txtStatus.text ="Sedang ditangani"
                        binding.txtStatus.setBackgroundColor(context.getColor(R.color.red))

                    }
                    2 ->{

                        binding.txtStatus.text ="Selesai ditangani"
                        binding.txtStatus.setBackgroundColor(context.getColor(R.color.green))
                    }
                }
                myViewModel. getDataUser().addSnapshotListener { value, error ->
                    if (value != null) {
                        for (document in value) {
                            val user = document.toObject(UserModel::class.java)
                            user.id = document.id
                            if (user.id==this.idUser) {
                                binding.txtName.text =user.nama
                                break
                            }
                        }
                    }
                }
                holder.itemView.setOnClickListener {
                    mCallBack?.onClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
       return data.size
    }
}