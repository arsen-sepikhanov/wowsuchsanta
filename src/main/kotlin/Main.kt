import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.SingularValueDecomposition
import java.awt.Color
import java.awt.Graphics
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.*

val fileContent = Map::class.java.getResource("/map.json")?.readText()!!
val map = jacksonObjectMapper().readValue(fileContent, Map::class.java)

val timePerUnit = 1.0
val timePerUnitInRed = timePerUnit * 7

fun main(args: Array<String>) {
    map.snowAreas.maxBy { it.r }.also { println(it) }
    SwingUtilities.invokeLater {
        JFrame("hello").also {
            it.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            it.add(canvas(map))
            it.pack()
            it.isVisible = true
            it.setSize(1000, 1000)
        }

    }
}

fun canvas(map: Map): JPanel {
    var c1: Children? = null
    var c2: Children? = null
    val panel = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val scale = min(width, height) / 10000.0
            fun Int.scaled() = (this * scale).toInt()
            g.color = Color.RED
            map.snowAreas.forEach { sa ->
                val d = (sa.r * 2).scaled()
                g.fillOval(
                    (sa.x - sa.r).scaled(),
                    (sa.y - sa.r).scaled(),
                    d,
                    d
                )
            }

            g.color = Color.BLACK
            val rad = 3
            map.children.forEach { ch ->
                g.fillOval(
                    ch.x.scaled(),
                    ch.y.scaled(),
                    rad,
                    rad
                )
            }
            if (c1 != null && c2 != null) {
                fastestLine(g, c1!!, c2!!, scale)
            }
        }
    }
    return panel.also {
        it.setSize(1000, 1000)
        it.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                panel.repaint()
            }
        })
        it.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                c1 = map.children.random()
                c2 = map.children.random()
                panel.repaint()
            }
        })
    }
}

fun fastestLine(g: Graphics, c1: Children, c2: Children, scale: Double) {
    fun Int.scaled() = (this * scale).toInt()
    fun Double.scaled() = (this * scale).toInt()
    g.drawLine(c1.x.scaled(), c1.y.scaled(), c2.x.scaled(), c2.y.scaled())
    g.fillRect(c1.x.scaled(), c1.y.scaled(), 5, 5)
    val linearCoefficients = solveLinearEquation(c1, c2)
    val minX = min(c1.x, c2.x)
    val maxX = max(c1.x, c2.x)

    val line = LineWithSnowAreas(c1.point, c2.point)
    for (snowArea in map.snowAreas) {
        val quad = prepareAndSolveQuad(linearCoefficients, snowArea)
        val intersec = IntersectionWithCircle(quad.res1, quad.res2, snowArea, linearCoefficients)
        val acceptablePoints = intersec.solutions()
            .filter { it.x in (minX .. maxX)  }
        if (acceptablePoints.isNotEmpty()) {
            println(acceptablePoints)
        }

        acceptablePoints.forEach {
            g.drawOval(
                it.x.scaled(),
                it.y.scaled(),
                5,
                5
            )
        }
    }

    calculateWeightOfLine(c1, c2)
}

fun solveLinearEquation(ch1: Children, ch2: Children): KxPlusB {
    val res = SingularValueDecomposition(
        MatrixUtils.createRealMatrix(
            arrayOf(
                arrayOf(ch1.x.toDouble(), 1.0).toDoubleArray(),
                arrayOf(ch2.x.toDouble(), 1.0).toDoubleArray()
            )
        )
    ).solver.solve(ArrayRealVector(arrayOf(ch1.y.toDouble(), ch2.y.toDouble())))
    val b = res.getEntry(1)
    val k = res.getEntry(0)
    return KxPlusB(k, b)
}

fun prepareAndSolveQuad(kxpb: KxPlusB, circle: SnowArea): QuadSolution {
    val (k, bQuad) = kxpb
    val a = k.pow(2) + 1
    val t = bQuad - circle.y
    val b = 2 * k * t - 2 * circle.x
    val c = t.pow(2) - circle.r.toDouble().pow(2) + circle.x.toDouble().pow(2)
    return solveQuad(a, b, c)
}

fun calculateWeightOfLine(ch1: Children, ch2: Children) {
    //обе точки в одном круге, ближайшее расстояние просто по прямой
    if (bothPointsInSameCircle(ch1.point, ch2.point)) {
        val distance = distanceBetweenCoords(ch1.point, ch2.point)
        val weight = distance * timePerUnitInRed
        println("Both points are in same circle, distance = $distance, weight = $weight")
    }

    var startRedSegment = 0.0
    //начало в круге,
    if (isPointInCircle(ch1)) {

    }
}

