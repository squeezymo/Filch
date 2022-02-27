package me.squeezymo.usercontent.impl.ui.vmdelegate

import me.squeezymo.core.ui.vmdelegate.ErrorHandlingVmDelegate
import me.squeezymo.usercontent.impl.data.UserContentError
import javax.inject.Inject

internal class UserContentErrorHandlingVmDelegate @Inject constructor(

) : ErrorHandlingVmDelegate<UserContentError>()
