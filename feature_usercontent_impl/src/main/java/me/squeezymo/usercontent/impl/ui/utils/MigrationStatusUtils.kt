package me.squeezymo.usercontent.impl.ui.utils

import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import me.squeezymo.core.ext.visibleOrGone
import me.squeezymo.streamingservices.api.domain.data.StreamingService
import me.squeezymo.streamingservices.api.domain.ui.getIconResId
import me.squeezymo.usercontent.impl.ui.viewobject.MigrationStatusUi

internal object MigrationStatusUtils {

    fun updateMigrationStatus(
        dstService: StreamingService,
        migrationStatus: MigrationStatusUi,
        isInSelectionMode: Boolean,
        migrationCompleteIv: ImageView?,
        migrateCb: CompoundButton?,
        migrationStatusRetrievingPi: View?,
        migrationProgressPi: View?,
        onSelectedForMigrationCheckedStateChanged: CompoundButton.OnCheckedChangeListener?
    ) {
        migrationCompleteIv?.setImageResource(dstService.getIconResId())

        when (migrationStatus) {
            MigrationStatusUi.Unknown -> {
                updateMigrationStatus(
                    migrationCompleteIv = migrationCompleteIv,
                    migrateCb = migrateCb,
                    migrationStatusRetrievingPi = migrationStatusRetrievingPi,
                    migrationProgressPi = migrationProgressPi,
                    onSelectedForMigrationCheckedStateChanged = onSelectedForMigrationCheckedStateChanged,
                    isMigrationCompleteIvVisible = false,
                    isMigrateCbVisible = false,
                    isMigrateCbChecked = false,
                    isMigrationStatusRetrievingIndicatorVisible = true,
                    isMigrationStatusInProgressIndicatorVisible = false
                )
            }
            is MigrationStatusUi.NotMigrated -> {
                updateMigrationStatus(
                    migrationCompleteIv = migrationCompleteIv,
                    migrateCb = migrateCb,
                    migrationStatusRetrievingPi = migrationStatusRetrievingPi,
                    migrationProgressPi = migrationProgressPi,
                    onSelectedForMigrationCheckedStateChanged = onSelectedForMigrationCheckedStateChanged,
                    isMigrationCompleteIvVisible = false,
                    isMigrateCbVisible = isInSelectionMode,
                    isMigrateCbChecked = migrationStatus.isSelected,
                    isMigrationStatusRetrievingIndicatorVisible = false,
                    isMigrationStatusInProgressIndicatorVisible = false
                )
            }
            MigrationStatusUi.InProgress -> {
                updateMigrationStatus(
                    migrationCompleteIv = migrationCompleteIv,
                    migrateCb = migrateCb,
                    migrationStatusRetrievingPi = migrationStatusRetrievingPi,
                    migrationProgressPi = migrationProgressPi,
                    onSelectedForMigrationCheckedStateChanged = onSelectedForMigrationCheckedStateChanged,
                    isMigrationCompleteIvVisible = false,
                    isMigrateCbVisible = false,
                    isMigrateCbChecked = false,
                    isMigrationStatusRetrievingIndicatorVisible = false,
                    isMigrationStatusInProgressIndicatorVisible = true
                )
            }
            MigrationStatusUi.Migrated -> {
                updateMigrationStatus(
                    migrationCompleteIv = migrationCompleteIv,
                    migrateCb = migrateCb,
                    migrationStatusRetrievingPi = migrationStatusRetrievingPi,
                    migrationProgressPi = migrationProgressPi,
                    onSelectedForMigrationCheckedStateChanged = onSelectedForMigrationCheckedStateChanged,
                    isMigrationCompleteIvVisible = true,
                    isMigrateCbVisible = false,
                    isMigrateCbChecked = false,
                    isMigrationStatusRetrievingIndicatorVisible = false,
                    isMigrationStatusInProgressIndicatorVisible = false
                )
            }
        }
    }

    private fun updateMigrationStatus(
        migrationCompleteIv: ImageView?,
        isMigrationCompleteIvVisible: Boolean,
        migrateCb: CompoundButton?,
        isMigrateCbVisible: Boolean,
        isMigrateCbChecked: Boolean,
        migrationStatusRetrievingPi: View?,
        isMigrationStatusRetrievingIndicatorVisible: Boolean,
        migrationProgressPi: View?,
        isMigrationStatusInProgressIndicatorVisible: Boolean,
        onSelectedForMigrationCheckedStateChanged: CompoundButton.OnCheckedChangeListener?
    ) {
        migrationCompleteIv?.visibleOrGone { isMigrationCompleteIvVisible }
        migrateCb?.apply {
            visibleOrGone { isMigrateCbVisible }

            setOnCheckedChangeListener(null)
            isChecked = isMigrateCbChecked
            setOnCheckedChangeListener(onSelectedForMigrationCheckedStateChanged)
        }
        migrationProgressPi?.visibleOrGone { isMigrationStatusInProgressIndicatorVisible }

        if (isMigrationStatusRetrievingIndicatorVisible) {
            migrationStatusRetrievingPi?.isVisible = true
        } else {
            migrationStatusRetrievingPi?.isGone = true
        }
    }

}
