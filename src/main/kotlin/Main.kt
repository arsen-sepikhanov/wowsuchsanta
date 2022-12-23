import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Color
import java.awt.Graphics
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.min

fun main(args: Array<String>) {
    val fileContent = Map::class.java.getResource("/map.json")?.readText()!!
    val map = jacksonObjectMapper().readValue(fileContent, Map::class.java)
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
    val panel = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val scale = min(width, height) / 10000.0
            g.color = Color.RED
            map.snowAreas.forEach { sa ->
                val r = sa.r * scale
                val d = (r * 2).toInt()
                g.fillOval(
                    (sa.x * scale - r).toInt(),
                    (sa.y * scale - r).toInt(),
                    d,
                    d
                )
            }
            g.color = Color.BLACK
            map.children.forEach { ch ->
                g.fillOval(
                    (ch.x * scale).toInt(),
                    (ch.y * scale).toInt(),
                    3,
                    3
                )
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
    }
}

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