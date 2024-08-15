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

package de.hasait.teleport.api;

import de.hasait.teleport.service.CanResult;

import java.util.List;

public interface HypervisorApi {

    // Create

    List<HostReferenceTO> canCreateVm(VmCreateTO vmCreateTO);

    CanResult canCreateVm(HostReferenceTO hostReferenceTO, VmCreateTO vmCreateTO);

    VirtualMachineTO createVm(HostReferenceTO hostReferenceTO, VmCreateTO vmCreateTO);

    // State

    VirtualMachineTO getVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    CanResult canStartVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    boolean startVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    CanResult canShutdownVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    boolean shutdownVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    boolean shutdownVm(String virtualMachineName);

    CanResult canKillVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    boolean killVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    // Update

    CanResult canUpdateVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    boolean updateVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    // Delete

    CanResult canDeleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    boolean deleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    CanResult canFullDeleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    boolean fullDeleteVm(VirtualMachineReferenceTO virtualMachineReferenceTO);

    // Sync

    void fullSyncVmToOtherHv(VirtualMachineReferenceTO srcVmTO, HostReferenceTO tgtHostTO);

}
