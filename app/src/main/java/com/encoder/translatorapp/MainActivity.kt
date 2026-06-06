package com.encoder.translatorapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.encoder.translatorapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private enum class ConvertMode {
        BASE64, URL_ENCODE, UNICODE, HTML_ENTITY, XML, MORSE, ENGLISH, ICELANDIC
    }

    private var selectedMode: ConvertMode? = null
    private var isEncodeMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupChips()
        setupButtons()
        updateModeUI()
    }

    private fun setupChips() {
        val chipModeMap = mapOf(
            binding.chipBase64 to ConvertMode.BASE64,
            binding.chipUrl to ConvertMode.URL_ENCODE,
            binding.chipUnicode to ConvertMode.UNICODE,
            binding.chipHtml to ConvertMode.HTML_ENTITY,
            binding.chipXml to ConvertMode.XML,
            binding.chipMorse to ConvertMode.MORSE,
            binding.chipEnglish to ConvertMode.ENGLISH,
            binding.chipIcelandic to ConvertMode.ICELANDIC
        )

        chipModeMap.forEach { (chip, mode) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedMode = mode
                }
            }
        }
    }

    private fun setupButtons() {
        binding.modeToggleButton.setOnClickListener {
            isEncodeMode = !isEncodeMode
            updateModeUI()
        }

        binding.convertButton.setOnClickListener {
            val input = binding.inputText.text?.toString()?.trim()
            if (input.isNullOrEmpty()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedMode == null) {
                Toast.makeText(this, "请选择转换方式", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isEncodeMode) encode(input) else decode(input)
        }

        binding.copyButton.setOnClickListener {
            val text = binding.outputText.text?.toString()
            if (text.isNullOrEmpty()) {
                Toast.makeText(this, "没有可复制的内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("result", text))
            Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateModeUI() {
        if (isEncodeMode) {
            binding.modeToggleButton.text = "编码"
            binding.convertButton.text = "编 码"
            binding.subtitleText.text = "中文 → 编码 / 翻译"
        } else {
            binding.modeToggleButton.text = "解码"
            binding.convertButton.text = "解 码"
            binding.subtitleText.text = "编码 / 外语 → 中文"
        }
    }

    private fun encode(input: String) {
        when (selectedMode) {
            ConvertMode.BASE64 -> showResult(Encoders.toBase64(input))
            ConvertMode.URL_ENCODE -> showResult(Encoders.toUrlEncoding(input))
            ConvertMode.UNICODE -> showResult(Encoders.toUnicodeEscape(input))
            ConvertMode.HTML_ENTITY -> showResult(Encoders.toHtmlEntities(input))
            ConvertMode.XML -> showResult(Encoders.toXml(input))
            ConvertMode.MORSE -> showResult(Encoders.toMorseCode(input))
            ConvertMode.ENGLISH -> translateOnline(input, "zh", "en")
            ConvertMode.ICELANDIC -> translateOnline(input, "zh", "is")
            null -> {}
        }
    }

    private fun decode(input: String) {
        when (selectedMode) {
            ConvertMode.BASE64 -> safeDecodeResult { Encoders.fromBase64(input) }
            ConvertMode.URL_ENCODE -> safeDecodeResult { Encoders.fromUrlEncoding(input) }
            ConvertMode.UNICODE -> safeDecodeResult { Encoders.fromUnicodeEscape(input) }
            ConvertMode.HTML_ENTITY -> safeDecodeResult { Encoders.fromHtmlEntities(input) }
            ConvertMode.XML -> safeDecodeResult { Encoders.fromXml(input) }
            ConvertMode.MORSE -> safeDecodeResult { Encoders.fromMorseCode(input) }
            ConvertMode.ENGLISH -> translateOnline(input, "en", "zh")
            ConvertMode.ICELANDIC -> translateOnline(input, "is", "zh")
            null -> {}
        }
    }

    private fun safeDecodeResult(block: () -> String) {
        try {
            showResult(block())
        } catch (e: Exception) {
            showResult("解码失败: ${e.message}")
        }
    }

    private fun translateOnline(input: String, sourceLang: String, targetLang: String) {
        binding.outputText.text = "翻译中..."
        binding.convertButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = TranslationApi.translate(input, sourceLang, targetLang)
                showResult(result)
            } catch (e: Exception) {
                showResult("翻译失败: ${e.message}")
            } finally {
                binding.convertButton.isEnabled = true
            }
        }
    }

    private fun showResult(result: String) {
        binding.outputText.text = result
    }
}
