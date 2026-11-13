import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "com.ayinbox"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all,-serial,-processing"))
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

val examples: SourceSet by sourceSets.creating {
    java.srcDir("examples")
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath
    runtimeClasspath += output + compileClasspath
}

tasks.register<JavaExec>("listCampaigns") {
    group = "examples"
    classpath = examples.runtimeClasspath
    mainClass.set("ListCampaigns")
}

tasks.register<JavaExec>("verifyWebhook") {
    group = "examples"
    classpath = examples.runtimeClasspath
    mainClass.set("VerifyWebhook")
}

tasks.named("check") {
    dependsOn("compileExamplesJava", "javadoc")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.Javadoc(), sourcesJar = true))
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
    coordinates("com.ayinbox", "ayinbox-java", version.toString())
    pom {
        name.set("ayinbox-java")
        description.set("Java client for the AY Inbox REST API")
        url.set("https://github.com/ayinbox/ayinbox-java")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("ayinbox")
                name.set("AY Inbox")
                email.set("team@ayinbox.com")
            }
        }
        scm {
            url.set("https://github.com/ayinbox/ayinbox-java")
            connection.set("scm:git:https://github.com/ayinbox/ayinbox-java.git")
            developerConnection.set("scm:git:ssh://git@github.com/ayinbox/ayinbox-java.git")
        }
    }
}
