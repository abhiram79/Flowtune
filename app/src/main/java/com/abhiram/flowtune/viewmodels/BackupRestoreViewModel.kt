package com.abhiram.flowtune.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.abhiram.flowtune.MainActivity
import com.abhiram.flowtune.R
import com.abhiram.flowtune.db.InternalDatabase
import com.abhiram.flowtune.db.MusicDatabase
import com.abhiram.flowtune.extensions.div
import com.abhiram.flowtune.extensions.zipInputStream
import com.abhiram.flowtune.extensions.zipOutputStream
import com.abhiram.flowtune.playback.MusicService
import com.abhiram.flowtune.playback.MusicService.Companion.PERSISTENT_QUEUE_FILE
import com.abhiram.flowtune.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltViewModel
class BackupRestoreViewModel
    @Inject
    constructor(
        val database: MusicDatabase,
    ) : ViewModel() {
        fun backup(
            context: Context,
            uri: Uri,
        ) {
            runCatching {
                context.applicationContext.contentResolver.openOutputStream(uri)?.use {
                    it.buffered().zipOutputStream().use { outputStream ->
                        (context.filesDir / "datastore" / SETTINGS_FILENAME).inputStream().buffered().use { inputStream ->
                            outputStream.putNextEntry(ZipEntry(SETTINGS_FILENAME))
                            inputStream.copyTo(outputStream)
                        }
                        runBlocking(Dispatchers.IO) {
                            database.checkpoint()
                        }
                        FileInputStream(database.openHelper.writableDatabase.path).use { inputStream ->
                            outputStream.putNextEntry(ZipEntry(InternalDatabase.DB_NAME))
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }.onSuccess {
                Toast.makeText(context, R.string.backup_create_success, Toast.LENGTH_SHORT).show()
            }.onFailure {
                reportException(it)
                Toast.makeText(context, R.string.backup_create_failed, Toast.LENGTH_SHORT).show()
            }
        }

        fun restore(
            context: Context,
            uri: Uri,
        ) {
            runCatching {
                context.applicationContext.contentResolver.openInputStream(uri)?.use {
                    it.zipInputStream().use { inputStream ->
                        var entry = inputStream.nextEntry
                        while (entry != null) {
                            when (entry.name) {
                                SETTINGS_FILENAME -> {
                                    (context.filesDir / "datastore" / SETTINGS_FILENAME).outputStream().use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }

                                InternalDatabase.DB_NAME -> {
                                    runBlocking(Dispatchers.IO) {
                                        database.checkpoint()
                                    }
                                    database.close()
                                    FileOutputStream(database.openHelper.writableDatabase.path).use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                            }
                            entry = inputStream.nextEntry
                        }
                    }
                }
                context.stopService(Intent(context, MusicService::class.java))
                context.filesDir.resolve(PERSISTENT_QUEUE_FILE).delete()
                context.filesDir.resolve(PERSISTENT_QUEUE_FILE).delete()
                context.startActivity(Intent(context, MainActivity::class.java))
                exitProcess(0)
            }.onFailure {
                reportException(it)
                Toast.makeText(context, R.string.restore_failed, Toast.LENGTH_SHORT).show()
            }
        }

        companion object {
            const val SETTINGS_FILENAME = "settings.preferences_pb"
        }
    }
