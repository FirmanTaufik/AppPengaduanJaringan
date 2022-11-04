package com.app.pengaduanjaringan

import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import com.app.pengaduanjaringan.databinding.ActivityCetakBinding
import com.app.pengaduanjaringan.utils.Constant
import com.app.pengaduanjaringan.utils.MainViewmodel
import ir.androidexception.datatable.model.DataTableHeader
import ir.androidexception.datatable.model.DataTableRow
import java.util.*


class CetakActivity : AppCompatActivity() {
    lateinit var binding: ActivityCetakBinding
    val rows: ArrayList<DataTableRow> = ArrayList();
    lateinit var myViewModel: MainViewmodel
    var globarStartDate :Long  = 0
    var globarEndDate  :Long = 0
    lateinit var myWebView:WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCetakBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        myViewModel = vita.with(VitaOwner.Multiple(this)).getViewModel<MainViewmodel>()
        myWebView = binding.webView

    }

    private fun setData() {
        //add webview client to handle event of loading
        //add webview client to handle event of loading
        val stringBuilder = StringBuilder()
        val top = """<!DOCTYPE html>
<html>
 <head>
  <title>${getString(R.string.app_name)}</title>
  <style type="text/css">
    table,th, td{
        padding: 5px;
    border: 1px solid black;
    border-collapse: collapse; }
    table{ width: 30%; }
    th, td{ text-align:center; }
  </style>
 </head>
<body>
"""
        stringBuilder.append(top)

        val m = """<center> <h3> DAFTAR MUTASI OBAT SUMBER   </center> <h3> 
"""
        stringBuilder.append(m)
        val p = "<center><h3> TANGGAL    /  <h3>  </center> "
        stringBuilder.append(p)


        val body = """ <table>
    <thead>
        <tr> 
            <th>No </th>
            <th>Tanggal</th>
            <th>Pelapor</th>
            <th>Judul</th>
            <th>Isi</th>
            <th>Alamat</th>
            <th>Status</th>
          </tr>
    </thead>
    <tbody>"""

        var baris :Int=1
        stringBuilder.append(body)
        for (i in 0 until 10) {
            val content = """  <tr>
            <td>${baris++}</td>
            <td> r</td>
            <td> r</td>
            <td> r</td>
            <td> r</td>
            <td> r</td>
            <td> r</td>
            <td> r</td>
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
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.builtInZoomControls = true
        myWebView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                //printMutasi(myWebView);
                //                createWebPrintJob(myWebView);
                //  printLibrary(myWebView);
                //  printMutasi(myWebView);
                //if page loaded successfully then show print button
                //   findViewById(R.id.fab).setVisibility(View.VISIBLE);
            }
        }
        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress == 100) {
                }
                super.onProgressChanged(view, newProgress)
            }
        }
        myWebView.loadData(stringBuilder.toString(), "text/HTML", "UTF-8")

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search) {
            Constant.dateRangePickerShow(this, object : Constant.Companion.Listener {
                override fun onSelected(startDate: Long, endDate: Long) {
                    globarStartDate = startDate
                    globarStartDate = endDate
                    myViewModel.getDataCetak(startDate, endDate).observe(this@CetakActivity) {
                        binding.txtTanggal.text = "Tanggal : " +Constant.convertToyyyMMdd(startDate )+" - "+Constant.convertToyyyMMdd(endDate)
                        binding.txtStatus.text= "Status : Semua"
                        rows.clear()
                        for (i in 0 until it.size) {
                            val hasil = i +1
                            val d = it[i]
                            val row = DataTableRow.Builder()
                                .value( hasil.toString())
                                .value(Constant.convertToyyyMMdd(d.tanggal!!))
                                .value(d.idUser)
                                .value(d.judul)
                                .value(d.isi)
                                .value(d.alamat)
                                .value(getStatus(d.status))
                                .build();
                            rows.add(row);
                        }

                        binding.dataTable.setTypeface(Typeface.DEFAULT);
                        binding.dataTable.setHeader(DataTableHeader.Builder() .item(" No ", 1)
                            .item(" Tanggal ", 1)
                            .item(" Pelapor ", 1)
                            .item(" Judul ", 1)
                            .item(" Isi ", 1)
                            .item(" Alamat ", 1)
                            .item(" Status ", 1)
                            .build());
                        binding.dataTable.setRows(rows);
                        binding.dataTable.inflate(this@CetakActivity);
                    }
                }
            })
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getStatus(status: Int?): String {
        return when(status) {
            0 ->"Menunggu Respon"
            1->"Sedang ditangani"
            else -> {
                "Selesai ditangani"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        menu.findItem(R.id.add).isVisible = false
        menu.findItem(R.id.acion_search).isVisible = false
        menu.findItem(R.id.filter).isVisible = true
        menu.findItem(R.id.search).isVisible = true
        menu.findItem(R.id.filter).setOnMenuItemClickListener {
            val view = findViewById<View>(R.id.filter)
            val popupMenu = PopupMenu(this,view)
            popupMenu.menu.add("Menunggu Respon")
            popupMenu.menu.add("Sedang ditangani")
            popupMenu.menu.add("Selesai ditangani")

            popupMenu.show()
            return@setOnMenuItemClickListener false
        }
        return true
    }


}