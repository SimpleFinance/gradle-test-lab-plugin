import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class UploadApkTest {
    @get:Rule val projectDir = TemporaryFolder()

    lateinit var buildFile: File

    @Before
    fun setup() {
        buildFile = projectDir.newFile("build.gradle")
        buildFile.appendText("""
            plugins { id 'com.simple.gradle.testlab' }
            """)
    }

    @Test
    fun `empty task`() {
        buildFile.appendText("""
            import com.simple.gradle.testlab.tasks.UploadApk

            task uploadApk(type: UploadApk) {}
            """)
        val result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments(":tasks", "--all", "--stacktrace")
                .withPluginClasspath()
                .build()
        assert.that(result.output, containsSubstring("uploadApk"))
        assert.that(result.task(":tasks")?.outcome, equalTo(SUCCESS))
    }
}