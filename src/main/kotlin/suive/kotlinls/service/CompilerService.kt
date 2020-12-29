package suive.kotlinls.service

import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.incremental.makeIncrementally
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.tinylog.kotlin.Logger
import java.nio.file.Path

class CompilerService {
    companion object {
        const val CLASSES_DIR_NAME = "classes"
        const val CACHES_DIR_NAME = "cache"
    }

    private val compilerEnvironment = KotlinCoreEnvironment.createForProduction(
        parentDisposable = Disposer.newDisposable(), // TODO what is Disposable
        configuration = CompilerConfiguration().apply {
            val langFeatures = mutableMapOf<LanguageFeature, LanguageFeature.State>()
            for (langFeature in LanguageFeature.values()) {
                // TODO not all features should be enabled
                langFeatures[langFeature] = LanguageFeature.State.ENABLED
            }
            val languageVersionSettings = LanguageVersionSettingsImpl(
                LanguageVersion.LATEST_STABLE, // TODO should be taken from Project config.
                ApiVersion.createByLanguageVersion(LanguageVersion.LATEST_STABLE),
                emptyMap(),
                langFeatures
            )

            put(CommonConfigurationKeys.MODULE_NAME, JvmProtoBufUtil.DEFAULT_MODULE_NAME)
            put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, languageVersionSettings)
            put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, object : MessageCollector {
                override fun clear() {}

                override fun report(
                    severity: CompilerMessageSeverity,
                    message: String,
                    location: CompilerMessageSourceLocation?
                ) {
                    Logger.tag("Kotlin Compiler").debug(message)
                }

                override fun hasErrors() = false

            })
//                add(ComponentRegistrar.PLUGIN_COMPONENT_REGISTRARS, ScriptingCompilerConfigurationComponentRegistrar())

//                addJvmClasspathRoots(classPath.map { it.toFile() })
//                addJavaSourceRoots(javaSourcePath.map { it.toFile() })
        },
        configFiles = EnvironmentConfigFiles.JVM_CONFIG_FILES
    )

    fun updateClasspath(classpath: List<Path>) {
        compilerEnvironment.updateClasspath(classpath.map { JvmClasspathRoot(it.toFile()) })
    }

    fun parseFile(text: String): PsiFile {
        val psiFileFactory = PsiFileFactory.getInstance(compilerEnvironment.project)
        return psiFileFactory.createFileFromText(KotlinLanguage.INSTANCE, text)
    }

    fun compile(rootPath: Path, srcPath: Path, messageCollector: MessageCollector) {
        val args = K2JVMCompilerArguments().apply {
            destination = rootPath.resolve(CLASSES_DIR_NAME).toAbsolutePath().toString()
            moduleName = "test"
            noReflect = true
        }

        makeIncrementally(rootPath.resolve(CACHES_DIR_NAME).toFile(), listOf(srcPath.toFile()), args, messageCollector)
    }
}
