package com.app.pengaduanjaringan.user

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.app.pengaduanjaringan.databinding.ListItemPengaduanBinding
import com.app.pengaduanjaringan.databinding.ListItemUserBinding
import com.app.pengaduanjaringan.user.UserModel

class UserAdapter( var data :ArrayList<UserModel> ):
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    val dataListfull = data
    private val TAG = "UserAdapterTAG"

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
                    val filteredList  : ArrayList<UserModel> = ArrayList()
                    if (dataListfull != null) {
                        for (row in dataListfull) {

                            if (row.nama!!.toLowerCase().contains(charString)
                                || row.nama.startsWith(charString)
                                || row.nama.toUpperCase().contains(charString)
                                || row.nama.contains(charString)) {
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
                data = filterResults.values as ArrayList<UserModel>
                notifyDataSetChanged()
            }
        }
    }

    inner  class ViewHolder(val binding: ListItemUserBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val binding = ListItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(data[position]){
                binding.txtName.text =  this.nama
                binding.txtUsername.text = this.username
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