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
import com.intellij.openapi.ui.popup.JBPopupFactory
import java.util.*
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JTextField
import javax.swing.ListSelectionModel

// Arif
// Jinha
// Patrik


private const val START_RIGHT_AWAY = "Start Right Away"
private const val PAUSE = "Pause"
private const val SKIP = "Skip"

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
            val timeHelpLabel = JBLabel("Turn time: ")
            val timeInput = JTextField("00:03")
            val timeLabel = JBLabel(timeInput.getText())
            var countdown : Int
            var currentTurn = 1

            val numberOfTurns2Label = JBLabel("Number of turns: ")
            val numberOfTurnsInput = JTextField("2")
            val numberOfTurnsLabel = JBLabel("Turn: 1")
            var numberOfTurns = 1

            val arr = ArrayList<String>()
            arr.add(START_RIGHT_AWAY)
            arr.add(PAUSE)
            arr.add(SKIP)

            val startButton = JButton(MyBundle.message("startButtonLabel"))
            var pauseButton = JButton(PAUSE)
            val popupBuilder = JBPopupFactory.getInstance().createPopupChooserBuilder(arr)
                .setTitle("Next Turn")
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
                .setCancelOnOtherWindowOpen(false)
                .setCancelOnClickOutside(false)
                .setCancelKeyEnabled(false)
                .setCancelOnWindowDeactivation(false)
                .setItemChosenCallback { value ->
                    if (value == START_RIGHT_AWAY) {
                        startButton.doClick()
                    } else if (value == PAUSE) {
                        pauseButton.doClick()
                    } else { // Skip
                        currentTurn++
                    }
                }
            var popup = popupBuilder.createPopup()

            var bigBox = Box.createVerticalBox()
            var buttonBox = Box.createVerticalBox()
            var ourTimer = Timer()

            startButton.apply {
                addActionListener {
                    if (!popup.isVisible) {
                        if(popup.canShow()) {
                            popup.show(buttonBox)
                        }
                        else {
                            popup = popupBuilder.createPopup()
                            popup.show(buttonBox)
                        }
                    }
                    countdown = toSeconds(timeInput.getText())
                    numberOfTurns = numberOfTurnsInput.getText().toInt()

                    ourTimer.schedule(object : TimerTask() {
                        override fun run() {
                            --countdown
                            if (countdown < 0) {
                                currentTurn++
                                if (currentTurn > numberOfTurns) {
                                    currentTurn = 1
                                    countdown = toSeconds(timeInput.getText())
                                    cancel()
                                } else {
                                    countdown = toSeconds(timeInput.getText())
                                }

                                timeLabel.text = timeInput.getText()
                                numberOfTurnsLabel.text = "Turn: $currentTurn"
                            } else {
                                timeLabel.text = String.format("%02d:%02d", countdown / 60, countdown % 60)
                            }
                        }
                    }, 1000L, 1000L)
                }
            }

            pauseButton.apply {
                addActionListener {
                    if (pauseButton.text == PAUSE) {
                        pauseButton.text = "Resume"
                        ourTimer.cancel()
                    } else {
                        pauseButton.text = PAUSE
                        countdown = toSeconds(timeInput.getText())
                        numberOfTurns = numberOfTurnsInput.getText().toInt()
                        ourTimer = Timer()
                        ourTimer.schedule(object : TimerTask() {
                            override fun run() {
                                --countdown
                                if (countdown < 0) {
                                    currentTurn++
                                    if (currentTurn > numberOfTurns) {
                                        currentTurn = 1
                                        countdown = toSeconds(timeInput.getText())
                                        cancel()
                                    } else {
                                        countdown = toSeconds(timeInput.getText())
                                    }

                                    timeLabel.text = timeInput.getText()
                                    numberOfTurnsLabel.text = "Turn: $currentTurn"
                                } else {
                                    timeLabel.text = String.format("%02d:%02d", countdown / 60, countdown % 60)
                                }
                            }
                        }, 1000L, 1000L)
                    }
                }
            }

            var timeBox = Box.createHorizontalBox()
            timeBox.add(timeHelpLabel)
            timeBox.add(timeInput)

            var turnBox = Box.createHorizontalBox()
            turnBox.add(numberOfTurns2Label)
            turnBox.add(numberOfTurnsInput)
            
            buttonBox.add(numberOfTurnsLabel)
            buttonBox.add(timeLabel)
            buttonBox.add(startButton)
            buttonBox.add(pauseButton)

            bigBox.add(timeBox)
            bigBox.add(Box.createVerticalStrut(50))
            bigBox.add(turnBox)
            bigBox.add(buttonBox)

            add(bigBox)
        }
    }
}
