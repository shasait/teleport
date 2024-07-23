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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VmCreateTO implements Serializable {

    private String name;

    private String description;

    private int cores;

    private int memMb;

    private boolean memHugePages;

    private String videoModel;

    private int vgaMemKb;

    private final List<VmCreateVolumeTO> volumes = new ArrayList<>();

    private final List<VmCreateNetIfTO> networkInterfaces = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public int getMemMb() {
        return memMb;
    }

    public void setMemMb(int memMb) {
        this.memMb = memMb;
    }

    public boolean isMemHugePages() {
        return memHugePages;
    }

    public void setMemHugePages(boolean memHugePages) {
        this.memHugePages = memHugePages;
    }

    public String getVideoModel() {
        return videoModel;
    }

    public void setVideoModel(String videoModel) {
        this.videoModel = videoModel;
    }

    public int getVgaMemKb() {
        return vgaMemKb;
    }

    public void setVgaMemKb(int vgaMemKb) {
        this.vgaMemKb = vgaMemKb;
    }

    public List<VmCreateVolumeTO> getVolumes() {
        return volumes;
    }

    public List<VmCreateNetIfTO> getNetworkInterfaces() {
        return networkInterfaces;
    }

}
