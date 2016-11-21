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
    builder.append("import static java.lang.StrictMath.*;").append(separator)
    builder.append("import model.*;").append(separator)
    builder.append(separator)
    //main class
    builder.append("class MyStrategy implements Strategy {private AI ai = new AI();")
    builder.append("@Override public void move(Wizard self, World world, Game game, Move move) { ai.updateWorldInfo(self, world, game, move); ai.makeDecision();}}")
    builder.append(doubleSeparator)
    //other classes
    files.forEach { file ->
      val content = prepareSource(file.readText())
      builder.append(content).append(doubleSeparator)
    }
    File("$projectDir/src/main/java/MyStrategy.java").printWriter().use { out ->
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
    if (file.name.equals("model")
        || file.name.equals("render_plugins")
        || file.name.equals("strategy")) {
      continue
    }
    if (file.isDirectory()) {
      files.addAll(sourceList(file))
      continue
    }
    if (!file.name.endsWith("java")) {
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
  val clazzIndex = source.indexOf(clazz)
  val enumzIndex = source.indexOf(enumz)
  val interfazeIndex = source.indexOf(interfaze)
  if (clazzIndex > 0) return source.substring(clazzIndex)
  if (enumzIndex > 0) return source.substring(enumzIndex)
  if (interfazeIndex > 0) return source.substring(interfazeIndex)
  return source
}