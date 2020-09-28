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
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.list.activity.BaseListActivity
import java.util.*

class ListFilterDialogFragment() : DialogFragment() {

    companion object {
        private const val KEY_HAS_FAV = "hasFav"

        fun newInstance(hasFav: Boolean): ListFilterDialogFragment {
            val fragment = ListFilterDialogFragment()
            fragment.arguments = bundleOf(KEY_HAS_FAV to hasFav)
            return fragment
        }
    }

    private var list: MutableList<HashMap<String, Any>> = mutableListOf()

    var selection: Int = 0

    var itemSelectListener: OnItemSelectListener? = null

    var filters = intArrayOf()

    private val hasFav: Boolean
        get() = arguments?.getBoolean(KEY_HAS_FAV) != false

    private val icons: IntArray by lazy {
        if (hasFav) {
            intArrayOf(R.mipmap.ic_launcher_round, R.drawable.ic_heart_on, R.drawable.ic_thumb_on)
        } else {
            intArrayOf(R.mipmap.ic_launcher_round, R.drawable.ic_thumb_on)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        filters.filterIndexed { index, i -> hasFav || index != 1 }.forEachIndexed { index, res ->
            list.add(hashMapOf("filter" to getString(res), "iconRes" to icons[index]))
        }

        val adapter = FilterAdapter()

        builder.setAdapter(adapter) { dialog, which ->
            val selection = if (hasFav) {
                when (which) {
                    0 -> BaseListActivity.Selection.ALL
                    1 -> BaseListActivity.Selection.MY_FAVORITE
                    else -> BaseListActivity.Selection.PEOPLES_CHOICE
                }
            } else {
                when (which) {
                    0 -> BaseListActivity.Selection.ALL
                    else -> BaseListActivity.Selection.PEOPLES_CHOICE
                }
            }
            itemSelectListener?.onItemSelected(selection)
            dialog.dismiss()
        }

        return builder.create()
    }

    interface OnItemSelectListener {
        fun onItemSelected(selection: BaseListActivity.Selection)
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
            ivFilter.setImageResource(map["iconRes"]!! as Int)
            tvFilter.isChecked = position == selection
            tvFilter.text = map["filter"].toString()
            return view
        }
    }
}
