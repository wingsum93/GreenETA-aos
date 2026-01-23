package com.ericho.myhospital

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HospitalWaitTimeAdapter(
    private var items: List<HospitalWaitTime> = emptyList(),
    private var topHospitalNames: Set<String> = emptySet(),
) : RecyclerView.Adapter<HospitalWaitTimeAdapter.HospitalViewHolder>() {

    fun submitData(newItems: List<HospitalWaitTime>, topNames: Set<String>) {
        items = newItems
        topHospitalNames = topNames
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        holder.bind(items[position], topHospitalNames.contains(items[position].name))
    }

    override fun getItemCount(): Int = items.size

    class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.hospital_name)
        private val badge: TextView = itemView.findViewById(R.id.hospital_badge)
        private val summary: TextView = itemView.findViewById(R.id.hospital_wait_summary)
        private val detail: TextView = itemView.findViewById(R.id.hospital_wait_detail)

        fun bind(item: HospitalWaitTime, isTop: Boolean) {
            name.text = item.name
            badge.visibility = if (isTop) View.VISIBLE else View.GONE
            summary.text = "Category 3: ${item.t3p50} (P95 ${item.t3p95})"
            detail.text =
                "T1: ${item.t1wt} · T2: ${item.t2wt} · T4/5: ${item.t45p50} (P95 ${item.t45p95})"
        }
    }
}
