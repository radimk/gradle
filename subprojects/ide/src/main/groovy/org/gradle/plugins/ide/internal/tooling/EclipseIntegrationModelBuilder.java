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

package org.gradle.plugins.ide.internal.tooling;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.internal.tooling.eclipse.DefaultEclipseIntegration;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

public class EclipseIntegrationModelBuilder implements ToolingModelBuilder {

    public boolean canBuild(String modelName) {
        return modelName.equals("org.gradle.tooling.model.eclipse.EclipseIntegrationModel");
    }

    public DefaultEclipseIntegration buildAll(String modelName, Project project) {
        if (!modelName.equals("org.gradle.tooling.model.eclipse.EclipseIntegrationModel")) {
            throw new GradleException("Unexpected call to build model " + modelName);
        }

        return new DefaultEclipseIntegration(hasEclipsePluginApplied(project.getRootProject()));
    }

    private boolean hasEclipsePluginApplied(Project project) {
        if (project.getPlugins().hasPlugin(EclipsePlugin.class)) {
            return true;
        }
        for (Project child : project.getSubprojects()) {
            if (hasEclipsePluginApplied(child)) {
                return true;
            }
        }
        return false;
    }
}
