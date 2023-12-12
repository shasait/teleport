package de.hasait.teleport.domain;

import de.hasait.common.domain.SearchableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HostConnectConfigRepository extends SearchableRepository<HostConnectConfigPO, Long> {

    @Override
    @Query("SELECT r FROM HostConnectConfigPO r WHERE r.fromName LIKE %:search% OR r.toName LIKE %:search%")
    Page<HostConnectConfigPO> search(String search, Pageable pageable);

    @Override
    @Query("SELECT COUNT(r) FROM HostConnectConfigPO r WHERE r.fromName LIKE %:search% OR r.toName LIKE %:search%")
    long searchCount(String search);

}
