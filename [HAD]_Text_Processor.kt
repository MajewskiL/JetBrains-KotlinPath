import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

const val FILE_NAME = "history.json"
data class MostUsedChars(var elements: Set<Char>, var count: Int)
data class MostUsedWords(var elements: Set<String>, var count: Int)
data class Characters(var count: Int, var `exclude-spaces`: Int, var `most-used`: MostUsedChars)
data class Words(var count: Int, var `most-used`: MostUsedWords)
data class Text(var type: String, var content:String, var characters: Characters, var words: Words)

fun selectLetters(lettersMap: Map<Char, Int>):Map<Char, Int>{
    val lettersMapFun = lettersMap.toMutableMap()
    val maxValue = lettersMapFun.maxBy { it.value }.value
    val mapOfLettersOut = lettersMapFun.filter { it.value == maxValue }
    return if (mapOfLettersOut.size == lettersMap.size) mapOf() else mapOfLettersOut
}

fun readTextLocal():Collection<String> {
    val inputText: MutableList<String> = mutableListOf()
    println("Enter text (enter a blank line to end): ")
    txtLoop@ do {
        when (val txt = readln()) {
            "" -> if (inputText.size == 0) println("No text provided!") else break@txtLoop
            else -> inputText.add(txt.trim()) // .removeSuffix("\n"))
        }
    } while (true)
    return (inputText)
}

fun writeToFile(newItem: Text, fileName: String){
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapterType = Types.newParameterizedType(List::class.java, Text::class.java)
    val textAdapter = moshi.adapter<List<Text>>(adapterType)
    val fileToSave = File(fileName)
    if (fileToSave.exists()) {
        val readFromFile = fileToSave.readText()

        var listOfJsons = textAdapter.fromJson(readFromFile)
        listOfJsons = listOfJsons?.toMutableList()
        listOfJsons?.add(newItem)
        fileToSave.writeText(textAdapter.toJson(listOfJsons))
    }
    else {
        val textList = listOf(newItem)
        val listOfJsons = textAdapter.toJson(textList)
        fileToSave.writeText(listOfJsons)
    }
}
fun messageGenerator(txt: Text):String {
    val firstL = if (txt.characters.`most-used`.elements.isEmpty()) ": -" else
        "${if (txt.characters.`most-used`.elements.size == 1) "" else "s"}: " + txt.characters.`most-used`.elements.joinToString(", ")
    val secondL =
        if (txt.characters.`most-used`.elements.isEmpty()) "" else " (${txt.characters.`most-used`.count} times)"
    val secondW =
        if (txt.words.`most-used`.elements.isEmpty()) "" else " (${txt.words.`most-used`.count} times)"
    val firstW = if (txt.words.`most-used`.elements.isEmpty()) ": -" else
        "${if (txt.words.`most-used`.elements.size == 1) "" else "s"}: " + txt.words.`most-used`.elements.joinToString(", ")
    return "Characters: ${txt.characters.count}\n" +
            "Characters excluding spaces: ${txt.characters.`exclude-spaces`}\n" +
            "Most used character$firstL$secondL\n" +
            "Words: ${txt.words.count}\n" +
            "Most used word$firstW$secondW"
}

fun printHistory(fileName:String = "") {
    println("== History ==")
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapterType = Types.newParameterizedType(List::class.java, Text::class.java)
    val textAdapter = moshi.adapter<List<Text>>(adapterType)
    val fileToSave = File(FILE_NAME)
    val readFromFile = fileToSave.readText()
    var listOfJsons = textAdapter.fromJson(readFromFile)
    listOfJsons = listOfJsons?.toMutableList()
    if (listOfJsons != null) {
        var messageToWrite = "== History ==\n\n"
        for (list in listOfJsons) {
            val header = if (list.type == "text") "\nText:\n${list.content}" else "\nFile: ${list.content}"
            val body = messageGenerator(list)
            messageToWrite += "$header\n$body"
        }
        if (fileName.isNotEmpty()) {
            val fileToWrite = File(fileName)
            fileToWrite.writeText(messageToWrite)
        } else println(messageToWrite)

    }
}



fun selectWords(wordsMap: Map<String, Int>):Map<String, Int>{
    val wordsMapFun = wordsMap.toMutableMap()
    val maxValue = wordsMapFun.maxBy { it.value }.value
    val mapOfWordsOut = wordsMapFun.filter { it.value == maxValue }
    return if (mapOfWordsOut.size == wordsMap.size) mapOf() else mapOfWordsOut
}

