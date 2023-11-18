package de.hasait.teleport.domain;

import de.hasait.common.domain.SearchableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HypervisorRepository extends SearchableRepository<HypervisorPO, Long> {

    @Override
    @Query("SELECT r FROM HypervisorPO r WHERE r.name LIKE %:search% OR r.description LIKE %:search%")
    Page<HypervisorPO> search(String search, Pageable pageable);

    @Override
    @Query("SELECT COUNT(r) FROM HypervisorPO r WHERE r.name LIKE %:search% OR r.description LIKE %:search%")
    long searchCount(String search);

}
