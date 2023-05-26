package com.isfan17.dicogram.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.isfan17.dicogram.R
import com.isfan17.dicogram.utils.Helper

class MyEditText: AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val textEmailAddress = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                val textPassword = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD

                if (inputType == textEmailAddress) {
                    error = if (s.isNotEmpty())
                    {
                        if (!Helper.isValidEmail(s.toString())) {
                            context.getString(R.string.ed_validation_email_error)
                        } else null
                    }
                    else null
                }
                else if (inputType == textPassword) {
                    error = if (s.length < 8) context.getString(R.string.ed_validation_password_error) else null
                }
            }

            override fun afterTextChanged(edt: Editable?) {
                // do nothing
            }
        })
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        maxLines = 1
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }
}