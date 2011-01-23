import java.io.File
import sbt._

class SbtOneJarPlugin(info: ProjectInfo) extends PluginProject(info) {
  override def managedStyle = ManagedStyle.Maven

  val publishTo = if (version.toString.endsWith("-SNAPSHOT"))
    Resolver.file("Snapshots Local",  new File("/Users/jason/code/retronym.github.com/repo/snapshots"))
  else
    Resolver.file("Releases Local",  new File("/Users/jason/code/retronym.github.com/repo/releases"))
}
