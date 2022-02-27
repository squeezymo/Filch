package me.squeezymo.core.ui.recyclerview

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import kotlin.reflect.KClass

class BaseAdapterDelegate<V : View, S : IListItem>(
    private val stateClass: KClass<S>,
    private val createWidget: (context: Context) -> V,
    private val widgetWidth: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    private val widgetHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    private val updateState: (widget: V, state: S, payloads: MutableList<Any>) -> Unit,
) : AdapterDelegate<List<IListItem>>() {

    override fun isForViewType(items: List<IListItem>, position: Int): Boolean =
        stateClass.isInstance(items[position])

    override fun onCreateViewHolder(parent: ViewGroup): BaseViewHolder<V> =
        BaseViewHolder(
            createWidget(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    widgetWidth,
                    widgetHeight
                )
            }
        )

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(
        items: List<IListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        holder as BaseViewHolder<V>
        updateState(holder.widget, items[position] as S, payloads)
    }

    protected class BaseViewHolder<V : View>(
        itemView: V
    ) : RecyclerView.ViewHolder(itemView) {

        val widget = itemView

    }

}
