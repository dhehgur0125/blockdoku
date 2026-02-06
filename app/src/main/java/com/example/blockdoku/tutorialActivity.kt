package com.example.blockdoku

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class tutorialActivity : ComponentActivity() {

    private val cellSizeDp = 36
    private val blockSizeDp = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tutorial)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorial)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val boardContainer = findViewById<FrameLayout>(R.id.boardContainer)

        // 샘플 튜토리얼 배치
        placeBlock(boardContainer, 0, 0, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 1, 0, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 2, 0, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 2, 1, R.drawable.gradient_block_purple)

        placeBlock(boardContainer, 3, 4, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 3, 5, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 4, 4, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 4, 5, R.drawable.gradient_block_purple)

        placeBlock(boardContainer, 7, 7, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 7, 8, R.drawable.gradient_block_purple)

        placeBomb(boardContainer, 6, 4, "7")
        placeBomb(boardContainer, 7, 1, "3")
    }

    private fun placeBlock(
        container: FrameLayout,
        row: Int,
        col: Int,
        drawableRes: Int
    ) {
        val block = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                dp(blockSizeDp).toInt(),
                dp(blockSizeDp).toInt()
            )
            background = getDrawable(drawableRes)
        }

        val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2

        block.x = col * dp(cellSizeDp) + offset
        block.y = row * dp(cellSizeDp) + offset

        container.addView(block)
    }

    private fun placeBomb(
        container: FrameLayout,
        row: Int,
        col: Int,
        timer: String
    ) {
        val bombView = layoutInflater.inflate(
            R.layout.view_bomb_block,
            container,
            false
        )

        val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2

        bombView.x = col * dp(cellSizeDp) + offset
        bombView.y = row * dp(cellSizeDp) + offset

        container.addView(bombView)
    }

    private fun dp(value: Int): Float =
        value * resources.displayMetrics.density
}
