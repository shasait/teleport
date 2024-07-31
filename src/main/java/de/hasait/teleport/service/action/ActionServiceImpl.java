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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ActionServiceImpl implements ActionService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;
    private TransactionTemplate transactionTemplate;
    private final List<ActionContribution> actionContributions;

    private final LinkedBlockingQueue<ActionFutureTask<?>> workQueue;
    private final AtomicReference<ActionFutureTask<?>> executingAction;
    private final AtomicBoolean threadShouldRun;
    private final Thread thread;

    public ActionServiceImpl(ApplicationContext applicationContext, TransactionTemplate transactionTemplate, List<ActionContribution> actionContributions) {
        this.applicationContext = applicationContext;
        this.transactionTemplate = transactionTemplate;
        this.actionContributions = actionContributions;
        this.workQueue = new LinkedBlockingQueue<>();
        this.executingAction = new AtomicReference<>();
        this.threadShouldRun = new AtomicBoolean(true);
        this.thread = new Thread(this::loop, getClass().getSimpleName());
    }

    private void loop() {
        log.info("Starting action execution loop...");
        try {
            while (threadShouldRun.get()) {
                ActionFutureTask<?> actionFutureTask;
                try {
                    actionFutureTask = workQueue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                executingAction.set(actionFutureTask);
                actionFutureTask.setRunning(true);
                try {
                    actionFutureTask.run();
                } catch (RuntimeException e) {
                    log.warn("Unexpected exception", e);
                }
                actionFutureTask.setRunning(false);
                executingAction.set(null);
                try {
                    actionFutureTask.get();
                } catch (InterruptedException e) {
                    // unexpected as get should never block because we wait for run to finish
                    log.warn("Unexpected exception", e);
                } catch (ExecutionException e) {
                    log.error("Action failed: {}", actionFutureTask.getAction().getDescription(), e);
                }
            }
        } finally {
            log.info("Action execution loop finished");
        }
    }

    @PostConstruct
    private void init() {
        thread.start();
    }

    @PreDestroy
    private void shutdown() {
        log.info("Shutting down...");
        threadShouldRun.set(false);
        while (thread.isAlive()) {
            thread.interrupt();
            log.info("Waiting for action execution loop...");
            try {
                thread.join(5000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        log.info("Shutdown finished");
    }

    @Override
    public <R> Future<R> submit(Action<R> action) {
        ActionFutureTask<R> actionFutureTask = new ActionFutureTask<>(applicationContext, transactionTemplate, action);
        workQueue.add(actionFutureTask);
        return actionFutureTask;
    }

    @Override
    @Transactional
    public List<Action<?>> determinePossibleActions() {
        List<Action<?>> actionList = new ArrayList<>();
        for (ActionContribution actionContribution : actionContributions) {
            actionContribution.contributeActions(actionList);
        }
        return actionList;
    }

    @Override
    public List<ActionFutureTask<?>> determineQueuedAndExecutingActions() {
        List<ActionFutureTask<?>> result = new ArrayList<>();
        getExecutingAction().ifPresent(result::add);
        result.addAll(workQueue);
        return result;
    }

    @Override
    public List<ActionFutureTask<?>> determineQueuedActions() {
        return new ArrayList<>(workQueue);
    }

    @Override
    public Optional<ActionFutureTask<?>> getExecutingAction() {
        return Optional.ofNullable(executingAction.get());
    }

}
