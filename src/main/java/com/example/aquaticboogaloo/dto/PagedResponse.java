package com.example.aquaticboogaloo.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponse<T> (
        List<T> content,
        int size,
        int page,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getSize(),
                page.getNumber(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
