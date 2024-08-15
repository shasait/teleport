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

package de.hasait.teleport.service.storage;

import de.hasait.teleport.api.VolumeReferenceTO;
import de.hasait.teleport.service.action.AbstractAction;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;

import java.util.Set;

public class TakeSnapshotAction extends AbstractAction<Void> {

    private final String snapshotName;

    private final Set<VolumeReferenceTO> volumes;

    public TakeSnapshotAction(String snapshotName, Set<VolumeReferenceTO> volumes) {
        super("Take snapshot " + snapshotName + " for " + volumes);

        this.snapshotName = snapshotName;
        this.volumes = volumes;
    }

    @Override
    public Void execute(ApplicationContext applicationContext, TransactionStatus transactionStatus) {
        applicationContext.getBean(StorageService.class).takeSnapshot(snapshotName, volumes.toArray(new VolumeReferenceTO[0]));
        return null;
    }

}
