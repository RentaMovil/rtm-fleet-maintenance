package com.rentamovil.fleetmaintenance.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

// One page of results (PaginatedMeta in _shared.yaml)
@Getter
@AllArgsConstructor
public class PagedResult<T> {

    private List<T> items;
    private int page;
    private int limit;
    private long total;
    private int totalPages;

    public static <T> PagedResult<T> of(List<T> all, int page, int limit) {
        int from = Math.min((page - 1) * limit, all.size());
        int to = Math.min(from + limit, all.size());
        int totalPages = (int) Math.ceil((double) all.size() / limit);
        return new PagedResult<>(all.subList(from, to), page, limit, all.size(), totalPages);
    }
}
