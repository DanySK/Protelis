@file:Suppress("UnstableApiUsage")

import com.github.spotbugs.SpotBugsTask
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import java.net.URL
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("de.fayard.buildSrcVersions") version
        Versions.de_fayard_buildsrcversions_gradle_plugin
    id("org.danilopianini.git-sensitive-semantic-versioning") version
        Versions.org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin
    eclipse
    `java-library`
    jacoco
    id("com.github.spotbugs") version Versions.com_github_spotbugs_gradle_plugin
    pmd
    checkstyle
    id("org.jlleitschuh.gradle.ktlint") version Versions.org_jlleitschuh_gradle_ktlint_gradle_plugin
    signing
    `maven-publish`
    id("org.danilopianini.publish-on-central") version Versions.org_danilopianini_publish_on_central_gradle_plugin
    id("com.jfrog.bintray") version Versions.com_jfrog_bintray_gradle_plugin
    id("com.gradle.build-scan") version Versions.com_gradle_build_scan_gradle_plugin
    id("org.jetbrains.kotlin.jvm") version Versions.org_jetbrains_kotlin_jvm_gradle_plugin
    id("com.eden.orchidPlugin") version Versions.com_eden_orchidplugin_gradle_plugin
}

apply(plugin = "com.gradle.build-scan")
apply(plugin = "com.jfrog.bintray")
apply(plugin = "com.eden.orchidPlugin")

val scmUrl = "git:git@github.com:Protelis/Protelis"

allprojects {

    apply(plugin = "org.danilopianini.git-sensitive-semantic-versioning")
    apply(plugin = "eclipse")
    apply(plugin = "java-library")
    apply(plugin = "jacoco")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "checkstyle")
    apply(plugin = "pmd")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "project-report")
    apply(plugin = "build-dashboard")
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "org.danilopianini.publish-on-central")
    apply(plugin = "com.jfrog.bintray")

    gitSemVer {
        version = computeGitSemVer()
    }

    repositories {
        mavenCentral()
        maven(url = "https://dl.bintray.com/kotlin/dokka/")
    }

    val doclet by configurations.creating
    dependencies {
        compileOnly(Libs.spotbugs_annotations)
        testImplementation(Libs.junit)
        testImplementation(Libs.slf4j_api)
        testRuntimeOnly(Libs.logback_classic)
        doclet(Libs.apiviz)
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        failFast = true
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed", "standardError")
        }
    }

    spotbugs {
        effort = "max"
        reportLevel = "low"
        val excludeFile = File("${project.rootProject.projectDir}/config/spotbugs/excludes.xml")
        if (excludeFile.exists()) {
            excludeFilterConfig = project.resources.text.fromFile(excludeFile)
        }
    }

    tasks.withType<SpotBugsTask> {
        reports {
            xml.isEnabled = false
            html.isEnabled = true
        }
    }

    pmd {
        ruleSets = listOf()
        ruleSetConfig = resources.text.fromFile("${project.rootProject.projectDir}/config/pmd/pmd.xml")
    }

    ktlint {
        filter {
            exclude {
                it.file.path.toString().contains("protelis2kotlin")
            }
        }
    }

    tasks.withType<Javadoc> {
        isFailOnError = false
        options {
            val title = "Protelis ${project.version} Javadoc API"
            windowTitle(title)
            docletpath = doclet.files.toList()
            doclet("org.jboss.apiviz.APIviz")
            if (this is CoreJavadocOptions) {
                addBooleanOption("nopackagediagram", true)
            }
        }
    }

    publishOnCentral {
        fun String.fromProperties(): String = extra[this].toString()
        projectDescription.set("projectDescription".fromProperties())
        projectLongName.set("longName".fromProperties())
        licenseName.set("licenseName".fromProperties())
        licenseUrl.set("licenseUrl".fromProperties())
        projectUrl.set("http://www.protelis.org")
        scmConnection.set(scmUrl)
    }

    publishing.publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Danilo Pianini")
                        email.set("danilo.pianini@unibo.it")
                        url.set("http://www.danilopianini.org")
                    }
                    developer {
                        name.set("Jacob Beal")
                        email.set("jakebeal@gmail.com")
                        url.set("http://web.mit.edu/jakebeal/www/")
                    }
                    developer {
                        name.set("Matteo Francia")
                        email.set("matteo.francia2@studio.unibo.it")
                        url.set("https://github.com/w4bo")
                    }
                }
                contributors {
                    contributor {
                        name.set("Mirko Viroli")
                        email.set("mirko.viroli@unibo.it")
                        url.set("http://mirkoviroli.apice.unibo.it/")
                    }
                    contributor {
                        name.set("Kyle Usbeck")
                        email.set("kusbeck@bbn.com")
                        url.set("https://dist-systems.bbn.com/people/kusbeck/")
                    }
                }
            }
        }
    }
    /*
     * Use Bintray for beta releases
     */
    val apiKeyName = "BINTRAY_API_KEY"
    val userKeyName = "BINTRAY_USER"
    bintray {
        user = System.getenv(userKeyName)
        key = System.getenv(apiKeyName)
        override = true
        publishing.publications.withType<MavenPublication>().names.forEach {
            setPublications(it)
        }
        with(pkg) {
            repo = "Protelis"
            name = project.name
            userOrg = "protelis"
            vcsUrl = scmUrl
            setLicenses("GPL-3.0-or-later")
            with(version) {
                name = project.version.toString()
            }
        }
    }
    tasks.withType<BintrayUploadTask> {
        onlyIf {
            val hasKey = System.getenv(apiKeyName) != null
            val hasUser = System.getenv(userKeyName) != null
            if (!hasKey) {
                println("The $apiKeyName environment variable must be set in order for the bintray deployment to work")
            }
            if (!hasUser) {
                println("The $userKeyName environment variable must be set in order for the bintray deployment to work")
            }
            hasKey && hasUser
        }
    }
}

