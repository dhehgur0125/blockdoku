package com.example.blockdoku

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.gridlayout.widget.GridLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.yourpackage.blockdoku.adapter.GameBoardAdapter
import com.yourpackage.blockdoku.model.BlockShape

class GameActivity : ComponentActivity() {

    private lateinit var adapter: GameBoardAdapter
    private var currentScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rvGameBoard = findViewById<RecyclerView>(R.id.rvGameBoard)
        val txtCurrentScore = findViewById<TextView>(R.id.txtCurrentScore)

        // 어댑터 생성 (콜백 함수 설정)
        adapter = GameBoardAdapter(
            onCellClick = { position ->
                // 셀 클릭 처리 (필요시)
                Log.d("Game", "Cell clicked: $position")
            },
            onBlockPlaced = { startPosition, blockShape ->
                // 블록 배치 완료
                Log.d("Game", "Block placed at: $startPosition")
            },
            onLinesCleared = { clearedPositions ->
                // 라인 제거 애니메이션 처리
                Log.d("Game", "Lines cleared: ${clearedPositions.size} cells")
            },
            onScoreUpdate = { score ->
                // 점수 업데이트
                currentScore = score
                txtCurrentScore.text = score.toString()
            },
            onGameOver = {
                // 게임 오버 처리
                showGameOverDialog()
            }
        )

        // GridLayoutManager 설정 (9열)
        val layoutManager = GridLayoutManager(this, 9)
        rvGameBoard.layoutManager = layoutManager
        rvGameBoard.adapter = adapter

        // 초기 블록 생성
        setupInitialBlocks()

        // 홈 버튼
        findViewById<MaterialButton>(R.id.btnHome).setOnClickListener {
            finish()
        }

        // 재시작 버튼
        findViewById<MaterialButton>(R.id.btnRestart).setOnClickListener {
            restartGame()
        }
    }

    private fun setupInitialBlocks() {
        // 3개의 랜덤 블록 생성
        val blocks = BlockShape.randomThree()

        // 각 블록 컨테이너에 블록 뷰 추가
        val block1Container = findViewById<FrameLayout>(R.id.block1Container)
        val block2Container = findViewById<FrameLayout>(R.id.block2Container)
        val block3Container = findViewById<FrameLayout>(R.id.block3Container)

        setupBlockView(block1Container, blocks[0])
        setupBlockView(block2Container, blocks[1])
        setupBlockView(block3Container, blocks[2])
    }

    private fun setupBlockView(container: FrameLayout, blockShape: BlockShape) {
        // 블록 뷰 생성 및 드래그 설정
        container.removeAllViews()

        val blockView = createBlockView(blockShape)
        container.addView(blockView)

        // 드래그 시작 리스너
        blockView.setOnLongClickListener { view ->
            val dragShadowBuilder = View.DragShadowBuilder(view)
            view.startDragAndDrop(null, dragShadowBuilder, blockShape, 0)
            true
        }
    }

    private fun createBlockView(blockShape: BlockShape): View {
        // 블록 모양에 따라 View 생성
        // 실제 구현은 프로젝트에 맞게 조정
        val gridLayout = GridLayout(this)

        // 블록의 최대 행/열 계산
        val maxRow = blockShape.cells.maxOf { it.first } + 1
        val maxCol = blockShape.cells.maxOf { it.second } + 1

        gridLayout.rowCount = maxRow
        gridLayout.columnCount = maxCol

        // 빈 공간 채우기
        for (row in 0 until maxRow) {
            for (col in 0 until maxCol) {
                val cellView = View(this)
                val cellSize = 32.dpToPx()
                val layoutParams = GridLayout.LayoutParams()
                layoutParams.width = cellSize
                layoutParams.height = cellSize
                layoutParams.setMargins(2, 2, 2, 2)
                layoutParams.rowSpec = GridLayout.spec(row)
                layoutParams.columnSpec = GridLayout.spec(col)

                // 블록 셀인지 확인
                if (blockShape.cells.contains(row to col)) {
                    cellView.setBackgroundResource(R.drawable.gradient_block_blue)
                } else {
                    cellView.visibility = View.INVISIBLE
                }

                gridLayout.addView(cellView, layoutParams)
            }
        }

        return gridLayout
    }

    private fun restartGame() {
        adapter.resetBoard()
        currentScore = 0
        findViewById<TextView>(R.id.txtCurrentScore).text = "0"
        setupInitialBlocks()
    }

    private fun showGameOverDialog() {
        AlertDialog.Builder(this)
            .setTitle("게임 오버")
            .setMessage("최종 점수: $currentScore")
            .setPositiveButton("재시작") { _, _ ->
                restartGame()
            }
            .setNegativeButton("종료") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // DP to PX 변환 헬퍼
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}