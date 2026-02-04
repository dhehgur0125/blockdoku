package com.yourpackage.blockdoku.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blockdoku.R

/**
 * 튜토리얼 화면용 9x9 보드 어댑터 (정적, 인터랙션 없음)
 *
 * 사용 방법:
 * ```kotlin
 * val adapter = TutorialBoardAdapter()
 * rvTutorialBoard.layoutManager = GridLayoutManager(this, 9)
 * rvTutorialBoard.adapter = adapter
 *
 * // 샘플 데이터로 보드 채우기
 * adapter.setTutorialBoardData()
 * ```
 */
class TutorialBoardAdapter : RecyclerView.Adapter<TutorialBoardAdapter.CellViewHolder>() {

    // 9x9 = 81칸의 셀 데이터
    // 0 = 빈 칸, 1 = 채워진 칸, 2 = 폭탄 칸
    private val boardData = IntArray(81) { 0 }

    // 폭탄 타이머 값 (셀 인덱스 -> 타이머 값)
    private val bombTimers = mutableMapOf<Int, Int>()

    inner class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val cellFilled: View = itemView.findViewById(R.id.cellFilled)
        val cellBomb: FrameLayout = itemView.findViewById(R.id.cellBomb)
        val txtBombTimer: TextView = itemView.findViewById(R.id.txtBombTimer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_cell, parent, false)
        return CellViewHolder(view)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        // 셀 상태에 따라 UI 업데이트 (배경은 투명, 그리드 라인은 RecyclerView 배경으로 처리)
        when (boardData[position]) {
            0 -> {
                // 빈 칸 - 투명
                holder.cellFilled.visibility = View.GONE
                holder.cellBomb.visibility = View.GONE
            }
            1 -> {
                // 채워진 칸 - 보라색 블록
                holder.cellFilled.visibility = View.VISIBLE
                holder.cellBomb.visibility = View.GONE
            }
            2 -> {
                // 폭탄 칸
                holder.cellFilled.visibility = View.GONE
                holder.cellBomb.visibility = View.VISIBLE
                holder.txtBombTimer.text = bombTimers[position]?.toString() ?: "5"
            }
        }
    }

    override fun getItemCount(): Int = 81

    /**
     * 튜토리얼용 샘플 보드 데이터 설정
     * 깔끔한 예시 보드 - 일부 블록과 폭탄만 표시
     */
    fun setTutorialBoardData() {
        // 모든 칸을 빈 칸으로 초기화 (선으로만 표현)
        boardData.fill(0)
        bombTimers.clear()

        // 왼쪽 상단 L자 블록 (보라색)
        boardData[0] = 1   // (0,0)
        boardData[9] = 1   // (1,0)
        boardData[18] = 1  // (2,0)
        boardData[19] = 1  // (2,1)

        // 중간 작은 정사각형 블록 (보라색)
        boardData[31] = 1  // (3,4)
        boardData[32] = 1  // (3,5)
        boardData[40] = 1  // (4,4)
        boardData[41] = 1  // (4,5)

        // 폭탄 1 (중앙 하단)
        boardData[58] = 2  // (6,4)
        bombTimers[58] = 7

        // 폭탄 2 (왼쪽 하단)
        boardData[64] = 2  // (7,1)
        bombTimers[64] = 3

        // 오른쪽 하단 작은 블록 (보라색)
        boardData[70] = 1  // (7,7)
        boardData[71] = 1  // (7,8)

        notifyDataSetChanged()
    }

    /**
     * 커스텀 보드 데이터 설정
     * @param data 81개 셀의 상태 배열 (0=빈칸, 1=채움, 2=폭탄)
     * @param timers 폭탄 타이머 맵
     */
    fun setCustomBoardData(data: IntArray, timers: Map<Int, Int> = emptyMap()) {
        if (data.size == 81) {
            data.copyInto(boardData)
            bombTimers.clear()
            bombTimers.putAll(timers)
            notifyDataSetChanged()
        }
    }
}