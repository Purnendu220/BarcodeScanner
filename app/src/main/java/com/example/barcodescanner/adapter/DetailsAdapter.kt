package com.abpl.decatholontest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.abpl.decatholontest.adapter.viewholder.DetailsViewHolder
import com.example.barcodescanner.R
import com.example.barcodescanner.model.Contact
import com.example.barcodescanner.model.DetailsModel
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DetailsAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var list: MutableList<DetailsModel>?
    private val listUnFiltered: MutableList<DetailsModel>?

    private val context: Context = context
    fun getList(): List<Any>? {
        return list
    }

    fun addFixture(model: DetailsModel) {
        list!!.add(model)
    }

    fun addAllFixture(models: List<DetailsModel>?) {
        list!!.addAll(models!!)
        listUnFiltered!!.addAll(models!!)
    }

    fun clearAll() {
        list!!.clear()
        listUnFiltered!!.clear()
    }

    override fun getItemViewType(position: Int): Int {
        var itemViewType = -1
        val item = getItem(position)
        if (item is DetailsModel) {
            itemViewType = VIEW_TYPE_FIXTURE
        }
        return itemViewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_details, parent, false)
            return DetailsViewHolder(view)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DetailsViewHolder) {
            if (getItem(position) != null) (holder as DetailsViewHolder?)?.bind(getItem(position), position)
        }
    }

    override fun getItemCount(): Int {
        if(list==null){
            return 0
        }
        return list?.size!!
    }

    fun getItem(position: Int): DetailsModel? {
        return if (list != null && list!!.size > 0) list!![position] else null
    }

    val isEmpty: Boolean
        get() = list!!.isEmpty()

    companion object {
        private const val VIEW_TYPE_FIXTURE = 1
    }

    init {
        list = ArrayList()
        listUnFiltered = ArrayList()
    }

    fun getFilter(): Filter? {
        return object : Filter() {
             override fun performFiltering(charSequence: CharSequence): FilterResults? {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    list = listUnFiltered
                } else {
                    val filteredList: MutableList<DetailsModel> = ArrayList()
                    if (listUnFiltered != null) {
                        for (row in listUnFiltered) {
                                 try {
                                     if (row?.CON_NAME.toLowerCase().contains(charString.toLowerCase()) || row?.DATE_OF_T!!.contains(charSequence)) {
                                         filteredList.add(row)   }
                                 }catch (e:Exception){}





                        }
                    }
                    list = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = list
                return filterResults
            }

             override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                 list = filterResults.values as MutableList<DetailsModel>?
                notifyDataSetChanged()
            }
        }
    }

}