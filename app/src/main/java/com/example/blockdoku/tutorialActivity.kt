package com.example.blockdoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yourpackage.blockdoku.adapter.TutorialBoardAdapter

class tutorialActivity : ComponentActivity() {
    private lateinit var adapter: TutorialBoardAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tutorial)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorial)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rvTutorialBoard = findViewById<RecyclerView>(R.id.rvTutorialBoard)
        adapter = TutorialBoardAdapter()

        val layoutManager = GridLayoutManager(this, 9)
        rvTutorialBoard.layoutManager = layoutManager
        rvTutorialBoard.adapter = adapter

        adapter.setTutorialBoardData()
    }
}