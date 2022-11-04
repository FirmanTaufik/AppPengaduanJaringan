package com.app.pengaduanjaringan.utils

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.pengaduanjaringan.pengaduan.PengaduanModel
import com.app.pengaduanjaringan.user.UserModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.math.log


class MainViewmodel(application: Application) : AndroidViewModel(application) {

    private val app = application

    private val TAG = "MainViewmodelTAG";
    val db = FirebaseFirestore.getInstance()

    fun postDataUser(ue: UserModel): Task<DocumentReference> {
        return db.collection("users")
            .add(ue)
    }

    fun getDataUser(): CollectionReference {
        return db.collection("users")

    }

    fun deleteUser(id: String): Task<Void> {
        return db.collection("users").document(id).delete()
    }

    fun editDataUser(ue: UserModel, id: String): Task<Void> {
        return db.collection("users").document(id)
            .set(ue)
    }

    fun checkAvailableUsername(username: String, data: ArrayList<UserModel>): Boolean {
        for (d in data) {
            if (d.username == username) {
                return false
            }
        }
        return true
    }

    fun postDataPengaduan(pengaduan: PengaduanModel): Task<DocumentReference> {
        return db.collection("pengaduan")
            .add(pengaduan)
    }
    var lst = MutableLiveData<ArrayList<PengaduanModel>>()
    var newlist = arrayListOf<PengaduanModel>()

    fun getDataPengaduan(startDate: Long, endDate: Long): MutableLiveData<ArrayList<PengaduanModel>> {
        db.collection("pengaduan")
            .whereGreaterThan("tanggal",startDate)
            .whereLessThan("tanggal", endDate)
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                newlist.clear()
                if (value != null) {
                    for (document in value) {
                        val penhaduan = document.toObject(PengaduanModel::class.java)
                        penhaduan.id = document.id
                        if (Constant.getRole(getApplication())==1) {
                            newlist.add(penhaduan)
                        }else{
                            if (penhaduan.idUser == Constant.getIdUser(getApplication())) {
                                newlist.add(penhaduan)
                            }
                        }

                        lst.value=newlist
                    }
                }
            }
        return lst;
    }


    private var lstCetak = MutableLiveData<ArrayList<PengaduanModel>>()
    private var newlistCetak = arrayListOf<PengaduanModel>()

//    fun getDataCetak2(startDate: Long, endDate: Long, status :Int): MutableLiveData<ArrayList<PengaduanModel>> {
//        newlistCetak.clear()
//        lstCetak.value?.clear()
//        db.collection("pengaduan")
//            .whereEqualTo("status", status)
//            .whereGreaterThan("tanggal",startDate)
//            .whereLessThan("tanggal", endDate)
//            .orderBy("tanggal", Query.Direction.DESCENDING)
//            .addSnapshotListener { value, error ->
//                if (value != null) {
//                    for (document in value) {
//                        val penhaduan = document.toObject(PengaduanModel::class.java)
//                        penhaduan.id = document.id
//                        newlistCetak.add(penhaduan)
//                    }
//                }
//                lstCetak.setValue(newlistCetak)
//            }
//
//        return lstCetak
//    }

    fun getDataCetak(startDate: Long, endDate: Long,status :Int): MutableLiveData<ArrayList<PengaduanModel>> {
        newlistCetak.clear()
      //  lstCetak.value?.clear()
        if (status==3) {
            db.collection("pengaduan")
                .whereGreaterThan("tanggal",startDate)
                .whereLessThan("tanggal", endDate)
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .get().addOnSuccessListener {
                    for (d in it.documents){
                        val data = d.toObject(PengaduanModel::class.java)
                        if (data != null) {
                            newlistCetak.add(data)
                        }
                    }
                    lstCetak.setValue(newlistCetak)
                }
        }else{
            Log.d(TAG, "getDataCetak: "+status)
            db.collection("pengaduan")
               // .whereEqualTo("status", status)
                .whereGreaterThan("tanggal",startDate)
                .whereLessThan("tanggal", endDate)
                .get().addOnSuccessListener {
                    for (d in it.documents){
                        val data = d.toObject(PengaduanModel::class.java)
                        if (data != null) {
                            if (data.status==status) {
                                newlistCetak.add(data)
                            }
                        }
                    }
                    lstCetak.setValue(newlistCetak)
                }
        }


        return lstCetak
    }

    fun deletePengaduan(id: String): Task<Void> {
        return db.collection("pengaduan").document(id).delete()
    }

    fun editDataPengaduan(pengaduan: PengaduanModel, id: String?): Task<Void> {
        return db.collection("pengaduan").document(id!!)
            .set(pengaduan)
    }

    var countNotReadYet = MutableLiveData<Int>()
    fun getListNotRead(): MutableLiveData<Int> {
        db.collection("pengaduan")
            .whereEqualTo("read", false)
            .addSnapshotListener { value, error ->

                countNotReadYet.value = 0
                if (value != null) {
                    for (document in value) {
                        countNotReadYet.value = countNotReadYet.value!! +1
                    }
                }
            }
        return countNotReadYet
    }

   private var userModel = MutableLiveData<UserModel>()
    fun getMyProfile(id :String): MutableLiveData<UserModel> {
        db.collection("users")
            .addSnapshotListener { value, error ->
                if (value != null) {
                    for (document in value) {
                        Log.d(TAG, "getMyProfile: "+document.id)
                        if (document.id==id){
                            userModel.value = document.toObject(UserModel::class.java)
                            userModel.value?.id = id

                        }
                    }
                }
            }
        return userModel
    }


    fun getJumlahCustomer(): MutableLiveData<Int> {
        val count = MutableLiveData<Int>()
        db.collection("users")
            .whereEqualTo("role", 2)
            .addSnapshotListener { value, error ->

                count.value = 0
                if (value != null) {
                    for (document in value) {
                        count.value = count.value!! +1
                    }
                }
            }
        return count
    }

    fun getJumlahPengaduan(): MutableLiveData<Int> {
        val count = MutableLiveData<Int>()
        db.collection("pengaduan")
            .addSnapshotListener { value, error ->
                count.value = 0
                if (value != null) {
                    for (document in value) {
                        count.value = count.value!! +1
                    }
                }
            }
        return count
    }

}
