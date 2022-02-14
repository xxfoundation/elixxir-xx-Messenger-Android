package io.xxlabs.messenger.support.misc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Environment
import android.text.format.Formatter
import androidx.core.content.FileProvider
import io.reactivex.Single
import io.xxlabs.messenger.support.extensions.toast
import io.xxlabs.messenger.support.ioThread
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class DebugLogger {
    companion object {
        lateinit var logFile: File
        private var runtime = Runtime.getRuntime()
        private var runningProcess: Process? = null

        //Consts
        private val dateFormat = SimpleDateFormat("MM_dd_yyyy", Locale.ENGLISH)
        private val timeFormat = SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.ENGLISH)
        private val currDate = Calendar.getInstance()

        private fun isLogInitialized() = ::logFile.isInitialized

        fun exportLatestLog(context: Context) {
            if (isLogInitialized() && logFile.exists()) {
                val shareIntent = Intent()
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "io.xxlabs.messenger.fileprovider",
                    logFile
                )

                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                val chooser = Intent.createChooser(shareIntent, "Share with")

                val resInfoList: List<ResolveInfo> = context.packageManager
                    .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(
                        packageName,
                        fileUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                context.startActivity(chooser)
            } else {
                context.toast("The logs were either disabled or deleted")
            }
        }

        fun getLogSize(context: Context): String {
            return if (isLogInitialized() && logFile.exists()) {
                Formatter.formatShortFileSize(context, logFile.length())
            } else {
                "-"
            }
        }

        private fun appDirectory(context: Context): File {
            return File(context.getExternalFilesDir(null), "logs")
        }

        /* Checks if external storage is available for read and write */
        val isExternalStorageWritable: Boolean
            get() {
                val state = Environment.getExternalStorageState()
                return Environment.MEDIA_MOUNTED == state
            }

        fun initService(context: Context): Single<Boolean> {
            return Single.create { emitter ->
                val appDirectory = appDirectory(context)
                if (isExternalStorageWritable) {
                    logFile = File(appDirectory, "log_" + dateFormat.format(currDate.time) + ".txt")

                    // create app folder
                    if (!appDirectory.exists()) {
                        appDirectory.mkdir()
                    }

                    deleteOldLogs(context)

                    // clear the previous logcat and then write the new one to the file
                    try {
                        ioThread {
                            runningProcess = runtime.exec("logcat -c")
                            if (isLogInitialized() && logFile.exists()) {
                                FileWriter(logFile, true).use { writer ->
                                    writer.appendLine(
                                        "\n[New Session - " + timeFormat.format(currDate.time) + "]\n"
                                    )
                                    writer.appendLine(
                                        "\n[Phone Model: ${Build.MANUFACTURER} - ${Build.MODEL} - ${Build.BRAND}"
                                    )
                                    writer.appendLine(
                                        "\n[SDK: ${Build.VERSION.SDK_INT} - ${Build.VERSION.RELEASE}\n"
                                    )

                                    writer.appendLine(
                                        "\n[SDK: ${Build.VERSION.SDK_INT} - ${Build.VERSION.RELEASE}\n"
                                    )

                                    runningProcess = runtime.exec("logcat -f $logFile")
                                }
                            } else {
                                runningProcess = runtime.exec("logcat -f $logFile")
                            }
                            Timber.v("Started running logcat")
                        }
                        emitter.onSuccess(true)
                    } catch (err: IOException) {
                        Timber.e("Error: ${err.localizedMessage}")
                        runningProcess = runtime.exec("logcat -f $logFile")
                        emitter.onError(err)
                    }
                } else {
                    emitter.onError(Exception("Storage is not writeable"))
                }
            }
        }

        private fun deleteOldLogs(context: Context) {
            appDirectory(context).walk().forEach { file ->
                val filename = file.name
                if (!file.isDirectory && !filename.startsWith("log_")) {
                    file.delete()
                } else {
                    val date = filename.substringAfter("log_").substringBefore(".txt")
                    if (date.matches(Regex("([0-9]{2})_([0-9]{2})_([0-9]{4})"))) {
                        dateFormat.parse(date)?.let {
                            if (getDateDiff(it, currDate.time) >= 1) {
                                file.delete()
                            }
                        }
                    }
                }
            }
        }

        private fun deleteAllLogs(context: Context) {
            appDirectory(context).deleteRecursively()
        }

        private fun getDateDiff(
            oldDate: Date,
            newDate: Date
        ): Long {
            return try {
                TimeUnit.DAYS.convert(
                    newDate.time - oldDate.time,
                    TimeUnit.MILLISECONDS
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                0
            }
        }

        fun cancelProcess(context: Context) {
            runningProcess?.destroy().apply {
                Timber.v("Finished running logcat")
                runningProcess = null
                deleteAllLogs(context)
            }
        }
    }
}