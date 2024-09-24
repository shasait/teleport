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

package de.hasait.teleport.service.hv;

import de.hasait.teleport.api.VirtualMachineReferenceTO;
import de.hasait.teleport.service.action.AbstractAction;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;

public class StartVmAction extends AbstractAction<Void> {

    private final VirtualMachineReferenceTO vm;

    public StartVmAction(String hostName, String hvName, String vmName) {
        super("Start VM " + hostName + "/" + hvName + "/" + vmName);

        this.vm = new VirtualMachineReferenceTO(hostName, hvName, vmName);

        addUiBinding(vm.toFqName(), "Start"); // TODO I18N
        addUiBinding(vm.toFqName() + ".state", "Start"); // TODO I18N
        addUiBinding(vm.getHypervisor().toFqName(), "Start " + vmName); // TODO I18N
        addUiBinding(vm.getHypervisor().getHost().toFqName(), getDescription()); // TODO I18N
    }

    @Override
    public Void execute(ApplicationContext applicationContext, TransactionStatus transactionStatus) {
        applicationContext.getBean(HypervisorService.class).startVm(vm);
        return null;
    }

}
