package com.tuckersoft.branchengine.dto;

import java.util.List;

public record PagedDecisionResponse(
        List<DecisionResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int size
) {}
