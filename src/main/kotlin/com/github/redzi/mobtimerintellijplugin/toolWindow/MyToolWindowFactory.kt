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
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import java.util.*
import javax.swing.Box
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JTextField

private const val START_RIGHT_AWAY = "Start Right Away"
private const val PAUSE = "Pause"
private const val SKIP = "Skip"
private const val START = "Start"
private const val STOP = "Stop"


// Tomek
// Patrik
// Arif

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
        val popupStartButton = JButton(START_RIGHT_AWAY)
        var popupPauseButton = JButton(PAUSE)
        var popupSkipButton = JButton(SKIP)
        val numberOfTurns2Label = JBLabel("Number of turns: ")
        val numberOfTurnsInput = JTextField("4")
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
            participantsList.setSelectedIndex((model.currentTurn - 1) % model.numberOfTurns)
            timeLabel.text = model.getDisplayTime()
            if (model.popup) {
                if (model.breakCountdown == 0) {
                    popup.cancel()
                }
                breakCountdownLabel.text = model.breakCountdown.toString()
            }
        }

        init {
            list.addElement("Tomek")
            list.addElement("Patrik")
            list.addElement("Arif")
            list.addElement("Jinha")
            participantsList = JBList(list)


             service.model.addListener(this, object : MobTimerModel.Listener {
                 override fun onStateChange(mobTimerModel: MobTimerModel) {
//                    ApplicationManager.getApplication().invokeLater {
                        updateUi(mobTimerModel)
//                    }
                 }
                 override fun onTurnEnded(mobTimerModel: MobTimerModel) {
                     service.model.popup = true
                     service.model.pause()
                     val arr = ArrayList<String>()
                     arr.add(START_RIGHT_AWAY)
                     arr.add(PAUSE)
                     arr.add(SKIP)


                     var popupBox = Box.createVerticalBox()
                     var nextDriverLabel = JBLabel("Next driver: ")
                     var nextDriverName = JBLabel(participantsList.selectedValue)
                     popupBox.add(nextDriverLabel)
                     popupBox.add(nextDriverName)

                     var timeBox = Box.createHorizontalBox()
                     var timeLabel = JBLabel("Next turn starts in: ")
                     timeBox.add(timeLabel)
                     timeBox.add(breakCountdownLabel)
                     popupBox.add(timeBox)

                     var popupButtonBox = Box.createHorizontalBox()
                     popupButtonBox.add(popupStartButton)   // not the start button that we want
                     popupButtonBox.add(popupPauseButton)
                     popupButtonBox.add(popupSkipButton)

                     popupBox.add(popupButtonBox)

                     val componentPopup = JBPopupFactory.getInstance().createComponentPopupBuilder(popupBox, popupBox)

                     popup = componentPopup.createPopup()
                     if (!popup.isVisible) {
                         if (popup.canShow()) {
                             popup.showInFocusCenter()
                         } else {
                             // TODO investigate if this is used
                             popup = componentPopup.createPopup()
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
                    service.model.numberOfTurns = numberOfTurnsInput.getText().toInt()
                    service.model.start()
                    startButton.isEnabled = false
                    pauseButton.isEnabled = true
                }
            }
            popupStartButton.apply {
                addActionListener {
                    service.model.start()
                    popup.cancel()
                }
            }

            pauseButton.isEnabled = false
            pauseButton.apply {
                addActionListener {
                    if (pauseButton.text == PAUSE) {
                        // paused
                        pauseButton.text = "Resume"
                        service.model.pause()
                    } else {
                        pauseButton.text = PAUSE
                        service.model.start()
                    }
                }
            }
            popupPauseButton.apply {
                addActionListener {
                    if (popupPauseButton.text == PAUSE) {
                        // paused
                        popupPauseButton.text = "Resume"
                        service.model.popupPause()
                    } else {
                        popupPauseButton.text = PAUSE
                        service.model.resume()
                    }
                }
            }

            popupSkipButton.apply {
                addActionListener {
                    service.model.skipTurn()
                    service.model.start()
                    popup.cancel()
                }
            }

            stopButton.apply {
                addActionListener {
                    startButton.isEnabled = true
                    pauseButton.text = PAUSE
                    pauseButton.isEnabled = false
                    service.model.pause()
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
