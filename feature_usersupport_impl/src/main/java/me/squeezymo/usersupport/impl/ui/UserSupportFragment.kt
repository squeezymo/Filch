package me.squeezymo.usersupport.impl.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.squeezymo.core.ext.dp
import me.squeezymo.core.ui.BaseFragment
import me.squeezymo.core.ui.recyclerview.BaseDiffItemCallback
import me.squeezymo.core.ui.recyclerview.IListItem
import me.squeezymo.core.ui.recyclerview.SimpleSpacingItemDecoration
import me.squeezymo.usersupport.impl.databinding.FUserSupportBinding
import me.squeezymo.usersupport.impl.ui.event.UserSupportEvent
import me.squeezymo.usersupport.impl.ui.uistate.UserSupportUiState
import me.squeezymo.usersupport.impl.ui.widget.IncomingMessageWidget
import me.squeezymo.usersupport.impl.ui.widget.OutgoingMessageWidget
import me.squeezymo.usersupport.impl.ui.widget.TechnicalMessageWidget

@AndroidEntryPoint
internal class UserSupportFragment :
    BaseFragment<FUserSupportBinding, IUserSupportViewModel>() {

    override val viewModel: IUserSupportViewModel by viewModels<UserSupportViewModel>()

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FUserSupportBinding {
        return FUserSupportBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contentAdapter = AsyncListDifferDelegationAdapter(
            BaseDiffItemCallback(),
            IncomingMessageWidget.createAdapterDelegate(),
            OutgoingMessageWidget.createAdapterDelegate(),
            TechnicalMessageWidget.createAdapterDelegate()
        )

        binding.contentRcv.adapter = contentAdapter
        binding.contentRcv.setHasFixedSize(true)
        (binding.contentRcv.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        binding.contentRcv.addItemDecoration(
            SimpleSpacingItemDecoration(
                orientation = RecyclerView.VERTICAL,
                spacePx = view.dp(8)
            )
        )

        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
        }
        binding.swipeToRefresh.isEnabled = false

        binding.newMessageWidget.setOnSubmitListener(viewModel::submitMessage)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel
                        .uiState
                        .collect { uiState ->
                            updateState(contentAdapter, uiState)
                        }
                }

                launch {
                    viewModel
                        .isSendingMessage
                        .collect(binding.newMessageWidget::setIsSendingMessage)
                }

                launch {
                    viewModel
                        .userSupportEvent
                        .filterNotNull()
                        .collect { userSupportEvent ->
                            try {
                                handleEvent(userSupportEvent)
                            }
                            finally {
                                viewModel.notifyOnUserSupportEventHandled()
                            }
                        }
                }
            }
        }
    }

    private fun updateState(
        adapter: AsyncListDifferDelegationAdapter<IListItem>,
        uiState: UserSupportUiState
    ) {
        when (uiState) {
            is UserSupportUiState.Loading -> {
                binding.chatLoadingPi.isVisible = true
                binding.swipeToRefresh.isGone = true
                binding.newMessageWidget.isGone = true
            }
            is UserSupportUiState.Chat -> {
                binding.chatLoadingPi.isGone = true
                binding.swipeToRefresh.isVisible = true
                binding.newMessageWidget.isVisible = true

                adapter.setItems(uiState.messages) {
                    val itemCount = adapter.itemCount
                    if (itemCount > 0) {
                        binding.contentRcv.smoothScrollToPosition(itemCount - 1)
                        viewModel.markAllMessagesAsRead()
                    }
                }
            }
        }
    }

    private fun handleEvent(event: UserSupportEvent) {
        when (event) {
            UserSupportEvent.ClearNewMessageText -> {
                binding.newMessageWidget.clear()
            }
        }
    }

}
