package minesweeper
import kotlin.random.Random

class BombsField(val rows:Int, val cols:Int){
    var mineField = MutableList(rows) { MutableList(cols) { '.' } }
    var bombsCoord = mutableListOf(mutableListOf<Int>())
    var flagsCoord = mutableListOf(mutableListOf<Int>())

    fun printField() {
        println(" │123456789│\n" +
                "—│—————————│")
        for ((index, row) in mineField.withIndex()) {
            println((index + 1).toString() + "|" + row.joinToString("") + "|")
        }
        println("—│—————————│")
    }

    fun putBombs() {for (bomb in bombsCoord){mineField[bomb[0]][bomb[1]] = 'X'}}

    fun delBombs() {for (bomb in bombsCoord){mineField[bomb[0]][bomb[1]] = '.'}}

    fun addBombs(bombsList: MutableList<MutableList<Int>>) {bombsCoord = bombsList}

    fun generateBombs(number: Int) {
        // val randomGenerator = Random(0)
        val bombs = MutableList(0) {0}
        while (bombs.size != number) {
            val bomb:Int = Random.nextInt(0, rows * cols)
            if (!bombs.contains(bomb)) {
                bombs.add(bomb)
            }
        }
        for (bomb in bombs){
            bombsCoord.add(intArrayOf(bomb / rows, bomb - (bomb / rows) * cols).toMutableList())
        }
        bombsCoord.removeFirst()
    }

    private fun findDirections(x: Int, y: Int): MutableList<MutableList<Int>> {
        val directions = arrayOf(arrayOf(-1, -1), arrayOf(-1, 0), arrayOf(-1, 1), arrayOf(0, -1), arrayOf(0, 1),
            arrayOf(1, -1), arrayOf(1, 0), arrayOf(1, 1))
        var tmpDirections = mutableListOf(mutableListOf<Int>())
        for (direction in directions) {
            try {
                if (mineField[x + direction[0]][y + direction[1]] != 'Q') // always true
                    tmpDirections.add((intArrayOf(x + direction[0], y + direction[1]).toMutableList()))
            } catch (e: IndexOutOfBoundsException) {
                null
            }
        }
        tmpDirections.removeFirst()
        return tmpDirections
    }

    fun addNumbers() {
        for (x in 0 until rows ) {
            for (y in 0 until cols) {
                var tmpDirections = findDirections(x, y)
                val numberOfBombs = tmpDirections.filter { it -> mineField[it[0]][it[1]] == 'X' }
                if (mineField[x][y] != 'X' && numberOfBombs.size != 0) mineField[x][y] = numberOfBombs.size.digitToChar()
            }
        }
    }

    fun putFlag(x: Int, y: Int) {
        when {
            mineField[x][y] == '*' -> {

                mineField[x][y] = '.'
                flagsCoord.remove(intArrayOf(x, y).toMutableList())

            }
            else -> {
                mineField[x][y] = '*'
                flagsCoord.add((intArrayOf(x, y).toMutableList()))
            }
        }
        printField()
    }
}

fun checkWin(user: BombsField):Boolean {
    var numberEmpty:Int = 0
    var numberAsterix:Int = 0
    for (x in user.mineField) for (y in x) if (y == '*') numberAsterix += 1
    for (x in user.mineField) for (y in x) if (y == '.') numberEmpty += 1
    // Every mine with flag
    if (user.flagsCoord.size == user.bombsCoord.size) for (flag in user.flagsCoord) {
            // println("${user.bombsCoord} ${user.flagsCoord} $flag ${flag in user.flagsCoord}")
            if (flag !in user.bombsCoord) return false
            println("Congratulations! You found all the mines!")
            return true
    }
    if (numberEmpty + numberAsterix == user.bombsCoord.size) {
        // println("${user.bombsCoord} ${user.flagsCoord} $flag ${flag in user.flagsCoord}")
        for ((y, x) in user.bombsCoord){
            if (user.mineField[y][x] != '*' &&  user.mineField[y][x] != '.') return false
        }
        println("Congratulations! You found all the mines!")
        return true
    }

    return false
}

fun findEmpty(x:Int, y:Int, game:BombsField, user:BombsField){
    val directions = arrayOf(arrayOf(-1, -1), arrayOf(-1, 0), arrayOf(-1, 1), arrayOf(0, -1), arrayOf(0, 1),
        arrayOf(1, -1), arrayOf(1, 0), arrayOf(1, 1))
    game.mineField[x][y] = '/'
    user.mineField[x][y] = '/'

    for (direct in directions) {
        try {
            if (game.mineField[x + direct[0]][y + direct[1]] == '.') {
                findEmpty(x + direct[0], y + direct[1], game, user)
            }
            if (game.mineField[x + direct[0]][y + direct[1]] in '1'..'9') {
                user.mineField[x + direct[0]][y + direct[1]] = game.mineField[x + direct[0]][y + direct[1]]
            }
        } catch (e: IndexOutOfBoundsException) {
          null
        }
    }
    return
}

fun main() {
    val width:Int = 9
    val hight:Int = 9
    // Generating the secret field
    var gameGen = BombsField(width, hight)
    // Generating bombs1
    print("How many mines do you want on the field? ")
    var howManyBombs:Int = readln().toInt()
    // Putting bombs on the field
    gameGen.generateBombs(howManyBombs)
    gameGen.putBombs()
    // Adding numbers to the secret field.
    gameGen.addNumbers()
    // Removing bombs
    // User field
    var gameUser = BombsField(width, hight)
    gameUser.addBombs(gameGen.bombsCoord)

    gameUser.printField()
    // Starting game
    gameUser.flagsCoord.removeFirst()
    do{
        print("Set/unset mines marks or claim a cell as free: ")
        val (yTemp, xTemp, command) = readln().split(" ") //.map(String::toInt)
        val x = xTemp.toInt()
        val y = yTemp.toInt()
        if (command == "free")
            when (gameGen.mineField[x - 1][y - 1]){
                in '1'..'9' -> gameUser.mineField[x - 1][y - 1] = gameGen.mineField[x - 1][y - 1]
                '.' -> findEmpty(x - 1, y - 1, gameGen, gameUser)
                '*' -> gameUser.mineField[x - 1][y - 1] = '.'
                'X' -> {
                    gameUser.printField()
                    println("You stepped on a mine and failed!")
                    break
                }// -> gameUser.putFlag(x - 1, y - 1)
            }
        else gameUser.putFlag(x - 1, y - 1)
        // gameGen.printField()
        gameUser.printField()
    } while (!checkWin(gameUser))
}
