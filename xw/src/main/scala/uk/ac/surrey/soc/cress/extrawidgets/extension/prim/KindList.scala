package uk.ac.surrey.soc.cress.extrawidgets.extension.prim

import org.nlogo.api.Argument
import org.nlogo.api.Context
import org.nlogo.api.DefaultReporter
import org.nlogo.api.LogoList
import org.nlogo.api.Syntax.ListType
import org.nlogo.api.Syntax.reporterSyntax
import uk.ac.surrey.soc.cress.extrawidgets.state.Reader
import uk.ac.surrey.soc.cress.extrawidgets.api.KindName

class KindList(kindName: KindName, reader: Reader) extends DefaultReporter {
  override def getSyntax = reporterSyntax(ListType)
  def report(args: Array[Argument], context: Context): AnyRef =
    LogoList.fromVector(
      reader.widgetKeyVector.filter(reader.get("KIND", _) == kindName)
    )
}