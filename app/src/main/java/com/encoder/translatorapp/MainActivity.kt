package com.encoder.translatorapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.encoder.translatorapp.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
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
                    updateChipColors()
                }
            }
        }
    }

    private fun updateChipColors() {
        val chipGroup = binding.chipGroup
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip ?: continue
            if (chip.isChecked) {
                chip.setChipBackgroundColorResource(R.color.chip_selected)
                chip.setTextColor(getColor(R.color.black))
            } else {
                chip.setChipBackgroundColorResource(R.color.chip_unselected)
                chip.setTextColor(getColor(R.color.white))
            }
        }
    }

    private fun setupButtons() {
        binding.convertButton.setOnClickListener {
            val input = binding.inputText.text?.toString()?.trim()
            if (input.isNullOrEmpty()) {
                Toast.makeText(this, "请输入中文", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedMode == null) {
                Toast.makeText(this, "请选择转换方式", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            convert(input)
        }

        binding.copyButton.setOnClickListener {
            val text = binding.outputText.text?.toString()
            if (text.isNullOrEmpty()) {
                Toast.makeText(this, "没有可复制的内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("encoded", text))
            Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convert(input: String) {
        when (selectedMode) {
            ConvertMode.BASE64 -> showResult(Encoders.toBase64(input))
            ConvertMode.URL_ENCODE -> showResult(Encoders.toUrlEncoding(input))
            ConvertMode.UNICODE -> showResult(Encoders.toUnicodeEscape(input))
            ConvertMode.HTML_ENTITY -> showResult(Encoders.toHtmlEntities(input))
            ConvertMode.XML -> showResult(Encoders.toXml(input))
            ConvertMode.MORSE -> showResult(Encoders.toMorseCode(input))
            ConvertMode.ENGLISH -> translateOnline(input, "en")
            ConvertMode.ICELANDIC -> translateOnline(input, "is")
            null -> {}
        }
    }

    private fun translateOnline(input: String, targetLang: String) {
        binding.outputText.text = "翻译中..."
        binding.convertButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = TranslationApi.translate(input, targetLang)
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
