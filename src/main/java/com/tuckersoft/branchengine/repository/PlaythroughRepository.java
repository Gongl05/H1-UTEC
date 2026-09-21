package com.tuckersoft.branchengine.repository;

import com.tuckersoft.branchengine.entity.Playthrough;
import com.tuckersoft.branchengine.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlaythroughRepository extends JpaRepository<Playthrough, Long> {
    List<Playthrough> findByUserOrderByCreatedAtDesc(User user);
    List<Playthrough> findAllByOrderByCreatedAtDesc();
    boolean existsByPlayerTag(String playerTag);
}
