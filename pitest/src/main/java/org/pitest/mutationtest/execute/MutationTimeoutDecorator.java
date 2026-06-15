/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.execute;

import org.pitest.extension.common.TestUnitDecorator;
import org.pitest.functional.SideEffect;
import org.pitest.mutationtest.TimeoutLengthStrategy;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public final class MutationTimeoutDecorator extends TestUnitDecorator {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final TimeoutLengthStrategy timeOutStrategy;
    private final SideEffect timeOutSideEffect;
    private final long executionTime;

    public MutationTimeoutDecorator(final TestUnit child,
                                    final SideEffect timeOutSideEffect,
                                    final TimeoutLengthStrategy timeStrategy, final long executionTime) {
        super(child);
        this.timeOutSideEffect = timeOutSideEffect;
        this.executionTime = executionTime;
        this.timeOutStrategy = timeStrategy;
    }

    @Override
    public void execute(final ResultCollector rc) {

        final long maxTime = this.timeOutStrategy
                .getAllowedTime(this.executionTime);
        Future<?> timeout = createFuture(maxTime, this.timeOutSideEffect);
        try {
            child().execute(rc);
        } catch (final Throwable ex) {
            rc.notifyEnd(child().getDescription(), ex);
        } finally {
            timeout.cancel(true);
        }
    }

    public Future<?> createFuture(long limit, SideEffect sideEffect) {
        FutureTask<Void> futureTask = new FutureTask<>(() -> {
            try {
                Thread.sleep(limit);
                sideEffect.apply();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        });

        return EXECUTOR.submit(futureTask);
    }
}

