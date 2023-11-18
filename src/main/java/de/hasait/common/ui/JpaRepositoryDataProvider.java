package de.hasait.common.ui;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import de.hasait.common.domain.SearchableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.stream.Stream;

public class JpaRepositoryDataProvider<PO, R extends SearchableRepository<PO, ?>> extends AbstractBackEndDataProvider<PO, String> {

    private final R repository;

    public JpaRepositoryDataProvider(R repository) {
        this.repository = repository;
    }

    @Override
    protected Stream<PO> fetchFromBackEnd(Query<PO, String> query) {
        Sort last = null;
        for (QuerySortOrder so : query.getSortOrders()) {
            Sort curr = Sort.by(so.getSorted());
            if (so.getDirection() == SortDirection.ASCENDING) {
                curr = curr.ascending();
            } else {
                curr = curr.descending();
            }
            if (last != null) {
                last = last.and(curr);
            } else {
                last = curr;
            }
        }
        Sort sort = last == null ? Sort.unsorted() : last;
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getPageSize(), sort);
        Page<PO> page;
        Optional<String> filter = query.getFilter();
        if (filter.isPresent()) {
            page = repository.search(filter.get(), pageRequest);
        } else {
            page = repository.findAll(pageRequest);
        }
        return page.stream();
    }

    @Override
    protected final int sizeInBackEnd(Query<PO, String> query) {
        Optional<String> filter = query.getFilter();
        long count;
        if (filter.isPresent()) {
            count = repository.searchCount(filter.get());
        } else {
            count = repository.count();
        }
        return count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
    }

}
