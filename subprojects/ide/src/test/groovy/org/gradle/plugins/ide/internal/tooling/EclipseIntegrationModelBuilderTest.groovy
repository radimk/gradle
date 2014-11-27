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



package org.gradle.plugins.ide.internal.tooling

import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.tooling.model.eclipse.EclipseIntegrationModel
import org.gradle.tooling.model.eclipse.EclipseProject
import org.gradle.util.TestUtil
import spock.lang.Specification

class EclipseIntegrationModelBuilderTest extends Specification {
    def builder = new EclipseIntegrationModelBuilder()

    def "can build model"() {
        expect:
        builder.canBuild(EclipseIntegrationModel.name)
        !builder.canBuild(EclipseProject.name)
    }

    def "builds model"() {
        def project = TestUtil.builder().withName("root").build()
        def child1 = TestUtil.builder().withName("child1").withParent(project).build()
        child1.plugins.apply(EclipsePlugin)

        expect:
        def model = builder.buildAll("org.gradle.tooling.model.eclipse.EclipseIntegrationModel", project)
        model.getPreImportTasks() as List == ['eclipse']

        and:
        def model2 = builder.buildAll("org.gradle.tooling.model.eclipse.EclipseIntegrationModel", child1)
        model2.getPreImportTasks() as List == ['eclipse']
    }

    def "builds model without eclipse plugin"() {
        def project = TestUtil.builder().withName("root").build()

        expect:
        def model = builder.buildAll("org.gradle.tooling.model.eclipse.EclipseIntegrationModel", project)
        model.getPreImportTasks() as List == []
    }
}
