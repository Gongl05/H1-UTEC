package com.tuckersoft.branchengine.dto;

import java.util.List;

public record PlaythroughPathResponse(
        Long playthroughId,
        String playerTag,
        String status,
        String endingCode,
        String startNodeCode,
        String currentNodeCode,
        List<PlaythroughPathStep> steps
) {}
