import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Color
import java.awt.Graphics
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

val fileContent = Map::class.java.getResource("/map.json")?.readText()!!
val map = jacksonObjectMapper().readValue(fileContent, Map::class.java)

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
                fastestLine(g, c1!!, c2!!)
//                g.drawLine(c1!!.x.scaled(), c1!!.y.scaled(), c2!!.x.scaled(), c2!!.y.scaled())
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
        it.addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                c1 = map.children.random()
                c2 = map.children.random()
                panel.repaint()
            }
        })
    }
}

fun fastestLine(g: Graphics, c1: Children, c2: Children) {

}

fun isPointInCircle(ch: Children): Boolean {
    return map.snowAreas.any { sa ->
        sqrt((sa.x - ch.x).toDouble().pow(2) + (sa.y - ch.y).toDouble().pow(2) ) < sa.r
    }
}

//если расстояние от дома до центра окружности меньше чем ее радиус то начинаем изнутри окружности
//уравнение точки: y = kx + b

data class Map(
    val gifts: Collection<Gift>,
    val snowAreas: Collection<SnowArea>,
    val children: Collection<Children>
)

data class Gift(val id: Long, val weight: Int, val volume: Int)
data class SnowArea(val x: Int, val y: Int, val r: Int)
data class Children(val x: Int, val y: Int)

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