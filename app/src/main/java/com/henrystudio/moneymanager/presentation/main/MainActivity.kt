package com.henrystudio.moneymanager.presentation.main

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.presentation.viewmodel.MainViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.bottomNavigation.dailyNavigate.DailyNavigateFragment
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.statistic.StatisticViewPagerFragment
import com.henrystudio.moneymanager.presentation.setting.SettingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.henrystudio.moneymanager.core.application.PrefsManager
import com.henrystudio.moneymanager.core.application.PrefsManager.getHasRate
import com.henrystudio.moneymanager.core.application.PrefsManager.getInstallTime
import com.henrystudio.moneymanager.core.application.PrefsManager.getMainSelectedTab
import com.henrystudio.moneymanager.core.application.PrefsManager.getOpenCount
import com.henrystudio.moneymanager.core.application.PrefsManager.saveHasRate
import com.henrystudio.moneymanager.core.application.PrefsManager.saveMainSelectedTab
import com.henrystudio.moneymanager.core.application.PrefsManager.saveOpenCount
import com.henrystudio.moneymanager.core.application.PrefsManager.setInstallTime
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private val sharedViewModel: SharedTransactionViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var adView: AdView

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lastTab = getMainSelectedTab(this)
        setInstallTime(this)

        init()
        initAds()

        if (shouldShowReview()) {
            showFeedbackDialog()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.currentFilterDate.collect { date ->
                    mainViewModel.updateFilterDate(date)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState.collectLatest { state ->
                    bottomNav.menu.findItem(R.id.nav_daily).title = state.bottomNavTitle
                }
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            saveMainSelectedTab(this, item.itemId)
            when(item.itemId) {
                R.id.nav_daily -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, DailyNavigateFragment())
                        .commit()
                    true
                }
                R.id.nav_stats -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, StatisticViewPagerFragment())
                        .commit()
                    true
                }
                R.id.nav_more -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, SettingFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
        lifecycleScope.launch {
            bottomNav.selectedItemId = lastTab
        }
    }

    private fun init() {
        bottomNav = findViewById(R.id.main_bottomBar)
    }

    private fun initAds() {
        adView = findViewById(R.id.adView)

        // ❗ Nếu chưa đủ 2 ngày → không làm gì
        if (!shouldShowAds()) {
            adView.visibility = View.GONE
            return
        }

        // init SDK (nên gọi 1 lần, main thread OK)
        MobileAds.initialize(this)

        val adRequest = AdRequest.Builder().build()

        adView.adListener = object : com.google.android.gms.ads.AdListener() {

            override fun onAdLoaded() {
                // hiện ads khi load xong
                adView.alpha = 0f
                adView.visibility = View.VISIBLE
                adView.animate().alpha(1f).setDuration(300).start()
            }

            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                // không có ads → ẩn luôn
                adView.visibility = View.GONE
            }
        }

        adView.loadAd(adRequest)
    }

    override fun onResume() {
        super.onResume()
        if (::adView.isInitialized && shouldShowAds()) {
            adView.resume()
        }
    }

    override fun onPause() {
        if (::adView.isInitialized && shouldShowAds()) {
            adView.pause()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (::adView.isInitialized && shouldShowAds()) {
            adView.destroy()
        }
        super.onDestroy()
    }

    private fun shouldShowAds(): Boolean {
        val installTime = getInstallTime(this)

        if (installTime == 0L) return false

        val currentTime = System.currentTimeMillis()
        val diff = currentTime - installTime

        val twoDaysMillis = 2 * 24 * 60 * 60 * 1000L

        val openCount = PrefsManager.getOpenCount(this)
        saveOpenCount(this, openCount + 1)

        return diff >= twoDaysMillis || openCount >= 5
    }

    private fun showReviewFlow() {
        val manager = com.google.android.play.core.review.ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(this, reviewInfo)

                flow.addOnCompleteListener {
                    // Không cần xử lý gì thêm
                }
            }
        }
    }

    private fun shouldShowReview(): Boolean {
        val installTime = getInstallTime(this)
        val openCount = getOpenCount(this)
        val hasRated = getHasRate(this)

        val threeDays = 3 * 24 * 60 * 60 * 1000L

        val enoughTime = System.currentTimeMillis() - installTime >= threeDays
        val enoughOpen = openCount >= 5

        return !hasRated && enoughTime && enoughOpen
    }

    private fun showFeedbackDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_feedback, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(true)
            .create()

        val btnPositive = view.findViewById<Button>(R.id.btnPositive)
        val btnNegative = view.findViewById<Button>(R.id.btnNegative)

        btnPositive.setOnClickListener {
            showReviewFlow()
            saveHasRate(this, true)
            dialog.dismiss()
        }

        btnNegative.setOnClickListener {
            openEmailFeedback()
            saveHasRate(this, true)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openEmailFeedback() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("thanhhieu995@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Feedback app")
            putExtra(Intent.EXTRA_TEXT, R.string.feedback_email_body)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
