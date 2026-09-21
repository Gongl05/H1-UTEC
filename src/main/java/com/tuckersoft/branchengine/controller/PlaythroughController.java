package com.tuckersoft.branchengine.controller;

import com.tuckersoft.branchengine.dto.PlaythroughPathResponse;
import com.tuckersoft.branchengine.dto.PlaythroughRequest;
import com.tuckersoft.branchengine.dto.PlaythroughResponse;
import com.tuckersoft.branchengine.service.PlaythroughService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/playthroughs")
@RequiredArgsConstructor
public class PlaythroughController {

    private final PlaythroughService playthroughService;

    @PostMapping
    public ResponseEntity<PlaythroughResponse> createPlaythrough(
            @Valid @RequestBody PlaythroughRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaythroughResponse response = playthroughService.createPlaythrough(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PlaythroughResponse>> getPlaythroughs(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PlaythroughResponse> response = playthroughService.getPlaythroughs(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaythroughResponse> getPlaythroughById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaythroughResponse response = playthroughService.getPlaythroughById(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<PlaythroughPathResponse> getPlaythroughPath(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaythroughPathResponse response = playthroughService.getPlaythroughPath(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
