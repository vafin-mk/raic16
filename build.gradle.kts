import org.gradle.api.tasks.*
import java.lang.ProcessBuilder
import java.io.File
import java.util.*

apply<ApplicationPlugin>()
apply<JavaPlugin>()

configure<ApplicationPluginConvention> {
  mainClassName = "Runner"
}

repositories {
  jcenter()
}

dependencies {
  testCompile("junit:junit:4.12")
}

task("combine") {
  doLast {
    val files = sourceList(File("$projectDir/src/main/java"))
    val builder = StringBuilder()
    val separator = "\n"
    val doubleSeparator = "\n\n"
    //imports
    builder.append("import java.util.*;").append(separator)
    builder.append("import java.lang.*;").append(separator)
    builder.append("import model.*;").append(separator)
    builder.append(separator)
    //main class
    builder.append("class MyStrategy : Strategy {val ai = AI()\n")
    builder.append("override fun move(self: Wizard, world: World, game: Game, move: Move) { ai.updateInfo(self, world, game, move); ai.decision();}}")
    builder.append(doubleSeparator)
    //other classes
    files.forEach { file ->
      val content = prepareSource(file.readText())
      builder.append(content).append(doubleSeparator)
    }
    File("$projectDir/src/main/java/MyStrategy.kt").printWriter().use { out ->
      out.println(builder)
    }
  }
}

task("runServer") {
  doLast {
    val pb = ProcessBuilder("java", "-Xms512m", "-Xmx2G", "-server", "-jar",
        "src/main/local-runner/local-runner.jar", "src/main/local-runner/local-runner.properties",
        "src/main/local-runner/local-runner.default.properties")
    pb.redirectErrorStream(true)
        .redirectOutput(ProcessBuilder.Redirect.to(File(project.buildDir, "runServer.log")))
    println("Starting local runner now ... ")
    pb.start();
  }
}

fun sourceList(dir: File) : List<File> {
  val files = ArrayList<File>()
  for (file in dir.listFiles()) {
    if (file.name == "model"
        || file.name.endsWith("Strategy.kt")) {
      continue
    }
    if (file.isDirectory) {
      files.addAll(sourceList(file))
      continue
    }
    if (!file.name.endsWith("kt")) {
      continue
    }
    files.add(file)
  }
  return files
}

fun prepareSource(source: String): String {
  val clazz = "class"
  val enumz = "enum"
  val interfaze = "interface"
  val funz = "fun"//extensions
  val clazzIndex = source.indexOf(clazz)
  val enumzIndex = source.indexOf(enumz)
  val interfazeIndex = source.indexOf(interfaze)
  val funzIndex = source.indexOf(funz)
  if (clazzIndex > 0) return source.substring(clazzIndex)
  if (enumzIndex > 0) return source.substring(enumzIndex)
  if (interfazeIndex > 0) return source.substring(interfazeIndex)
  if (funzIndex > 0) return source.substring(funzIndex)
  return source
}
