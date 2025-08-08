package com.example.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.HostJoin.Turn
import com.example.tictactoe.databinding.GameScreenBinding

class HostJoin : AppCompatActivity() {

    enum class Turn {
        NOUGHT,
        CROSS
    }

    var firstTurn = Turn.CROSS
    var currentTurn = Turn.CROSS
    var isRemoving = false
    var lastRemovedButton: Button? = null

    var boardList = mutableListOf<Button>()
    private lateinit var binding : GameScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GameScreenBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        initBoard()
    }

    private fun initBoard() {
        boardList.add(binding.a1)
        boardList.add(binding.a2)
        boardList.add(binding.a3)
        boardList.add(binding.b1)
        boardList.add(binding.b2)
        boardList.add(binding.b3)
        boardList.add(binding.c1)
        boardList.add(binding.c2)
        boardList.add(binding.c3)
    }

    private fun currentTurnSymbol(): String {
        return if(currentTurn == Turn.CROSS) CROSS else NOUGHT
    }

    fun boardTapped(view: View) {
        if(view !is Button)
            return

        if (isRemoving) {
            if(view.text == currentTurnSymbol()){
                view.text = ""
                lastRemovedButton = view
                isRemoving = false
                SetTurnLable()
            }
            return
        }

        if(view.text != "") return

        if(checkAmount(currentTurnSymbol()) >= 3) {
            isRemoving = true
            Toast.makeText(this, "Remove one of your tokens", Toast.LENGTH_SHORT).show()
            return
        }

        if (view == lastRemovedButton){
            Toast.makeText(this, "You can't place back on the removed spot!", Toast.LENGTH_SHORT).show()
            return
        }


        addToBoard(view)

        lastRemovedButton = null

        if (CheckVictory(NOUGHT)) {
            result("NOUGHT WIN")
        }
        if (CheckVictory(CROSS)) {
            result("CROSS WIN")
        }
    }

    private fun result(title: String) {
        AlertDialog.Builder(this).setTitle(title).setPositiveButton("reset"){_,_ ->
            resetBoard()
        }.show()
    }

    fun checkAmount(s: String): Int{
        var amount = 0
        for(button in boardList){
            if(button.text == s){
                amount++
            }
        }
        return amount
    }

    private fun addToBoard(button: Button) {
        if(button.text != "")
            return

        if(currentTurn == Turn.NOUGHT){
            button.text = NOUGHT
            currentTurn = Turn.CROSS
        } else if (currentTurn == Turn.CROSS){
            button.text = CROSS
            currentTurn = Turn.NOUGHT
        }

        // Check if player now has 3 tokens after this placement
        if (checkAmount(currentTurnSymbol()) >= 3) {
            isRemoving = true
            Toast.makeText(this, "Remove one of your tokens", Toast.LENGTH_SHORT).show()
        }

        SetTurnLable()
    }

    private fun CheckVictory(player: String): Boolean{
        if(match(binding.a1,player) && match(binding.a2,player) && match(binding.a3,player) ||
            match(binding.b1,player) && match(binding.b2,player) && match(binding.b3,player) ||
            match(binding.c1,player) && match(binding.c2,player) && match(binding.c3,player)){
            return true
        }
        if(match(binding.a1,player) && match(binding.b1,player) && match(binding.c1,player) ||
            match(binding.a2,player) && match(binding.b2,player) && match(binding.c2,player) ||
            match(binding.a3,player) && match(binding.b3,player) && match(binding.c3,player)){
            return true
        }
        if (match(binding.a1,player) && match(binding.b2,player) && match(binding.c3,player) ||
            match(binding.a3,player) && match(binding.b2,player) && match(binding.c1,player)){
            return true
        }

        return false
    }

    private fun match(button: Button, symbol : String): Boolean = button.text == symbol

    fun SetTurnLable() {
        var turntext = ""
        if(currentTurn == Turn.NOUGHT){
            turntext = "Turn $NOUGHT"
        } else if (currentTurn == Turn.CROSS){
            turntext = "Turn $CROSS"
        }

        binding.turnTV.text = turntext
    }

    companion object{
        const val NOUGHT = "O"
        const val CROSS = "X"
    }

}

private fun HostJoin.resetBoard() {
    for(button in boardList){
        button.text = ""
    }
    if(firstTurn == Turn.NOUGHT){
        firstTurn = Turn.CROSS
    }
    if (firstTurn == Turn.CROSS){
        firstTurn = Turn.NOUGHT
    }
    currentTurn = firstTurn
    lastRemovedButton = null
    isRemoving = false
    SetTurnLable()

}
