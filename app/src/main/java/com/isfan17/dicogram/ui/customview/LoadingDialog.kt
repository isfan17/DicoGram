package com.isfan17.dicogram.ui.customview

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.isfan17.dicogram.R


class LoadingDialog(
    private val activity: Activity
) {
    lateinit var dialog: AlertDialog

    fun start() {
        val builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = activity.layoutInflater

        builder.setView(inflater.inflate(R.layout.loading_dialog, null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun stop() {
        dialog.dismiss()
    }
}