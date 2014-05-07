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

package org.gradle.tooling.internal.protocol;

import org.gradle.tooling.internal.protocol.exceptions.InternalUnsupportedBuildArgumentException;

/**
 * Mixed into a provider connection, to allow tooling models to be requested by the client.
 *
 * <p>DO NOT CHANGE THIS INTERFACE - it is part of the cross-version protocol.
 *
 * <p>Consumer compatibility: This interface is used by all consumer versions from 1.6-rc-1.</p>
 * <p>Provider compatibility: This interface is implemented by all provider versions from 1.6-rc-1.</p>
 *
 * @since 1.6-rc-1
 * @see org.gradle.tooling.internal.protocol.ConnectionVersion4
 */
public interface CancellableModelBuilder extends InternalProtocolInterface {
    /**
     * Performs some action against a build and returns the requested model.
     *
     * <p>Consumer compatibility: This method is used by all consumer versions from 2.0-rc-1.</p>
     * <p>Provider compatibility: This method is implemented by all provider versions from 2.0-rc-1.</p>
     *
     * @param modelIdentifier The identifier of the model to build.
     * @param cancellationToken The token to propagate cancellation.
     * @throws BuildExceptionVersion1 On build failure.
     * @throws InternalUnsupportedModelException When the requested model is not supported.
     * @throws InternalUnsupportedBuildArgumentException When the specified command-line options are not supported.
     * @throws IllegalStateException When this connection has been stopped.
     * @since 1.6-rc-1
     */
    BuildResult<?> getModel(ModelIdentifier modelIdentifier, InternalCancellationToken cancellationToken,
                            BuildParameters operationParameters) throws
            BuildExceptionVersion1,
            InternalUnsupportedModelException,
            InternalUnsupportedBuildArgumentException,
            IllegalStateException;
}
