import java.util.*


class Trees(val cellIndex: Int,
            val size: Int,
            val isDormant: Boolean) {

}


class Cells(val index: Int,
            val richness: Int,
            val neigh0: Int,
            val neigh1: Int,
            val neigh2: Int,
            val neigh3: Int,
            val neigh4: Int,
            val neigh5: Int) {

}

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
fun main(args: Array<String>) {

    val listOfMineTrees = mutableListOf<Trees>()
    val listOfEnemieTrees = mutableListOf<Trees>()
    val forest = mutableListOf<Cells>()
    val goodRichness = mutableListOf<Cells>()
    val bestRichness = mutableListOf<Cells>()

    val input = Scanner(System.`in`)
    val numberOfCells = input.nextInt() // 37
    for (i in 0 until numberOfCells) {
        val index = input.nextInt() // 0 is the center cell, the next cells spiral outwards
        val richness = input.nextInt() // 0 if the cell is unusable, 1-3 for usable cells
        val neigh0 = input.nextInt() // the index of the neighbouring cell for each direction
        val neigh1 = input.nextInt()
        val neigh2 = input.nextInt()
        val neigh3 = input.nextInt()
        val neigh4 = input.nextInt()
        val neigh5 = input.nextInt()
        val cell = Cells(index, richness, neigh0, neigh1, neigh2, neigh3, neigh4, neigh5)
        forest.add(cell)
        if (richness == 3) {
            bestRichness.add(cell)
        } else if (richness == 2) {
            goodRichness.add(cell)
        }
    }

    // game loop
    while (true) {
        listOfEnemieTrees.clear()
        listOfMineTrees.clear()
        //forest.clear()
        val day = input.nextInt() // the game lasts 24 days: 0-23
        val nutrients = input.nextInt() // the base score you gain from the next COMPLETE action
        val sun = input.nextInt() // your sun points
        val score = input.nextInt() // your current score
        val oppSun = input.nextInt() // opponent's sun points
        val oppScore = input.nextInt() // opponent's score
        val oppIsWaiting = input.nextInt() != 0 // whether your opponent is asleep until the next day
        val numberOfTrees = input.nextInt() // the current amount of trees
        for (i in 0 until numberOfTrees) {
            val cellIndex = input.nextInt() // location of this tree
            val size = input.nextInt() // size of this tree: 0-3
            val isMine = input.nextInt() != 0 // 1 if this is your tree
            val isDormant = input.nextInt() != 0 // 1 if this tree is dormant
            val tree = Trees(cellIndex, size, isDormant)
            if (isMine) {
                listOfMineTrees.add(tree)
            } else {
                listOfEnemieTrees.add(tree)
            }
        }
        val numberOfPossibleActions = input.nextInt() // all legal actions
        if (input.hasNextLine()) {
            input.nextLine()
        }
        val listOfPossibleActions = mutableListOf<String>()
        for (i in 0 until numberOfPossibleActions) {
            val possibleAction = input.nextLine() // try printing something from here to start with
            listOfPossibleActions.add(possibleAction)
        }

        if (day >= 7) {
            if (listOfPossibleActions.size == 1) {
                println(listOfPossibleActions[0])
            } else {
                if(listOfPossibleActions[1].contains("COMPLETE")) {
                    println(listOfPossibleActions[1])
                }
                else if (listOfPossibleActions[1].contains("GROW")) {
                    println(listOfPossibleActions[1])
                } else {
                    println(listOfPossibleActions[0])
                }
            }
        } else {
            if (listOfPossibleActions.size == 1) {
                println(listOfPossibleActions[0])
            } else {
                if (listOfPossibleActions[1].contains("SEED")) {
                    listOfPossibleActions.removeAt(0)
                    if (makeBestSeedThrow(bestRichness, listOfPossibleActions)) continue
                    else if (makeGoodSeedThrow(goodRichness, listOfPossibleActions)) continue
                    else println(listOfPossibleActions[1])
                } else {
                    println(listOfPossibleActions[1])
                }
            }
        }


/*        if (listOfMineTrees.isEmpty()) {
            println("WAIT")
        } else if (completeTreeWithSizeThree(listOfMineTrees)) continue
        else if (growTreesWithSizeTwo(listOfMineTrees)) continue
        else if (growSmallestTree(listOfMineTrees)) continue
        else {
            println("COMPLETE ${listOfMineTrees[0].cellIndex}")
        }*/
    }
}

