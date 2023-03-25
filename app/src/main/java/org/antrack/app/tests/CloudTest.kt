package org.antrack.app.tests

import org.antrack.app.Env
import org.antrack.app.cloud.Cloud
import org.antrack.app.cloud.CloudFileMetadata
import org.antrack.app.functions.sleepS
import org.antrack.app.functions.touch
import java.io.File

class CloudTest: Test {
    private val testFile = File(Env.testingTempFilePath)

    override fun before() {
        testFile.delete()
    }
    override fun after() {
        testFile.delete()
    }

    override fun run(): List<String> {
        return testFileUpload() + testFileDownload()
    }

    private fun testFileUpload(): List<String> {
        testFile.touch()
        testFile.writeText(genRandomString())

        val meta1 = Cloud.getMetadata(Env.cloudTestingTempFilePath) as? CloudFileMetadata
        val modTime1 = meta1?.lastModified ?: -1

        sleepS(5)

        val meta2 = Cloud.getMetadata(Env.cloudTestingTempFilePath) as? CloudFileMetadata
        val modTime2 = meta2?.lastModified ?: -2

        return if (modTime2 > modTime1) {
            listOf("File upload test passed: true")
        } else {
            listOf(
                "File upload test passed: false",
                "Old cloud file date: $modTime1",
                "New cloud file date: $modTime2",
            )
        }
    }

    private fun testFileDownload(): List<String> {
        val expectedString = genRandomString()

        Cloud.putFile(expectedString.byteInputStream(), Env.cloudCtlPath)

        sleepS(5)

        val resultString = try {
            File(Env.ctlFilePath).readText()
        } catch (e: Exception) {
            "empty"
        }

        return if (expectedString == resultString) {
            listOf("File download test passed: true")
        } else {
            listOf(
                "File upload test passed: false",
                "Expected: $expectedString",
                "Actual: $resultString",
            )
        }
    }
}