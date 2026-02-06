package com.example.blockdoku

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * 스플래시 화면 (앱 시작 시 3초간 표시)
 *
 * 흐름:
 * SplashActivity (3초) → HomeActivity (메인 메뉴)
 */
class SplashActivity : ComponentActivity() {

    // 스플래시 화면 표시 시간 (밀리초)
    private val splashDuration = 3000L  // 3초

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // 시스템 바 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3초 후 홈 화면으로 이동
        navigateToHome()
    }

    /**
     * 지정된 시간 후 홈 화면으로 이동
     */
    private fun navigateToHome() {
        Handler(Looper.getMainLooper()).postDelayed({
            // 홈 화면으로 이동
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

            // 스플래시 화면 종료 (뒤로가기 방지)
            finish()
        }, splashDuration)
    }

}