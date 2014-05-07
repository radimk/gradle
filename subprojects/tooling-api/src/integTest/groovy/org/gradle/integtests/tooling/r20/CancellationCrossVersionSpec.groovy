/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests.tooling.r20

import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.integtests.tooling.fixture.ToolingApiVersion
import org.gradle.test.fixtures.ConcurrentTestUtil
import org.gradle.tooling.CancellationTokenSource
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.ResultHandler
import org.gradle.tooling.exceptions.BuildCancelledException

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// @ToolingApiVersion(">=2.0")
@ToolingApiVersion("current")
@TargetGradleVersion(">=1.0-milestone-8")
class CancellationCrossVersionSpec extends ToolingApiSpecification {
    def setup() {
        // in-process call does not support cancelling (yet)
        toolingApi.isEmbedded = false
        settingsFile << '''
rootProject.name = 'cancelling'
'''
    }

    @TargetGradleVersion(">=2.1")
    def "can cancel getModel"() {
        def marker = file("marker.txt")

        buildFile << """
task hang << {
    println "waiting"
    def marker = file('${marker.toURI()}')
    long timeout = System.currentTimeMillis() + 10000
    while (!marker.file && System.currentTimeMillis() < timeout) { Thread.sleep(200) }
    if (!marker.file) { throw new RuntimeException("Timeout waiting for marker file") }
    println "finished"
}
"""
        def cancel = new CancellationTokenSource()
        def resultHandler = new TestResultHandler()
        def output = new TestOutputStream()

        when:
        withConnection { ProjectConnection connection ->
            def build = connection.newBuild()
            build.forTasks('hang')
            build.withCancellationToken(cancel.token())
            build.run(resultHandler)
            ConcurrentTestUtil.poll(10) { output.toString().contains("waiting") }
            cancel.cancel()
            marker.text = 'go!'
            resultHandler.finished()
        }
        println resultHandler.failure
        resultHandler.failure.printStackTrace()

        then:
        // output.toString().contains("waiting")
        !output.toString().contains("finished")
        resultHandler.failure instanceof GradleConnectionException
    }


    @TargetGradleVersion(">=2.1")
    def "early cancel stops the build before beginning"() {
        def marker = file("marker.txt")

        buildFile << """
task hang << {
    println "waiting"
    def marker = file('${marker.toURI()}')
    long timeout = System.currentTimeMillis() + 10000
    while (!marker.file && System.currentTimeMillis() < timeout) { Thread.sleep(200) }
    if (!marker.file) { throw new RuntimeException("Timeout waiting for marker file") }
    println "finished"
}
"""
        def cancel = new CancellationTokenSource()
        def resultHandler = new TestResultHandler()
        def output = new TestOutputStream()

        when:
        cancel.cancel()
        withConnection { ProjectConnection connection ->
            def build = connection.newBuild()
            build.forTasks('hang')
            build.withCancellationToken(cancel.token())
            build.run(resultHandler)
            resultHandler.finished()
        }
        then:
        resultHandler.failure instanceof BuildCancelledException
        !output.toString().contains("waiting")
        !output.toString().contains("finished")
    }

    def "can cancel build"() {
    }
    def "can cancel action"() {
    }

    class TestResultHandler implements ResultHandler<Object> {
        final latch = new CountDownLatch(1)
        def failure

        void onComplete(Object result) {
            latch.countDown()
        }

        void onFailure(GradleConnectionException failure) {
            this.failure = failure
            latch.countDown()
        }

        // TODO matcher on expected exception
        def finished() {
            latch.await(10, TimeUnit.SECONDS)
            if (failure == null) {
                assert false
            }
        }
    }

    class TestOutputStream extends OutputStream {
        final buffer = new ByteArrayOutputStream()

        @Override
        void write(int b) throws IOException {
            synchronized (buffer) {
                buffer.write(b)
            }
        }

        @Override
        String toString() {
            synchronized (buffer) {
                return buffer.toString()
            }
        }
    }
}
