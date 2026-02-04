package com.yourpackage.blockdoku.adapter

import android.graphics.Color
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blockdoku.R
import com.yourpackage.blockdoku.model.BlockShape

/**
 * 실제 게임 플레이용 9x9 보드 어댑터 (드래그 앤 드롭 인터랙션 지원)
 *
 * 사용 방법:
 * ```kotlin
 * val adapter = GameBoardAdapter(
 *     onCellClick = { position -> /* 셀 클릭 처리 */ },
 *     onBlockPlaced = { startPosition, blockShape -> /* 블록 배치 처리 */ },
 *     onLinesCleared = { clearedLines -> /* 라인 제거 처리 */ },
 *     onScoreUpdate = { score -> /* 점수 업데이트 */ },
 *     onGameOver = { /* 게임 오버 처리 */ }
 * )
 *
 * val layoutManager = GridLayoutManager(this, 9)
 * rvGameBoard.layoutManager = layoutManager
 * rvGameBoard.adapter = adapter
 * ```
 */
class GameBoardAdapter(
    private val onCellClick: ((Int) -> Unit)? = null,
    private val onBlockPlaced: ((Int, BlockShape) -> Unit)? = null,
    private val onLinesCleared: ((List<Int>) -> Unit)? = null,
    private val onScoreUpdate: ((Int) -> Unit)? = null,
    private val onGameOver: (() -> Unit)? = null
) : RecyclerView.Adapter<GameBoardAdapter.GameCellViewHolder>() {

    // 게임 보드 상태
    // 0 = 빈 칸, 1 = 채워진 칸, 2 = 폭탄 칸, -1 = 폭발한 칸 (사용 불가)
    private val boardState = IntArray(81) { 0 }

    // 폭탄 타이머 (셀 인덱스 -> 남은 카운트)
    private val bombTimers = mutableMapOf<Int, Int>()

    // 점수
    private var currentScore = 0

    // 배치된 블록 수 (폭탄 생성 카운터)
    private var blocksPlaced = 0

    // 드래그 중인 블록 정보
    private var draggingBlock: BlockShape? = null

    inner class GameCellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cellContainer: FrameLayout = itemView.findViewById(R.id.cellContainer)
        val cellFilled: View = itemView.findViewById(R.id.cellFilled)
        val cellBomb: FrameLayout = itemView.findViewById(R.id.cellBomb)
        val txtBombTimer: TextView = itemView.findViewById(R.id.txtBombTimer)

        init {
            // 클릭 리스너
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCellClick?.invoke(position)
                }
            }

            // 드래그 앤 드롭 리스너
            itemView.setOnDragListener { view, event ->
                handleDragEvent(view, event, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameCellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_cell, parent, false)

        return GameCellViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameCellViewHolder, position: Int) {
        // 셀 상태에 따라 UI 업데이트 (배경은 투명, 그리드 라인은 RecyclerView 배경으로 처리)
        when (boardState[position]) {
            -1 -> {
                // 폭발한 칸 (사용 불가)
                holder.cellFilled.visibility = View.VISIBLE
                holder.cellFilled.setBackgroundResource(R.drawable.gradient_bomb_exploded)
                holder.cellBomb.visibility = View.GONE
            }
            0 -> {
                // 빈 칸
                holder.cellFilled.visibility = View.GONE
                holder.cellBomb.visibility = View.GONE
            }
            1 -> {
                // 채워진 칸
                holder.cellFilled.visibility = View.VISIBLE
                holder.cellFilled.setBackgroundResource(R.drawable.gradient_block_purple)
                holder.cellBomb.visibility = View.GONE
            }
            2 -> {
                // 폭탄 칸
                holder.cellFilled.visibility = View.GONE
                holder.cellBomb.visibility = View.VISIBLE
                holder.txtBombTimer.text = bombTimers[position]?.toString() ?: "0"
            }
        }
    }

    override fun getItemCount(): Int = 81

    /**
     * 드래그 이벤트 처리
     */
    private fun handleDragEvent(view: View, event: DragEvent, position: Int): Boolean {
        return when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                // 드래그 시작 - 블록 정보 저장
                val blockData = event.localState as? BlockShape
                draggingBlock = blockData
                true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                // 드래그가 셀에 들어옴 - 하이라이트 표시
                if (canPlaceBlock(position, draggingBlock)) {
                    view.setBackgroundColor(Color.parseColor("#E0E7FF")) // 연보라색 하이라이트
                }
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                // 드래그가 셀에서 나감 - 하이라이트 제거
                view.background = null
                true
            }
            DragEvent.ACTION_DROP -> {
                // 블록 드롭
                view.background = null
                draggingBlock?.let { block ->
                    if (canPlaceBlock(position, block)) {
                        placeBlock(position, block)
                    }
                }
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                // 드래그 종료
                view.background = null
                draggingBlock = null
                true
            }
            else -> false
        }
    }

    /**
     * 블록을 해당 위치에 놓을 수 있는지 확인
     */
    private fun canPlaceBlock(startPosition: Int, block: BlockShape?): Boolean {
        if (block == null) return false

        val startRow = startPosition / 9
        val startCol = startPosition % 9

        for ((rowOffset, colOffset) in block.cells) {
            val row = startRow + rowOffset
            val col = startCol + colOffset
            val pos = row * 9 + col

            // 범위 체크
            if (row < 0 || row >= 9 || col < 0 || col >= 9) {
                return false
            }

            // 이미 채워져 있거나 폭발한 칸인지 체크
            if (boardState[pos] != 0) {
                return false
            }
        }

        return true
    }

    /**
     * 블록 배치
     */
    private fun placeBlock(startPosition: Int, block: BlockShape) {
        val startRow = startPosition / 9
        val startCol = startPosition % 9

        // 블록 셀 채우기
        for ((rowOffset, colOffset) in block.cells) {
            val row = startRow + rowOffset
            val col = startCol + colOffset
            val pos = row * 9 + col
            boardState[pos] = 1
        }

        blocksPlaced++

        // 블록 배치 콜백
        onBlockPlaced?.invoke(startPosition, block)

        // 폭탄 타이머 감소
        decrementBombTimers()

        // 라인/구역 체크 및 제거
        val clearedPositions = checkAndClearLines()
        if (clearedPositions.isNotEmpty()) {
            onLinesCleared?.invoke(clearedPositions)

            // 점수 계산 (클리어한 라인/구역 수에 따라)
            val linesCleared = clearedPositions.size / 9
            currentScore += linesCleared * 100
            onScoreUpdate?.invoke(currentScore)
        }

        // 폭탄 생성 체크 (4-5개 블록마다)
        if (blocksPlaced % 5 == 0) {
            spawnBomb()
        }

        // 게임 오버 체크
        if (isGameOver()) {
            onGameOver?.invoke()
        }

        notifyDataSetChanged()
    }

    /**
     * 라인 및 3x3 구역 체크 및 제거
     */
    private fun checkAndClearLines(): List<Int> {
        val toClear = mutableSetOf<Int>()

        // 가로 라인 체크
        for (row in 0..8) {
            var isFull = true
            for (col in 0..8) {
                val pos = row * 9 + col
                if (boardState[pos] != 1) {
                    isFull = false
                    break
                }
            }
            if (isFull) {
                for (col in 0..8) {
                    toClear.add(row * 9 + col)
                }
            }
        }

        // 세로 라인 체크
        for (col in 0..8) {
            var isFull = true
            for (row in 0..8) {
                val pos = row * 9 + col
                if (boardState[pos] != 1) {
                    isFull = false
                    break
                }
            }
            if (isFull) {
                for (row in 0..8) {
                    toClear.add(row * 9 + col)
                }
            }
        }

        // 3x3 구역 체크
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                var isFull = true
                for (row in 0..2) {
                    for (col in 0..2) {
                        val pos = (boxRow * 3 + row) * 9 + (boxCol * 3 + col)
                        if (boardState[pos] != 1) {
                            isFull = false
                            break
                        }
                    }
                    if (!isFull) break
                }
                if (isFull) {
                    for (row in 0..2) {
                        for (col in 0..2) {
                            toClear.add((boxRow * 3 + row) * 9 + (boxCol * 3 + col))
                        }
                    }
                }
            }
        }

        // 제거
        for (pos in toClear) {
            boardState[pos] = 0
            bombTimers.remove(pos)
        }

        return toClear.toList()
    }

    /**
     * 폭탄 타이머 감소 및 폭발 처리
     */
    private fun decrementBombTimers() {
        val toExplode = mutableListOf<Int>()

        for ((pos, timer) in bombTimers) {
            val newTimer = timer - 1
            if (newTimer <= 0) {
                toExplode.add(pos)
            } else {
                bombTimers[pos] = newTimer
            }
        }

        // 폭탄 폭발
        for (pos in toExplode) {
            boardState[pos] = -1 // 사용 불가능한 칸
            bombTimers.remove(pos)
        }
    }

    /**
     * 빈 칸에 랜덤 폭탄 생성
     */
    private fun spawnBomb() {
        val emptyCells = boardState.indices.filter { boardState[it] == 0 }
        if (emptyCells.isEmpty()) return

        val randomCell = emptyCells.random()
        val randomTimer = (5..10).random()

        boardState[randomCell] = 2
        bombTimers[randomCell] = randomTimer
    }

    /**
     * 게임 오버 체크 (더 이상 블록을 놓을 수 없는지)
     */
    private fun isGameOver(): Boolean {
        // 간단한 구현: 빈 칸이 없으면 게임 오버
        return boardState.all { it != 0 }
    }

    /**
     * 보드 리셋
     */
    fun resetBoard() {
        boardState.fill(0)
        bombTimers.clear()
        currentScore = 0
        blocksPlaced = 0
        notifyDataSetChanged()
    }

    /**
     * 현재 점수 가져오기
     */
    fun getCurrentScore(): Int = currentScore

    /**
     * 보드 상태 가져오기
     */
    fun getBoardState(): IntArray = boardState.copyOf()
}

/**
 * 블록 형태 데이터 클래스
 */
data class BlockShape(
    val cells: List<Pair<Int, Int>>, // (행 오프셋, 열 오프셋) 리스트
    val color: String = "#9333EA" // 기본 보라색
) {
    companion object {
        // 미리 정의된 블록 형태들

        // 1x1 블록
        val SINGLE = BlockShape(listOf(0 to 0))

        // 1x2 블록 (가로)
        val HORIZONTAL_2 = BlockShape(listOf(0 to 0, 0 to 1))

        // 2x1 블록 (세로)
        val VERTICAL_2 = BlockShape(listOf(0 to 0, 1 to 0))

        // 1x3 블록 (가로)
        val HORIZONTAL_3 = BlockShape(listOf(0 to 0, 0 to 1, 0 to 2))

        // 3x1 블록 (세로)
        val VERTICAL_3 = BlockShape(listOf(0 to 0, 1 to 0, 2 to 0))

        // 2x2 블록
        val SQUARE_2 = BlockShape(listOf(
            0 to 0, 0 to 1,
            1 to 0, 1 to 1
        ))

        // 3x3 블록
        val SQUARE_3 = BlockShape(listOf(
            0 to 0, 0 to 1, 0 to 2,
            1 to 0, 1 to 1, 1 to 2,
            2 to 0, 2 to 1, 2 to 2
        ))

        // L자 블록
        val L_SHAPE = BlockShape(listOf(
            0 to 0,
            1 to 0,
            2 to 0, 2 to 1
        ))

        // T자 블록
        val T_SHAPE = BlockShape(listOf(
            0 to 0, 0 to 1, 0 to 2,
            1 to 1
        ))

        // Z자 블록
        val Z_SHAPE = BlockShape(listOf(
            0 to 0, 0 to 1,
            1 to 1, 1 to 2
        ))

        // 랜덤 블록 생성
        fun random(): BlockShape {
            val shapes = listOf(
                SINGLE, HORIZONTAL_2, VERTICAL_2, HORIZONTAL_3, VERTICAL_3,
                SQUARE_2, SQUARE_3, L_SHAPE, T_SHAPE, Z_SHAPE
            )
            return shapes.random()
        }

        // 3개의 랜덤 블록 생성
        fun randomThree(): List<BlockShape> {
            return List(3) { random() }
        }
    }
}