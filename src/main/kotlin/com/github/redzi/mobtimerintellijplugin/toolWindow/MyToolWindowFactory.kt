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
import com.github.redzi.mobtimerintellijplugin.model.MobTimerModel
import com.github.redzi.mobtimerintellijplugin.services.MyProjectService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import java.util.*
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JTextField
import javax.swing.ListSelectionModel

private const val START_RIGHT_AWAY = "Start Right Away"
private const val PAUSE = "Pause"
private const val SKIP = "Skip"
private const val START = "Start"

// Jinha
// Arif
// Patrik
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
        val numberOfTurnsLabel = JBLabel("Turn: 1")
        val timeInput = JTextField("00:03")
        val timeLabel = JBLabel(timeInput.getText())
        val startButton = JButton(START)
        var pauseButton = JButton(PAUSE)
        val numberOfTurns2Label = JBLabel("Number of turns: ")
        val numberOfTurnsInput = JTextField("2")
        var bigBox = Box.createVerticalBox()
        var buttonBox = Box.createVerticalBox()

        private fun updateUi(model: MobTimerModel) {
            numberOfTurnsLabel.text = "Turn: " + model.currentTurn
            timeLabel.text = model.getDisplayTime()
        }

        init {
             service.model.addListener(this, object : MobTimerModel.Listener {
                override fun onStateChange(mobTimerModel: MobTimerModel) {
                    ApplicationManager.getApplication().invokeLater {
                        updateUi(mobTimerModel)
                    }
                }
                 override fun onTurnEnded(mobTimerModel: MobTimerModel) {
                     service.model.pause()
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
                                 if (value == START_RIGHT_AWAY) {
                                     service.model.start()
                                 } else if (value == PAUSE) {
                                     // pause the break, not the session
                                 } else { // Skip
//                                     currentTurn++
                                 }
                             }
                     var popup = popupBuilder.createPopup()

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
                    service.model.setTimeInput(timeInput.getText())
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
                        service.model.pause()
                    } else {
                        pauseButton.text = PAUSE
                        service.model.start()
                    }
                }
            }

            // TODO: stop the sesson button

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
