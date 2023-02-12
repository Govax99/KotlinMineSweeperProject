package minesweeper

import java.util.*
import kotlin.math.abs

const val SIZE = 9
enum class State() { WON, CONTINUE, LOSS }

class MineSweeper() {
    var field: MutableList<MutableList<String>> = MutableList(SIZE) {
                                                MutableList<String>(SIZE) { "." } }
    var foundFlags: MutableList<Pair<Int, Int>> = mutableListOf()
    var turnCount = 0
    var playedFirstFree = true
    var numMines = 0

    fun placeMines(numberMines: Int, xPlayer: Int, yPlayer: Int) {
        // xPlayer, yPlayer -> initial player coordinates, you cannot place near
        var n = 0
        while (n < numberMines) {
            val randRow = Random().nextInt(0, SIZE)
            val randCol = Random().nextInt(0, SIZE)
            if (field[randRow][randCol] == "X") {
                continue
            }
            if (abs(randRow - xPlayer) <= 1 && abs(randCol - yPlayer) <= 1) {
                continue
            }
            if (field[randRow][randCol] == "*") {
                continue
            }
            field[randRow][randCol] = "X"
            n++
        }
    }

    fun closeSymbol(x: Int, y: Int, symbol: String): Int {
        var mines: Int = 0
        for (i in x-1..x+1) {
            for (j in y-1..y+1) {
                if (i == x && j == y) continue
                if (i !in 0 until SIZE || j !in 0 until SIZE) continue
                if (field[i][j] == symbol) mines++
            }
        }
        return mines
    }

    fun exploreNear(x: Int, y: Int): MutableList<Pair<Int,Int>> {
        val closeFree = mutableListOf<Pair<Int, Int>>()
        for (i in x-1..x+1) {
            for (j in y-1..y+1) {
                if (i == x && j == y) continue
                if (i !in 0 until SIZE || j !in 0 until SIZE) continue
                if (field[i][j] == ".") {
                    val mines = closeSymbol(i, j, "X")
                    if (mines == 0) {
                        field[i][j] = "/"
                        closeFree.add(Pair(i, j))
                    } else {
                        field[i][j] = mines.toString()
                    }
                }
            }
        }
        return closeFree
    }

    fun exploreAuto(x: Int, y: Int) {
        val stack = mutableListOf<Pair<Int, Int>>()
        var currPos = Pair(x, y)
        while(true) {
            val closeToExplore = exploreNear(currPos.first, currPos.second)
            stack.addAll(closeToExplore)
            if (stack.isEmpty()) break
            currPos = stack.last()
            stack.removeLast()
        }
        this.clearUp()
    }

    fun printField(state: State) {
        println(" │123456789│")
        println("—│—————————│")
        for (i in field.indices) {
            print("${i+1}|")
            for (j in field.first().indices) {
                if (Pair(i, j) in foundFlags) {
                    print("*")
                    continue
                }
                if (field[i][j] == "X" && state == State.CONTINUE) {
                    print(".")
                    continue
                }
                print(field[i][j])
            }
            println("|")
        }
        println("—│—————————│")
    }

    fun clearUp() {
        for (i in field.indices) {
            for (j in field.first().indices) {
                val nearFree = this.closeSymbol(i, j, "/")
                if (field[i][j] == "*" && nearFree != 0) {
                    val nearMines = this.closeSymbol(i,j,"X")
                    if (nearMines == 0) {
                        field[i][j] = "/"
                        this.exploreAuto(i, j)
                    } else {
                        field[i][j] = nearMines.toString()
                    }
                }
            }
        }
    }

    fun buildFinal() {
        for (cell in foundFlags) {
            field[cell.first][cell.second] = "X"
        }
    }

    fun playerTurn(): State {
        print("Set/unset mine marks or claim a cell as free: ")
        val (yInp, xInp, command) = readln().split(" ")
        val x = xInp.toInt() - 1
        val y = yInp.toInt() - 1



        if (command == "free") {
            if (playedFirstFree) {
                this.placeMines(numMines, x, y)
                playedFirstFree = false
            }
            if (field[x][y] == "X") {
                foundFlags.add(Pair(x, y))
                this.buildFinal()
                this.printField(State.LOSS)
                return State.LOSS
            }
            val mines = this.closeSymbol(x, y, "X")
            if (mines == 0) {
                field[x][y] = "/"
                this.exploreAuto(x, y)
            } else {
                field[x][y] = mines.toString()
            }
            this.printField(State.CONTINUE)
        }

        if (command == "mine") {
            if (field[x][y] == "X" || field[x][y] == ".") {
                if (field[x][y] == "X") {
                    val ind = foundFlags.indexOf(Pair(x,y))
                    if (ind != -1) {
                        foundFlags.removeAt(ind)
                    } else {
                        foundFlags.add(Pair(x, y))
                    }
                } else {
                    field[x][y] = "*"
                }
                //field[x][y] = "*"
                this.printField(State.CONTINUE)
            } else if (field[x][y] == "*") {
                field[x][y] = "."
                this.printField(State.CONTINUE)
            } else {
                println("There is a number or free space here!")
            }
        }
        return State.CONTINUE
    }

    fun checkWin(state: State): State {
        if (state == State.LOSS) return State.LOSS
        if (playedFirstFree) return State.CONTINUE
        if (foundFlags.size == numMines)    {
            return State.WON
        }
        return State.CONTINUE
    }

    fun normalOperation() {
        var state = State.CONTINUE
        print("How many mines do you want on the field? ")
        numMines = readln().toInt()
        this.printField(state)
        while (true) {
            state = this.playerTurn()
            val state = checkWin(state)

            if (state == State.WON) {
                println("Congratulations! You found all the mines!")
                break
            }
            if (state == State.LOSS) {
                println("You stepped on a mine and failed!")
                break
            }
            turnCount++
        }
    }

}

fun main() {
    val game = MineSweeper()
    game.normalOperation()
}
