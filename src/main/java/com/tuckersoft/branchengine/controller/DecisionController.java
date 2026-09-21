package com.tuckersoft.branchengine.controller;

import com.tuckersoft.branchengine.dto.DecisionRequest;
import com.tuckersoft.branchengine.dto.DecisionResponse;
import com.tuckersoft.branchengine.dto.PagedDecisionResponse;
import com.tuckersoft.branchengine.dto.RealityLogResponse;
import com.tuckersoft.branchengine.service.DecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/decisions")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    @PostMapping
    public ResponseEntity<DecisionResponse> createDecision(
            @RequestHeader(value = "X-Bandersnatch-Simulate", required = false) String simulateHeader,
            @Valid @RequestBody DecisionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        DecisionResponse response = decisionService.createDecision(request, userDetails.getUsername(), simulateHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedDecisionResponse> getDecisions(
            @RequestParam(required = false) String branchType,
            @RequestParam(required = false) String impactLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long playthroughId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        PagedDecisionResponse response = decisionService.getDecisions(
                branchType, impactLevel, status, playthroughId, page, size, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DecisionResponse> getDecisionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        DecisionResponse response = decisionService.getDecisionById(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/reality-logs")
    public ResponseEntity<List<RealityLogResponse>> getRealityLogsByDecisionId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<RealityLogResponse> response = decisionService.getRealityLogsByDecisionId(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
