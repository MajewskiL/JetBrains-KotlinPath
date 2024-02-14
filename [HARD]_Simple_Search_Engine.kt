package search
import java.io.File

fun main(args: Array<String>) {
    val file = File(args[1])
    val allList = file.readLines()
    val mapOfWords  = mutableMapOf<String, List<Int>>()
    for ((index, line) in allList.withIndex())
        for (l in line.split(" ")){
            val lUp = l.uppercase()
            if (mapOfWords[lUp]?.isNotEmpty() == true) mapOfWords[lUp] = mapOfWords[lUp]!! + index
            else mapOfWords[lUp] = listOf(index)

        }
    do {
        println("\n=== Menu ===\n1. Find a person\n2. Print all people\n0. Exit")
        val ans = readln()
        when (ans){
            "1" -> {
                val allIndexes = allList.indices.toMutableList()
                val cntResults = mutableListOf<String>()
                val listOfResults = mutableListOf<List<Int>>()
                println("Select a matching strategy: ALL, ANY, NONE")
                val startegy = readln()
                println("\nEnter a name or email to search all suitable people.")
                val listOfTxt = readln().uppercase().split(" ")
                for (word in listOfTxt) {
                    try {
                        mapOfWords[word]?.let { listOfResults.add(it) }
                    } catch (e: NullPointerException) {
                        continue
                        //println("No matching people found.\n")
                    }
                }

                val result: MutableList<Int> = mutableListOf()
                listOfResults.forEach { list -> result.addAll(list) }
                when (startegy) {
                    "ANY" -> {
                        if (listOfResults.isEmpty()) {
                            println("No matching people found.\n")
                            continue
                        }
                        val finalResult = result.toSet().sorted()
                        println("\n${finalResult.size} person${if (finalResult.size > 1) "s" else ""} found:")
                        for (index in finalResult){
                            println(allList[index])
                        }
                    }
                    "NONE" -> {
                        val finalResult = allIndexes - result.sorted().toSet()
                        println("\n${finalResult.size} person${if (finalResult.size > 1) "s" else ""} found:")
                        for (index in finalResult){
                            println(allList[index])
                        }
                    }
                    "ALL" -> {
                        val finalResult = result.groupingBy { it }.eachCount().filter { it.value == listOfTxt.size }.toSortedMap()
                        println("\n${finalResult.size} person${if (finalResult.size > 1) "s" else ""} found:")
                        for ((key, _) in finalResult){
                            println(allList[key])
                    }
                }
            }
            }
            "2" -> {
                println("=== List of people ===")
                println(allList.joinToString("\n"))
            }
            "0" -> continue
            else -> println("Incorrect option! Try again.")
        }
    } while (ans != "0")
    println("Bye!")
}


