package de.hasait.teleport.domain;

import de.hasait.common.domain.SearchableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends SearchableRepository<StoragePO, Long> {

    @Override
    @Query("SELECT r FROM StoragePO r WHERE r.name LIKE %:search% OR r.description LIKE %:search%")
    Page<StoragePO> search(String search, Pageable pageable);

    @Override
    @Query("SELECT COUNT(r) FROM StoragePO r WHERE r.name LIKE %:search% OR r.description LIKE %:search%")
    long searchCount(String search);

}
