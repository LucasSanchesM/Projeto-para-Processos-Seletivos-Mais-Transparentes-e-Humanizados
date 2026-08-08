package com.unisal.processoseletivoapi.user;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.unisal.processoseletivoapi.user.dto.CreateUserDto;
import com.unisal.processoseletivoapi.user.dto.UpdateUserDto;
import com.unisal.processoseletivoapi.user.dto.UserResponseDto;
import com.unisal.processoseletivoapi.user.entity.User;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto createUser(CreateUserDto dto) {
        var existingUser = userRepository.findByEmail(dto.email());

        if (existingUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "E-mail já está em uso");
        }

        UserRole role = dto.role() != null ? dto.role() : UserRole.CANDIDATE;

        if (role == UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não é permitido se cadastrar como ADMIN diretamente");
        }

        var user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(role.name());
        user.setActive(true);

        var savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public List<UserResponseDto> findAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    public UserResponseDto findUserById(UUID id) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        return mapToResponse(user);
    }

    public UserResponseDto updateUser(UUID id, UpdateUserDto dto) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name());
        }

        if (dto.email() != null && !dto.email().isBlank()) {
            if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "E-mail já está em uso");
            }
            user.setEmail(dto.email());
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
        }

        if (dto.role() != null) {
            user.setRole(dto.role().name());
        }

        if (dto.active() != null) {
            user.setActive(dto.active());
        }

        var updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    public void deleteUser(UUID id) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        userRepository.delete(user);
    }

    private UserResponseDto mapToResponse(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
