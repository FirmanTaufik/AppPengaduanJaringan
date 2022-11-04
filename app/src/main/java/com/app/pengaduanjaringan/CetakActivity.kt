package com.app.pengaduanjaringan

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import com.app.pengaduanjaringan.databinding.ActivityCetakBinding
import com.app.pengaduanjaringan.pengaduan.PengaduanModel
import com.app.pengaduanjaringan.user.UserModel
import com.app.pengaduanjaringan.utils.Constant
import com.app.pengaduanjaringan.utils.MainViewmodel
import java.util.*
import kotlin.collections.ArrayList


class CetakActivity : AppCompatActivity() {
    val TAG ="CetakActivityTAG"
    lateinit var binding: ActivityCetakBinding
    lateinit var myViewModel: MainViewmodel
    var globarStartDate: Long = 0
    var globarEndDate: Long = 0
    lateinit var myWebView: WebView
    lateinit var userList :ArrayList<UserModel>
    private val stringBuilder = StringBuilder()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCetakBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        myViewModel = vita.with(VitaOwner.Multiple(this)).getViewModel<MainViewmodel>()
        userList = ArrayList();
        myWebView = binding.webView

        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.builtInZoomControls = true

        myViewModel.getDataUser().whereEqualTo("role",2)
            . addSnapshotListener { value, error ->
                if (value != null) {
                    userList.clear()
                    for (document in value) {
                        val user = document.toObject(UserModel::class.java)
                        user.id = document.id
                        userList.add(user)
                    }
                }

                myViewModel.getDataCetak(globarStartDate, globarEndDate,3).observe(this@CetakActivity) {
                    myWebView.loadUrl("about:blank")
                    setData(it)
                }
            }
    }

    private fun setData(arrayList: ArrayList<PengaduanModel>) {
       if ( stringBuilder.isNotEmpty() ) stringBuilder.setLength(0)
        val top = """<!DOCTYPE html>
                <html>
                 <head>
                  <title>${getString(R.string.app_name)}</title>
                  <style type="text/css">
                    table,th, td{
                     font-size: 2px;
                        padding: 2px;
                    border: 1px solid black;
                    border-collapse: collapse; } 
                    th, td{ text-align:center; }
                  </style>
                 </head>
                <body>
                """
        stringBuilder.append(top)

        val body = """ <table>
        <thead>
            <tr> 
                <th>No </th>
                <th>Tanggal</th>
                <th>Pelapor</th>
                <th>Judul</th>
                <th>Isi</th>
                <th  style='width:60%;'>Alamat</th>
                <th>Status</th>
              </tr>
        </thead>
        <tbody>"""

        stringBuilder.append(body)
        var baris: Int = 1
        for (i in 0 until arrayList.size) {
            val content = """  <tr>
            <td>${baris++}</td>
            <td> ${Constant.convertToyyyMMdd(arrayList[i].tanggal!!)}</td>
            <td> ${ getName(arrayList[i].idUser!! )}</td>
            <td> ${arrayList[i].judul}</td>
            <td> ${arrayList[i].isi}</td>
            <td style='width:60%;'  > ${arrayList[i].alamat}</td>
            <td> ${getStatus(arrayList[i].status)}</td>
        </tr>"""
            stringBuilder.append(content)
        }

        val bot = """</tbody>   
         </table>
         
        <br>
        <br>
        <br> 
         <div  style="float:left">
          
        
         </div>
        
         
         <div style="float:right">
          
           </div></body>
        </html>"""

        stringBuilder.append(bot)
        //load your html to webview

        //load your html to webview
        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
            }
        }
        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }
        }
        myWebView.loadData(stringBuilder.toString(), "text/HTML", "UTF-8")

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.filter){
            val view = findViewById<View>(R.id.filter)
            val popupMenu = PopupMenu(this, view)
            popupMenu.menu.add("Semua")
            popupMenu.menu.add("Menunggu Respon")
            popupMenu.menu.add("Sedang ditangani")
            popupMenu.menu.add("Selesai ditangani")

            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                when(it.title) {
                    "Semua" ->{
                        filterByStatus(3)
                    }
                    "Menunggu Respon" ->{
                        filterByStatus(0)
                    }
                    "Sedang ditangani" ->{
                        filterByStatus(1)
                    }
                    "Selesai ditangani" ->{
                        filterByStatus(2)
                    }
                }

                return@setOnMenuItemClickListener false
            }
        }
        if (item.itemId==R.id.print) {
            createWebPrintJob(myWebView)
        }
        if (item.itemId == R.id.search) {
            Constant.dateRangePickerShow(this, object : Constant.Companion.Listener {
                override fun onSelected(startDate: Long, endDate: Long) {
                    globarStartDate = startDate
                    globarEndDate = endDate
                    myViewModel.getDataCetak(startDate, endDate,3).observe(this@CetakActivity) {
                        myWebView.loadUrl("about:blank")
                        binding.toolbar.subtitle =
                          Constant.convertToyyyMMdd(startDate) + " - " + Constant.convertToyyyMMdd(
                                endDate
                            )
//                        binding.txtStatus.text = "Status : Semua"
                        setData(it)
                    }
                }
            })
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getStatus(status: Int?): String {
        return when (status) {
            0 -> "Menunggu Respon"
            1 -> "Sedang ditangani"
            else -> {
                "Selesai ditangani"
            }
        }
    }

    fun getName(id:String):String {
        for (user in userList) {
            if (user.id==id) return user.nama!!
        }
        return ""
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        menu.findItem(R.id.add).isVisible = false
        menu.findItem(R.id.acion_search).isVisible = false
        menu.findItem(R.id.filter).isVisible = true
        menu.findItem(R.id.search).isVisible = true
        return true
    }

    fun filterByStatus(status: Int){
        myWebView.loadUrl("about:blank")
        myViewModel.getDataCetak(globarStartDate, globarEndDate,status).observe(this){
            Log.d(TAG, "filterByStatus: "+status)
            setData(it)
        }
    }


    private fun createWebPrintJob(webView: WebView) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName: String = getString(R.string.app_name)
        val printAdapter = webView.createPrintDocumentAdapter(jobName)
        val builder = PrintAttributes.Builder()
        builder.setMediaSize(PrintAttributes.MediaSize.NA_GOVT_LETTER)
        printManager.print(jobName, printAdapter, builder.build())
    }
}