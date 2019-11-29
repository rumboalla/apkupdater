package com.apkupdater.util.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.Delegates

class BindAdapter<T: Id>(@LayoutRes private val id: Int, private val onBind: (View, T) -> Unit) : RecyclerView.Adapter<BindAdapter<T>.ViewHolder>() {

	var items: List<T> by Delegates.observable(emptyList()) { _, old, new -> diff(old, new) { o, n -> o.id == n.id } }

	override fun getItemCount() = items.size
	override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(id, parent, false))

	private fun <T> diff(old: List<T>, new: List<T>, compare: (T, T) -> Boolean) = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
		override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean = compare(old[oldPos], new[newPos])
		override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean = old[oldPos] == new[newPos]
		override fun getOldListSize() = old.size
		override fun getNewListSize() = new.size
	}).dispatchUpdatesTo(this)

	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { fun bind(app: T) = onBind(itemView, app) }

}