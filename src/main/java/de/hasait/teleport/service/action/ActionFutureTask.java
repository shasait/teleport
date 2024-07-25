/*
 * Copyright (C) 2024 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.teleport.service.action;

import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActionFutureTask<R> extends FutureTask<R> {

    private final Action<R> action;

    private final AtomicBoolean running = new AtomicBoolean();

    public ActionFutureTask(TransactionTemplate transactionTemplate, Action<R> action) {
        super(() -> transactionTemplate.execute(action));

        this.action = action;
    }

    public Action<R> getAction() {
        return action;
    }

    public boolean isRunning() {
        return running.get();
    }

    void setRunning(boolean running) {
        this.running.set(running);
    }

}
