import java.util.*
import java.lang.Exception
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * Grab Snaffles and try to throw them through the opponent's goal!
 * Move towards a Snaffle and use your team id to determine where you need to throw it.
 **/
var myTeamId = 10
var alreadyIved: Boolean = false
var tempTarget: Entity = Entity()
const val BORDER_RIGHT: Int = 16000
const val BORDER_LEFT: Int = 0
const val UPPER_GOAL_POLE = 1950
const val UNDER_GOAL_POLE = 5550
val RANGE_GOAL = 2020..5480
val RANGE_UNDER_GOAL_EDGING = 9800..12920
val RANGE_UPPER_GOAL_EDGING = -5480..-3000
const val OBLIVION_COST = 5
const val PETRIFICUS_COST = 10
const val ACCIO_COST = 15
const val FLIPENDO_COST = 20

val myWizards = mutableListOf(Entity(), Entity())
val enemyWizards = mutableListOf(Entity(), Entity())
val allEntitys = mutableListOf<Entity>()

data class GameInput(
        var myScore: Int = 0,
        var myMagic: Int = 0,
        var opponentScore: Int = 0,
        var opponentMagic: Int = 0,
        var entities: Int = 0,
        var entityId: Int = 0,
        var entityType: String = "",
        var x: Int = 0,
        var y: Int = 0,
        var vx: Int = 0,
        var vy: Int = 0,
        var state: Int = 0
)

fun main(args: Array<String>) {
    val gameInput = GameInput()
    val snaffles = mutableListOf<Entity>()
    var index: Int
    val input = Scanner(System.`in`)
    input.use {
        myTeamId = input.nextInt() // if 0 you need to score on the right of the map, if 1 you need to score on the left

        // game loop
        while (true) {
            parseGameStats(input, gameInput)
            snaffles.clear()
            allEntitys.clear()
            index = 0

            for (i in 0 until gameInput.entities) {
                parseEntityStats(input, gameInput)
                val objectInArena = Entity(gameInput.entityId, gameInput.x, gameInput.y, gameInput.vx, gameInput.vy)
                allEntitys.add(objectInArena)

                when (gameInput.entityType) {
                    "WIZARD" -> doWizardStuff(gameInput)
                    "OPPONENT_WIZARD" -> doOpponentWizardStuff(gameInput)
                    "SNAFFLE" -> {
                        val snaffle = Entity()
                        snaffles.add(snaffle)
                        snaffles[index].setParams(
                                gameInput.entityId, gameInput.x + gameInput.vx, gameInput.y + gameInput.vy,
                                gameInput.vx, gameInput.vy
                        )
                        snaffles[index].grabbed = (gameInput.state == 1)
                        index++
                    }
                    else -> System.err.println("NO MATCHING MATCH")
                }
            }
            for (i in 0 until myWizards.size) {
                // casting spells
                if (myWizards[i].intervene(gameInput, snaffles)) continue
                if (myWizards[i].castSomeShooting(gameInput, snaffles)) continue
                if (myWizards[i].grabBall(gameInput, snaffles)) continue

                // standard movement
                if (myWizards[i].grabbed) { //|| myWizards[i].distanceTo(myWizards[i].getNearestSnaffle(snaffles)) < 400
                    println(myWizards[i].getShootingString(myTeamId) + " FOR GONDOR!!!")
                } else {
                    myWizards[i].moving(snaffles)
                }
            }
        }
    }
}

fun parseEntityStats(input: Scanner, gameInput: GameInput) {
    gameInput.entityId = input.nextInt() // entity identifier
    gameInput.entityType = input.next() // "WIZARD", "OPPONENT_WIZARD", "SNAFFLE" or "BLUDGER"
    gameInput.x = input.nextInt() // position
    gameInput.y = input.nextInt() // position
    gameInput.vx = input.nextInt() // velocity
    gameInput.vy = input.nextInt() // velocity
    gameInput.state = input.nextInt() // 1 if the wizard is holding a Snaffle, 0 otherwise
}

fun parseGameStats(input: Scanner, gameInput: GameInput) {
    gameInput.myScore = input.nextInt()
    gameInput.myMagic = input.nextInt()
    gameInput.opponentScore = input.nextInt()
    gameInput.opponentMagic = input.nextInt()
    gameInput.entities = input.nextInt()  // number of entities still in game
}

