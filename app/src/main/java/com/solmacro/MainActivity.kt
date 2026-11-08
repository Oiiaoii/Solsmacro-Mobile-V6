package com.solmacro

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

private const val FILL = ViewGroup.LayoutParams.MATCH_PARENT
private const val HUG = ViewGroup.LayoutParams.WRAP_CONTENT

class MainActivity : Activity() {
    private val cBg = 0xFF0B111C.toInt()
    private val cCard = 0xFF151E30.toInt()
    private val cBd = 0xFF243050.toInt()
    private val cTx = 0xFFE8EEFC.toInt()
    private val cMut = 0xFF8B9BBD.toInt()
    private val cAcc = 0xFF3EA6FF.toInt()
    private val cOk = 0xFF38D39F.toInt()
    private val cBad = 0xFFFF6B6B.toInt()

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var root: LinearLayout
    private lateinit var pill: TextView
    private lateinit var stateTv: TextView
    private lateinit var statsTv: TextView
    private lateinit var posTv: TextView
    private lateinit var logTv: TextView

    private val tick = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 500)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun shape(color: Int, r: Int, stroke: Int = 0) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(r).toFloat()
        if (stroke != 0) setStroke(dp(1), stroke)
    }

    private fun tv(t: String, size: Float, color: Int = cTx, bold: Boolean = false) = TextView(this).apply {
        text = t
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun add(p: LinearLayout, v: View, top: Int = 8) {
        p.addView(v, LinearLayout.LayoutParams(FILL, HUG).apply { topMargin = dp(top) })
    }

    private fun card(title: String?): LinearLayout {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = shape(cCard, 16, cBd)
            setPadding(dp(16), dp(14), dp(16), dp(16))
        }
        add(root, c, 12)
        if (title != null) c.addView(tv(title, 16f, cTx, true))
        return c
    }

    private fun btn(t: String, color: Int, onClick: () -> Unit) = Button(this).apply {
        text = t
        isAllCaps = false
        setTextColor(if (color == cOk) 0xFF06251C.toInt() else Color.WHITE)
        background = shape(color, 12)
        setOnClickListener { onClick() }
    }

    private fun row(p: LinearLayout, vararg b: Button) {
        val r = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for ((i, x) in b.withIndex()) {
            r.addView(x, LinearLayout.LayoutParams(0, HUG, 1f).apply { if (i > 0) leftMargin = dp(8) })
        }
        add(p, r, 10)
    }

    private fun field(p: LinearLayout, label: String, key: String) {
        add(p, tv(label, 12f, cMut), 10)
        val e = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(Cfg.get(this@MainActivity, key).toString())
            setTextColor(cTx)
            background = shape(cBg, 10, cBd)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    Cfg.put(this@MainActivity, key, s.toString().toIntOrNull() ?: 0)
                }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
        }
        add(p, e, 4)
    }

    private fun sw(p: LinearLayout, label: String, key: String) {
        val s = Switch(this).apply {
            text = label
            setTextColor(cTx)
            isChecked = Cfg.get(this@MainActivity, key) == 1
            setOnCheckedChangeListener { _, on -> Cfg.put(this@MainActivity, key, if (on) 1 else 0) }
        }
        add(p, s, 10)
    }

    private fun preset(cast: Int, wait: Int, taps: Int, gap: Int) {
        Cfg.put(this, "cast", cast); Cfg.put(this, "wait", wait); Cfg.put(this, "taps", taps); Cfg.put(this, "gap", gap)
        recreate()
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(40), dp(16), dp(32))
        }
        root.addView(tv("Sol Macro", 28f, cTx, true))
        root.addView(tv("Fishing and selling helper for Sol’s RNG. Not affiliated with Roblox. Macros can break game rules and get accounts banned.", 12f, cMut))

        val st = card(null)
        pill = tv("", 12f, Color.WHITE, true).apply { setPadding(dp(14), dp(6), dp(14), dp(6)) }
        st.addView(pill, LinearLayout.LayoutParams(HUG, HUG))
        stateTv = tv("", 15f)
        add(st, stateTv, 10)
        statsTv = tv("", 13f, cMut)
        add(st, statsTv, 4)
        posTv = tv("", 11f, cMut)
        add(st, posTv, 10)
        row(st,
            btn("Turn on service", cAcc) { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
            btn("Open Roblox", cBd) {
                val i = packageManager.getLaunchIntentForPackage(Cfg.ROBLOX)
                if (i != null) startActivity(i) else Toast.makeText(this, "Roblox is not installed", Toast.LENGTH_SHORT).show()
            })

        val how = card("How to use")
        add(how, tv("1. Turn on the service, then open Roblox. A floating bar appears.\n2. SET: tap the 5 buttons it asks for.\n3. COLOR: start fishing, the screen freezes after 8 seconds. Tap the bar corners, the green zone and the white marker.\n4. TEST: check the result while the bar shows. Turn on the debug overlay below to see boxes.\n5. START. Drag the bar to move it.", 12f, cMut), 6)

        val fish = card("Fishing")
        row(fish, btn("Fast", cBd) { preset(700, 3000, 15, 80) }, btn("Balanced", cBd) { preset(900, 4000, 12, 120) }, btn("Safe", cBd) { preset(1000, 5500, 10, 160) })
        field(fish, "Cast hold (ms)", "cast")
        field(fish, "Bite wait (ms), timed mode", "wait")
        field(fish, "Reel taps, timed mode", "taps")
        field(fish, "Tap gap (ms), timed mode", "gap")
        field(fish, "Stop after N catches (0 = never)", "maxcatch")

        val det = card("Screen detection (Android 11+)")
        sw(det, "Use screen detection instead of timing", "detect")
        sw(det, "Debug overlay (green, yellow, red box)", "debug")
        field(det, "Color match tolerance (0-255)", "tol")
        field(det, "Minimum matching pixels", "minpx")
        field(det, "Max seconds to wait for the bar", "maxwait")
        field(det, "Bar direction (0 left/right, 1 up/down)", "axis")
        field(det, "Holding moves the marker (0 left/up, 1 right/down)", "holddir")
        field(det, "Dead zone (pixels)", "deadband")
        field(det, "Hold time per move (ms)", "holdms")

        val sell = card("Selling")
        sw(sell, "Auto sell", "sell")
        field(sell, "Sell every N catches", "every")
        field(sell, "Menu delay (ms)", "mdelay")

        val wh = card("Discord webhook")
        sw(wh, "Send webhook messages", "whon")
        add(wh, tv("Webhook URL", 12f, cMut), 10)
        val url = EditText(this).apply {
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            setText(Cfg.gets(this@MainActivity, "webhook"))
            setTextColor(cTx)
            background = shape(cBg, 10, cBd)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { Cfg.puts(this@MainActivity, "webhook", s.toString().trim()) }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
        }
        add(wh, url, 4)
        field(wh, "Progress message every N catches (0 = off)", "whevery")
        add(wh, btn("Send test message", cAcc) {
            Hook.send(this, "Test", "Sol Macro webhook works", 0x38D39F)
            Toast.makeText(this, "Sent, check the log below", Toast.LENGTH_SHORT).show()
        }, 12)

        val lg = card("Log")
        logTv = tv("", 11f, cMut).apply { typeface = Typeface.MONOSPACE }
        add(lg, logTv, 8)
        add(lg, btn("Copy log", cBd) {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("Sol Macro log", Dbg.tail(150)))
            Toast.makeText(this, "Log copied", Toast.LENGTH_SHORT).show()
        }, 10)

        setContentView(ScrollView(this).apply {
            setBackgroundColor(cBg)
            addView(root)
        })
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
    }

    private fun refresh() {
        val on = (Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "")
            .contains("$packageName/")
        pill.text = if (on) "Service ON" else "Service OFF"
        pill.background = shape(if (on) cOk else cBad, 20)
        pill.setTextColor(if (on) 0xFF06251C.toInt() else Color.WHITE)
        stateTv.text = "State: " + Dbg.state
        val run = if (Dbg.startMs > 0 && Dbg.state != "Idle") (SystemClock.uptimeMillis() - Dbg.startMs) / 1000 else 0L
        statsTv.text = "Catches ${Dbg.catches}    Sells ${Dbg.sells}    Run ${run / 60}:${(run % 60).toString().padStart(2, '0')}"
        posTv.text = Cfg.keys.joinToString("   ") { k -> "${Cfg.label[k]}: ${Cfg.get(this, k + "_x")},${Cfg.get(this, k + "_y")}" }
        logTv.text = Dbg.tail(25).ifEmpty { "No log yet" }
    }
}
