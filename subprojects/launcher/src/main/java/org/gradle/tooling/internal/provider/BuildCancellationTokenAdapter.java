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

package org.gradle.tooling.internal.provider;

import org.gradle.launcher.exec.BuildCancellationToken;
import org.gradle.tooling.internal.protocol.InternalCancellationToken;

import java.util.concurrent.Callable;

public class BuildCancellationTokenAdapter implements BuildCancellationToken {
    private final InternalCancellationToken internalCancellationToken;

    public BuildCancellationTokenAdapter(InternalCancellationToken internalCancellationToken) {
        this.internalCancellationToken = internalCancellationToken;
    }

    public boolean canBeCancelled() {
        return internalCancellationToken.canBeCancelled();
    }

    public boolean isCancellationRequested() {
        return internalCancellationToken.isCancellationRequested();
    }

    public boolean addCallback(Callable<Boolean> cancellationHandler) {
        return internalCancellationToken.addCallback(cancellationHandler);
    }
}
