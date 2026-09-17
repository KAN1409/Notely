package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val scheduler = ReminderScheduler(context)
            val db = AppDatabase.getInstance(context)

            CoroutineScope(Dispatchers.IO).launch {
                val active = db.reminderDao().getActiveRemindersSync()
                val now = System.currentTimeMillis()
                for (reminder in active) {
                    if (reminder.scheduledTimeMillis > now) {
                        scheduler.schedule(
                            reminderId = reminder.id,
                            noteId = reminder.noteId,
                            title = reminder.title,
                            timeMillis = reminder.scheduledTimeMillis
                        )
                    }
                }
            }
        }
    }
}