fun countWords(txt:String):Words {
    var txtInput = txt.lowercase()
    for (sign in "{}[](),.!?;:")
        while (sign in txtInput) txtInput = txtInput.replace(sign.toString(), "")
    while ("\n" in txtInput) txtInput = txtInput.replace("\n", " ")
    val words = txtInput.split(" ")
    val freqMap: MutableMap<String, Int> = words.groupingBy { it }.eachCount().toMutableMap()
    if (freqMap.containsKey("")) freqMap.remove("")
    if (freqMap.containsKey("\n")) freqMap.remove("\n")
    val freqMapMax = selectWords(freqMap)
    val keysToReturn = freqMapMax.keys
    val countToReturn = if ((keysToReturn.isEmpty())) 0 else freqMap[keysToReturn.first()]!!
    return Words(words.size, MostUsedWords(keysToReturn , countToReturn))
}

fun countLetters(txt:String):Characters {
    val numberOfLetters = txt.length
    var numberOfSpacesAndNewLines = 0
    val frequenciesByChar = txt.groupingBy { it }.eachCount().toMutableMap()
    if (frequenciesByChar.containsKey(' ')) {
        numberOfSpacesAndNewLines += frequenciesByChar[' ']!!
        frequenciesByChar -= ' '
    }
    if (frequenciesByChar.containsKey('\n')) {
        numberOfSpacesAndNewLines += frequenciesByChar['\n']!!
        frequenciesByChar -= '\n'
    }
    val maxLetters = selectLetters(frequenciesByChar)
    val keysToReturn = maxLetters.keys
    val countToReturn = if ((keysToReturn.isEmpty())) 0 else maxLetters[maxLetters.keys.first()]!!
    return Characters(numberOfLetters, numberOfLetters - numberOfSpacesAndNewLines, MostUsedChars(keysToReturn, countToReturn))
}

fun inputManual():Text{
    val txt = readTextLocal()
    if (txt.isEmpty()) return Text("escape", "",
        Characters(0, 0, MostUsedChars(setOf(), 0)), Words(0, MostUsedWords(setOf(), 0)))
    val txtToString = txt.joinToString("\n")
    return Text("text", txtToString, countLetters(txtToString), countWords(txtToString))
}

fun inputFile(fileName: String):Text{
    val fileToRead = File(fileName)
    if (fileToRead.exists()) {
        val txt = fileToRead.readText()
        val newItemType = "file"
        val newText = Text(newItemType, fileName, countLetters(txt), countWords(txt))
        return newText
    }
    else println("File not found!1")
    return Text("escape", "", Characters(0, 0, MostUsedChars(setOf(), 0)),
                Words(0, MostUsedWords(setOf(), 0)))
}

fun correctText(data: String):String{
    val listTmp = data.lines().toMutableList()
    for (i in 0 until listTmp.size) listTmp[i] = listTmp[i].trim()             // trim
    var txt = listTmp.joinToString("\n")
    while ("  " in txt) txt = txt.replace("  ", " ")
    for (bracket in setOf("{", "(", "["))                                            // delete space after open brackets
        while ("$bracket " in txt) txt = txt.replace("$bracket ", bracket)
    for (bracket in setOf("}", ")", "]", ".", ",", ":", ";", "?", "!"))              // delete space before all close signs and punctuation
        while (" $bracket" in txt) txt = txt.replace(" $bracket", bracket)
    val indexes = mutableListOf<Int>()
    for (bracket in setOf(".", ",", ":", ";", "?", "!", "{", "(", "[", "}", ")", "]")) //, "\n"))   // collect all non-letter signs
        for (i in txt.indices) if (txt[i].toString() == bracket) indexes.add(i)
    if (indexes.isNotEmpty()) {
        for (i in indexes.sorted().asReversed()) {
            when (txt[i]){
                in "{[(" -> try {
                                if (!txt[i - 1].isWhitespace() && txt[i - 1].isLetter()) txt =
                                "${txt.subSequence(0, i)} ${txt.subSequence(i, txt.length)}"
                                }                                                   // add space if letter is before brackets
                            catch (e: StringIndexOutOfBoundsException) {continue}
                in "}])" -> try {
                                if (!txt[i + 1].isWhitespace() && txt[i + 1].isLetter()) txt =
                                    "${txt.subSequence(0, i + 1)} ${txt.subSequence(i + 1, txt.length)}"
                                }                                                   // add space if letter is after brackets
                            catch (e: StringIndexOutOfBoundsException) {continue}
                in ".?!" -> {
                    try {
                        if (!txt[i + 1].isWhitespace() && txt[i + 1].isLetter()) txt =
                            "${txt.subSequence(0, i + 1)} ${txt.subSequence(i + 1, txt.length)}"
                    }
                        catch (e: StringIndexOutOfBoundsException) {continue}
                     try {
                        for (j in i + 1 until txt.length) if (txt[j].isLetter()) {
                            txt = "${txt.subSequence(0, j)}${txt[j].uppercase()}${txt.subSequence(j + 1, txt.length)}"
                            break
                        }
                    }
                        catch (e: StringIndexOutOfBoundsException) {continue}
                }                                                       // add space and UpperCase after end-sentence punctuation
                else -> try {
                            if (!txt[i + 1].isWhitespace()) txt =
                            "${txt.subSequence(0, i + 1)} ${txt.subSequence(i + 1, txt.length)}"
                            }                                                       // add space after other punctuation
                        catch (e: StringIndexOutOfBoundsException) {continue}
            }
        }
    }
    for (i in txt.indices) if (txt[i].isLetter()) {
        txt = "${txt.subSequence(0, i)}${txt[i].uppercase()}${txt.subSequence(i + 1, txt.length)}"
        break
    }
    return txt
}

