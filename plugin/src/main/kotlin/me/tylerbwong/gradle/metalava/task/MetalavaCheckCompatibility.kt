package me.tylerbwong.gradle.metalava.task

import me.tylerbwong.gradle.metalava.Module
import me.tylerbwong.gradle.metalava.extension.MetalavaExtension
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

internal object MetalavaCheckCompatibility : MetalavaTaskContainer() {
    fun registerMetalavaCheckCompatibilityTask(project: Project, extension: MetalavaExtension, module: Module) {
        with(project) {
            val tempFilename = layout.buildDirectory.file("metalava/current.txt").get().asFile.absolutePath
            val generateTempMetalavaSignatureTask = MetalavaSignature.registerMetalavaSignatureTask(
                project = this,
                name = "metalavaTempSignature",
                description = """
                    Generates a Metalava signature descriptor file in the project build directory for API compatibility 
                    checking.
                """.trimIndent(),
                extension = extension,
                module = module,
                filename = tempFilename
            )
            tasks.register("metalavaCheckCompatibility", JavaExec::class.java) {
                group = "verification"
                description = "Checks API compatibility between the code base and the current API."
                main = "com.android.tools.metalava.Driver"
                classpath(extension.metalavaJarPath?.let { files(it) } ?: getMetalavaClasspath(extension.version))
                dependsOn(generateTempMetalavaSignatureTask)

                // TODO Consolidate flags between tasks
                val hidePackages = extension.hiddenPackages.flatMap { listOf("--hide-package", it) }
                val hideAnnotations = extension.hiddenAnnotations.flatMap { listOf("--hide-annotation", it) }

                val args: List<String> = listOf(
                    "--no-banner",
                    "--no-color",
                    "--format=${extension.format}",
                    "--source-files", tempFilename,
                    "--check-compatibility:api:current", extension.filename,
                    "--input-kotlin-nulls=${extension.outputKotlinNulls.flagValue}"
                ) + extension.reportWarningsAsErrors.flag("--warnings-as-errors") +
                    extension.reportLintsAsErrors.flag("--lints-as-errors")  + hidePackages + hideAnnotations

                isIgnoreExitValue = false
                setArgs(args)
            }
        }
    }
}