subprojects.forEach { subproject -> rootProject.evaluationDependsOn(subproject.path) }

repositories {
    mavenCentral()
    jcenter {
        content {
            includeGroupByRegex("""io\.github\.javaeden.*""")
            includeGroupByRegex("""com\.eden.*""")
            includeModuleByRegex("""org\.jetbrains\.kotlinx""", """kotlinx-serialization.*""")
        }
    }
}

dependencies {
    api(project(":protelis-interpreter"))
    api(project(":protelis-lang"))
    orchidRuntime("io.github.javaeden.orchid:OrchidEditorial:+")
    orchidRuntime(Libs.orchidbsdoc)
    orchidRuntime(Libs.orchidplugindocs)
    orchidRuntime(Libs.orchidsearch)
    orchidRuntime(Libs.orchidsyntaxhighlighter)
    orchidRuntime(Libs.orchidwiki)
    orchidRuntime(Libs.orchidgithub)
}

val isMarkedStable by lazy { """\d+(\.\d+){2}""".toRegex().matches(rootProject.version.toString()) }
orchid {
    theme = "BsDoc"
    // Determine whether it's a deployment or a dry run
    baseUrl = "https://protelis.github.io/wiki${ "-beta".takeUnless { isMarkedStable } ?: "" }'"
    // Fetch the latest version of the website, if this one is more recent enable deploy
    val versionRegex = """.*Currently\s*(.+)\.\s*Created""".toRegex()
    println("asdsdasdas")
    val matchedVersions: List<String> = try {
        URL(baseUrl).openConnection().getInputStream().use { stream ->
            listOf("")
            stream.bufferedReader().lineSequence()
                .flatMap { line -> versionRegex.find(line)?.groupValues?.last()?.let { sequenceOf(it) } ?: emptySequence() }
                .toList()
        }
    } catch (e: Exception) { emptyList() }
    val shouldDeploy = matchedVersions
        .takeIf { it.size == 1 }
        ?.first()
        ?.let { rootProject.version.toString() > it }
        ?: false
    dryDeploy = shouldDeploy.not().toString()
    println(
        when (matchedVersions.size) {
            0 -> "Unable to fetch the current site version from $baseUrl"
            1 -> "Website $baseUrl is at version ${matchedVersions.first()}"
            else -> "Multiple site versions fetched from $baseUrl: $matchedVersions"
        } + ". Orchid deployment ${if (shouldDeploy) "enabled" else "set as dry run"}."
    )
}

val orchidSeedConfiguration = "orchidSeedConfiguration"
tasks.register(orchidSeedConfiguration) {
    doLast {
        /*
         * Detect files
         */
        val configFolder = listOf(projectDir.toString(), "src", "orchid", "resources")
            .joinToString(separator = File.separator)
        val baseConfig = file("$configFolder${File.separator}config-origin.yml").readText()
        val finalConfig = file("$configFolder${File.separator}config.yml")
        /*
         * Compute Kdoc targets
         */
        val deploymentConfiguration = if (!baseConfig.contains("services:")) {
            """
                services:
                  publications:
                    stages:
                      - type: githubPages
                        username: 'DanySK'
                        commitUsername: Danilo Pianini
                        commitEmail: danilo.pianini@gmail.com
                        repo: 'Protelis/wiki${ "-beta".takeUnless { isMarkedStable } ?: "" }'
                        branch: master
                        publishType: CleanBranchMaintainHistory
            """.trimIndent()
        } else ""
        finalConfig.writeText(baseConfig + deploymentConfiguration)
    }
}
tasks.orchidClasses.orNull!!.dependsOn(tasks.getByName(orchidSeedConfiguration))


tasks.withType<Javadoc> {
    dependsOn(subprojects.map { it.tasks.javadoc })
    source(subprojects.map { it.tasks.javadoc.get().source })
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("${rootProject.name}-redist")
    isZip64 = true
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        // remove all signature files
        exclude("META-INF/")
        exclude("ant_tasks/")
        exclude("about_files/")
        exclude("help/about/")
        exclude("build")
        exclude(".gradle")
        exclude("build.gradle")
        exclude("gradle")
        exclude("gradlew")
        exclude("gradlew.bat")
    }
    with(tasks.jar.get() as CopySpec)
    dependsOn(subprojects.flatMap { it.tasks.withType<Jar>() })
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

/*
 * Work around for:

* What went wrong:
Execution failed for task ':buildDashboard'.
> Could not create task ':ktlintKotlinScriptCheck'.
   > Cannot change dependencies of configuration ':ktlint' after it has been resolved.

 */
tasks.withType<GenerateBuildDashboard>().forEach { it.dependsOn(tasks.ktlintKotlinScriptCheck) }
