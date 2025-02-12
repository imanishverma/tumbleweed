package com.legacycode.tumbleweed.cli.ownership

import com.legacycode.tumbleweed.web.ownership.OwnershipServer
import java.io.File
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(
  name = "ownership",
  description = ["visualize file ownership information in your browser"],
)
class OwnershipCommand : Runnable {
  companion object {
    private const val DEFAULT_PORT = 7080
  }

  @CommandLine.Option(
    names = ["-r", "--repo"],
    description = ["path to the git repo"],
    required = true,
  )
  lateinit var repoDir: File

  override fun run() {
    OwnershipServer().start(repoDir.path, DEFAULT_PORT)
  }
}
