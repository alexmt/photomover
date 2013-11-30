package plus2flickr

import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.CmdLineException

fun main(args: Array<String>) {
  val commands = mapOf("organize" to wrapOperation(::organize, OrganizeOptions()))
  if (args.size == 0) {
    println("Please specify one of following commands:")
    println(commands.keySet())
  } else {
    val command = commands[args[0]]
    if (command == null) {
      println("Command '${args[0]}' is not supported")
    } else {
      command(args.drop(1))
    }
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
