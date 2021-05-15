package com.abpl.decatholontest.adapter.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.barcodescanner.R
import com.example.barcodescanner.model.DetailsModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class DetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var text_view_date: TextView
    var text_view_text: TextView
    var text_view_format: TextView
    var text_view_out_Count: TextView
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val context: Context = itemView.context
    @SuppressLint("NewApi")
    fun bind(data: Any?, position: Int) {
        if (data != null && data is DetailsModel) {
            val model: DetailsModel = data
            itemView.visibility = View.VISIBLE
            try {
                val localDateTime: LocalDateTime = LocalDateTime.parse(model.DATE_OF_T)
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val output: String = formatter.format(localDateTime)
                text_view_date.text = output

            }catch (e: Exception){
                text_view_date.text = model.DATE_OF_T

            }
            text_view_text.text = model.CON_NAME
            text_view_format.text = "IN: ${model.TOT_IN} times"
            text_view_out_Count.text = "OUT: ${model.TOT_OUT} times"


        } else {
            itemView.visibility = View.GONE
        }
    }

    init {
        itemView.isClickable = true
        text_view_date = itemView.findViewById(R.id.text_view_daten_new)
        text_view_text = itemView.findViewById(R.id.text_view_text)
        text_view_format = itemView.findViewById(R.id.text_view_format)
        text_view_out_Count = itemView.findViewById(R.id.text_view_out_Count)

    }
}