import java.util.Properties
import org.gradle.authentication.http.BasicAuthentication

val localProps = Properties().apply {
    val f = file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val mapboxDownloadsToken: String =
    localProps.getProperty("MAPBOX_DOWNLOADS_TOKEN")
        ?: System.getenv("MAPBOX_DOWNLOADS_TOKEN")
        ?: ""

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication { create<BasicAuthentication>("basic") }
            credentials {
                username = "mapbox"
                password = mapboxDownloadsToken
            }
        }
    }
}

rootProject.name = "StignIt"
include(":app")
