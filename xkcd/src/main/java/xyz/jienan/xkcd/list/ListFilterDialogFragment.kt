package xyz.jienan.xkcd.list

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckedTextView
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

import java.util.HashMap

import xyz.jienan.xkcd.R

class ListFilterDialogFragment : DialogFragment() {

    private var list: MutableList<HashMap<String, String>> = mutableListOf()

    var selection: Int = 0

    var itemSelectListener: OnItemSelectListener? = null

    var filters = intArrayOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        val icons = intArrayOf(R.mipmap.ic_launcher_round, R.drawable.ic_heart_on, R.drawable.ic_thumb_on)
        for (i in filters.indices) {
            val hashMap = hashMapOf<String, String>()
            hashMap["filter"] = getString(filters[i])
            hashMap["iconRes"] = icons[i].toString()
            list.add(hashMap)
        }
        val adapter = FilterAdapter()

        builder.setAdapter(adapter) { dialog, which ->
            itemSelectListener?.onItemSelected(which)
            dialog.dismiss()
        }

        return builder.create()
    }

    interface OnItemSelectListener {
        fun onItemSelected(which: Int)
    }

    private inner class FilterAdapter : BaseAdapter() {

        override fun getCount() = list.size

        override fun getItem(position: Int) = list[position]

        override fun getItemId(position: Int) = position.toLong()

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(context).inflate(R.layout.item_filter_dialog, parent, false)
            val map = list[position]
            val ivFilter = view.findViewById<ImageView>(R.id.iv_filter)
            val tvFilter = view.findViewById<CheckedTextView>(R.id.tv_filter)
            ivFilter.setImageResource(Integer.valueOf(map["iconRes"]!!))
            tvFilter.isChecked = position == selection
            tvFilter.text = map["filter"]
            return view
        }
    }
}
