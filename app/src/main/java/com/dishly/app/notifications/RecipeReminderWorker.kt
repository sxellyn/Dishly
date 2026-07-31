package com.dishly.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RecipeReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val recipeId = inputData.getInt(KEY_RECIPE_ID, -1)
        val recipeTitle = inputData.getString(KEY_RECIPE_TITLE).orEmpty()
        if (recipeId <= 0 || recipeTitle.isBlank()) return Result.failure()

        val (title, body) = ReminderCopy.pick(recipeTitle)
        DishlyNotifications.showRecipeReminder(
            context = applicationContext,
            recipeId = recipeId,
            title = title,
            body = body
        )
        return Result.success()
    }

    companion object {
        const val KEY_RECIPE_ID = "recipe_id"
        const val KEY_RECIPE_TITLE = "recipe_title"
    }
}

object ReminderCopy {
    private data class Template(val title: String, val body: (String) -> String)

    private val templates = listOf(
        Template("Still hungry?") { name ->
            "You checked out $name earlier… fancy cooking it tonight?"
        },
        Template("Kitchen calling") { name ->
            "Hey, $name looked pretty good. Don't leave it hanging!"
        },
        Template("Quick reminder") { name ->
            "That $name isn't going to cook itself. Just saying."
        },
        Template("Craving alert") { name ->
            "Remember $name? Your future self (and stomach) would thank you."
        },
        Template("Dishly nudge") { name ->
            "No pressure, but $name would be a great idea right about now."
        },
        Template("Missed opportunity?") { name ->
            "You opened $name a while ago. Ready to actually make it?"
        }
    )

    fun pick(recipeTitle: String): Pair<String, String> {
        val shortTitle = recipeTitle.trim().let { if (it.length > 40) it.take(37) + "…" else it }
        val template = templates.random()
        return template.title to template.body(shortTitle)
    }
}
