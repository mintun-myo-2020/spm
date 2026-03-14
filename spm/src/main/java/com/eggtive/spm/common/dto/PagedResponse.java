package com.eggtive.spm.common.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record PagedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    public static <T> PagedResponse<T> from(Page<?> p, List<T> content) {
        return new PagedResponse<>(content, p.getNumber(), p.getSize(),
            p.getTotalElements(), p.getTotalPages(), p.isFirst(), p.isLast());
    }
}
