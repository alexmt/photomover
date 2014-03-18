package plus2flickr.cli

import plus2flickr.cli.utils.organize
import plus2flickr.cli.utils.upload
import plus2flickr.cli.web.start

import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.CmdLineException
import plus2flickr.cli.utils.OrganizeOptions
import plus2flickr.cli.utils.UploadOptions
import plus2flickr.cli.web.StartWebOptions

fun main(arguments: Array<String>) {
  val commands = mapOf(
      "organize" to wrapOperation(::organize, OrganizeOptions()),
      "upload" to wrapOperation(::upload, UploadOptions()),
      "startWeb" to wrapOperation(::start, StartWebOptions()))
  val args = if (arguments.size == 0) {
    array("startWeb")
  } else {
    arguments
  }
  val command = commands[args[0]]
  if (command == null) {
    println("Command '${args[0]}' is not supported.")
    println("Please specify one of following: ${commands.keySet()}")
  } else {
    command(args.drop(1))
  }
}

fun wrapOperation<T>(
    operation: (options: T) -> Unit, defaultOptions: T): (args: List<String>) -> Unit {
  return { args ->
    val parser = CmdLineParser(defaultOptions)
    try {
      parser.parseArgument(args)
    } catch (ex: CmdLineException) {
      println(ex.getMessage())
      println(parser.printUsage(System.err))
    }
    operation(defaultOptions)
  }
}