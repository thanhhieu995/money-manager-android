package com.henrystudio.moneymanager.presentation.setting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.application.PrefsManager
import com.henrystudio.moneymanager.databinding.FragmentSettingBinding
import com.henrystudio.moneymanager.presentation.main.MainActivity
import java.util.*

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding?= null
    private val binding get() = _binding!!
    private lateinit var languageLayout: LinearLayout
    private lateinit var currencyLayout: LinearLayout
    private lateinit var languageText: TextView
    private lateinit var currencyText: TextView
    private var selectedCurrency: AppCurrency = AppCurrency.VND
    private var selectLanguage = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        languageText.text = getCurrentLanguageName(requireContext())
        selectLanguage = getCurrentLanguageName(requireContext())
        languageLayout.setOnClickListener {
            showDialogOption()
        }

        selectedCurrency = PrefsManager.getCurrency(requireContext())
        currencyText.text = selectedCurrency.code

        currencyLayout.setOnClickListener {
            showCurrencyDialog()
        }
    }

    private fun init() {
        languageText = binding.settingSelectedLanguage
        currencyText = binding.settingSelectedCurrency
        languageLayout = binding.settingLanguage
        currencyLayout = binding.settingCurrency
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialogOption() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.item_language_dialog, null)
        bottomSheetDialog.setContentView(view)

        // Map ngôn ngữ -> Check icon
        val checkViews = mapOf(
            getString(R.string.lang_english) to view.findViewById<ImageView>(R.id.lang_enCheck),
            getString(R.string.lang_vietnamese) to view.findViewById<ImageView>(R.id.lang_viCheck),
            getString(R.string.lang_japanese) to view.findViewById<ImageView>(R.id.lang_jaCheck),
            getString(R.string.lang_korean) to view.findViewById<ImageView>(R.id.lang_koCheck),
            getString(R.string.lang_french) to view.findViewById<ImageView>(R.id.lang_frCheck),
            getString(R.string.lang_chinese) to view.findViewById<ImageView>(R.id.lang_zhCheck),
            getString(R.string.lang_russian) to view.findViewById<ImageView>(R.id.lang_ruCheck),
            getString(R.string.lang_german) to view.findViewById<ImageView>(R.id.lang_deCheck),
            getString(R.string.lang_spanish) to view.findViewById<ImageView>(R.id.lang_esCheck)
        )

        // Danh sách option: (Tên ngôn ngữ, layoutId, mã ngôn ngữ)
        val optionConfigs = listOf(
            Triple(getString(R.string.lang_english), R.id.lang_enLayout, "en"),
            Triple(getString(R.string.lang_vietnamese), R.id.lang_viLayout, "vi"),
            Triple(getString(R.string.lang_japanese), R.id.lang_jaLayout, "ja"),
            Triple(getString(R.string.lang_korean), R.id.lang_koLayout, "ko"),
            Triple(getString(R.string.lang_french), R.id.lang_frLayout, "fr"),
            Triple(getString(R.string.lang_chinese), R.id.lang_zhLayout, "zh"),
            Triple(getString(R.string.lang_russian), R.id.lang_ruLayout, "ru"),
            Triple(getString(R.string.lang_german), R.id.lang_deLayout, "de"),
            Triple(getString(R.string.lang_spanish), R.id.lang_esLayout, "es"),
        )

        fun updateCheckMarks(selected: String) {
            languageText.text = selected
            checkViews.forEach { (option, imageView) ->
                imageView.visibility = if (option == selected) View.VISIBLE else View.GONE
            }
            bottomSheetDialog.dismiss()
        }

        updateCheckMarks(selectLanguage) // hiển thị ban đầu

        optionConfigs.forEach { (optionName, layoutId, langCode) ->
            view.findViewById<LinearLayout>(layoutId).setOnClickListener {
                selectLanguage = optionName
                updateCheckMarks(optionName)

                // 1. Lưu ngôn ngữ mới để dùng cho lần khởi động app tiếp theo
                PrefsManager.saveLanguage(requireContext(), langCode)

                // 2. Yêu cầu hệ thống áp dụng ngôn ngữ mới ngay lập tức
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
                AppCompatDelegate.setApplicationLocales(appLocale)

                // 3. -> QUAN TRỌNG: Khởi động lại ứng dụng để áp dụng ngôn ngữ cho toàn bộ UI
                // Thay thế MainActivity::class.java bằng Activity chính của bạn
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)

                // Kết thúc activity hiện tại để người dùng không thể back lại màn hình setting cũ
                requireActivity().finish()
            }
        }

        view.findViewById<TextView>(R.id.lang_optionCancel).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun getCurrentLanguageName(context: Context): String {
        // Lấy ngôn ngữ đang dùng trong app
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val langCode = if (!currentLocales.isEmpty) {
            currentLocales[0]?.language
        } else {
            Locale.getDefault().language
        }

        return when (langCode) {
            "en" -> context.getString(R.string.lang_english)
            "vi" -> context.getString(R.string.lang_vietnamese)
            "fr" -> context.getString(R.string.lang_french)
            "de" -> context.getString(R.string.lang_german)
            "es" -> context.getString(R.string.lang_spanish)
            "ja" -> context.getString(R.string.lang_japanese)
            "ko" -> context.getString(R.string.lang_korean)
            "ru" -> context.getString(R.string.lang_russian)
            "zh" -> context.getString(R.string.lang_chinese)
            else -> context.getString(R.string.lang_english) // mặc định English
        }
    }

    private fun showCurrencyDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.item_currency_dialog, null)
        dialog.setContentView(view)

        // Map currency -> check icon
        val checkViews = mapOf(
            AppCurrency.VND to view.findViewById<ImageView>(R.id.currency_vndCheck),
            AppCurrency.USD to view.findViewById<ImageView>(R.id.currency_usdCheck),
            AppCurrency.EUR to view.findViewById<ImageView>(R.id.currency_eurCheck),
            AppCurrency.JPY to view.findViewById<ImageView>(R.id.currency_jpyCheck),
        )

        val options = listOf(
            Pair(AppCurrency.VND, R.id.currencyVndLayout),
            Pair(AppCurrency.USD, R.id.currencyUsdLayout),
            Pair(AppCurrency.EUR, R.id.currencyEurLayout),
            Pair(AppCurrency.JPY, R.id.currencyJpyLayout),
        )

        fun updateCheck(selected: AppCurrency) {
            currencyText.text = selected.code

            checkViews.forEach { (currency, imageView) ->
                imageView.visibility =
                    if (currency == selected) View.VISIBLE else View.GONE
            }
        }

        // 👉 Hiển thị check ban đầu
        updateCheck(selectedCurrency)

        options.forEach { (currency, layoutId) ->
            view.findViewById<LinearLayout>(layoutId).setOnClickListener {
                selectedCurrency = currency
                currencyText.text = currency.code

                updateCheck(currency)

                PrefsManager.saveCurrency(requireContext(), currency.code)

                dialog.dismiss()
            }
        }

        view.findViewById<TextView>(R.id.currency_optionCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}