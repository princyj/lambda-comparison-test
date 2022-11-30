import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.amazonaws.services.lambda.model.InvokeResult
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.system.measureTimeMillis

object LambdaInvoker {
        //Name of the lambda to be entered here
        private const val lambdaName = ""

        @JvmStatic
        fun main(args: Array<String>) {
                runWithValidPayLoadForFunction(lambdaName)
        }

        private fun runWithValidPayLoadForFunction(functionName: String) {
                val lam = AWSLambdaClientBuilder.standard().build()
                val path = System.getProperty("user.dir")
                val list = Files.readAllLines(
                        File("$path/src/main/kotlin/automation/payload.txt").toPath(),
                        Charset.defaultCharset()
                )

                val resultFile = "payload_${functionName}.csv"
                var fileOut = File("$path/src/main/kotlin/automation", resultFile)
                fileOut.delete()
                val elapsedTime = measureTimeMillis {
                        for (i in list) {
                                val payload = """
                                    {
                                        "": "${i}",
                                        "": "",
                                        "": true
                                        ...
                                    }
                                """.trimIndent()

                                val invokeReq = InvokeRequest().withFunctionName(functionName)
                                        .withPayload(payload)
                                invokeReq.setInvocationType(InvocationType.RequestResponse)
                                var lamResult: InvokeResult? = null
                                val elapsedSingleInvocationTime = measureTimeMillis {
                                        lamResult = lam.invoke(invokeReq)
                                }
                                println("Time taken to execute this request is $elapsedSingleInvocationTime ms")
                                val result = lamResult?.payload?.array()?.let { String(it, Charset.forName("UTF-8")) }

                                fileOut.createNewFile()
                                if (result != null) {
                                        fileOut.appendText(result)
                                }
                                fileOut.appendText("\n")
                        }
                }
                println("Time taken to execute $functionName is $elapsedTime ms")
        }

}