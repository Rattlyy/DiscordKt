package me.aberrantfox.kjdautils.internal.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.command.*
import java.io.File

@Service
class DocumentationService(container: CommandsContainer) {
    private val saveFile = File("commands.md")

    data class CategoryDocs(val name: String, val docString: String)

    data class CommandData(val name: String, val args: String, val description: String) {
        fun format(format: String) = String.format(format, name, args, description)
    }

    init {
        val docs = container.commands
            .groupBy { it.category }
            .map { generateCategoryDoc(it.key, it.value) }
            .sortedBy { it.name }

        outputDocs(docs)
    }

    private fun outputDocs(rawDocs: List<CategoryDocs>) {
        val docString = "# Commands\n\n" +
            "## Key\n" +
            "| Symbol     | Meaning                    |\n" +
            "| ---------- | -------------------------- |\n" +
            "| (Argument) | This argument is optional. |\n\n" +
            rawDocs.joinToString("") { "## ${it.name}\n${it.docString}\n" }

        saveFile.writeText(docString)
    }

    private fun generateCategoryDoc(name: String, commands: List<Command>): CategoryDocs {
        val commandData = commands.map { extractCommandData(it) }
        val docs = formatDocs(commandData.toMutableList())

        return CategoryDocs(name, docs)
    }

    private fun extractCommandData(command: Command): CommandData {
        val expectedArgs = command.expectedArgs.arguments.joinToString {
            if (it.isOptional) "(${it.name})" else it.name
        }.takeIf { it.isNotEmpty() } ?: "<none>"

        return CommandData(command.names.joinToString().replace("|", "\\|"),
            expectedArgs.replace("|", "\\|"),
            command.description.replace("|", "\\|"))
    }

    private fun formatDocs(commandData: MutableList<CommandData>): String {
        val headerData = CommandData("Commands", "Arguments", "Description")

        commandData.add(headerData)
        val longestName = commandData.maxBy { it.name.length }!!.name.length
        val longestArgs = commandData.maxBy { it.args.length }!!.args.length
        val longestDescription = commandData.maxBy { it.description.length }!!.description.length
        commandData.remove(headerData)

        val formatString = "| %-${longestName}s | %-${longestArgs}s | %-${longestDescription}s |"

        val headerString = headerData.format(formatString)
        val separator = formatString.format("-".repeat(longestName), "-".repeat(longestArgs), "-".repeat(longestDescription))
        val commandString = commandData.sortedBy { it.name }.joinToString("\n") { it.format(formatString) }

        return "$headerString\n$separator\n$commandString\n"
    }
}