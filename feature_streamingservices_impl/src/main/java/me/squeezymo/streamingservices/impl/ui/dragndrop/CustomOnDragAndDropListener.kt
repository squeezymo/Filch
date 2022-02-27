package me.squeezymo.streamingservices.impl.ui.dragndrop

import android.content.ClipDescription
import android.view.DragEvent
import android.view.View

internal abstract class CustomOnDragAndDropListener : View.OnDragListener {

    abstract fun canAcceptDrop(clipDescription: ClipDescription): Boolean

    open fun onStarted(state: Any) {
        /* do nothing */
    }

    open fun onEntered(state: Any, clipDescription: ClipDescription) {
        /* do nothing */
    }

    open fun onExited(state: Any, clipDescription: ClipDescription) {
        /* do nothing */
    }

    open fun onDrop(state: Any, clipDescription: ClipDescription) {
        /* do nothing */
    }

    open fun onEnded(state: Any) {
        /* do nothing */
    }

    override fun onDrag(view: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                onStarted(event.localState)
                return canAcceptDrop(event.clipDescription)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                onEntered(event.localState, event.clipDescription)
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                onExited(event.localState, event.clipDescription)
            }
            DragEvent.ACTION_DROP -> {
                onDrop(event.localState, event.clipDescription)
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                onEnded(view)
            }
        }

        return true
    }

}
