package com.apkupdater.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.apkupdater.repository.UpdatesRepository
import com.apkupdater.util.millisUntilHour
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class UpdatesWorker(
    context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams), KoinComponent {

    companion object {

        private const val TAG = "UpdatesWorker"

        fun launch(context: Context) {
            val request = PeriodicWorkRequestBuilder<UpdatesWorker>(1L, TimeUnit.DAYS)
                .setInitialDelay(millisUntilHour(12), TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

    }

    private val updatesRepository: UpdatesRepository by inject()

    override suspend fun doWork(): Result {
        Log.e("UpdatesWorker", "Start")
        updatesRepository.updates().collect {
            // TODO: Send notification
            Log.e("UpdatesWorker", "Got ${it.size} updates.")
        }
        Log.e("UpdatesWorker", "Stop")
        return Result.success()
    }

}
