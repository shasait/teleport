package de.hasait.teleport.domain;

import de.hasait.common.domain.SearchableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VolumeGroupRepository extends SearchableRepository<VolumeGroupPO, Long> {

    @Override
    @Query("SELECT r FROM VolumeGroupPO r WHERE r.name LIKE %:search% OR r.description LIKE %:search%")
    Page<VolumeGroupPO> search(String search, Pageable pageable);

    @Override
    @Query("SELECT COUNT(r) FROM VolumeGroupPO r WHERE r.name LIKE %:search% OR r.description LIKE %:search%")
    long searchCount(String search);

}
