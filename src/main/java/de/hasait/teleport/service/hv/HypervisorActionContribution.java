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

import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.domain.VirtualMachineRepository;
import de.hasait.teleport.service.action.Action;
import de.hasait.teleport.service.action.ActionContribution;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HypervisorActionContribution implements ActionContribution {

    private final VirtualMachineRepository virtualMachineRepository;

    public HypervisorActionContribution(VirtualMachineRepository virtualMachineRepository) {
        this.virtualMachineRepository = virtualMachineRepository;
    }

    @Override
    public void contributeActions(List<Action<?>> actionList) {
        List<Object[]> fullSyncVms = virtualMachineRepository.findFullSyncVms();
        for (var fullSyncVm : fullSyncVms) {
            VirtualMachinePO srcVm = (VirtualMachinePO) fullSyncVm[0];
            String tgtHostName = (String) fullSyncVm[1];
            actionList.add(new FullSyncVmToOtherHvAction(srcVm.obtainHost().getName(), srcVm.obtainHypervisor().getName(), srcVm.getName(), tgtHostName));
        }

        List<VirtualMachinePO> vms = virtualMachineRepository.findAll();
        for (var vm : vms) {
            if (vm.stateIsShutOff()) {
                actionList.add(new StartVmAction(vm.obtainHost().getName(), vm.obtainHypervisor().getName(), vm.getName()));
            } else {
                actionList.add(new ShutdownVmAction(vm.obtainHost().getName(), vm.obtainHypervisor().getName(), vm.getName()));
                actionList.add(new KillVmAction(vm.obtainHost().getName(), vm.obtainHypervisor().getName(), vm.getName()));
            }
        }
    }

}
