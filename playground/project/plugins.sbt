
// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.2")

// The BuildInfo Plugin
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.5.0")

// Dependency tree plugin
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.1")

// Code Formatter
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

// Comment to get more information during initialization
logLevel := Level.Warn

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")
