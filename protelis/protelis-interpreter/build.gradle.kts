java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api("net.sf.trove4j:trove4j:_")
    api("org.apache.commons:commons-math3:_")
    api("com.google.guava:guava:_")
    api("org.protelis:protelis.parser:_")
    implementation("commons-codec:commons-codec:_")
    implementation("commons-io:commons-io:_")
    implementation("de.ruedigermoeller:fst:_")
    implementation("org.apache.commons:commons-lang3:_")
    implementation("org.slf4j:slf4j-api:_")
}

eclipse {
    classpath {
        isDownloadSources = true
    }
}
