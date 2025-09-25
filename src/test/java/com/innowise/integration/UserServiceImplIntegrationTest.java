package com.innowise.integration;

import com.innowise.AbstractIntegrationTest;
import com.innowise.dto.user.UserCreateDto;
import com.innowise.dto.user.UserResponseDto;
import com.innowise.dto.user.UserUpdateDto;
import com.innowise.dto.user.UserWithCardsDto;
import com.innowise.model.User;
import com.innowise.repository.UserRepository;
import com.innowise.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        cacheManager.getCache("usersWithCards").clear();
        cacheManager.getCache("usersInfo").clear();
    }

    @Test
    void createUser_shouldSaveUserToDatabase() {
        UserCreateDto createDto = new UserCreateDto("John", "Doe", LocalDate.of(1990, 5, 15), "john.doe@example.com");

        UserResponseDto responseDto = userService.createUser(createDto);

        assertNotNull(responseDto.id());
        Optional<User> savedUserOptional = userRepository.findById(responseDto.id());
        assertTrue(savedUserOptional.isPresent());

        User savedUser = savedUserOptional.get();
        assertAll(
                () -> assertEquals(createDto.name(), savedUser.getName()),
                () -> assertEquals(createDto.surname(), savedUser.getSurname()),
                () -> assertEquals(createDto.email(), savedUser.getEmail())
        );
    }

    @Test
    void getUserById_shouldCacheUser() {
        User savedUser = userRepository.save(
                new User(null, "Jane", "Doe", LocalDate.of(1992, 8, 20), "jane.doe@example.com", null)
        );
        Long userId = savedUser.getId();

        UserWithCardsDto userFromService = userService.getUserById(userId);
        UserWithCardsDto cachedUser = cacheManager.getCache("usersWithCards").get(userId, UserWithCardsDto.class);

        assertAll(
                () -> assertNotNull(userFromService),
                () -> assertEquals(userId, userFromService.id()),
                () -> assertNotNull(cachedUser),
                () -> assertEquals(userId, cachedUser.id())
        );
    }

    @Test
    void updateUser_shouldUpdateUserInDatabaseAndCache() {
        User savedUser = userRepository.save(
                new User(null, "Old Name", "Old Surname", LocalDate.of(1980, 1, 1), "old.email@example.com", null)
        );
        Long userId = savedUser.getId();

        userService.getUserById(userId);
        assertThat(cacheManager.getCache("usersWithCards").get(userId)).isNotNull();
        userService.getUserByEmail("old.email@example.com");
        assertThat(cacheManager.getCache("usersInfo").get("old.email@example.com")).isNotNull();

        UserUpdateDto updateDto = new UserUpdateDto("New Name", "New Surname", null, "new.email@example.com");
        userService.updateUser(userId, updateDto);

        User updatedUser = userRepository.findById(userId).orElseThrow();
        UserWithCardsDto cachedUser = cacheManager.getCache("usersWithCards").get(userId, UserWithCardsDto.class);
        UserResponseDto newCachedUserInfo = cacheManager.getCache("usersInfo").get("new.email@example.com", UserResponseDto.class);

        assertAll(
                () -> assertEquals("New Name", updatedUser.getName()),
                () -> assertEquals("new.email@example.com", updatedUser.getEmail()),

                () -> assertNotNull(cachedUser),
                () -> assertEquals("New Name", cachedUser.name()),

                () -> assertNull(cacheManager.getCache("usersInfo").get("old.email@example.com")),
                () -> assertNotNull(newCachedUserInfo),
                () -> assertEquals("New Name", newCachedUserInfo.name())
        );
    }

    @Test
    void deleteUser_shouldRemoveUserFromDatabaseAndCache() {
        User savedUser = userRepository.save(
                new User(null, "Temp", "User", LocalDate.of(2000, 1, 1), "temp.user@example.com", null)
        );
        Long userId = savedUser.getId();
        String userEmail = savedUser.getEmail();

        userService.getUserById(userId);
        userService.getUserByEmail(userEmail);

        assertAll(
                () -> assertThat(cacheManager.getCache("usersWithCards").get(userId)).isNotNull(),
                () -> assertThat(cacheManager.getCache("usersInfo").get(userEmail)).isNotNull()
        );

        userService.deleteUser(userId);

        assertAll(
                () -> assertThat(userRepository.findById(userId)).isEmpty(),
                () -> assertThat(cacheManager.getCache("usersWithCards").get(userId)).isNull(),
                () -> assertThat(cacheManager.getCache("usersInfo").get(userEmail)).isNull()
        );
    }
}