package me.squeezymo.usercontent.impl.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.transition.TransitionManager
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.squeezymo.core.ext.doOnSizeChanged
import me.squeezymo.core.ext.dp
import me.squeezymo.core.ui.recyclerview.BaseDiffItemCallback
import me.squeezymo.core.uicomponents.dialog.FilchAlertDialogBuilder
import me.squeezymo.streamingservices.api.domain.ui.getMainColor
import me.squeezymo.usercontent.impl.R
import me.squeezymo.usercontent.impl.data.UserContentError
import me.squeezymo.usercontent.impl.databinding.FUserContentBinding
import me.squeezymo.usercontent.impl.ui.utils.Utils
import me.squeezymo.usercontent.impl.ui.viewdelegate.addErrorHandlingViewDelegate
import me.squeezymo.usercontent.impl.ui.viewdelegate.addPlayerViewDelegate
import me.squeezymo.usercontent.impl.ui.widget.PlaylistsWidget
import me.squeezymo.usercontent.impl.ui.widget.TrackLoadingWidget
import me.squeezymo.usercontent.impl.ui.widget.TrackWidget

@AndroidEntryPoint
internal class UserContentFragment :
    BaseUserContentChildFragment<FUserContentBinding, IUserContentViewModel>() {

    override val viewModel: IUserContentViewModel by viewModels<UserContentViewModel>()

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FUserContentBinding {
        return FUserContentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playerDelegate = addPlayerViewDelegate(viewModel, binding.playerWidget)
        addErrorHandlingViewDelegate(viewModel, ::handleError)

        val contentAdapter = AsyncListDifferDelegationAdapter(
            BaseDiffItemCallback(),
            PlaylistsWidget.createAdapterDelegate(
                onPlaylistPicked = viewModel::selectPlaylist,
                onCheckedForMigrationChanged = viewModel::selectPlaylistForMigration
            ),
            TrackLoadingWidget.createAdapterDelegate(),
            TrackWidget.createAdapterDelegate(
                onCheckedForMigrationChanged = viewModel::selectTrackForMigration,
                playerDelegate = playerDelegate
            )
        )

        binding.contentRcv.adapter = contentAdapter
        binding.contentRcv.setHasFixedSize(true)
        (binding.contentRcv.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false

        binding.swipeToRefresh.setColorSchemeColors(
            viewModel.toService.getMainColor(requireContext())
        )
        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            viewModel.load(forceRefresh = true)
        }
        binding.swipeToRefresh.isEnabled = false

        binding.migrateBtn.setOnClickListener {
            viewModel.migrate()
        }
        binding.migrateBtn.doOnSizeChanged { _, height ->
            binding.contentRcv.setPadding(
                binding.contentRcv.paddingLeft,
                binding.contentRcv.paddingTop,
                binding.contentRcv.paddingRight,
                height + (binding.migrateBtn.layoutParams as ViewGroup.MarginLayoutParams).let {
                    it.topMargin + it.bottomMargin
                } + view.dp(16)
            )
        }

        binding.playerWidget.doOnSizeChanged { _, height ->
            binding.contentRcv.setPadding(
                binding.contentRcv.paddingLeft,
                (if (binding.playerWidget.isVisible) height else 0) + view.dp(12),
                binding.contentRcv.paddingRight,
                binding.contentRcv.paddingBottom
            )
        }

        binding.selectTracksBtn.setOnClickListener {
            viewModel.toggleSelectionMode()
        }

        binding.toggleShowMigratedTracksBtn.setOnClickListener {
            viewModel.toggleShowMigratedTracks()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        TransitionManager.beginDelayedTransition(binding.header)

                        contentAdapter.items = uiState.userContent

                        Utils.updateCounter(
                            binding.tracksCounterTv,
                            uiState.tracksInSourceService,
                            uiState.tracksInDestinationService
                        )
                        Utils.updateSelectionModeButton(
                            binding.selectTracksBtn,
                            uiState.selectionModeUiState
                        )
                        Utils.updateShowMigratedTracks(
                            binding.toggleShowMigratedTracksBtn,
                            uiState.showMigratedTracksUiState,
                            viewModel.toService
                        )
                        Utils.updateProgress(
                            binding.migrationProgressPi,
                            uiState.progress
                        )
                        Utils.updateMigrateButton(
                            binding.migrateBtn,
                            uiState.searchInProgress,
                            uiState.tracksToMigrate,
                            uiState.playlistsToMigrate,
                            viewModel.toService
                        )
                    }
                }

                launch {
                    rootViewModel
                        .playlistToUserContentResult
                        .filterNotNull()
                        .collect { result ->
                            try {
                                viewModel.handlePlaylistToUserContentResult(result)
                            }
                            finally {
                                rootViewModel.notifyOnPlaylistToUserContentResultHandled()
                            }
                        }
                }
            }
        }
    }

    private fun handleError(error: UserContentError) {
        when (error) {
            UserContentError.TracksNotRetrieved -> {
                var dialog: AlertDialog? = null

                dialog = FilchAlertDialogBuilder(
                    requireContext(),
                    text = getString(R.string.user_content_err_tracks_not_retrieved),
                    buttons = listOf(
                        FilchAlertDialogBuilder.Button(
                            text = getString(R.string.user_content_err_tracks_not_retrieved_option_retry),
                            isBold = true,
                            clickListener = {
                                dialog?.dismiss()
                                viewModel.load(forceRefresh = true)
                            }
                        ),
                        FilchAlertDialogBuilder.Button(
                            text = getString(R.string.user_content_err_tracks_not_retrieved_option_back),
                            clickListener = {
                                dialog?.dismiss()
                                findNavController().navigateUp()
                            }
                        ),
                    )
                ).build().show()
            }
        }
    }

}
