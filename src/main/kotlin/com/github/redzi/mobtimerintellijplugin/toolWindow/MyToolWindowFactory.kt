package com.github.redzi.mobtimerintellijplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.redzi.mobtimerintellijplugin.model.MobTimerModel
import com.github.redzi.mobtimerintellijplugin.services.MyProjectService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import java.util.*
import javax.swing.Box
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JTextField
import javax.swing.ListSelectionModel

private const val START_RIGHT_AWAY = "Start Right Away"
private const val PAUSE = "Pause"
private const val SKIP = "Skip"
private const val START = "Start"
private const val STOP = "Stop"


// Arif
// Patrik
// Tomek

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
        val timeHelpLabel = JBLabel("Turn time: ")
        val breakHelpLabel = JBLabel("Break time: ")
        val sessionBreakHelpLabel = JBLabel("Session break time: ")
        val numberOfTurnsLabel = JBLabel("Turn: 1")
        val timeInput = JTextField("00:03")
        val breakInput = JTextField("00:03")
        val sessionBreakInput = JTextField("00:10")
        val timeLabel = JBLabel(timeInput.getText())
        val startButton = JButton(START)
        var pauseButton = JButton(PAUSE)
        var stopButton = JButton(STOP)
        val numberOfTurns2Label = JBLabel("Number of turns: ")
        val numberOfTurnsInput = JTextField("2")
        var bigBox = Box.createVerticalBox()
        var buttonBox = Box.createVerticalBox()
        var list = DefaultListModel<String>()
        var participantsList = JBList<String>()
        var addParticipantInput = JBTextField()
        val addParticipantButton = JButton("Add")
        var breakCountdownLabel = JBLabel("00:03")
        lateinit var popup : JBPopup

        private fun updateUi(model: MobTimerModel) {
            numberOfTurnsLabel.text = "Turn: " + model.currentTurn
            participantsList.setSelectedIndex(model.currentTurn-1)
            timeLabel.text = model.getDisplayTime()
            if (model.popup) {
                if (model.breakCountdown == 0) {
                    popup.cancel()
                }
                breakCountdownLabel.text = model.breakCountdown.toString()
            }
        }

        init {
            participantsList = JBList(list)

             service.model.addListener(this, object : MobTimerModel.Listener {
                 override fun onStateChange(mobTimerModel: MobTimerModel) {
                    ApplicationManager.getApplication().invokeLater {
                        updateUi(mobTimerModel)
                    }
                 }
                 override fun onTurnEnded(mobTimerModel: MobTimerModel) {
                     service.model.popup = true
                     service.model.pauseAndStop()
                     val arr = ArrayList<String>()
                     arr.add(START_RIGHT_AWAY)
                     arr.add(PAUSE)
                     arr.add(SKIP)

                     val popupBuilder = JBPopupFactory.getInstance().createPopupChooserBuilder(arr)
                             .setTitle("Next Turn")
                             .setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
                             .setCancelOnOtherWindowOpen(false)
                             .setCancelOnClickOutside(false)
                             .setCancelKeyEnabled(false)
                             .setCancelOnWindowDeactivation(false)
                             .setItemChosenCallback { value ->
                                 service.model.popup = false
                                 if (value == START_RIGHT_AWAY) {
                                     service.model.start()
                                 } else if (value == PAUSE) {
                                     // pause the break, not the session
                                 } else { // Skip
//                                     currentTurn++
                                 }
                             }

                     popup = popupBuilder.createPopup()
                     if (!popup.isVisible) {
                         if (popup.canShow()) {
                             popup.showInFocusCenter()
                         } else {
                             popup = popupBuilder.createPopup()
                             popup.showInFocusCenter()
                         }
                     }
                 }
             })
        }

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            startButton.apply {
                addActionListener {
                    service.model.setTimeInput(timeInput.getText(), breakInput.getText(), sessionBreakInput.getText())
                    service.model.setNumberOfTurns(numberOfTurnsInput.getText().toInt())
                    service.model.start()
                    startButton.isEnabled = false
                }
            }

            pauseButton.apply {
                addActionListener {
                    if (pauseButton.text == PAUSE) {
                        // paused
                        pauseButton.text = "Resume"
                        service.model.pauseAndStop()
                    } else {
                        pauseButton.text = PAUSE
                        service.model.start()
                    }
                }
            }

            stopButton.apply {
                addActionListener {
                    startButton.isEnabled = true
                    pauseButton.text = PAUSE
                    pauseButton.isEnabled = false
                    service.model.pauseAndStop()
                }
            }

            addParticipantButton.apply {
                addActionListener {
                    list.addElement(addParticipantInput.text)
                    addParticipantInput.text = ""
                }
            }

            // TODO: stop the sesson button

            var timeBox = Box.createHorizontalBox()
            timeBox.add(timeHelpLabel)
            timeBox.add(timeInput)
            timeBox.add(breakHelpLabel)
            timeBox.add(breakInput)
            timeBox.add(sessionBreakHelpLabel)
            timeBox.add(sessionBreakInput)

            var turnBox = Box.createHorizontalBox()
            turnBox.add(numberOfTurns2Label)
            turnBox.add(numberOfTurnsInput)

            buttonBox.add(numberOfTurnsLabel)
            buttonBox.add(timeLabel)
            buttonBox.add(breakCountdownLabel)
            buttonBox.add(startButton)
            buttonBox.add(pauseButton)
            buttonBox.add(stopButton)

            bigBox.add(timeBox)
            bigBox.add(Box.createVerticalStrut(50))
            bigBox.add(turnBox)
            bigBox.add(buttonBox)

            bigBox.add(addParticipantInput)
            bigBox.add(addParticipantButton)
            bigBox.add(participantsList)

            add(bigBox)
        }
    }
}
