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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupChips()
        setupButtons()
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
        binding.encodeButton.setOnClickListener {
            val input = binding.inputChinese.text?.toString()?.trim()
            if (input.isNullOrEmpty()) {
                Toast.makeText(this, "请在上方输入中文", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedMode == null) {
                Toast.makeText(this, "请选择转换方式", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            encode(input)
        }

        binding.decodeButton.setOnClickListener {
            val input = binding.inputEncoded.text?.toString()?.trim()
            if (input.isNullOrEmpty()) {
                Toast.makeText(this, "请在下方输入编码内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedMode == null) {
                Toast.makeText(this, "请选择转换方式", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            decode(input)
        }

        binding.copyTopButton.setOnClickListener {
            copyToClipboard(binding.inputChinese.text?.toString(), "中文")
        }

        binding.copyBottomButton.setOnClickListener {
            copyToClipboard(binding.inputEncoded.text?.toString(), "编码")
        }
    }

    private fun copyToClipboard(text: String?, label: String) {
        if (text.isNullOrEmpty()) {
            Toast.makeText(this, "没有可复制的内容", Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show()
    }

    private fun encode(input: String) {
        when (selectedMode) {
            ConvertMode.BASE64 -> showEncoded(Encoders.toBase64(input))
            ConvertMode.URL_ENCODE -> showEncoded(Encoders.toUrlEncoding(input))
            ConvertMode.UNICODE -> showEncoded(Encoders.toUnicodeEscape(input))
            ConvertMode.HTML_ENTITY -> showEncoded(Encoders.toHtmlEntities(input))
            ConvertMode.XML -> showEncoded(Encoders.toXml(input))
            ConvertMode.MORSE -> showEncoded(Encoders.toMorseCode(input))
            ConvertMode.ENGLISH -> translateOnline(input, "zh", "en", false)
            ConvertMode.ICELANDIC -> translateOnline(input, "zh", "is", false)
            null -> {}
        }
    }

    private fun decode(input: String) {
        when (selectedMode) {
            ConvertMode.BASE64 -> safeDecode { Encoders.fromBase64(input) }
            ConvertMode.URL_ENCODE -> safeDecode { Encoders.fromUrlEncoding(input) }
            ConvertMode.UNICODE -> safeDecode { Encoders.fromUnicodeEscape(input) }
            ConvertMode.HTML_ENTITY -> safeDecode { Encoders.fromHtmlEntities(input) }
            ConvertMode.XML -> safeDecode { Encoders.fromXml(input) }
            ConvertMode.MORSE -> safeDecode { Encoders.fromMorseCode(input) }
            ConvertMode.ENGLISH -> translateOnline(input, "en", "zh", true)
            ConvertMode.ICELANDIC -> translateOnline(input, "is", "zh", true)
            null -> {}
        }
    }

    private fun showEncoded(result: String) {
        binding.inputEncoded.setText(result)
    }

    private fun showDecoded(result: String) {
        binding.inputChinese.setText(result)
    }

    private fun safeDecode(block: () -> String) {
        try {
            showDecoded(block())
        } catch (e: Exception) {
            Toast.makeText(this, "解码失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun translateOnline(input: String, sourceLang: String, targetLang: String, isDecode: Boolean) {
        val loadingTarget = if (isDecode) binding.inputChinese else binding.inputEncoded
        loadingTarget.setText("翻译中...")
        binding.encodeButton.isEnabled = false
        binding.decodeButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = TranslationApi.translate(input, sourceLang, targetLang)
                if (isDecode) showDecoded(result) else showEncoded(result)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "翻译失败: ${e.message}", Toast.LENGTH_SHORT).show()
                loadingTarget.setText("")
            } finally {
                binding.encodeButton.isEnabled = true
                binding.decodeButton.isEnabled = true
            }
        }
    }
}
