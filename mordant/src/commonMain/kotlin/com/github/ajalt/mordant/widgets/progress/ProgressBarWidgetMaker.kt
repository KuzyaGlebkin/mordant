package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.*
import com.github.ajalt.mordant.widgets.EmptyWidget
import com.github.ajalt.mordant.widgets.Padding

interface ProgressBarWidgetMaker {
    /**
     * Build a progress widget from the given [definition] and [states].
     */
    fun <T> build(
        definition: ProgressBarDefinition<T>,
        states: List<ProgressState<T>>,
    ): Widget

    /**
     * Build the widgets for each cell in the progress bar.
     *
     * This can be used if you want to include the individual cells in a larger layout like a table.
     *
     * @return A list of rows, where each row is a list of widgets for the cells in that row.
     */
    fun <T> buildCells(
        definition: ProgressBarDefinition<T>,
        states: List<ProgressState<T>>,
    ): List<List<Widget>>
}

object BaseProgressBarWidgetMaker : ProgressBarWidgetMaker {
    override fun <T> build(
        definition: ProgressBarDefinition<T>,
        states: List<ProgressState<T>>,
    ): Widget {
        return when {
            states.isEmpty() -> EmptyWidget
            definition.alignColumns -> makeTable(definition, states)
            else -> makeLinearLayout(definition, states)
        }
    }

    override fun <T> buildCells(
        definition: ProgressBarDefinition<T>,
        states: List<ProgressState<T>>,
    ): List<List<Widget>> {
        return states.map {
            definition.cells.map { cell -> cell.content(it) }
        }
    }

    private fun <T> makeLinearLayout(
        d: ProgressBarDefinition<T>,
        states: List<ProgressState<T>>,
    ): Widget {
        return when {
            states.size == 1 -> makeHorizontalLayout(d, states.single())
            else -> verticalLayout {
                for (state in states) {
                    cell(makeHorizontalLayout(d, state))
                }
            }
        }
    }

    private fun <T> makeHorizontalLayout(
        d: ProgressBarDefinition<T>, state: ProgressState<T>,
    ): Widget = horizontalLayout {
        spacing = d.spacing
        for ((i, barCell) in d.cells.withIndex()) {
            cell(barCell.content(state))
            column(i) {
                width = barCell.columnWidth
                align = barCell.align
                verticalAlign = barCell.verticalAlign
            }
        }
    }

    private fun <T> makeTable(d: ProgressBarDefinition<T>, states: List<ProgressState<T>>): Table {
        return table {
            cellBorders = Borders.NONE
            padding = Padding { left = d.spacing }
            column(0) { padding = Padding(0) }
            d.cells.forEachIndexed { i, cell ->
                column(i) {
                    align = cell.align
                    verticalAlign = cell.verticalAlign
                    val w = cell.columnWidth
                    width = when {
                        // The fixed width cells don't include padding, so add it here
                        i > 0 && w is ColumnWidth.Fixed -> ColumnWidth.Fixed(w.width + d.spacing)
                        else -> w
                    }
                }
            }
            body {
                for (state in states) {
                    rowFrom(d.cells.map { it.content(state) })
                }
            }
        }
    }
}
