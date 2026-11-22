package com.dong.app

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.security.MessageDigest

class MainActivity : Activity() {

    // SHA-256 của key (không lưu key dạng chữ thường trong code)
    private val keyHash = "d9d3341ac9a1187aa45ecff5bbf492c5bfa18e1d83dee39fe2f9957ea262e6db"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val unlocked = getSharedPreferences("dong", MODE_PRIVATE).getBoolean("unlocked", false)
        if (unlocked) showVm() else showKeyScreen()
    }

    private fun sha256(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun showKeyScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(32), dp(32), dp(32), dp(32))
        }
        root.addView(TextView(this).apply {
            text = "Đồng"
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "Nhập key để sử dụng máy ảo"
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(24))
        })
        val input = EditText(this).apply {
            hint = "Key"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSingleLine = true
        }
        root.addView(input)
        root.addView(Button(this).apply {
            text = "Mở khóa"
            setOnClickListener {
                if (sha256(input.text.toString().trim()) == keyHash) {
                    getSharedPreferences("dong", MODE_PRIVATE)
                        .edit().putBoolean("unlocked", true).apply()
                    showVm()
                } else {
                    Toast.makeText(this@MainActivity, "Key sai", Toast.LENGTH_SHORT).show()
                }
            }
        })
        setContentView(root)
    }

    private fun showVm() {
        val sample = """
            ; Tính giai thừa 5
            MOV R0, 1
            MOV R1, 5
            loop:
            MUL R0, R1
            SUB R1, 1
            JNZ R1, loop
            PRINT R0
            HALT
        """.trimIndent()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        root.addView(TextView(this).apply {
            text = "Đồng - Máy ảo"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "Lệnh: MOV, ADD, SUB, MUL, PRINT, JMP, JNZ, HALT. Thanh ghi R0..R7."
            textSize = 12f
            setPadding(0, dp(4), 0, dp(8))
        })
        val code = EditText(this).apply {
            setText(sample)
            typeface = Typeface.MONOSPACE
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            gravity = Gravity.TOP
            minLines = 8
        }
        root.addView(code)
        val output = TextView(this).apply {
            typeface = Typeface.MONOSPACE
            setPadding(0, dp(12), 0, 0)
        }
        root.addView(Button(this).apply {
            text = "Chạy"
            setOnClickListener {
                output.text = Vm().run(code.text.toString()).ifEmpty { "(không có kết quả)" }
            }
        })
        root.addView(output)
        root.addView(Button(this).apply {
            text = "Khóa lại"
            setOnClickListener {
                getSharedPreferences("dong", MODE_PRIVATE)
                    .edit().putBoolean("unlocked", false).apply()
                showKeyScreen()
            }
        })

        val scroll = ScrollView(this)
        scroll.addView(root)
        scroll.visibility = View.VISIBLE
        setContentView(scroll)
    }
}
