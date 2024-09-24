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

import de.hasait.teleport.api.HostReferenceTO;
import de.hasait.teleport.api.VirtualMachineReferenceTO;
import de.hasait.teleport.service.action.AbstractAction;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;

public class FullSyncVmToOtherHvAction extends AbstractAction<Void> {

    private final VirtualMachineReferenceTO srcVm;

    private final HostReferenceTO tgtHost;

    public FullSyncVmToOtherHvAction(String srcHostName, String srcHvName, String srcVmName, String tgtHostName) {
        super("Full Sync VM " + srcHostName + "/" + srcHvName + "/" + srcVmName + " to " + tgtHostName);

        this.srcVm = new VirtualMachineReferenceTO(srcHostName, srcHvName, srcVmName);
        this.tgtHost = new HostReferenceTO(tgtHostName);

        addUiBinding(srcVm.toFqName(), "Full Sync to " + tgtHostName); // TODO I18N
        addUiBinding(srcVm.getHypervisor().toFqName(), "Full Sync of " + srcVm + " to " + tgtHostName); // TODO I18N
        addUiBinding(srcVm.getHypervisor().getHost().toFqName(), getDescription()); // TODO I18N
    }

    @Override
    public Void execute(ApplicationContext applicationContext, TransactionStatus transactionStatus) {
        applicationContext.getBean(HypervisorService.class).fullSyncVmToOtherHv(srcVm, tgtHost);
        return null;
    }

}
