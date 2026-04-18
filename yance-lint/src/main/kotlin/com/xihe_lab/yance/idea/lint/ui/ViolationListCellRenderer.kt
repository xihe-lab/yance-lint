package com.xihe_lab.yance.idea.lint.ui

import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

class ViolationListCellRenderer : JPanel(), ListCellRenderer<ViolationItem> {

    private val colorBar = JPanel()
    private val messageLabel = JLabel()
    private val metaLabel = JLabel()

    private val severityColors = mapOf(
        ViolationItem.Severity.ERROR to Color(0xDB, 0x58, 0x60),
        ViolationItem.Severity.WARNING to Color(0xE5, 0xC0, 0x7B),
        ViolationItem.Severity.INFO to Color(0x61, 0xAF, 0xEF)
    )

    init {
        layout = BorderLayout(8, 0)
        border = EmptyBorder(4, 0, 4, 8)

        colorBar.preferredSize = Dimension(3, 0)
        add(colorBar, BorderLayout.WEST)

        messageLabel.font = Font(messageLabel.font.name, Font.PLAIN, 12)
        metaLabel.font = Font(metaLabel.font.name, Font.PLAIN, 11)
        metaLabel.foreground = Color.GRAY

        val textPanel = JPanel().apply {
            layout = BorderLayout(0, 2)
            isOpaque = false
            add(messageLabel, BorderLayout.NORTH)
            add(metaLabel, BorderLayout.SOUTH)
        }
        add(textPanel, BorderLayout.CENTER)
    }

    override fun getListCellRendererComponent(
        list: JList<out ViolationItem>,
        value: ViolationItem,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): java.awt.Component {
        colorBar.background = severityColors[value.severity] ?: Color.GRAY

        messageLabel.text = value.message
        val fileName = value.filePath.substringAfterLast('/')
        metaLabel.text = "[${value.tool}] $fileName:${value.line}"

        if (isSelected) {
            background = list.selectionBackground
            messageLabel.foreground = list.selectionForeground
            metaLabel.foreground = list.selectionForeground
        } else {
            background = list.background
            messageLabel.foreground = list.foreground
            metaLabel.foreground = Color.GRAY
        }

        isOpaque = true
        colorBar.isOpaque = true
        return this
    }
}
