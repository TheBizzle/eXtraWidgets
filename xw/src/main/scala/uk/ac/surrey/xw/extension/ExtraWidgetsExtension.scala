package uk.ac.surrey.xw.extension

import org.nlogo.api.DefaultClassManager
import org.nlogo.api.ExtensionManager
import org.nlogo.api.Primitive
import org.nlogo.api.PrimitiveManager

import uk.ac.surrey.xw.WidgetsLoader
import uk.ac.surrey.xw.api.KindName
import uk.ac.surrey.xw.api.WidgetKind
import uk.ac.surrey.xw.extension.prim.Ask
import uk.ac.surrey.xw.extension.prim.ClearAll
import uk.ac.surrey.xw.extension.prim.Create
import uk.ac.surrey.xw.extension.prim.Get
import uk.ac.surrey.xw.extension.prim.GetProperty
import uk.ac.surrey.xw.extension.prim.KindList
import uk.ac.surrey.xw.extension.prim.LoadJSON
import uk.ac.surrey.xw.extension.prim.Of
import uk.ac.surrey.xw.extension.prim.Remove
import uk.ac.surrey.xw.extension.prim.Set
import uk.ac.surrey.xw.extension.prim.SetProperty
import uk.ac.surrey.xw.extension.prim.ToJSON
import uk.ac.surrey.xw.extension.prim.Version
import uk.ac.surrey.xw.extension.prim.Widgets
import uk.ac.surrey.xw.extension.prim.With
import uk.ac.surrey.xw.extension.util.getApp
import uk.ac.surrey.xw.gui.Manager
import uk.ac.surrey.xw.state.Reader
import uk.ac.surrey.xw.state.Writer
import uk.ac.surrey.xw.state.newMutableWidgetMap

class ExtraWidgetsExtension extends DefaultClassManager {

  private var widgetContextManager: WidgetContextManager = null
  private var writer: Writer = null
  private var reader: Reader = null
  private var primitives: Seq[(String, Primitive)] = null

  override def runOnce(extensionManager: ExtensionManager): Unit = {

    val widgetKinds: Map[KindName, WidgetKind[_]] =
      WidgetsLoader.loadWidgetKinds

    locally {
      val widgetMap = newMutableWidgetMap
      reader = new Reader(widgetMap)
      writer = new Writer(widgetMap, widgetKinds, reader)
      widgetContextManager = new WidgetContextManager
    }

    val kindInfo = new KindInfo(reader, widgetKinds)

    val staticPrimitives = Seq(
      "VERSION" -> new Version("0.0.0-wip"),
      "ASK" -> new Ask(widgetContextManager),
      "OF" -> new Of(widgetContextManager),
      "WITH" -> new With(widgetContextManager),
      "GET" -> new Get(reader, kindInfo, widgetContextManager),
      "SET" -> new Set(writer, kindInfo, widgetContextManager),
      "REMOVE" -> new Remove(writer),
      "WIDGETS" -> new Widgets(reader),
      "CLEAR-ALL" -> new ClearAll(writer),
      "JSON" -> new ToJSON(reader),
      "LOAD-JSON" -> new LoadJSON(writer)
    )

    val kindListPrimitives = for {
      (kindName, pluralName) ← widgetKinds.mapValues(_.pluralName)
    } yield pluralName -> new KindList(kindName, reader)

    val constructorPrimitives = widgetKinds.keys.map { kindName ⇒
      ("CREATE-" + kindName) -> new Create(kindName, writer, widgetContextManager)
    }

    // When building getters and setters for properties that are
    // multiply defined, we fold their syntactic indications together
    // by using bitwise ORs (.reduce(_ | _))

    val outputTypes = for {
      kind ← widgetKinds.values
      property ← kind.properties.values
    } yield (property.key, property.outputType)
    val getters = for {
      (key, outputType) ← outputTypes.groupBy(_._1)
        .mapValues(_.unzip._2.reduce(_ | _))
      getter = new GetProperty(reader, key, outputType, widgetContextManager)
    } yield key -> getter

    val inputTypes = for {
      kind ← widgetKinds.values
      property ← kind.properties.values
      if !property.readOnly
    } yield (property.key, property.inputType)
    val setters = for {
      (key, inputType) ← inputTypes.groupBy(_._1)
        .mapValues(_.unzip._2.reduce(_ | _))
      setter = new SetProperty(writer, key, inputType, kindInfo, widgetContextManager)
    } yield ("SET-" + key) -> setter

    primitives =
      staticPrimitives ++ constructorPrimitives ++
        kindListPrimitives ++ getters ++ setters

    for (app ← getApp(extensionManager))
      new Manager(app, reader, writer, widgetKinds)
  }

  def load(primitiveManager: PrimitiveManager): Unit =
    for ((name, prim) ← primitives)
      primitiveManager.addPrimitive(name, prim)

  override def unload(em: ExtensionManager): Unit = clearAll()

}
