package com.opentext.browser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for displaying tabs in the tab switcher
 */
class TabAdapter(
    private val onTabClick: (Tab) -> Unit,
    private val onTabClose: (Tab) -> Unit
) : RecyclerView.Adapter<TabAdapter.TabViewHolder>() {

    private val tabs = mutableListOf<Tab>()

    fun setTabs(newTabs: List<Tab>) {
        tabs.clear()
        tabs.addAll(newTabs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tab_item, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        val tab = tabs[position]
        holder.bind(tab)
    }

    override fun getItemCount(): Int = tabs.size

    inner class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTabTitle: TextView = itemView.findViewById(R.id.tvTabTitle)
        private val tvTabUrl: TextView = itemView.findViewById(R.id.tvTabUrl)
        private val btnClose: ImageButton = itemView.findViewById(R.id.btnClose)

        fun bind(tab: Tab) {
            tvTabTitle.text = tab.title.ifEmpty { "New Tab" }
            tvTabUrl.text = tab.url

            itemView.setOnClickListener {
                onTabClick(tab)
            }

            btnClose.setOnClickListener {
                onTabClose(tab)
            }
        }
    }
}
