package me.squeezymo.musicmigrator

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import me.squeezymo.usersupport.api.FLUTTER_ENGINE_USER_SUPPORT

@HiltAndroidApp
class FilchApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FlutterEngine(this).also { flutterEngine ->
            flutterEngine
                .dartExecutor
                .executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())

            FlutterEngineCache
                .getInstance()
                .put(FLUTTER_ENGINE_USER_SUPPORT, flutterEngine)
        }
    }

}
