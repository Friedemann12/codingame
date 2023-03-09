import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

sealed class Action {
    data class Complete(val index: Int) : Action()
    data class Grow(val index: Int) : Action()
    data class Seed(val index: Int, val to: Int) : Action()
    data class Wait(val say: String) : Action()

    // GROW cellIdx | SEED sourceIdx targetIdx | COMPLETE cellIdx | WAIT <message>
    fun toOutput() = when (this) {
        is Action.Complete -> "COMPLETE $index"
        is Action.Grow -> "GROW $index"
        is Action.Seed -> "SEED $index $to"
        is Action.Wait -> "WAIT $say"
    }

    fun getTarget(): Int {
        when (this) {
            is Action.Complete -> return index
            is Action.Grow -> return index
            is Action.Seed -> return index
            else -> return 99
        }
    }

    fun getSource(): Int {
        when (this) {
            is Action.Seed -> return to
            else -> return 99
        }
    }


    companion object {
        fun of(st: String): Action = st.split(" ").let {
            when (it[0]) {
                "COMPLETE" -> Complete(it[1].toInt())
                "GROW" -> Grow(it[1].toInt())
                "SEED" -> Seed(it[1].toInt(), it[2].toInt())
                "WAIT" -> Wait("")
                else -> error("unknow Action $st")
            }
        }
    }
}

data class Cell(val index: Int, val richness: Int, val neigh: Array<Int>)
data class Tree(val cellIndex: Int, var size: Int, val isMine: Boolean, val isDormant: Boolean)

class Game(val input: Scanner) {

    // index 0 is the center cell, the next cells spiral outwards
    // richness 0 if the cell is unusable, 1-3 for usable cells
    val nbCells = input.nextInt() // 37
    val cells = List(nbCells) {
        Cell(input.nextInt(), input.nextInt(), Array(6) { input.nextInt() })
    }

    var day: Int = -1 // the game lasts 24 days: 0-23
    var nutrients: Int = -1 // the base score you gain from the next COMPLETE action
    var sun: Int = -1 // your sun points
    var score: Int = -1 // your current score
    var oppSun: Int = -1 // opponent's sun points
    var oppScore: Int = -1 // opponent's score
    var oppIsWaiting: Boolean = false // whether your opponent is asleep until the next day
    var trees: List<Tree> = emptyList()
    var possibleMoves: List<Action> = emptyList()

    fun read() {
        day = input.nextInt()
        nutrients = input.nextInt()
        sun = input.nextInt()
        score = input.nextInt()
        oppSun = input.nextInt()
        oppScore = input.nextInt()
        oppIsWaiting = input.nextInt() != 0
        val numberOfTrees = input.nextInt()
        trees = List(numberOfTrees) {
            Tree(input.nextInt(), input.nextInt(), input.nextInt() != 0, input.nextInt() != 0)
        }
        val numberOfPossibleMoves = input.nextInt()
        if (input.hasNextLine()) {
            input.nextLine()
        }
        possibleMoves = List(numberOfPossibleMoves) { Action.of(input.nextLine()) }
    }
}

fun filterPossiMoves(possibleMoves: List<Action>): List<Action> {
    val possibleMovesTmp = possibleMoves.toMutableList()
    possibleMoves.forEach {
        if (it is Action.Seed || it is Action.Wait) {
            possibleMovesTmp.remove(it)
        }
    }
    return possibleMovesTmp
}


fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    val game = Game(input)
    var seeded = 0
    var growOpp = 0
    // game loop
    while (true) {
        game.read()
        if (seeded != game.day && canSeed(game.possibleMoves, game.trees) && game.day <= 10) {
            seeded = game.day
            getBestSeedMove(game.possibleMoves, game.cells)
        } else {
            val filterdPossiMoves = filterPossiMoves(game.possibleMoves)
            println(nextMove(filterdPossiMoves, game).toOutput())
        }

    }
}

fun nextMove(filterdPossiMoves: List<Action>, game: Game): Action {
    if (filterdPossiMoves.isEmpty()) {
        return Action.of("WAIT So eine KACKE!!!")
    }
    var bestMove = Action.of("WAIT")
    var bestH = -9999
    filterdPossiMoves.forEach {
        val tempH = minimax(it, game.sun, game.score, game.trees, game.cells, game.oppSun, game.oppScore, game.nutrients, game.day, 0, true, game.day)
        if (tempH >= bestH) {
            bestH = tempH
            bestMove = it
        }
    }
    return bestMove
}

