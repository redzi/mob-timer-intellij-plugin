package com.github.redzi.mobtimerintellijplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.redzi.mobtimerintellijplugin.MyBundle
import com.github.redzi.mobtimerintellijplugin.services.MyProjectService
import javax.swing.JButton
import javax.swing.JTextField
import kotlin.concurrent.timer

class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()

        private fun toSeconds(input: String): Int {
            val s = input.split(":")
            return s[0].toInt() * 60 + s[1].toInt()
        }

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val timeInput = JTextField("05:00")
            val label = JBLabel(timeInput.getText())
            var countdown : Int

            add(timeInput)
            add(label)
            add(JButton(MyBundle.message("startButtonLabel")).apply {
                addActionListener {
                    countdown = toSeconds(timeInput.getText())
                    timer(initialDelay = 1000L, period = 1000L, daemon = true) {
                        --countdown
                        if (countdown < 0) {
                            label.text = timeInput.getText()
                            cancel()
                        } else {
                            label.text = String.format("%02d:%02d", countdown / 60, countdown % 60)
                        }
                    }
                }
            })
        }
    }
}
