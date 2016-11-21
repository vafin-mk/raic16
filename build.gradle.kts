import org.gradle.api.tasks.*
import java.lang.ProcessBuilder
import java.io.File

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

task("runServer") {
  val pb = ProcessBuilder("java", "-Xms512m", "-Xmx2G", "-server", "-jar",
    "src/main/local-runner/local-runner.jar", "src/main/local-runner/local-runner.properties",
    "src/main/local-runner/local-runner.default.properties")
    pb.redirectErrorStream(true)
        .redirectOutput(ProcessBuilder.Redirect.to(File(project.buildDir, "runServer.log")))
    println("Starting local runner now ... ")
    pb.start();
}