fun minimax(move: Action, sun: Int, score: Int, trees: List<Tree>, cells: List<Cell>, oppSun: Int, oppScore: Int, gameNutrients: Int, day: Int, depth: Int, isPlayer: Boolean, startDay: Int): Int {
    System.err.println("Tag: $day, Tiefe: $depth")
    if (day - startDay == 2 || day >= 23) {
        return rateState(sun, move, trees, day, oppScore, gameNutrients, cells, score)
    }
    var oppSunPoints = oppSun
    var oppPoints = oppScore
    var tag = day
    var nutrients = gameNutrients
    var sunPoints = sun
    var treeList = trees
    var points = score
    var tiefe = depth
    var newPossibleMoves = getNewPossibleMoves(treeList, sunPoints)
    if (notEnoughSunPoint(move, sun, treeList) || newPossibleMoves.isEmpty()) {
        sunPoints = getSunPointsForNextDay(trees, calculateShadows(treeList, tag, cells), sunPoints, true)
        tag += 1
        nutrients -= 1
    } else {
        treeList = makeMove(treeList, move)
        sunPoints = sunPointsAfterMove(sunPoints, move, treeList)
        points = scoreAfterMove(points, move, nutrients, cells)
    }
    if (isPlayer) {
        var bestH = -9999
        newPossibleMoves = getNewPossibleMoves(treeList, sunPoints)
        if (newPossibleMoves.isEmpty()) {
            return rateState(sun, move, trees, day, oppScore, gameNutrients, cells, score)
        }
        newPossibleMoves.forEach {
            val tempH = minimax(it, sunPoints, points, treeList, cells, oppSunPoints, oppPoints, nutrients, tag, tiefe++, false, startDay)
            bestH = max(tempH, bestH)
        }
        return bestH
    } else {
        var bestH = 9999
        newPossibleMoves = getNewPossibleMoves(treeList, sunPoints)
        treeList = growAllOppTrees(treeList)
        newPossibleMoves.forEach {
            val tempH = minimax(it, sunPoints, points, treeList, cells, oppSunPoints, oppPoints, nutrients, tag, tiefe++, false, startDay)
            bestH = min(tempH, bestH)
        }
        return bestH
    }
}

fun getOppPoints(treeList: List<Tree>, oppScore: Int, nutri: Int): Int {
    var oppPoint = oppScore
    treeList.forEach {
        if (!it.isMine && it.size == 3) {
            oppPoint += nutri + 3
        }
    }
    return oppPoint
}

fun growAllOppTrees(treeList: List<Tree>): List<Tree> {
    treeList.forEach {
        if (!it.isMine && it.size < 3) {
            it.size++
        }
    }
    return treeList
}


fun rateState(sun: Int, move: Action, trees: List<Tree>, day: Int, oppScore: Int, nutrients: Int, cells: List<Cell>, points: Int): Int {
  //  if (day >= 15 && move is Action.Complete) {
//        return 999 * (points + (0.33 + 0.01 * 23 - day) * (sun)).toInt() - (0 * getOppPoints(trees, oppScore, nutrients)).toInt()

    //}
    //if (day <= 8 && move is Action.Grow) {
    //    return 999 * (points + (0.33 + 0.01 * 23 - day) * (sun)).toInt() - (0 * getOppPoints(trees, oppScore, nutrients)).toInt()
    //}
    return (points + (0.33 + 0.01 * 23 - day) * (sun)).toInt() - (0 * getOppPoints(trees, oppScore, nutrients)).toInt()
}


fun getNewPossibleMoves(trees: List<Tree>, sun: Int): List<Action> {
    val returnList = mutableListOf<Action>()
    trees.forEach {
        if (it.isMine) {
            if (it.size >= 3) {
                val move = Action.of("COMPLETE ${it.cellIndex}")
                if (enougSunPoints(sun, move, trees)) {
                    returnList.add(move)
                }
            } else {
                val move = Action.of("GROW ${it.cellIndex}")
                if (enougSunPoints(sun, move, trees)) {
                    returnList.add(move)
                }
            }
        }
    }
    return returnList

}

fun enougSunPoints(sun: Int, move: Action, trees: List<Tree>): Boolean {
    return (sun - calcSunPointsAfterMove(sun, move, trees) >= 0)
}