fun makeGoodSeedThrow(goodRichness: MutableList<Cells>, listOfPossibleActions: MutableList<String>): Boolean {
    listOfPossibleActions.forEach() {
        val regex = "[0-9][0-9]\\s*([0-9]?[0-9])".toRegex()
        val cellIndex1 = regex.find(it)?.groupValues?.get(1)?.toInt()
        if (cellIndex1 in goodRichness.map { cells -> cells.index }) {
            println(it)
            return true
        }

    }
    return false
}

fun makeBestSeedThrow(bestRichness: MutableList<Cells>, listOfPossibleActions: MutableList<String>): Boolean {
    listOfPossibleActions.forEach() {
        val regex = "[0-9][0-9]\\s*([0-9]?[0-9])".toRegex()
        val cellIndex1 = regex.find(it)?.groupValues?.get(1)?.toInt()
        if (cellIndex1 in bestRichness.map { cells -> cells.index }) {
            println(it)
            return true
        }

    }
    return false
}

/*fun getRichness(cellIndex: String?, forest: MutableList<Cells>): Int {
    return forest[cellIndex?.toInt()!!].index
}

fun getBestSeedThrow(actions: MutableList<String>, seedThrow: String, forest: MutableList<Cells>): String {
    if (actions.size == 1) {
        return actions[0]
    }
    val potentialBest = actions[0]
    val regex = "[0-9][0-9]\\s*([0-9]?[0-9])".toRegex()
    val cellIndex1 = regex.find(seedThrow)?.groupValues?.get(1)
    val cellIndex2 = regex.find(potentialBest)?.groupValues?.get(1)
    val richness1 = getRichness(cellIndex1, forest)
    val richness2 = getRichness(cellIndex2, forest)
    return if (richness1 < richness2) {
        actions.remove(seedThrow)
        getBestSeedThrow(actions, potentialBest, forest)
    } else {
        actions[0]
    }
}*/

fun getNeighbors(distance: Int, cellIndex: Int, forest: MutableList<Cells>): List<Int> {
    val neighborsList = mutableListOf<Int>()
    when (distance) {
        1 -> {
            neighborsList.addAll(cellToIndexList(forest, cellIndex))
        }
        2 -> {
            neighborsList.addAll(cellToIndexList(forest, cellIndex))
            neighborsList.removeAll(listOf(-1))
            neighborsList.addAll(neighborsOfNeighbors(forest, neighborsList))
            neighborsList.removeAll(listOf(-1))
        }
        3 -> {
            neighborsList.addAll(cellToIndexList(forest, cellIndex))
            neighborsList.removeAll(listOf(-1))
            neighborsList.addAll(neighborsOfNeighbors(forest, neighborsList))
            neighborsList.removeAll(listOf(-1))
            neighborsList.addAll(neighborsOfNeighbors(forest, neighborsList))
            neighborsList.removeAll(listOf(-1))
        }
    }
    neighborsList.removeAll(listOf(cellIndex))
    return neighborsList.distinct()
}

fun neighborsOfNeighbors(forest: MutableList<Cells>, neighborsList: MutableList<Int>): Collection<Int> {
    val neighbors = mutableListOf<Int>()
    neighborsList.forEach() {
        neighbors.addAll(cellToIndexList(forest, it))
    }
    return neighbors
}

fun cellToIndexList(forest: MutableList<Cells>, cellIndex: Int): MutableList<Int> {
    return mutableListOf(forest[cellIndex].neigh0, forest[cellIndex].neigh1, forest[cellIndex].neigh2, forest[cellIndex].neigh3, forest[cellIndex].neigh4, forest[cellIndex].neigh5)
}

fun completeTreeWithSizeThree(listOfMineTrees: MutableList<Trees>): Boolean {
    var tree = Trees(0, 0, false)
    listOfMineTrees.forEach() {
        if (it.size == 3) {
            tree = it
        }
    }
    return if (tree.size == 0) {
        false
    } else {
        println("COMPLETE ${tree.cellIndex}")
        true
    }
}

fun growTreesWithSizeTwo(listOfMineTrees: MutableList<Trees>): Boolean {
    var tree = Trees(0, 0, false)
    listOfMineTrees.forEach() {
        if (it.size == 2) {
            tree = it
        }
    }
    return if (tree.size == 0) {
        false
    } else {
        println("GROW ${tree.cellIndex}")
        true
    }
}

fun growSmallestTree(listOfMineTrees: MutableList<Trees>): Boolean {
    listOfMineTrees.sortBy { it.size }
    val tree = listOfMineTrees[0]
    return if (tree.size == 3) {
        false
    } else {
        println("GROW ${tree.cellIndex}")
        true
    }
}