fun doWizardStuff(gameInput: GameInput) {
    val indexOfWizard = gameInput.entityId % 2
    System.err.println("Predicted: ${myWizards[indexOfWizard].x} ${myWizards[indexOfWizard].y}")
    System.err.println("Actual:    ${gameInput.x} ${gameInput.y}")
    myWizards[indexOfWizard].setParams(
            gameInput.entityId, gameInput.x + gameInput.vx, gameInput.y + gameInput.vy,
            gameInput.vx, gameInput.vy
    )
    myWizards[indexOfWizard].id = gameInput.entityId
    myWizards[indexOfWizard].grabbed = (gameInput.state == 1)
}

fun doOpponentWizardStuff(gameInput: GameInput) {
    val indexOfWizard = gameInput.entityId % 2
    enemyWizards[indexOfWizard].setParams(
            gameInput.entityId, gameInput.x + gameInput.vx, gameInput.y + gameInput.vy,
            gameInput.vx, gameInput.vy
    )
    enemyWizards[indexOfWizard].grabbed = (gameInput.state == 1)
}


class Entity(var id: Int = 0, var x: Int = 0, var y: Int = 0, var vx: Int = 0, var vy: Int = 0,
             var grabbed: Boolean = false) {

    fun setParams(otherId: Int, otherX: Int, otherY: Int, otherVX: Int, otherVY: Int) {
        id = otherId
        x = otherX
        y = otherY
        vx = otherVX
        vy = otherVY
    }

    fun distanceToCoord(otherX: Int, otherY: Int): Int {
        return sqrt((x - otherX).toDouble().pow(2.0) + (y - otherY).toDouble().pow(2.0)).toInt()
    }

    fun distanceTo(other: Entity): Int {
        return sqrt((x - other.x).toDouble().pow(2.0) + (y - other.y).toDouble().pow(2.0)).toInt()
    }

    fun getNearestWizard(enemyWizards: List<Entity>): Entity {
        return if (distanceTo(enemyWizards[0]) < distanceTo(enemyWizards[1])) {
            enemyWizards[0]
        } else {
            enemyWizards[1]
        }
    }

    fun getNearestSnaffle(snaffles: List<Entity>): Entity {
        var distance: Int = Int.MAX_VALUE
        var target = snaffles[0]
        for (any in snaffles) {
            if (distanceTo(any) < distance) {
                distance = distanceTo(any)
                target = any
            }
        }
        return target
    }

    fun intervene(gameInput: GameInput, snaffles: List<Entity>): Boolean {
        if (!alreadyIved && gameInput.myMagic >= PETRIFICUS_COST) {
            val targetGoal: Double = myTeamId - 0.5


            for (any in snaffles) {
                val value = abs((any.x - BORDER_RIGHT * myTeamId))
                //   System.err.println("Enfernung zum Tor von Snaffle ${any.id} beträgt $value und speed: ${any.vx*targetGoal}")
                // zu nah am tor
                // und Beschleunigung in Richtung tor

                // nearest enemy:
                val distanceToEnemy = any.distanceTo(any.getNearestWizard(enemyWizards))
                val distanceToFriend = any.distanceTo(any.getNearestWizard(myWizards))
                if ((distanceToEnemy > distanceToFriend && !any.grabbed && value <= 700 &&
                                ((any.vx * targetGoal) in 30.0..200.0) && (any.y in 2201..5299) && distanceToFriend > 800)
                        || ((distanceToFriend < distanceToEnemy || distanceToEnemy > 3000) && !any.grabbed && value <= 3000
                                && value >= 1800 && any.vx * targetGoal > 400 && (any.y in 2101..5399))
                ) {
                    gameInput.myMagic -= PETRIFICUS_COST
                    alreadyIved = true
                    tempTarget = any
                    println("PETRIFICUS ${any.id} EIER!")
                    return true
                }

            }

        } else {
            alreadyIved = false

            /*        if (tempTarget.x > 0 && myMagic >= ACCIO_COST && distanceTo(tempTarget) < 10000) {
                        println("ACCIO ${tempTarget.id} WIR BRAUCHEN EIER!")
                        tempTarget = Entity()
                        return true
                    } */
        }
        return false
    }

    fun betweenGoal(wizard: Entity): Boolean {
        val distanceToWizard = distanceTo(wizard)
        val ownVector = Pair(
                x.toDouble() + (3 * (min(6000.00 / (distanceToWizard.toDouble() / 1000.00).pow(2.0), 1000.00))) - x,
                (3 * y.toDouble() + min(6000.00 / (distanceToWizard.toDouble() / 1000.00).pow(2.0), 1000.00)) - y
        )
        allEntitys.forEach {

            val entityVector = Pair(
                    (3 * it.vx) - it.x,
                    (3 * it.vy) - it.y
            )

            if (ownVector.first * entityVector.first == ownVector.second * entityVector.second) {
                throw Exception()
            }
        }
        return false
    }

    fun grabBall(gameInput: GameInput, snaffles: List<Entity>): Boolean {
        if (gameInput.myMagic >= ACCIO_COST) {
            val targetGoal: Double = myTeamId - 0.5
            for (any in snaffles) {
                val distanceToEnemy = any.distanceTo(any.getNearestWizard(enemyWizards))
                val distanceToFriend = any.distanceTo(any.getNearestWizard(myWizards))
                val isBehindWizard = (x - any.x) * targetGoal < 0
                if (distanceToEnemy < distanceToFriend && abs(any.x - x) > 1300 && distanceTo(any) > 1500 && (distanceTo(
                                any
                        ) < 5000 && isBehindWizard && snaffles.size <= 3)
                //             || (distanceTo(any) < 6000 && abs(any.x - BORDER_RIGHT*myTeamId) <= 3500)
                ) {
                    gameInput.myMagic -= ACCIO_COST
                    println("ACCIO ${any.id} COME HERE!!")
                    return true
                }

            }
        }
        return false
    }

    fun castSomeShooting(gameInput: GameInput, snaffles: List<Entity>): Boolean {
        if (gameInput.myMagic >= FLIPENDO_COST) {
            val targetGoal: Double = myTeamId - 0.5
            val goalXCoord: Int = if (myTeamId == 0) {
                BORDER_RIGHT
            } else {
                BORDER_LEFT
            }

            for (any in snaffles) {
                if (any.x != x) {
                    val rightDirection = (x - any.x) * targetGoal > 0
                    val impactY: Int =
                            (((goalXCoord - x).toDouble() / (any.x - x)) * (any.y - y).toDouble() + y).toInt()
                    val willHitGoal: Boolean =
                            (impactY in RANGE_GOAL) || ( //Trifft direkt ins Tor oder mit Bande
                                    (any.distanceToCoord(goalXCoord, 3750)) <= 8000 &&
                                            (impactY in RANGE_UNDER_GOAL_EDGING || impactY in RANGE_UPPER_GOAL_EDGING)
                                    )
                    // Fehlerfall: 300364546
                    if (distanceTo(any) < 6000 && rightDirection && willHitGoal && !betweenGoal(this)) {
                        System.err.println("Wizard$id: $x $y   - shoots on: ${any.x} ${any.y}")
                        System.err.println("expecting impact at: $impactY")
                        gameInput.myMagic -= FLIPENDO_COST
                        println("FLIPENDO ${any.id} BAM!!")
                        return true
                    }
                }
            }
        }
        return false
    }

    fun moving(snaffles: List<Entity>): Boolean {
        var target: Entity = snaffles[0]
        //   var distance: Int = Int.MAX_VALUE

        val byDistance = Comparator.comparing { entity: Entity -> this.distanceTo(entity) }

        target = snaffles.sortedWith(byDistance.thenComparing(byDistance)).elementAt(0)
        var i = 1
        while (target.getNearestWizard(myWizards) != this && snaffles.size > i && id % 2 == 0) {
            target = snaffles.sortedWith(byDistance.thenComparing(byDistance)).elementAt(i++)
        }


        /*   for (any in snaffles) {
               if (distanceTo(any) < distance && !(id%2 == 0 && any.getNearestWizard(myWizards) == this) ) {
                   distance = distanceTo(any)
                   target = any
               }
           }*/
        println("MOVE ${target.x} ${target.y} 150 I AM HAIRY POTTER")
        if (snaffles.size > 1) {
            target.x = Int.MAX_VALUE
            target.y = Int.MAX_VALUE
        }
        return true
    }

    fun getShootingString(myTeamId: Int): String {
        val direction: Int = if (myTeamId == 0) {
            BORDER_RIGHT
        } else {
            BORDER_LEFT
        }
        if (y in 2500..5000 && abs(x - direction) < 1500) {
            return "THROW $direction $y 500"
        }
        if (abs(x - direction) < 5000) {
            return if (y > 4500) {
                "THROW $direction 4200 500"
            } else if (y < 3000) {
                "THROW $direction 3300 500"
            } else {
                "THROW $direction 3750 500"

            }
        }
        return "THROW $direction 3750 300"

        //return "MOVE $direction 3750 150 I AM HAIRY POTTER"

        //return "THROW $direction 3750 500"
    }
}
