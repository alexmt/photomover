package plus2flickr

import org.kohsuke.args4j.Option
import java.io.File
import java.util.regex.Pattern
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.apache.commons.io.FileUtils

data class OrganizeOptions() {
  Option("-s") var source: String? = null
  Option("-o") var output: String? = null
  Option("-deleteSource") var deleteSource = false
}

fun getUniqueFile(file: File): File {
  var result = file
  while (result.exists()) {
    result = File(result.directory, result.name + "(copy)")
  }
  return result
}

fun organize(options: OrganizeOptions) {
  val outputDir = File(options?.output ?: "output")
  val datePattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})")
  val filesByDate = File(options.source!!).listFiles {
    it.isDirectory()
  }!!.map {
    val matcher = datePattern.matcher(it.name)
    object {
      val directory = it
      val date = if (matcher.find()) {
        DateTime(
            Integer.parseInt(matcher.group(1)!!),
            Integer.parseInt(matcher.group(2)!!),
            Integer.parseInt(matcher.group(3)!!), 0, 0, 0)
      } else {
        null
      }
    }
  }.filter { it.date != null }.groupBy { it.date }
  FileUtils.deleteDirectory(outputDir)
  val dateFormat = DateTimeFormat.forPattern("MMM yyyy")
  for ((date, groupDirs) in filesByDate) {
    val dateOutputDir = File(outputDir, date!!.toString(dateFormat)!!)
    for (group in groupDirs) {
      for (file in group.directory.listFiles()!!) {
        file.copyTo(getUniqueFile(File(dateOutputDir, file.name)))
      }
      if (options.deleteSource) {
        FileUtils.deleteDirectory(group.directory)
      }
      println("${group.directory} -> ${dateOutputDir} ")
    }
  }
}
