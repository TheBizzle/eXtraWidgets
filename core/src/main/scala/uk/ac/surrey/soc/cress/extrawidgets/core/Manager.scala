package uk.ac.surrey.soc.cress.extrawidgets.core

import org.nlogo.app.App
import org.nlogo.app.ToolsMenu

import uk.ac.surrey.soc.cress.extrawidgets.api.Kind
import uk.ac.surrey.soc.cress.extrawidgets.core.Swing.enrichComponent
import uk.ac.surrey.soc.cress.extrawidgets.plugin.ExtraWidgetsPlugin
import uk.ac.surrey.soc.cress.extrawidgets.state.getOrCreateModel

class Manager(val app: App, val toolsMenu: ToolsMenu) {

  val (reader, writer) = getOrCreateModel(app.workspace.getExtensionManager)

  val widgetKinds: Map[String, Kind] = WidgetsLoader.loadWidgetKinds()

  val gui = new GUI(app, toolsMenu, writer, widgetKinds)
  val view = new View(reader, gui)

  app.frame.onComponentShown { _ ⇒
    (0 until app.tabs.getTabCount)
      .map(i ⇒ i -> app.tabs.getComponentAt(i))
      .find(_._2.isInstanceOf[ExtraWidgetsPlugin])
      .foreach {
        case (i, pluginTab) ⇒
          app.tabs.remove(pluginTab)
          app.tabs.removeMenuItem(i)
      }
  }

}