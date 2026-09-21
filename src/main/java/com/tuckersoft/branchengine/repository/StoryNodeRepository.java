package com.tuckersoft.branchengine.repository;

import com.tuckersoft.branchengine.entity.StoryNode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoryNodeRepository extends JpaRepository<StoryNode, Long> {
    Optional<StoryNode> findByNodeCode(String nodeCode);
    boolean existsByNodeCode(String nodeCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StoryNode s WHERE s.nodeCode = :nodeCode")
    Optional<StoryNode> findByNodeCodeForUpdate(@Param("nodeCode") String nodeCode);
}
