tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}

dependencies {
    implementation(projects.modules.application)
    implementation(projects.modules.domain)
    implementation(projects.modules.common)
    implementation(libs.spring.boot.starter.web)
    testImplementation(libs.spring.boot.starter.test) {
        exclude(module = "mockito-core")
    }
}
