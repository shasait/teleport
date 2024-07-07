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

import de.hasait.teleport.api.VirtualMachineApi;
import de.hasait.teleport.domain.VirtualMachinePO;

public interface VirtualMachineService extends VirtualMachineApi {

    // State

    CanResult canStartVm(VirtualMachinePO virtualMachine);

    boolean startVm(VirtualMachinePO virtualMachine);

    CanResult canShutdownVm(VirtualMachinePO virtualMachine);

    boolean shutdownVm(VirtualMachinePO virtualMachine);

    boolean killVm(VirtualMachinePO virtualMachine);

    // Update

    CanResult canUpdateVm(VirtualMachinePO virtualMachine);

    boolean updateVm(VirtualMachinePO virtualMachine);

    // Delete

    CanResult canDeleteVm(VirtualMachinePO virtualMachine);

    boolean deleteVm(VirtualMachinePO virtualMachine);

    boolean fullDeleteVm(VirtualMachinePO virtualMachine);

}
