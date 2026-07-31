package com.dishly.app

import android.app.Application
import com.dishly.app.notifications.DishlyNotifications

class DishlyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DishlyNotifications.ensureChannel(this)
    }
}
