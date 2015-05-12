package interplay

import sbt._
import sbt.Keys._
import sbt.Package.ManifestAttributes

object Omnidoc extends AutoPlugin {

  object autoImport {
    lazy val omnidocGithubRepo = settingKey[String]("Github repository for source URL")
    lazy val omnidocSnapshotBranch = settingKey[String]("Git branch for development versions")
    lazy val omnidocTagPrefix = settingKey[String]("Prefix before git tagged versions")
    lazy val omnidocPathPrefix = settingKey[String]("Prefix before source directory paths")
    lazy val omnidocSourceUrl = settingKey[Option[String]]("Source URL for scaladoc linking")
  }

  val SourceUrlKey = "Omnidoc-Source-URL"

  override def requires = sbt.plugins.JvmPlugin

  override def trigger = noTrigger

  import autoImport._

  override def projectSettings = Seq(
    omnidocSourceUrl := omnidocGithubRepo.?.value map { repo =>
      val development = (omnidocSnapshotBranch ?? "master").value
      val tagged = (omnidocTagPrefix ?? "v").value + version.value
      val tree = if (isSnapshot.value) development else tagged
      val prefix = "/" + (omnidocPathPrefix ?? "").value
      val directory = IO.relativize((baseDirectory in ThisBuild).value, baseDirectory.value)
      val path = directory.fold("")(prefix.+)
      s"https://github.com/${repo}/tree/${tree}${path}"
    },
    packageOptions in (Compile, packageSrc) ++= omnidocSourceUrl.value.toSeq map { url =>
      ManifestAttributes(SourceUrlKey -> url)
    }
  )

}