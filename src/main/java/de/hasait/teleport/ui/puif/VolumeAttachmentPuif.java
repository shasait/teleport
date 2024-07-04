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

package de.hasait.teleport.ui.puif;

import de.hasait.common.ui.puif.AbstractToOnePuiFactory;
import de.hasait.teleport.domain.VolumeAttachmentPO;
import de.hasait.teleport.domain.VolumeAttachmentRepository;
import org.springframework.stereotype.Service;

@Service
public class VolumeAttachmentPuif extends AbstractToOnePuiFactory<VolumeAttachmentPO, VolumeAttachmentRepository, Void> {

    public VolumeAttachmentPuif(VolumeAttachmentRepository repository) {
        super(VolumeAttachmentPO.class, () -> null, repository);
    }

    @Override
    protected String getPoLabel(VolumeAttachmentPO po) {
        return po.obtainHost().getName() + "/" + po.obtainVirtualMachine().getName() + " -> " + po.obtainStorage().getName() + "/" + po.obtainVolume().getName();
    }

}
