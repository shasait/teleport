package de.hasait.teleport.domain;

import de.hasait.common.domain.SearchableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VolumeRepository extends SearchableRepository<VolumePO, Long> {

    @Override
    @Query("SELECT r FROM VolumePO r WHERE r.name LIKE %:search%")
    Page<VolumePO> search(String search, Pageable pageable);

    @Override
    @Query("SELECT COUNT(r) FROM VolumePO r WHERE r.name LIKE %:search%")
    long searchCount(String search);

}
