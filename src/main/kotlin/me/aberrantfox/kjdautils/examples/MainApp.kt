package me.aberrantfox.kjdautils.examples

import com.google.gson.Gson
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color

data class Properties(val version: String, val repository: String)

private val propFile = Properties::class.java.getResource("/properties.json").readText()
private val project = Gson().fromJson(propFile, Properties::class.java)

data class MyCustomBotConfiguration(val version: String)

fun main(args: Array<String>) {
    val token = args.firstOrNull()
        ?: throw IllegalArgumentException("No program arguments provided. Expected bot token.")

    startBot(token) {
        val myConfig = MyCustomBotConfiguration(project.version)

        registerInjectionObjects(myConfig)

        configure {
            prefix = "!"

            mentionEmbed { event ->
                val self = event.guild.jda.selfUser

                color = Color(0x00bfff)
                thumbnail = self.effectiveAvatarUrl
                addField(self.fullName(), "This is an example embed that can be created whenever the bot is pinged.")
                addInlineField("Prefix", prefix)

                with(project) {
                    addField("Build Info", "```" +
                        "Version: $version\n" +
                        "Kotlin:  ${KotlinVersion.CURRENT}" +
                        "```")

                    addInlineField("Source", repository)
                }
            }
        }
    }
}

@CommandSet("Utility")
fun commandSet(myConfig: MyCustomBotConfiguration) = commands {
    //Command with no args and multiple names
    command("Version", "V") {
        description = "Display the version."
        execute {
            it.respond(myConfig.version)
        }
    }
}