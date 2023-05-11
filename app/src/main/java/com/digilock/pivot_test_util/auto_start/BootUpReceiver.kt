package com.digilock.pivot_test_util.auto_start

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.digilock.pivot_test_util.main.MainActivity



class BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(LOG_TAG, "Bootup: SM Controller received: ${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(LOG_TAG, "Bootup close all previous instances")
//            val intent0 = Intent(CLOSE_ALL_PREVIOUS_INSTANCES)
//            context.sendBroadcast(intent0)


            Log.i(LOG_TAG, "SM Controller Bootup")
//            Handler().postDelayed({
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
//            }, 5000)
        }
    }

    companion object {
        private val LOG_TAG: String = BootUpReceiver::class.java.simpleName

        val CLOSE_ALL_PREVIOUS_INSTANCES = "SM.controller.close_all_previous_instances."
    }
}