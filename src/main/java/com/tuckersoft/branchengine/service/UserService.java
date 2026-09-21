package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.RoleUpdateRequest;
import com.tuckersoft.branchengine.dto.UserResponse;
import com.tuckersoft.branchengine.entity.User;
import com.tuckersoft.branchengine.exception.BadRequestException;
import com.tuckersoft.branchengine.exception.ResourceNotFoundException;
import com.tuckersoft.branchengine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse updateRole(Long userId, RoleUpdateRequest request, String currentAdminEmail) {
        User admin = userRepository.findByEmail(currentAdminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado"));

        if (admin.getId().equals(userId)) {
            throw new BadRequestException("Un administrador no puede cambiar su propio rol");
        }

        String targetRole = request.role();
        if (!"ROLE_USER".equals(targetRole) && !"ROLE_ADMIN".equals(targetRole)) {
            throw new BadRequestException("Rol inválido: " + targetRole);
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        targetUser.setRole(targetRole);
        userRepository.save(targetUser);

        return mapToResponse(targetUser);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
