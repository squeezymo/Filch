package me.squeezymo.musicmigrator.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import me.squeezymo.core.ui.BaseActivityViewModel
import me.squeezymo.core.ui.IBaseActivityViewModel
import javax.inject.Inject

internal interface IMainViewModel : IBaseActivityViewModel

@HiltViewModel
internal class MainViewModel @Inject constructor() :
    BaseActivityViewModel(),
    IMainViewModel
