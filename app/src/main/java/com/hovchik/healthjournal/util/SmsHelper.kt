package com.hovchik.healthjournal.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object SmsHelper {
    fun sendSms(context: Context, phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Health Report", text)
        clipboard.setPrimaryClip(clip)
    }
}
