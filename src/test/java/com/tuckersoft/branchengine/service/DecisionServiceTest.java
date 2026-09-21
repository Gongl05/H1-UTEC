package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.DecisionRequest;
import com.tuckersoft.branchengine.dto.DecisionResponse;
import com.tuckersoft.branchengine.entity.Decision;
import com.tuckersoft.branchengine.entity.Playthrough;
import com.tuckersoft.branchengine.entity.StoryNode;
import com.tuckersoft.branchengine.entity.User;
import com.tuckersoft.branchengine.event.DecisionCommittedEvent;
import com.tuckersoft.branchengine.repository.DecisionRepository;
import com.tuckersoft.branchengine.repository.PlaythroughRepository;
import com.tuckersoft.branchengine.repository.RealityLogRepository;
import com.tuckersoft.branchengine.repository.StoryNodeRepository;
import com.tuckersoft.branchengine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private PlaythroughRepository playthroughRepository;
    @Mock
    private StoryNodeRepository storyNodeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RealityLogRepository realityLogRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DecisionService decisionService;

    private User testUser;
    private StoryNode nodeOrigen;
    private StoryNode nodeGlitch;
    private Playthrough testPlaythrough;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("stefan@tuckersoft.co.uk")
                .displayName("Stefan Butler")
                .role("ROLE_USER")
                .createdAt(Instant.now())
                .build();

        nodeOrigen = StoryNode.builder()
                .id(10L)
                .nodeCode("NODE-ORIGEN")
                .title("Origen")
                .sceneText("Escena de inicio de prueba")
                .branchCapacity(5)
                .currentBranches(1)
                .primaryBranchCode("NODE-BUS")
                .glitchBranchCode("NODE-ESPEJO")
                .createdAt(Instant.now())
                .build();

        nodeGlitch = StoryNode.builder()
                .id(11L)
                .nodeCode("NODE-ESPEJO")
                .title("Espejo")
                .sceneText("Escena de fallo o espejo")
                .branchCapacity(5)
                .currentBranches(0)
                .primaryBranchCode("NODE-ORIGEN")
                .glitchBranchCode("NODE-ESPEJO")
                .createdAt(Instant.now())
                .build();

        testPlaythrough = Playthrough.builder()
                .id(100L)
                .playerTag("STEFAN-TEST")
                .user(testUser)
                .startNodeCode("NODE-ORIGEN")
                .currentNode(nodeOrigen)
                .lucidity(100)
                .controlLevel(0)
                .status("ACTIVA")
                .endingCode(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("1. 'Stefan destruye la camara' clasifica como RUPTURA_CUARTA_PARED (precedencia de reglas)")
    void test1_precedenciaReglaCamara() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(playthroughRepository.findById(100L)).thenReturn(Optional.of(testPlaythrough));
        when(storyNodeRepository.findByNodeCode("NODE-ESPEJO")).thenReturn(Optional.of(nodeGlitch));
        when(decisionRepository.save(any(Decision.class))).thenAnswer(i -> {
            Decision d = i.getArgument(0);
            d.setId(1L);
            return d;
        });

        DecisionRequest request = new DecisionRequest(100L, "Stefan destruye la camara que lo estaba grabando.", "LEVE");
        DecisionResponse response = decisionService.createDecision(request, testUser.getEmail(), null);

        assertEquals("RUPTURA_CUARTA_PARED", response.branchType());
        assertEquals("Departamento Netflix", response.handlerUnit());
        assertEquals("BREAK_FOURTH_WALL", response.outcomeCode());
    }

    @Test
    @DisplayName("2. Un texto sin ninguna letra clasifica como ENTRADA_CORRUPTA y la partida no se modifica")
    void test2_entradaCorruptaNoModificaPartida() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(playthroughRepository.findById(100L)).thenReturn(Optional.of(testPlaythrough));
        when(decisionRepository.save(any(Decision.class))).thenAnswer(i -> {
            Decision d = i.getArgument(0);
            d.setId(2L);
            return d;
        });

        int initialLucidity = testPlaythrough.getLucidity();
        int initialControl = testPlaythrough.getControlLevel();
        String initialStatus = testPlaythrough.getStatus();

        DecisionRequest request = new DecisionRequest(100L, "%%%% 01001 ### @@@ 110", "CRITICO");
        DecisionResponse response = decisionService.createDecision(request, testUser.getEmail(), null);

        assertEquals("ENTRADA_CORRUPTA", response.branchType());
        assertEquals("ERROR", response.status());
        assertNull(response.resolvedNodeCode());

        // Verificar que la partida quedó intacta
        assertEquals(initialLucidity, testPlaythrough.getLucidity());
        assertEquals(initialControl, testPlaythrough.getControlLevel());
        assertEquals(initialStatus, testPlaythrough.getStatus());
        verify(playthroughRepository, never()).save(testPlaythrough);
    }

    @Test
    @DisplayName("3. Con impacto CRITICO, lucidity baja 40 y controlLevel sube 45, sin pasarse de los límites")
    void test3_impactoCriticoYLimitesStats() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(playthroughRepository.findById(100L)).thenReturn(Optional.of(testPlaythrough));
        when(storyNodeRepository.findByNodeCode("NODE-ESPEJO")).thenReturn(Optional.of(nodeGlitch));
        when(decisionRepository.save(any(Decision.class))).thenAnswer(i -> {
            Decision d = i.getArgument(0);
            d.setId(3L);
            return d;
        });

        DecisionRequest request = new DecisionRequest(100L, "Stefan sigue adelante con el juego.", "CRITICO");
        DecisionResponse response = decisionService.createDecision(request, testUser.getEmail(), null);

        assertEquals(60, response.lucidity());      // 100 - 40 = 60
        assertEquals(45, response.controlLevel());  // 0 + 45 = 45
    }

    @Test
    @DisplayName("4. controlLevel = 100 termina la partida con ENDING_PAC_SYMBOL aunque la lucidez también sea 0")
    void test4_prioridadFinalControlLevel100() {
        // Forzamos la partida en lucidity=30 y controlLevel=90
        testPlaythrough.setLucidity(30);
        testPlaythrough.setControlLevel(90);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(playthroughRepository.findById(100L)).thenReturn(Optional.of(testPlaythrough));
        when(storyNodeRepository.findByNodeCode("NODE-ESPEJO")).thenReturn(Optional.of(nodeGlitch));
        when(decisionRepository.save(any(Decision.class))).thenAnswer(i -> {
            Decision d = i.getArgument(0);
            d.setId(4L);
            return d;
        });

        // Un impacto CRITICO con lucidity 30 la baja a 0 (30 - 40 -> 0), y controlLevel 90 lo sube a 100 (90 + 45 -> 100)
        DecisionRequest request = new DecisionRequest(100L, "Stefan decide tomar el control total.", "CRITICO");
        DecisionResponse response = decisionService.createDecision(request, testUser.getEmail(), null);

        assertEquals(0, response.lucidity());
        assertEquals(100, response.controlLevel());
        assertEquals("FINALIZADA", response.playthroughStatus());
        assertEquals("ENDING_PAC_SYMBOL", response.endingCode());
    }

    @Test
    @DisplayName("5. publishEvent() se llama una vez en una decisión normal y cero veces en una ENTRADA_CORRUPTA")
    void test5_publicacionDeEventos() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(playthroughRepository.findById(100L)).thenReturn(Optional.of(testPlaythrough));
        when(storyNodeRepository.findByNodeCode("NODE-ESPEJO")).thenReturn(Optional.of(nodeGlitch));
        when(decisionRepository.save(any(Decision.class))).thenAnswer(i -> {
            Decision d = i.getArgument(0);
            d.setId(5L);
            return d;
        });

        // 1. Decisión normal
        DecisionRequest requestNormal = new DecisionRequest(100L, "Stefan decide mirar la camara.", "LEVE");
        decisionService.createDecision(requestNormal, testUser.getEmail(), null);

        verify(eventPublisher, times(1)).publishEvent(any(DecisionCommittedEvent.class));

        // 2. Decisión corrupta
        DecisionRequest requestCorrupta = new DecisionRequest(100L, "%%%% 01001 ### @@@", "LEVE");
        decisionService.createDecision(requestCorrupta, testUser.getEmail(), null);

        // publishEvent no debió llamarse de nuevo (sigue siendo 1 vez)
        verify(eventPublisher, times(1)).publishEvent(any(DecisionCommittedEvent.class));
    }
}
