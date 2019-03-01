import org.gradle.api.Project

class Meta(project: Project) {
    private val snapshot: String by project.properties.default("true")
    val bintrayUser: String by project.properties.default("")
    val bintrayKey: String by project.properties.default("")
    val nexusUsername: String by project.properties.default("")
    val nexusPassword: String by project.properties.default("")

    val name = "gradle-test-lab-plugin"
    val displayName = "Gradle plugin for Firebase Test Lab"
    val description = "Run Firebase Test Lab tests directly from Gradle"
    val url = "https://github.com/SimpleFinance/gradle-test-lab-plugin"
    val git = "$url.git"
    val groupId = "com.simple.gradle.testlab"
    val artifactId = "test-lab-plugin"
    val baseVersion = "0.3.1"
    val pluginId = groupId

    val isSnapshot: Boolean = snapshot.toBoolean()
    val version = if (isSnapshot) "$baseVersion-SNAPSHOT" else baseVersion
    val gitTag = if (isSnapshot) "master" else "v$baseVersion"
}

private val cache = mutableMapOf<String, Meta>()

val Project.meta: Meta get() = cache.getOrPut(project.path) { Meta(project) }

@Suppress("UNCHECKED_CAST")
private fun <K, V> Map<K, V>.default(value: Any?) = withDefault { value as V }
