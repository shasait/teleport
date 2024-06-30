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

package de.hasait.common.ui;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import de.hasait.common.domain.SearchableRepository;
import de.hasait.common.domain.IdAndVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.stream.Stream;

public class JpaRepositoryDataProvider<PO extends IdAndVersion, R extends SearchableRepository<PO, ?>> extends AbstractBackEndDataProvider<PO, String> {

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
