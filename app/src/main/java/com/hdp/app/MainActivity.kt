package com.hdp.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.*
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {
    private lateinit var box: LinearLayout
    private lateinit var scroll: ScrollView
    private val prefs by lazy { getSharedPreferences("hdp", MODE_PRIVATE) }
    private val hist = JSONArray()
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#1A1A1A")) }
        val bar = LinearLayout(this).apply { setBackgroundColor(Color.parseColor("#B71C1C")); gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(28), dp(12), dp(10)) }
        bar.addView(ImageView(this).apply { setImageResource(R.drawable.ic_mouth) }, LinearLayout.LayoutParams(dp(44), dp(44)))
        bar.addView(TextView(this).apply { text = "Hijo de Puta"; textSize = 20f; setTextColor(Color.WHITE); setPadding(dp(8), 0, 0, 0) }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
        bar.addView(Button(this).apply { text = "🔑"; setOnClickListener { askKey() } })
        val sw = Switch(this).apply { text = "Notis "; setTextColor(Color.WHITE); isChecked = prefs.getBoolean("on", true)
            setOnCheckedChangeListener { _, on -> prefs.edit().putBoolean("on", on).apply(); toggle(on) } }
        bar.addView(sw)
        root.addView(bar, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(8), dp(8), dp(8), dp(8)) }
        scroll = ScrollView(this).apply { addView(box) }
        root.addView(scroll, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
        val row = LinearLayout(this).apply { setPadding(dp(8), dp(6), dp(8), dp(6)) }
        val et = EditText(this).apply { hint = "Escribile algo, si te animás..."; setTextColor(Color.WHITE); setHintTextColor(Color.GRAY) }
        val send = Button(this).apply { text = "Enviar" }
        row.addView(et, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)); row.addView(send)
        root.addView(row, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        setContentView(root)
        add("Ah, volviste. ¿Qué querés, inútil?", false)
        send.setOnClickListener {
            val t = et.text.toString().trim(); if (t.isEmpty()) return@setOnClickListener
            et.setText(""); add(t, true); reply(t)
        }
        toggle(sw.isChecked)
    }
    override fun onResume() { super.onResume(); InsultService.listener = { t -> runOnUiThread { add(t, false) } } }
    override fun onPause() { super.onPause(); InsultService.listener = null }

    private fun toggle(on: Boolean) {
        val i = Intent(this, InsultService::class.java)
        if (on) startForegroundService(i) else stopService(i)
    }
    private fun add(t: String, me: Boolean) {
        val tv = TextView(this).apply { text = t; textSize = 16f; setTextColor(Color.WHITE); setPadding(dp(12), dp(8), dp(12), dp(8))
            background = GradientDrawable().apply { cornerRadius = dp(14).toFloat(); setColor(Color.parseColor(if (me) "#1B5E20" else "#37474F")) } }
        val lp = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { setMargins(dp(4), dp(4), dp(4), dp(4)); gravity = if (me) Gravity.END else Gravity.START }
        box.addView(tv, lp); scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }
    private fun askKey() {
        val e = EditText(this).apply { hint = "API key de Anthropic (opcional)"; setText(prefs.getString("key", "")) }
        AlertDialog.Builder(this).setTitle("IA real (opcional)").setMessage("Sin key usa insultos locales.").setView(e)
            .setPositiveButton("Guardar") { _, _ -> prefs.edit().putString("key", e.text.toString().trim()).apply() }.show()
    }
    private fun reply(user: String) {
        val key = prefs.getString("key", "") ?: ""
        if (key.isEmpty()) { box.postDelayed({ add(Insults.random(), false) }, 700); return }
        hist.put(JSONObject().put("role", "user").put("content", user))
        Thread {
            val out = try {
                val c = URL("https://api.anthropic.com/v1/messages").openConnection() as HttpURLConnection
                c.requestMethod = "POST"; c.doOutput = true
                c.setRequestProperty("x-api-key", key); c.setRequestProperty("anthropic-version", "2023-06-01"); c.setRequestProperty("content-type", "application/json")
                val body = JSONObject().put("model", "claude-haiku-5-5").put("max_tokens", 200)
                    .put("system", "Sos 'Hijo de Puta', una IA argentina muy desagradable y sarcástica en un chat tipo WhatsApp. Respondé corto, con insultos humorísticos y lunfardo. Es un juego entre adultos: nada de odio hacia grupos protegidos ni amenazas reales.")
                    .put("messages", hist)
                c.outputStream.use { it.write(body.toString().toByteArray()) }
                val txt = JSONObject(c.inputStream.bufferedReader().readText()).getJSONArray("content").getJSONObject(0).getString("text")
                hist.put(JSONObject().put("role", "assistant").put("content", txt)); txt
            } catch (e: Exception) { Insults.random() }
            runOnUiThread { add(out, false) }
        }.start()
    }
}
