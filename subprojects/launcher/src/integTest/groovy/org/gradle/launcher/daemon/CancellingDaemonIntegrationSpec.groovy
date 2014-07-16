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

package org.gradle.launcher.daemon

import org.gradle.launcher.daemon.logging.DaemonMessages
import org.gradle.test.fixtures.ConcurrentTestUtil

import java.util.regex.Pattern

class CancellingDaemonIntegrationSpec extends DaemonIntegrationSpec {
    def "can handle multiple concurrent cancel requests"() {
        given:
        file('build.gradle') << '''
file('marker.txt') << 'waiting'
Thread.sleep(60000)
'''

        when:
        def build = executer.start()
        ConcurrentTestUtil.poll(20) { assert file('marker.txt').file }
        def buildCmdPattern = Pattern.compile('Dispatching Build\\{id=([0-9a-z\\-]+\\.1),')
        def buildCmdMatcher = buildCmdPattern.matcher(build.standardOutput)
        assert buildCmdMatcher.find()
        def buildId = buildCmdMatcher.group(1)

        def stopExecutions = []
        3.times { idx ->
            stopExecutions << executer.withArguments('--cancel=' + buildId, '--debug').start()
        }
        stopExecutions.each { it.waitForFinish(); println 'finished' }
        build.waitForFailure()
        def out = executer.withArguments('--cancel=' + buildId, '--debug').run().output

        then:
        out.contains(DaemonMessages.NO_DAEMONS_RUNNING)
    }

}
