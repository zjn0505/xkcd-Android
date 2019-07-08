package xyz.jienan.xkcd.list

import androidx.recyclerview.widget.RecyclerView

abstract class ListBaseAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>(), IAdapter

interface IAdapter {

    var pauseLoading: Boolean
}