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

package de.hasait.teleport.service.vm;

import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.HypervisorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HypervisorService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final HypervisorRepository repository;
    private final VirtualMachineDriverService driverService;

    public HypervisorService(HypervisorRepository repository, VirtualMachineDriverService driverService) {
        this.repository = repository;
        this.driverService = driverService;
    }

    public void refreshAll() {
        List<HypervisorPO> hypervisors = repository.findAll();
        for (HypervisorPO hypervisor : hypervisors) {
            driverService.refresh(hypervisor);
        }
    }

}
