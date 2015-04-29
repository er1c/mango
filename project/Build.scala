import sbt._
import Keys._
import Dependencies._

object BuildSettings {
	
	val buildOrganization = "org.feijoas"
	val buildVersion      = "0.11" 
	val buildScalaVersion = "2.11.6"
	val gitHeadCommitSha  = Process("git rev-parse HEAD").lines.head
	val release           = sys.props("release")=="true"

	val buildSettings = Defaults.defaultSettings ++ 
		org.scalastyle.sbt.ScalastylePlugin.Settings  ++
		Seq (
		organization := buildOrganization,
		scalaVersion := buildScalaVersion,
		shellPrompt  := ShellPrompt.buildShellPrompt,
		version      := {  if(release) buildVersion 
						   else buildVersion + "-" + gitHeadCommitSha
		},

		// Scala compiler options
		scalacOptions  ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-Xlint", "-feature","-language:implicitConversions,reflectiveCalls,postfixOps,higherKinds,existentials"),
		// Scaladoc title
		scalacOptions in (Compile, doc) ++= Opts.doc.title("Mango"),
		// Scaladoc title page
		scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", "rootdoc.txt"),
		// Show durations for tests
		testOptions in Test += Tests.Argument("-oD"),
		// Disable parallel execution of tests
		parallelExecution in Test := false,
		// Using the Scala version in output paths and artifacts
		crossPaths := true,
		// publish test jar, sources, and docs
		publishArtifact in Test := false,
		// Publish Maven style,
		publishMavenStyle := true,
		// Remove the repositories for optional dependencies
		pomIncludeRepository := { _ => false },
		// POM metadata that isn't generated by SBT
		pomExtra := (
		  <url>mango.feijoas.org</url>
		  <licenses>
		    <license>
		      <name>The Apache Software License, Version 2.0</name>
		      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
		      <distribution>repo</distribution>
		    </license>
		  </licenses>
		  <scm>
		    <url>https://github.com/feijoas/mango.git</url>
		    <connection>https://github.com/feijoas/mango.git</connection>
		  </scm>
		  <developers>
		    <developer>
		      <id>mschneiderwng</id>
		      <name>Markus Schneider</name>
		    </developer>
		  </developers>),
		// SNAPSHOT versions go to the /snapshot repository while other versions go to the /releases repository
		publishTo <<= version { (v: String) =>
		  val nexus = "https://oss.sonatype.org/"
		  if (v.trim.endsWith("SNAPSHOT"))
		    Some("snapshots" at nexus + "content/repositories/snapshots")
		  else
		    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
		}		
	)			
}

object Resolvers {
	val sonatypeSnapshot = "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
	val sonatypeRelease  = "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
	val sonatypeResolvers = Seq (sonatypeRelease, sonatypeSnapshot)
}

object MangoBuild extends Build {

	import Resolvers._
	import Dependencies._
	import BuildSettings._

	lazy val mango = Project(
		"mango", 
		file("."),
		settings = buildSettings ++ Seq (
			resolvers ++= sonatypeResolvers,
			libraryDependencies ++= deps
		)
	)
}
