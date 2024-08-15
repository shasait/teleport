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
import de.hasait.teleport.api.VolumeSnapshotReferenceTO;
import de.hasait.teleport.service.action.AbstractAction;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;

public class FullSyncVolumeAction extends AbstractAction<Void> {

    private final VolumeSnapshotReferenceTO srcVolumeSnapshot;

    private final VolumeReferenceTO tgtVolume;

    private final boolean replaceExisting;

    public FullSyncVolumeAction(String srcHostName, String srcStorageName, String srcVolumeName, String srcSnapshotName, String tgtHostName, String tgtStorageName, String tgtVolumeName, boolean replaceExisting) {
        super("Full Sync VM " + srcHostName + "/" + srcStorageName + "/" + srcVolumeName + "@" + srcSnapshotName + " to " + tgtHostName + "/" + tgtStorageName + "/" + tgtVolumeName);

        this.srcVolumeSnapshot = new VolumeSnapshotReferenceTO(srcHostName, srcStorageName, srcVolumeName, srcSnapshotName);
        this.tgtVolume = new VolumeReferenceTO(tgtHostName, tgtStorageName, tgtVolumeName);
        this.replaceExisting = replaceExisting;
    }

    @Override
    public Void execute(ApplicationContext applicationContext, TransactionStatus transactionStatus) {
        applicationContext.getBean(StorageService.class).fullSync(srcVolumeSnapshot, tgtVolume, replaceExisting);
        return null;
    }

}
