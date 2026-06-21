package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.domain.model.UserRole;
import com.sqlab.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryTest extends AbstractPersistenceTest {

    @Autowired
    private UserJpaRepository userRepository;

    private UserJpaEntity createUser(String username, String email) {
        return UserJpaEntity.builder()
                .username(username)
                .email(email)
                .passwordHash("hash")
                .xp(0)
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void saveAndFindByUsername() {
        userRepository.save(createUser("alice", "alice@example.com"));
        var found = userRepository.findByUsername("alice");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void saveAndFindByEmail() {
        userRepository.save(createUser("bob", "bob@example.com"));
        var found = userRepository.findByEmail("bob@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("bob");
    }

    @Test
    void existsByEmail() {
        userRepository.save(createUser("carol", "carol@example.com"));
        assertThat(userRepository.existsByEmail("carol@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("none@example.com")).isFalse();
    }

    @Test
    void existsByUsername() {
        userRepository.save(createUser("dave", "dave@example.com"));
        assertThat(userRepository.existsByUsername("dave")).isTrue();
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    void returnEmptyWhenNotFound() {
        assertThat(userRepository.findByUsername("nonexistent")).isEmpty();
        assertThat(userRepository.findByEmail("none@test.com")).isEmpty();
    }

    @Test
    void uniqueConstraintOnUsername() {
        userRepository.save(createUser("unique", "u1@test.com"));
        assertThatThrownBy(() -> userRepository.saveAndFlush(createUser("unique", "u2@test.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void uniqueConstraintOnEmail() {
        userRepository.save(createUser("user1", "dup@test.com"));
        assertThatThrownBy(() -> userRepository.saveAndFlush(createUser("user2", "dup@test.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
