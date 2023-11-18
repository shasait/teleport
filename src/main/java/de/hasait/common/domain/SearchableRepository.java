package de.hasait.common.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SearchableRepository<PO, ID> extends JpaRepository<PO, ID> {

    Page<PO> search(String search, Pageable pageable);

    long searchCount(String search);

}
