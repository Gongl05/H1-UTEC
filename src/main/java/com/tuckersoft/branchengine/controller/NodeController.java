package com.tuckersoft.branchengine.controller;

import com.tuckersoft.branchengine.dto.NodeRequest;
import com.tuckersoft.branchengine.dto.NodeResponse;
import com.tuckersoft.branchengine.service.NodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nodes")
@RequiredArgsConstructor
public class NodeController {

    private final NodeService nodeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NodeResponse> createNode(@Valid @RequestBody NodeRequest request) {
        NodeResponse response = nodeService.createNode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NodeResponse> getNodeById(@PathVariable Long id) {
        NodeResponse response = nodeService.getNodeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<NodeResponse>> getAllNodes() {
        List<NodeResponse> response = nodeService.getAllNodes();
        return ResponseEntity.ok(response);
    }
}
