package com.tuckersoft.branchengine.node;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Una escena de Bandersnatch.
 *
 * primaryBranchCode y glitchBranchCode son Strings, NO llaves foraneas: los nodos se
 * crean en cualquier orden y pueden apuntar a escenas que todavia no existen. La
 * resolucion ocurre al decidir, no al crear.
 */
@Entity
@Table(name = "story_node")
@Getter
@Setter
public class StoryNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nodeCode;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sceneText;

    @Column(nullable = false)
    private Integer branchCapacity;

    @Column(nullable = false)
    private Integer currentBranches;

    private String primaryBranchCode;

    private String glitchBranchCode;

    @Column(nullable = false)
    private Instant createdAt;

    /** Un nodo lleno ya no admite que arranquen mas partidas en el. */
    public boolean estaLleno() {
        return currentBranches >= branchCapacity;
    }
}
