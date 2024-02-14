package indigo

class Game {
    val colors = listOf("♦", "♥", "♠", "♣")
    val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    val initialNrOfCardsOnTable = 4
    var deck = mutableListOf<String>()
    var pile = mutableListOf<String>()
    var winner = String()
    var starter = String()

    fun startGame(){
        deck = generateDeck()
        shuffleDeck()
        generatePile(initialNrOfCardsOnTable)
    }

    private fun generateDeck():MutableList<String>{
        val newDeck = mutableListOf<String>()
        for (color in colors)
            for (rank in ranks)
                newDeck.add("$rank$color")

        return newDeck
    }

    fun generatePile(nrCards: Int) {
        for (i in 0 until nrCards) pile.add(drawCard())
    }

    fun resetDeck(){
        deck = generateDeck()
        println("Card deck is reset.")
    }

    fun shuffleDeck(){
        deck = deck.shuffled().toMutableList()
    }

    fun drawCard():String{
        val last = deck.removeLast()
        return last
    }

    fun checkPileResult(first:String, second:String):Boolean{
        return (first.first() == second.first() || first.last() == second.last())
    }
}

class Player(val game: Game){
    var hand = drawCards(6)
    var graveyard = mutableListOf<String>() // MutableList<String> = [""]

    fun drawCards(handSize: Int): MutableList<String> {
        val playerCards = mutableListOf<String>()
        for (i in 0 until handSize) playerCards.add(game.drawCard())
        return playerCards
    }

    fun addToGraveyard(listToAdd: MutableList<String>){
        graveyard.let { list1 -> listToAdd.let(list1::addAll) }
    }

    fun calculateScore():Int{
        var cardsMap = graveyard.groupingBy { it[0] }.eachCount()
        cardsMap = cardsMap.filter { "AKJQ1".contains(it.key.toString()) }
        return cardsMap.values.sum()
    }

    fun chooseCart(game: Game):Int{
        var suitMap = emptyList<String>()
        var rankMap = emptyList<String>()
        if (game.pile.isNotEmpty()) {
            suitMap = hand.filter { game.pile.last().contains(it.last()) }
            rankMap = hand.filter { game.pile.last().contains(it.first()) }
        }
        if (game.pile.isEmpty() || (suitMap.isEmpty() && rankMap.isEmpty())) {
            var suitCount = hand.groupingBy { it.last() }.eachCount()
            val suitMax = suitCount.maxOf { it.value }
            var rankCount = hand.groupingBy { it.first() }.eachCount()
            val rankMax = rankCount.maxOf { it.value }
            suitCount = suitCount.filter { it.value == suitMax }
            rankCount = rankCount.filter { it.value == rankMax }
            val suitToChoose = hand.filter { suitCount.keys.contains(it.last()) }
            val rankToChoose = hand.filter { rankCount.keys.contains(it.first()) }
            return if (suitMax > 1) hand.indexOf(suitToChoose[0]) else hand.indexOf(rankToChoose[0])
        }
        when {
            hand.size == 1 -> return 0
            suitMap.size + rankMap.size == 1 -> return if (suitMap.isEmpty()) hand.indexOf(rankMap[0]) else hand.indexOf(suitMap[0])
            suitMap.size >= rankMap.size -> return hand.indexOf(suitMap[0])
            suitMap.size < rankMap.size -> return hand.indexOf(rankMap[0])
        }
        return 0
    }

}

fun printScore(player: Player, computer:Player, name:String){
    if (name != "last") println("$name wins cards")
    val lastAddPlayer = if (player.graveyard.size > computer.graveyard.size && name == "last") 3 else 0
    val lastAddComputer = if (player.graveyard.size < computer.graveyard.size && name == "last") 3 else 0
    println("Score: Player ${player.calculateScore() + lastAddPlayer} - Computer ${computer.calculateScore()  + lastAddComputer}")
    println("Cards: Player ${player.graveyard.size} - Computer ${computer.graveyard.size}")
}

fun main() {
    val game = Game()
    game.startGame()
    var ans: String
    val player = Player(game)
    val computer = Player(game)
    println("Indigo Card Game")
    do {
        println("Play first?")
        ans = readln()
    } while (ans !in listOf("yes", "no"))
    println("Initial cards on the table: ${game.pile.joinToString(" ")}")
    game.starter = if (ans == "yes") "Player" else "Computer"
    while (true){
        if (game.pile.size != 0) println("\n${game.pile.size} cards on the table, and the top card is ${game.pile.last()}")
            else println("\nNo cards on the table")
        when (ans){
            "yes" -> {
                print("Cards in hand: ")
                var cardIndex: Int
                for ((index, value) in player.hand.withIndex()) print("${index + 1})$value ")
                do {
                    println("\nChoose a card to play (1-${player.hand.size}):")
                    val ans2 = readln()
                    if (ans2 == "exit") println("Game Over").also {return}
                    cardIndex = if (ans2 in (1..player.hand.size).toList().map { it.toString() }) ans2.toInt() else 0
                } while (cardIndex !in 1..player.hand.size)
                val playerCard = player.hand.removeAt(cardIndex - 1)
                val pileLastCard = game.pile.lastOrNull()
                game.pile.add(playerCard)
                if (pileLastCard != null) {
                    if (game.checkPileResult(pileLastCard, playerCard)) {
                        game.winner = "Player"
                        player.addToGraveyard(game.pile)
                        game.pile.clear()
                        printScore(player, computer, "Player")
                    }
                }
                if (player.hand.size == 0 && game.deck.size != 0) player.hand = player.drawCards(6)
            }

            "no" -> {
                println(computer.hand.joinToString(" "))
                val computerCard = computer.hand.removeAt(computer.chooseCart(game)) //computer.hand.removeLast()

                println("Computer plays $computerCard")
                val pileLastCard = game.pile.lastOrNull()
                game.pile.add(computerCard)
                if (pileLastCard != null) {
                    if (game.checkPileResult(pileLastCard, computerCard)) {
                        game.winner = "Computer"
                        computer.addToGraveyard(game.pile)
                        game.pile.clear()
                        printScore(player, computer, "Computer")
                    }
                }
                if (computer.hand.size == 0 && game.deck.size != 0) computer.hand = computer.drawCards(6)
            }
        }
        if (ans == "yes") ans = "no" else ans = "yes"
        if (game.deck.size == 0 && computer.hand.size == 0 && player.hand.size == 0) {
            if (game.winner == "Player") player.addToGraveyard(game.pile) else computer.addToGraveyard(game.pile)
            if (game.pile.size != 0) println("\n${game.pile.size} cards on the table, and the top card is ${game.pile.last()}")
            else println("\nNo cards on the table")
            printScore(player, computer, "last")
            println("Game Over").also { return }
        }
    }
}