fun distanceBetweenCoords(a: Point, b: Point): Double {
    return sqrt(
        (a.x - b.x).toDouble().pow(2) + (a.y - b.y).toDouble().pow(2)
    )
}

data class Point(val x: Int, val y: Int)

fun bothPointsInSameCircle(p1: Point, p2: Point): Boolean {
    return map.snowAreas.any { sa -> bothPointsInSameCircle(p1, p2, sa) }
}

fun bothPointsInSameCircle(c1: Point, c2: Point, area: SnowArea): Boolean {
    return isPointInCircle(c1, area) && isPointInCircle(c2, area)
}

fun solveQuad(a: Double, b: Double, c: Double): QuadSolution {
    val discr = b.pow(2) - 4 * a * c
    return if (discr < 0) {
        QuadSolution(null, null)
    } else if (discr.compareTo(0.0) == 0) {
        val res = -b / (2 * a)
        QuadSolution(res, null)
    } else {
        val res1 = (-b - sqrt(discr)) / (2 * a)
        val res2 = (-b + sqrt(discr)) / (2 * a)
        QuadSolution(res1, res2)
    }
}

data class KxPlusB(val k: Double, val b: Double)
data class QuadSolution(val res1: Double?, val res2: Double?)

class IntersectionWithCircle(x1: Double?, x2: Double?, srcCircle: SnowArea, kxpb: KxPlusB) {

    private val solution1: Point?
    private val solution2: Point?

    init {
        solution1 = if (x1 != null) {
            Point(x = x1.toInt(), y = calculateY(x1, srcCircle, kxpb).toInt())
        } else {
            null
        }

        solution2 = if (x2  != null) {
            Point(x = x2.toInt(), y = calculateY(x2, srcCircle, kxpb).toInt())
        }  else {
            null
        }
    }

    private fun calculateY(x: Double, scrCircle: SnowArea, kxpb: KxPlusB): Double {
        // (x - xo)^2 + (y - yo)^2 = r^2
        // (x1 - xo)^2 + (y - yo)^2 = r^2
        // (y - yo)^2 = r^2 - (x1 - x0)^2
        // y = +- sqrt( r^2 - (x1 - x0)^2 ) + yo

        val plusSqrt = sqrt(
            scrCircle.r.toDouble().pow(2) - (x - scrCircle.x).pow(2)
        )
        val minusSqrt = -plusSqrt
        val res1 = plusSqrt + scrCircle.y
        val res2 = minusSqrt + scrCircle.y
        val pointOnLine = kxpb.k * x + kxpb.b

        return if (abs(res1 - pointOnLine) < 0.01) res1 else res2
    }

    fun solutions() = listOfNotNull(solution1, solution2)
}

fun isPointInCircle(ch: Children): Boolean {
    return map.snowAreas.any { sa -> isPointInCircle(ch.point, sa) }
}

fun isPointInCircle(ch: Point, sa: SnowArea): Boolean {
    return sqrt((sa.x - ch.x).toDouble().pow(2) + (sa.y - ch.y).toDouble().pow(2)) < sa.r
}

data class Map(
    val gifts: Collection<Gift>,
    val snowAreas: Collection<SnowArea>,
    val children: Collection<Children>
)


var snowAreaId = 0
data class Gift(val id: Long, val weight: Int, val volume: Int)
class SnowArea(val x: Int, val y: Int, val r: Int) {
    val id: Int = snowAreaId.inc()
}
data class Children(val x: Int, val y: Int) {
    val point = Point(x, y)
}

class LineWithSnowAreas(private val p1: Point, private val p2: Point) {

    private val solutions: MutableCollection<QuadSolution> = mutableListOf()

    fun addSolution(sol: QuadSolution) {
        solutions.add(sol)
    }

//    fun sortSolutionsFromStartToEndPoint() {
//        if (p1.x < p2.x) {
//            solutions = solutions.sortedBy { it. }
//        } else {
//
//        }
//    }
}


fun a() {
    val c = OkHttpClient()
    c.newCall(
        Request.Builder()
            .header("X-Api-Key", "7bc8f6e0-1bda-4b22-b897-d5a825072321")
            .url("https://datsanta.dats.team/json/map/faf7ef78-41b3-4a36-8423-688a61929c08.json")
            .build()
    ).execute().body()
        .use {
            println(it.string())
        }
}