package com.tuckersoft.branchengine.event;

import lombok.Getter;

@Getter
public class DecisionCommittedEvent {
    private final Long decisionId;
    private final String simulateHeader;

    public DecisionCommittedEvent(Long decisionId, String simulateHeader) {
        this.decisionId = decisionId;
        this.simulateHeader = simulateHeader;
    }
}
