package com.example.barcodescanner

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abpl.decatholontest.adapter.DetailsAdapter
import com.example.barcodescanner.model.DetailsModel
import com.example.barcodescanner.restapi.RetrofitClient
import com.example.barcodescanner.usecase.Logger
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.fragment_barcode_history_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class DetailsActivity : AppCompatActivity() {

    private val detailAdapter:DetailsAdapter= DetailsAdapter(this);
    private var detailsList:List<DetailsModel>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        initRecyclerView()
        callApi()
        btnGet.setOnClickListener {
            callApi()
        }
        setupSearch()
    }
    private fun setupSearch() {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {

                    startSearching(editable.toString())

            }
        })
    }
    private fun startSearching(search: String) {
        detailAdapter.getFilter()?.filter(search);

    }
    private fun initRecyclerView() {
        detailList.apply {
            layoutManager = LinearLayoutManager(baseContext)
            adapter = detailAdapter
        }
    }
    private fun loadHistory(list: List<DetailsModel>) {
        CoroutineScope(Main).launch {
            detailsList = list;
            detailAdapter.addAllFixture(list)
            detailAdapter.notifyDataSetChanged()
        }

    }

    private fun callApi(){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val call =  RetrofitClient.apiInterface.getDetails() ;
                val resp= call?.execute()
                if(resp.body()!=null&& resp.body()!!.size>0){
                    loadHistory(resp.body()!!);
                }

            }catch (e: Exception){
                Logger.log(e.fillInStackTrace())

            }


        }

    }


}