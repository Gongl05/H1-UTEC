package com.tuckersoft.branchengine.node;

import com.tuckersoft.branchengine.common.exception.ConflictException;
import com.tuckersoft.branchengine.common.exception.NotFoundException;
import com.tuckersoft.branchengine.node.dto.StoryNodeRequest;
import com.tuckersoft.branchengine.node.dto.StoryNodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryNodeService {

    private final StoryNodeRepository storyNodeRepository;

    @Transactional
    public StoryNodeResponse crear(StoryNodeRequest peticion) {
        if (storyNodeRepository.existsByNodeCode(peticion.nodeCode())) {
            throw new ConflictException("Ya existe un nodo con el codigo " + peticion.nodeCode() + ".");
        }

        StoryNode nodo = new StoryNode();
        nodo.setNodeCode(peticion.nodeCode());
        nodo.setTitle(peticion.title());
        nodo.setSceneText(peticion.sceneText());
        nodo.setBranchCapacity(peticion.branchCapacity());
        nodo.setCurrentBranches(0);
        nodo.setPrimaryBranchCode(peticion.primaryBranchCode());
        nodo.setGlitchBranchCode(peticion.glitchBranchCode());
        nodo.setCreatedAt(Instant.now());

        return StoryNodeResponse.de(storyNodeRepository.save(nodo));
    }

    @Transactional(readOnly = true)
    public List<StoryNodeResponse> listar() {
        return storyNodeRepository.findAllByOrderByIdAsc().stream().map(StoryNodeResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public StoryNodeResponse porId(Long id) {
        return StoryNodeResponse.de(buscar(id));
    }

    @Transactional(readOnly = true)
    public StoryNode buscar(Long id) {
        return storyNodeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe un nodo con id " + id + "."));
    }
}
