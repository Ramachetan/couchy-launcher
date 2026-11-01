package com.conreo.couchytv

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

/** Opt-in Home redirect for Google TV devices that ignore the HOME role. */
class HomeRedirectService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.packageName?.toString() != GOOGLE_HOME ||
            event.className?.toString() != "$GOOGLE_HOME.home.HomeActivity" ||
            Settings.Secure.getInt(contentResolver, "user_setup_complete", 0) != 1
        ) return

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val now = SystemClock.elapsedRealtime()
        val pending = prefs.getLong("pending", -1)
        if (pending >= 0 && now >= pending && now - pending < 3000) return

        // Persist attempts before launching, so a crashing activity cannot cause a loop.
        // A successful activity resume or a manual launch clears this circuit breaker.
        val failures = prefs.getInt("failures", 0)
        if (failures >= 3) return
        if (!prefs.edit().putLong("pending", now).putInt("failures", failures + 1).commit()) return
        try {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: RuntimeException) {
            // Leave Google TV in front; stop retrying after three unsuccessful launches.
        }
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val GOOGLE_HOME = "com.google.android.apps.tv.launcherx"
        private const val PREFS = "home_redirect"

        fun onHomeResumed(context: Context) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        }

        fun isEnabled(context: Context): Boolean {
            if (Settings.Secure.getInt(
                    context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0,
                ) != 1
            ) return false
            val services = Settings.Secure.getString(
                context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ).orEmpty().split(':')
            return services.any {
                it == "${context.packageName}/.HomeRedirectService" ||
                    it == "${context.packageName}/${HomeRedirectService::class.java.name}"
            }
        }
    }
}
