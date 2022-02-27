package me.squeezymo.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding, VM : IBaseActivityViewModel> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = checkNotNull(_binding) {
            "Binding has not been created yet"
        }

    abstract val viewModel: VM

    abstract fun createViewBinding(
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = createViewBinding(layoutInflater, savedInstanceState)
        val view = binding.root
        setContentView(view)
    }

}
