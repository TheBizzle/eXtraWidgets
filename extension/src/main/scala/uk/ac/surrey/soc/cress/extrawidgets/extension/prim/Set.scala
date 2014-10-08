package uk.ac.surrey.soc.cress.extrawidgets.extension.prim

import org.nlogo.api.Argument
import org.nlogo.api.Context
import org.nlogo.api.DefaultCommand
import org.nlogo.api.Syntax.StringType
import org.nlogo.api.Syntax.WildcardType
import org.nlogo.api.Syntax.commandSyntax

import uk.ac.surrey.soc.cress.extrawidgets.extension.util.tryTo
import uk.ac.surrey.soc.cress.extrawidgets.state.Writer

// xw:set property-key widget-key property-value
class Set(writer: Writer) extends DefaultCommand {
  override def getSyntax = commandSyntax(Array(StringType, StringType, WildcardType))
  def perform(args: Array[Argument], context: Context): Unit = {
    val propertyKey = args(0).getString
    val widgetKey = args(1).getString
    val propertyValue = args(2).get
    tryTo(writer.set(propertyKey, widgetKey, propertyValue))
  }
}