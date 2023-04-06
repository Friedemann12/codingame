import java.util.*
import kotlin.math.*

data class Point(val x: Int, val y: Int) {
    fun toPointDouble(): PointDouble {
        return PointDouble(this.x.toDouble(), this.y.toDouble())
    }
}

data class PointDouble(val x: Double, val y: Double) {
    fun toPoint(): Point {
        return Point(this.x.roundToInt(), this.y.roundToInt())
    }
}

abstract class Unit(val id: Int, var point: Point)


fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    var shortestPathMap: MutableMap<Point, Point> = mutableMapOf()
    var totalLaps = input.nextLine().toInt()
    var checkpointCount = input.nextLine().toInt()
    var path = mutableListOf<Point>()
    for (i in 0 until checkpointCount) {
        path.add(Point(input.nextInt(), input.nextInt()))
    }
    //System.err.println("total laps: $totalLaps")
    //System.err.println("checkpoint count: $checkpointCount")
    //System.err.println("path: $path")
    //System.err.println("path size: ${path.size}")
    var shortestPath = shortestPath(path, 400)
    path.forEachIndexed { i, point ->
        shortestPathMap[point] = shortestPath.minBy { distance(point, it) }
    }
    //System.err.println("shortest path: $shortestPath")
    //System.err.println("shortest path size: ${shortestPath.size}")

    data class Pod(
        val podId: Int,
        var position: Point,
        var vx: Int,
        var vy: Int,
        var rotationAngle: Int,
        var nextCheckpointId: Int,
        var lap: Int
    ) :
        Unit(podId, position) {
        var nextPoint = Point(0, 0)
        var lastPoint = nextPoint


        fun update(position: Point, vx: Int, vy: Int, rotationAngle: Int, nextCheckpointId: Int) {
            this.position = position
            this.vx = vx
            this.vy = vy
            this.rotationAngle = rotationAngle
            this.nextCheckpointId = nextCheckpointId - 1
            this.lastPoint = nextPoint
            this.nextPoint = path[nextCheckpointId]
            //System.err.println("nextCheckpoint for Pod $podId: $nextCheckpointId")
            //System.err.println("nextCheckpoint for Pod $podId: $nextPoint")
            //System.err.println("lastCheckpoint for Pod $podId: $lastPoint")
            var firstCheckpoint = path[1]
            if (firstCheckpoint == nextPoint && lastPoint != nextPoint) {
                lap++
            }
            //System.err.println("Pod $podId on lap: $lap")
        }

        fun nextTurn() {
            var thurst = 100
            var nextCheckpointDist = distance(position, nextPoint).roundToInt()

            if (lap == totalLaps && nextPoint == path.last() && abs(
                    getAngleOfNextTarget(
                        nextCheckpointDist,
                        400
                    )
                ) <= 3
            ) {
                println("${shortestPathMap[nextPoint]!!.x} ${shortestPathMap[nextPoint]!!.y} BOOST")
            } else {
                var angleToTarget = getAngleOfNextTarget(nextCheckpointDist, 400)
                if (abs(angleToTarget) > 25) {
                    if (nextCheckpointDist <= 800) {
                        thurst = 25
                    } else thurst = 0
                } else thurst =
                    getThrust(
                        distance(position, shortestPathMap[nextPoint]!!).roundToInt(),
                        angleToTarget
                    )
                System.err.println("angle to targe: $angleToTarget")
                println("${shortestPathMap[nextPoint]!!.x} ${shortestPathMap[nextPoint]!!.y} $thurst")
            }
        }
    }

    val pod1 = Pod(1, Point(0, 0), 0, 0, 0, 0, 0)
    val pod2 = Pod(2, Point(0, 0), 0, 0, 0, 0, 0)
    val opp1 = Pod(3, Point(0, 0), 0, 0, 0, 0, 0)
    val opp2 = Pod(4, Point(0, 0), 0, 0, 0, 0, 0)

    // game loop
    while (true) {

        pod1.update(
            Point(input.nextInt(), input.nextInt()),
            input.nextInt(),
            input.nextInt(),
            input.nextInt(),
            input.nextInt()
        )
        pod2.update(
            Point(input.nextInt(), input.nextInt()),
            input.nextInt(),
            input.nextInt(),
            input.nextInt(),
            input.nextInt()
        )
        opp1.update(
            Point(input.nextInt(), input.nextInt()),
            input.nextInt(),
            input.nextInt(),
            input.nextInt(),
            input.nextInt()
        )
        opp2.update(
            Point(input.nextInt(), input.nextInt()),
            input.nextInt(),
            input.nextInt(),
            input.nextInt(),
            input.nextInt()
        )

        pod1.nextTurn()
        pod2.nextTurn()

        //System.err.println("Last PosX: ${lastPosition.x}")
        //System.err.println("Last PosY: ${lastPosition.y}")
        //System.err.println("Curr PosX: ${currentPosition.x}")
        //System.err.println("Curr PosY: ${currentPosition.y}")
        //System.err.println("Dist: $nextCheckpointDist");
        //System.err.println("Angle: $nextCheckpointAngle");
    }
}

fun getAngleOfNextTarget(nextCheckpointDist: Int, radius: Int): Int {
    val angle = Math.toDegrees(asin(radius.toDouble() / nextCheckpointDist.toDouble())).roundToInt()
    //System.err.println("Calculated Angel: $angle")
    return angle
}

