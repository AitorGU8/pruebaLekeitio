package es.aitorgu.pruebalekeitio

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.MotionEvent
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val palabras = listOf(
        "ANTOLIN", "ANTZARA", "MUSIKA", "TXALUPA", "PORTUA",
        "ANTZARRRAK", "KIROLA", "DANTZAK"
    )
    private val tamaño = 10// Tamaño de la sopa de letras
    private val sopa = Array(tamaño) { CharArray(tamaño) { '.' } }
    private val seleccionadas = mutableListOf<Pair<Int, Int>>()
    private lateinit var gridLayout: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        palabras.forEach { palabra ->
            insertarPalabraAleatoria(sopa, palabra)
        }

        // Rellenar espacios vacíos con letras aleatorias
        for (i in sopa.indices) {
            for (j in sopa[i].indices) {
                if (sopa[i][j] == '.') {
                    sopa[i][j] = Random.nextInt('A'.code, 'Z'.code + 1).toChar()
                }
            }
        }

        gridLayout = findViewById(R.id.sopaGrid)
        gridLayout.rowCount = tamaño
        gridLayout.columnCount = tamaño

        // Mostrar la sopa de letras
        for (i in sopa.indices) {
            for (j in sopa[i].indices) {
                val textView = TextView(this).apply {
                    text = sopa[i][j].toString()
                    textSize = 18f
                    setPadding(8, 8, 8, 8)
                    setBackgroundColor(Color.WHITE)
                    setTextColor(Color.BLACK)
                }
                textView.tag = Pair(i, j) // Asociar posición con la celda
                gridLayout.addView(textView)
            }
        }

        // Agregar manejo de eventos táctiles
        gridLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    seleccionadas.clear()
                    seleccionarLetra(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    seleccionarLetra(event)
                }
                MotionEvent.ACTION_UP -> {
                    verificarPalabra()
                }
            }
            true
        }
    }

    private fun seleccionarLetra(event: MotionEvent) {
        val celda = obtenerCelda(event) ?: return
        if (!seleccionadas.contains(celda)) {
            if (seleccionadas.isEmpty()) {
                // Si no hay ninguna selección, añadir la primera celda
                seleccionadas.add(celda)
            } else {
                // Verificar si la nueva celda sigue la dirección horizontal o vertical
                if (esLineaRecta(celda)) {
                    seleccionadas.add(celda)
                    val textView = obtenerTextView(celda)
                    textView.setBackgroundColor(Color.LTGRAY) // Resaltar celda seleccionada
                }
            }
        }
    }

    private fun esLineaRecta(celda: Pair<Int, Int>): Boolean {
        // Verificar si la línea seleccionada es horizontal o vertical
        val (fila, columna) = celda
        val (primeraFila, primeraColumna) = seleccionadas.first()

        // Línea horizontal o vertical
        return fila == primeraFila || columna == primeraColumna
    }

    private fun verificarPalabra() {
        val palabraSeleccionada = seleccionadas.map { (i, j) -> sopa[i][j] }.joinToString("")
        if (palabras.contains(palabraSeleccionada)) {
            seleccionadas.forEach { (i, j) ->
                val textView = obtenerTextView(Pair(i, j))
                textView.setBackgroundColor(Color.YELLOW) // Resaltar permanente
                textView.setTypeface(null, Typeface.BOLD)
            }
        } else {
            seleccionadas.forEach { (i, j) ->
                val textView = obtenerTextView(Pair(i, j))
                textView.setBackgroundColor(Color.WHITE) // Revertir resaltado
            }
        }
        seleccionadas.clear()
    }

    private fun obtenerCelda(event: MotionEvent): Pair<Int, Int>? {
        val x = event.x.toInt()
        val y = event.y.toInt()
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i) as TextView
            val location = IntArray(2)
            child.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]
            val right = left + child.width
            val bottom = top + child.height
            if (x in left..right && y in top..bottom) {
                return child.tag as Pair<Int, Int>
            }
        }
        return null
    }

    private fun obtenerTextView(celda: Pair<Int, Int>): TextView {
        return gridLayout.getChildAt(celda.first * tamaño + celda.second) as TextView
    }

    private fun insertarPalabraAleatoria(sopa: Array<CharArray>, palabra: String) {
        val direcciones = listOf(
            Pair(0, 1), // Derecha
            Pair(0, -1), // Izquierda
            Pair(1, 0), // Abajo
            Pair(-1, 0) // Arriba
        )

        val random = Random.Default
        var colocada = false
        while (!colocada) {
            val fila = random.nextInt(tamaño)
            val columna = random.nextInt(tamaño)
            val (dx, dy) = direcciones.random()

            if (puedeColocar(sopa, palabra, fila, columna, dx, dy)) {
                for (i in palabra.indices) {
                    sopa[fila + i * dx][columna + i * dy] = palabra[i]
                }
                colocada = true
            }
        }
    }

    private fun puedeColocar(
        sopa: Array<CharArray>,
        palabra: String,
        fila: Int,
        columna: Int,
        dx: Int,
        dy: Int
    ): Boolean {
        for (i in palabra.indices) {
            val nuevaFila = fila + i * dx
            val nuevaColumna = columna + i * dy

            if (nuevaFila !in sopa.indices || nuevaColumna !in sopa[0].indices) {
                return false
            }
            if (sopa[nuevaFila][nuevaColumna] != '.' && sopa[nuevaFila][nuevaColumna] != palabra[i]) {
                return false
            }
        }
        return true
    }
}




