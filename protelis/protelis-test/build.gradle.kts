dependencies {
    api("junit:junit:_")
    api(project(":protelis-interpreter"))
    implementation("it.unibo.alchemist:alchemist-interfaces:_") {
        exclude(module = "asm-debug-all")
    }
    implementation("it.unibo.alchemist:alchemist-loading:_") {
        exclude(module = "asm-debug-all")
    }
    implementation("org.apache.commons:commons-lang3:_")
    implementation("io.github.classgraph:classgraph:_")
}