fun getThrust(dist: Int, angle: Int): Int {
    return when {
        dist >= 5000 -> 100
        dist in 2999 downTo 1000 -> 75
        dist <= 999 -> {
            if (angle > 25) 25 else 100
        }

        else -> 100
    }
}

fun getClosestPointOnCircleToPoint(point: PointDouble, circleCenter: PointDouble, radius: Int): Point {
    val dx = point.x - circleCenter.x
    val dy = point.y - circleCenter.y
    val distance = sqrt(dx * dx + dy * dy)

    // Normalize the vector from the circle's center to the given point
    val normalizedX = dx / distance
    val normalizedY = dy / distance

    // Scale the normalized vector by the circle's radius
    val scaledX = normalizedX * radius
    val scaledY = normalizedY * radius

    // Add the scaled vector to the center of the circle
    val closestX = circleCenter.x + scaledX
    val closestY = circleCenter.y + scaledY

    return Point(closestX.roundToInt(), closestY.roundToInt())
}

fun distance(p1: Point, p2: Point) = sqrt((p2.x - p1.x.toDouble()).pow(2) + (p2.y - p1.y.toDouble()).pow(2))

fun shortestPath(circleCenters: List<Point>, radius: Int): List<Point> {
    fun distance(p1: Point, p2: Point) = sqrt((p2.x - p1.x.toDouble()).pow(2) + (p2.y - p1.y.toDouble()).pow(2))
    fun distanceDouble(p1: PointDouble, p2: PointDouble) = sqrt((p2.x - p1.x).pow(2) + (p2.y - p1.y).pow(2))

    fun tangentPoints(
        c1: PointDouble, c2: PointDouble, r: Int
    ): Pair<Pair<PointDouble, PointDouble>, Pair<PointDouble, PointDouble>> {
        val d = distanceDouble(c1, c2)
        val alpha = PI / 2
        val beta = atan2((c2.y - c1.y).toDouble(), (c2.x - c1.x).toDouble())
        val gamma1 = beta + alpha
        val gamma2 = beta - alpha

        val tpA1 = PointDouble((c1.x + r * cos(gamma1)), (c1.y + r * sin(gamma1)))
        val tpB1 = PointDouble((c2.x + r * cos(gamma1)), (c2.y + r * sin(gamma1)))

        val tpA2 = PointDouble((c1.x + r * cos(gamma2)), (c1.y + r * sin(gamma2)))
        val tpB2 = PointDouble((c2.x + r * cos(gamma2)), (c2.y + r * sin(gamma2)))

        return ((tpA1 to tpB1) to (tpA2 to tpB2))
    }

    val pathPoints = mutableListOf<Point>()
    if (circleCenters.size < 2) return pathPoints


    for (i in circleCenters.indices) {
        val circleA = circleCenters[i]
        val circleB = circleCenters[(i + 1) % circleCenters.size]
        val circleC = circleCenters[(i + 2) % circleCenters.size]

        val (tangentAB1, tangentAB2) = tangentPoints(circleA.toPointDouble(), circleB.toPointDouble(), radius)
        val (tangentBC1, tangentBC2) = tangentPoints(circleB.toPointDouble(), circleC.toPointDouble(), radius)

        val listOfIntersections = listOf(
            lineLineIntersection(tangentAB1.first, tangentAB1.second, tangentBC1.first, tangentBC1.second),
            lineLineIntersection(tangentAB1.first, tangentAB1.second, tangentBC2.first, tangentBC2.second),
            lineLineIntersection(tangentAB2.first, tangentAB2.second, tangentBC1.first, tangentBC1.second),
            lineLineIntersection(tangentAB2.first, tangentAB2.second, tangentBC2.first, tangentBC2.second)
        )
        //System.err.println(listOfIntersections.toString())
        val closestIntersection = listOfIntersections.minBy {
            distanceDouble(
                it,
                circleC.toPointDouble()
            ) + distanceDouble(it, circleA.toPointDouble())
        }
        //System.err.println(closestIntersection)
        pathPoints.add(getClosestPointOnCircleToPoint(closestIntersection, circleB.toPointDouble(), radius))
    }
    //System.err.println("Shortest Path: $pathPoints")
    return pathPoints
}

fun lineLineIntersection(A: PointDouble, B: PointDouble, C: PointDouble, D: PointDouble): PointDouble {
    // Line AB represented as a1x + b1y = c1
    val a1 = (B.y - A.y)
    val b1 = (A.x - B.x)
    val c1 = a1 * A.x + b1 * A.y

    // Line CD represented as a2x + b2y = c2
    val a2 = (D.y - C.y)
    val b2 = (C.x - D.x)
    val c2 = a2 * C.x + b2 * C.y
    val determinant = a1 * b2 - a2 * b1
    return if (determinant == 0.0) {
        // The lines are parallel. This is simplified
        // by returning a pair of FLT_MAX
        PointDouble(Double.MAX_VALUE, Double.MAX_VALUE)
    } else {
        val x = (b2 * c1 - b1 * c2) / determinant
        val y = (a1 * c2 - a2 * c1) / determinant
        PointDouble(x, y)
    }
}