fun main(args: Array<String>){
    var ans = "exit"
    var ans2 = "manual"
    var fileLoadFromArg = ""
    var fileSaveFromArg = ""
    if (args.isNotEmpty()) {
        when(args[0]) {
            "-h", "--help" -> ans = "exit"
            "--format", "--statistics", "--history"-> ans = args[0].subSequence(2, args[0].length).toString()
            else -> println("Invalid argument!").also {return}
        }
    }
    if (args.size > 1) if (args[1] !in setOf("--out", "--output", "--input", "--in") || args.size > 5) println("Invalid argument!").also {return}
    if (args.size > 3) if (args[3] !in setOf("--out", "--output", "--input", "--in")) println("Invalid argument!").also {return}

    for (argument in setOf("--input", "--in"))
        if (args.contains(argument)) ans2 = "file".also {fileLoadFromArg = args[args.indexOf(argument) + 1]}
    for (argument in setOf("--out", "--output"))
        if (args.contains(argument)) fileSaveFromArg = args[args.indexOf(argument) + 1]

    when (ans) {
        "exit" -> println("Help Me Obi-Wan Kenobi, You’re My Only Hope!").also { return }
        "format" -> formatLoop@ while (true) {
            when (ans2) {
                "manual" -> {
                    val newText = inputManual()
                    if (newText.type != "escape") {
                        newText.content = correctText(newText.content)
                        if (fileSaveFromArg.isNotEmpty()) {
                            val filePointer = File(fileSaveFromArg)
                            filePointer.writeText(newText.content)
                            println("Formatted text saved to the file successfully!$fileSaveFromArg")
                        }
                        else println(newText.content)
                    }
                    return
                }
                "file" -> {
                    val filePointer = File(fileLoadFromArg)
                    if (filePointer.exists()) {
                        var txtToCorrect = filePointer.readText()
                        txtToCorrect = correctText(txtToCorrect)
                        if (fileSaveFromArg.isNotEmpty()) {
                            val filePointerToSave = File(fileSaveFromArg)
                            filePointerToSave.writeText(txtToCorrect)
                            println("Formatted text saved to the file successfully!")
                            return
                        }
                        else println(txtToCorrect)
                    }
                    else println("File not found!")
                    return
                }
                else -> println("Invalid input!")
            }
        }
        "statistics" -> {
            inputLoop@ while (true) {
                when (ans2) {
                    "manual" -> {
                        val newText = inputManual()
                        if (newText.type != "escape") {
                            if (fileSaveFromArg.isNotEmpty()) {
                                val filePointer = File(fileSaveFromArg)
                                filePointer.writeText(messageGenerator(newText))
                            }
                            else println(messageGenerator(newText))
                            writeToFile(newText, FILE_NAME)
                        }
                        return
                    }
                    "file" -> {
                        val newText = inputFile(fileLoadFromArg)
                        if (newText.type != "escape") {
                            if (fileSaveFromArg.isNotEmpty()) {
                                val filePointer = File(fileSaveFromArg)
                                filePointer.writeText(messageGenerator(newText))
                            }
                            else println(messageGenerator(newText))
                            writeToFile(newText, FILE_NAME)
                            return
                        }
                        else return
                    }
                    else -> println("Invalid input!")
                }
            }
        }
        "history" -> printHistory(fileSaveFromArg)
        else -> println("Invalid input!")
    }
}
