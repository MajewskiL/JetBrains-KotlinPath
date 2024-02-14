package bullscows

class Game(val guesLong:Int, val guesUnique:Int){
    private val toGues = generateNumber()

    fun printToGues(): String {
        return toGues.joinToString("")
    }

    private fun generateNumber():List<Char> {//}:List<Char>{
        val allChars = ('0'..'9') + ('a'..'z')
        println(allChars.size)
        val allowedChars = allChars.slice(0 until guesUnique)
        println(allowedChars.size)
        val listToGues = (1..1000).map { allowedChars.random() }.toSet().toList().slice(0 until guesUnique - 1)
        val uniqueListToPrint = if (guesUnique < 11) "(0-${guesUnique - 1})"
            else "(0-9, a${if (guesUnique == 11) ")" else "-${allChars[guesUnique - 1]}"})"
        println("The secret is prepared: ${"*".repeat(guesLong)} $uniqueListToPrint.")
        //println(listToGues)
        return listToGues
    }

    fun checkGues(userGues: String):Boolean{
        println("Congrats! The secret code is $toGues")
        return true
    }

    private fun checkBulls(userInput:String):Int{
        val userList = userInput.toList()
        var bullsCount = 0
        for ((index, value) in userList.withIndex()){
            if (toGues[index] == userList[index]) bullsCount += 1
        }
        return bullsCount
    }

    private fun checkCows(userInput:String):Int{
        val userList = userInput.toList()
        var cowsCount = 0
        for ((index, value) in userList.withIndex()){
            if (toGues[index] != userList[index] && toGues.contains(value)) cowsCount += 1
        }
        return cowsCount
    }

    fun printBullsAndCows(userInput:String):Boolean{
        val bulls = checkBulls(userInput)
        val cows = checkCows(userInput)
        val bullsS = if (bulls == 1) "" else "s"
        val cowsS = if (cows == 1) "" else "s"
        val numberOfAnimals = if (cows + bulls == 0) "None" else "$bulls bull$bullsS and $cows cow$cowsS"
        println("Grade: $numberOfAnimals")
        return bulls == userInput.length
    }

}

fun main() {
    var turn = 0
    var guesLong = 0
    var guesUnique = 0
    println("Input the length of the secret code:")

    try {
        guesLong = readln().toInt()
        }
    catch (e: NumberFormatException)
    {
        println("Error: \"$guesLong\" isn't a valid number.")
        return
    }

    println("Input the number of possible symbols in the code:")
    try {
        guesUnique = readln().toInt()
    }
    catch (e: NumberFormatException)
    {
        println("Error: \"$guesUnique\" isn't a valid number.")
        return
    }

    if (guesUnique == 0 || guesLong == 0) println("Error: 0 isn't a valid number..").also {return}

    if (guesUnique < guesLong) println("Error: it's not possible to generate a code with a length of $guesLong with $guesUnique unique symbols.").also {return}

    if (guesLong >= 36 || guesUnique > 36) println("Error: maximum number of possible symbols in the code is 36 (0-9, a-z).").also { return }

    val game = Game(guesLong, guesUnique)
    println("Okay, let's start a game!")
    while (true) {
        println("Turn $turn:")
        val gues = readln()
        if (game.printBullsAndCows(gues)) println("Congratulations! You guessed the secret code.").also { return }
        turn += 1
    }
}