package com.dishly.app.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dishly.app.BuildConfig
import java.util.concurrent.TimeUnit

object RecipeReminderScheduler {

    /** Production delay after viewing a recipe. */
    private val DELAY_HOURS = 3L

    /** Shorter delay in debug builds so you can test without waiting hours. */
    private val DEBUG_DELAY_SECONDS = 30L

    fun schedule(context: Context, recipeId: Int, recipeTitle: String) {
        if (recipeId <= 0 || recipeTitle.isBlank()) return

        val data = workDataOf(
            RecipeReminderWorker.KEY_RECIPE_ID to recipeId,
            RecipeReminderWorker.KEY_RECIPE_TITLE to recipeTitle
        )

        val requestBuilder = OneTimeWorkRequestBuilder<RecipeReminderWorker>()
            .setInputData(data)
            .addTag(workName(recipeId))

        if (BuildConfig.DEBUG) {
            requestBuilder.setInitialDelay(DEBUG_DELAY_SECONDS, TimeUnit.SECONDS)
        } else {
            requestBuilder.setInitialDelay(DELAY_HOURS, TimeUnit.HOURS)
        }

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(recipeId),
            ExistingWorkPolicy.REPLACE,
            requestBuilder.build()
        )
    }

    fun cancel(context: Context, recipeId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(recipeId))
    }

    private fun workName(recipeId: Int) = "recipe_reminder_$recipeId"
}
