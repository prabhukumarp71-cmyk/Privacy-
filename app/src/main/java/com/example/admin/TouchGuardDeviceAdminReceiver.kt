package com.example.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class TouchGuardDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, TouchGuardDeviceAdminReceiver::class.java)
        }

        fun isAdminActive(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            return dpm?.isAdminActive(getComponentName(context)) == true
        }

        fun lockDevice(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            return if (dpm != null && dpm.isAdminActive(getComponentName(context))) {
                dpm.lockNow()
                true
            } else {
                false
            }
        }

        fun createEnableAdminIntent(context: Context): Intent {
            val componentName = getComponentName(context)
            return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "TouchGuard requires Device Administrator permission to turn off the screen and lock the device instantly when an unauthorized touch or pick-up is detected."
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