fun getSunPointsForNextDay(allTrees: List<Tree>, shadows: List<Int>, sun: Int, isPlayer: Boolean): Int {
    var sunPoints = sun
    for (each in allTrees) {
        if (each.isMine == isPlayer && each.size > shadows[each.cellIndex]) sunPoints += each.size
    }
    return sunPoints
}

fun calculateShadows(allTrees: List<Tree>, day: Int, cells: List<Cell>): List<Int> {
    val sunDirection = day % 6
    //      val shadowDirection = (day+3) % 6
    val shadows = List(37) { 0 }.toMutableList()  // initial hat keine der 37 Zellen einen Schatten (shadow = 0)
    for (each in allTrees) {
        var tempCounter = 0  // ein counter, der die Schattenlänge (=^ Baumgröße) berücksichtigt
        var currentCell = cells[each.cellIndex]
        while (currentCell.neigh[sunDirection] != -1 && tempCounter++ < each.size) {  // so lang der Schatten nicht aus dem Spielfeld fällt:
            currentCell = cells[currentCell.neigh[sunDirection]]
            if (shadows[currentCell.index] < each.size) {
                shadows[currentCell.index] = each.size
            }
        }
    }
    return shadows
}


fun scoreAfterMove(points: Int, move: Action, nutrients: Int, cells: List<Cell>): Int {
    return if (move is Action.Complete) {
        when (cells[move.index].richness) {
            2 -> points + nutrients + 2
            3 -> points + nutrients + 4
            else -> points
        }
    } else points
}

private fun notEnoughSunPoint(nextMove: Action, sun: Int, trees: List<Tree>): Boolean {
    return if (calcSunPointsAfterMove(sun, nextMove, trees) <= 0) {
        true
    } else true
}


private fun calcSunPointsAfterMove(currentSunPoint: Int, nextMove: Action, trees: List<Tree>): Int {
    return when (nextMove) {
        is Action.Seed -> currentSunPoint - getNumberOfSizeXTrees(0, trees)
        is Action.Wait -> currentSunPoint
        is Action.Grow -> currentSunPoint - getCostOfGrowing(nextMove, trees)
        is Action.Complete -> currentSunPoint - 4
    }

}


fun sunPointsAfterMove(currentSunPoint: Int, nextMove: Action, trees: List<Tree>): Int {
    return when (nextMove) {
        is Action.Seed -> currentSunPoint - getNumberOfSizeXTrees(0, trees)
        is Action.Wait -> currentSunPoint
        is Action.Grow -> currentSunPoint - getCostOfGrowing(nextMove, trees)
        is Action.Complete -> currentSunPoint - 4
    }

}

fun getCostOfGrowing(move: Action.Grow, trees: List<Tree>): Int {
    val cellOfTree = move.index
    var tree = trees[0]
    trees.forEach() {
        if (it.cellIndex == cellOfTree)
            tree = it
    }
    return when (tree.size) {
        0 -> 1 + getNumberOfSizeXTrees(1, trees)
        1 -> 3 + getNumberOfSizeXTrees(2, trees)
        2 -> 7 + getNumberOfSizeXTrees(3, trees)
        else -> 99
    }
}


fun makeMove(trees: List<Tree>, move: Action): List<Tree> {
    when (move) {
        is Action.Grow -> {
            trees.forEach { tree ->
                if (tree.cellIndex == move.index) {
                    tree.size += 1
                }
            }
        }
        is Action.Complete -> {
            trees.forEach { tree ->
                if (tree.cellIndex == move.index) {
                    tree.size = 99
                }

            }

        }
    }
    return trees.filter { it.size != 99 }
}


fun canSeed(possibleMoves: List<Action>, trees: List<Tree>): Boolean {
    return getNumberOfSizeXTrees(0, trees) == 0
}

private fun getNumberOfSizeXTrees(size: Int, trees: List<Tree>): Int {
    var sum = 0
    trees.forEach {
        if (it.size == size && it.isMine) {
            sum++
        }

    }
    return sum
}


fun getBestSeedMove(possibleMoves: List<Action>, cells: List<Cell>) {
    var bestSeedMove = possibleMoves.last()
    var bestRichness = 0
    possibleMoves.forEach {
        when (it) {
            is Action.Seed -> {
                if (bestRichness <= cells[it.to].richness) {
                    bestRichness = cells[it.to].richness
                    bestSeedMove = it
                }
            }
        }
    }
        println(bestSeedMove.toOutput())
    
}





