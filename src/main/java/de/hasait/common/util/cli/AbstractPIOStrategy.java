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

package de.hasait.common.util.cli;

public class AbstractPIOStrategy implements ProcessIOStrategy {

    private final ProcessBuilder.Redirect redirect;

    public AbstractPIOStrategy(ProcessBuilder.Redirect redirect) {
        this.redirect = redirect;
    }

    @Override
    public final ProcessBuilder.Redirect getRedirect() {
        return redirect;
    }

    @Override
    public void begin(Process process, ProcessFd fd) {
        // nop
    }

    @Override
    public void join(long millis) {
        // nop
    }

    @Override
    public void close(Process process, ProcessFd fd) {
        // nop
    }

    @Override
    public String appendToCommandToString() {
        return "";
    }

}
