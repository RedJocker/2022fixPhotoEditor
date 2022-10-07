package org.hyperskill.photoeditor

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class Permission(private val context: Context, private val permission: String) {

    fun check(callback: Callback){
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) -> callback.onHasPermission()
            else -> callback.onNoPermission()
        }
    }

    interface Callback {
        fun onHasPermission()
        fun onNoPermission()
    }
}