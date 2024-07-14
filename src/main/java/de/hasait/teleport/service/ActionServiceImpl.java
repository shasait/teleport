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

package de.hasait.teleport.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class ActionServiceImpl implements ActionService {

    private final List<ActionContribution> actionContributions;

    private final ExecutorService executorService;

    public ActionServiceImpl(List<ActionContribution> actionContributions) {
        this.actionContributions = actionContributions;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @PreDestroy
    private void shutdown() {
        executorService.shutdown();
    }

    @Override
    public <R> Future<R> submit(Action<R> action) {
        return executorService.submit(action);
    }

    @Override
    @Transactional
    public List<Action<?>> determineActions() {
        List<Action<?>> actionList = new ArrayList<>();
        for (ActionContribution actionContribution : actionContributions) {
            actionContribution.contributeActions(actionList);
        }
        return actionList;
    }

}
