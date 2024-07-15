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

import de.hasait.common.service.AbstractProviderService;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.HypervisorRepository;
import de.hasait.teleport.domain.VirtualMachinePO;
import de.hasait.teleport.spi.vm.HypervisorDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HypervisorServiceImpl extends AbstractProviderService<HypervisorDriver> implements HypervisorService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final HypervisorRepository repository;

    public HypervisorServiceImpl(HypervisorRepository repository, HypervisorDriver[] drivers) {
        super(drivers);

        this.repository = repository;
    }

    @Override
    @Transactional
    public void refreshAll() {
        List<HypervisorPO> hypervisors = repository.findAll();
        for (HypervisorPO hypervisor : hypervisors) {
            try {
                refresh(hypervisor);
            } catch (RuntimeException e) {
                log.warn("Refresh failed for {}", hypervisor, e);
            }
        }
    }

    @Override
    @Transactional
    public void refresh(HypervisorPO hypervisor) {
        HypervisorDriver driver = getProviderByIdNotNull(hypervisor.getDriver());
        driver.refresh(hypervisor);
        hypervisor.setLastSeen(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void fullSyncVmToOtherHv(String srcHostName, String srcHvName, String srcVmName, String tgtHostName) {
        HypervisorPO srcHv = repository.findByHostAndName(srcHostName, srcHvName).orElseThrow();
        VirtualMachinePO srcVm = srcHv.findVirtualMachineByName(srcVmName).orElseThrow();

        HypervisorPO tgtHv = repository.findByHostAndName(tgtHostName, srcHvName).orElseThrow();
        VirtualMachinePO existingTgtVm = tgtHv.findVirtualMachineByName(srcVmName).orElse(null);
        if (existingTgtVm != null) {
            throw new IllegalArgumentException("Target VM already exists");
        }

        // TODO
    }

}
