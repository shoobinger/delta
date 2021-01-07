package suive.delta.service

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.jvm.compiler.CliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.psi.impl.PsiFileFactoryImpl
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.LazyTopDownAnalyzer
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import suive.delta.Workspace
import suive.delta.model.CompletionItem
import suive.delta.model.CompletionResult
import suive.delta.util.DiagnosticMessageCollector
import suive.delta.util.getOffset
import java.nio.file.Files

class CompletionService(
    private val workspace: Workspace
) {

    fun getCompletions(fileUri: String, row: Int, col: Int): CompletionResult {
        return CompletionResult(items = getDeclarationDescriptors(fileUri, row, col).mapNotNull { descriptor ->
            when (descriptor) {
                is FunctionDescriptor -> {
                    val name = descriptor.name
                    val parameters = descriptor.valueParameters.joinToString(",") { "${it.name}: ${it.type}" }
                    val returnType = descriptor.returnType
                    val label = "$name($parameters): $returnType"
                    val insertText = if (parameters.isEmpty()) "$name()" else "$name("
                    CompletionItem(label, insertText)
                }
                else -> null
            }
        })
    }

    private fun getDeclarationDescriptors(fileUri: String, row: Int, col: Int): List<DeclarationDescriptor> {
        val trace = CliBindingTrace()
        val rootDisposable = Disposer.newDisposable()
        val configuration = CompilerConfiguration().apply {
            put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, DiagnosticMessageCollector(workspace))
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
        }
        val environment = KotlinCoreEnvironment.createForProduction(
            rootDisposable,
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
        val psiFileFactory = PsiFileFactory.getInstance(environment.project) as PsiFileFactoryImpl

        val file = workspace.toInternalPath(fileUri)
        val text = Files.readString(file)
        val ktFile = psiFileFactory.createFileFromText(
            file.fileName.toString(),
            KotlinFileType.INSTANCE,
            text
        ) as KtFile

        // TODO this should be saved and updated after each rebuild.
        val container = TopDownAnalyzerFacadeForJVM.createContainer(
            project = environment.project,
            files = listOf(ktFile),
            trace = trace,
            configuration = environment.configuration,
            packagePartProvider = environment::createPackagePartProvider,
            declarationProviderFactory = ::FileBasedDeclarationProviderFactory
        )
        container.get<LazyTopDownAnalyzer>()
            .analyzeDeclarations(TopDownAnalysisMode.TopLevelDeclarations, listOf(ktFile))
        val bc = trace.bindingContext

        val element = ktFile.findElementAt(getOffset(text, row, col))
        val parent = element?.parent

        if (parent is KtDotQualifiedExpression) {
            val receiverType = bc.getType(parent.receiverExpression) ?: return emptyList()
            return receiverType.memberScope.getContributedDescriptors().toList()
        }

        return emptyList()
    }
}
