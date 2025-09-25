package com.innowise.service.impl;

import com.innowise.dto.user.UserCreateDto;
import com.innowise.dto.user.UserResponseDto;
import com.innowise.dto.user.UserUpdateDto;
import com.innowise.dto.user.UserWithCardsDto;
import com.innowise.exception.ResourceNotFoundException;
import com.innowise.mapper.UserMapper;
import com.innowise.model.User;
import com.innowise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private CacheManager cacheManager;
    
    @Mock
    private Cache usersWithCardsCache;
    
    @Mock
    private Cache usersInfoCache;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserCreateDto testUserCreateDto;
    private UserUpdateDto testUserUpdateDto;
    private UserResponseDto testUserResponseDto;
    private UserWithCardsDto testUserWithCardsDto;

    @BeforeEach
    void setUp() {
        testUser = createTestUser(1L, "John", "Doe", LocalDate.of(1990, 1, 1), "john.doe@test.com");
        testUserCreateDto = createTestUserCreateDto("John", "Doe", LocalDate.of(1990, 1, 1), "john.doe@test.com");
        testUserUpdateDto = createTestUserUpdateDto("Jane", "Smith", LocalDate.of(1995, 5, 15), "jane.smith@test.com");
        testUserResponseDto = createTestUserResponseDto(1L, "John", "Doe", LocalDate.of(1990, 1, 1), "john.doe@test.com");
        testUserWithCardsDto = createTestUserWithCardsDto(1L, "John", "Doe", LocalDate.of(1990, 1, 1), "john.doe@test.com");
    }

    @Test
    void createUser_WhenValidUserCreateDto_ThenReturnsUserResponseDto() {
        when(userMapper.toUser(testUserCreateDto)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);

        UserResponseDto result = userService.createUser(testUserCreateDto);

        assertThat(result).isEqualTo(testUserResponseDto);
        verify(userMapper).toUser(testUserCreateDto);
        verify(userRepository).save(testUser);
        verify(userMapper).toUserResponseDto(testUser);
    }

    @Test
    void createUser_WhenRepositoryThrowsException_ThenPropagatesException() {
        when(userMapper.toUser(testUserCreateDto)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> userService.createUser(testUserCreateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
        
        verify(userMapper).toUser(testUserCreateDto);
        verify(userRepository).save(testUser);
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    void createUser_WhenMapperReturnsNull_ThenHandledByRepository() {
        when(userMapper.toUser(testUserCreateDto)).thenReturn(null);
        when(userRepository.save(null)).thenThrow(new IllegalArgumentException("User cannot be null"));

        assertThatThrownBy(() -> userService.createUser(testUserCreateDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getUserById_WhenUserExists_ThenReturnsUserWithCardsDto() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toUserWithCards(testUser)).thenReturn(testUserWithCardsDto);

        UserWithCardsDto result = userService.getUserById(userId);

        assertThat(result).isEqualTo(testUserWithCardsDto);
        verify(userRepository).findById(userId);
        verify(userMapper).toUserWithCards(testUser);
    }

    @Test
    void getUserById_WhenUserNotFound_ThenThrowsResourceNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User by id: " + userId + " not found");
        
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    void getUserById_WhenVariousInvalidIds_ThenHandledByRepository(Long userId) {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User by id: " + userId + " not found");
    }

    @Test
    void getUserByEmail_WhenUserExists_ThenReturnsUserResponseDto() {
        String email = "john.doe@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);

        UserResponseDto result = userService.getUserByEmail(email);

        assertThat(result).isEqualTo(testUserResponseDto);
        verify(userRepository).findByEmail(email);
        verify(userMapper).toUserResponseDto(testUser);
    }

    @Test
    void getUserByEmail_WhenUserNotFound_ThenThrowsResourceNotFoundException() {
        String email = "nonexistent@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User by email: " + email + " not found");
        
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(userMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email"})
    @NullSource
    void getUserByEmail_WhenInvalidEmail_ThenHandledByRepository(String email) {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getUsersByIds_WhenValidIds_ThenReturnsUserResponseDtoList() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        User secondUser = createTestUser(2L, "Jane", "Smith", LocalDate.of(1995, 5, 15), "jane@test.com");
        User thirdUser = createTestUser(3L, "Bob", "Johnson", LocalDate.of(1985, 12, 10), "bob@test.com");
        List<User> users = Arrays.asList(testUser, secondUser, thirdUser);
        
        UserResponseDto secondUserDto = createTestUserResponseDto(2L, "Jane", "Smith", LocalDate.of(1995, 5, 15), "jane@test.com");
        UserResponseDto thirdUserDto = createTestUserResponseDto(3L, "Bob", "Johnson", LocalDate.of(1985, 12, 10),"bob@test.com");
        List<UserResponseDto> expectedResponse = Arrays.asList(testUserResponseDto, secondUserDto, thirdUserDto);
        
        when(userRepository.findAllById(ids)).thenReturn(users);
        when(userMapper.toUserResponseDto(users)).thenReturn(expectedResponse);

        List<UserResponseDto> result = userService.getUsersByIds(ids);

        assertThat(result)
                .hasSize(3)
                .isEqualTo(expectedResponse);
        verify(userRepository).findAllById(ids);
        verify(userMapper).toUserResponseDto(users);
    }

    @Test
    void getUsersByIds_WhenEmptyIdsList_ThenReturnsEmptyList() {
        List<Long> emptyIds = Collections.emptyList();
        when(userRepository.findAllById(emptyIds)).thenReturn(Collections.emptyList());
        when(userMapper.toUserResponseDto(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<UserResponseDto> result = userService.getUsersByIds(emptyIds);

        assertThat(result).isEmpty();
        verify(userRepository).findAllById(emptyIds);
        verify(userMapper).toUserResponseDto(Collections.emptyList());
    }

    @Test
    void getUsersByIds_WhenSomeIdsNotFound_ThenReturnsOnlyFoundUsers() {
        List<Long> ids = Arrays.asList(1L, 100L, 2L);
        List<User> foundUsers = Arrays.asList(testUser);
        List<UserResponseDto> expectedResponse = Arrays.asList(testUserResponseDto);
        
        when(userRepository.findAllById(ids)).thenReturn(foundUsers);
        when(userMapper.toUserResponseDto(foundUsers)).thenReturn(expectedResponse);

        List<UserResponseDto> result = userService.getUsersByIds(ids);

        assertThat(result)
                .hasSize(1)
                .containsExactly(testUserResponseDto);
    }

    @Test
    void updateUser_WhenEmailChanged_ThenEvictsOldEmailAndUpdatesCache() {
        Long userId = 1L;
        String oldEmail = "john.doe@test.com";
        String newEmail = "jane.smith@test.com";
        
        User updatedUser = createTestUser(userId, "Jane", "Smith", LocalDate.of(1995, 5, 15), newEmail);
        UserResponseDto updatedResponseDto = createTestUserResponseDto(userId, "Jane", "Smith", LocalDate.of(1995, 5, 15), newEmail);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser).thenReturn(updatedUser);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(updatedResponseDto);
        when(cacheManager.getCache("usersWithCards")).thenReturn(usersWithCardsCache);
        when(cacheManager.getCache("usersInfo")).thenReturn(usersInfoCache);
        when(userMapper.toUserWithCards(any(User.class))).thenReturn(testUserWithCardsDto);

        UserResponseDto result = userService.updateUser(userId, testUserUpdateDto);

        assertThat(result).isEqualTo(updatedResponseDto);
        verify(userRepository, times(2)).save(any(User.class));
        verify(usersWithCardsCache).put(eq(userId), any());
        verify(usersInfoCache).evict(oldEmail);
        verify(usersInfoCache).put(eq(newEmail), any());
    }

    @Test
    void updateUser_WhenEmailNotChanged_ThenDoesNotEvictOldEmail() {
        Long userId = 1L;
        String sameEmail = "john.doe@test.com";
        UserUpdateDto updateDtoSameEmail = createTestUserUpdateDto("Jane", "Smith", LocalDate.of(1995, 5, 15), sameEmail);
        User updatedUser = createTestUser(userId, "Jane", "Smith", LocalDate.of(1995, 5, 15), sameEmail);
        UserResponseDto updatedResponseDto = createTestUserResponseDto(userId, "Jane", "Smith", LocalDate.of(1995, 5, 15), sameEmail);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser).thenReturn(updatedUser);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(updatedResponseDto);
        when(cacheManager.getCache("usersWithCards")).thenReturn(usersWithCardsCache);
        when(cacheManager.getCache("usersInfo")).thenReturn(usersInfoCache);
        when(userMapper.toUserWithCards(any(User.class))).thenReturn(testUserWithCardsDto);

        UserResponseDto result = userService.updateUser(userId, updateDtoSameEmail);

        assertThat(result).isEqualTo(updatedResponseDto);
        verify(userRepository, times(2)).save(any(User.class));
        verify(usersWithCardsCache).put(eq(userId), any());
        verify(usersInfoCache, never()).evict(sameEmail);
        verify(usersInfoCache).put(eq(sameEmail), any());
    }

    @Test
    void updateUser_WhenOnlyNonEmailFieldsUpdated_ThenDoesNotEvictCache() {
        Long userId = 1L;
        UserUpdateDto updateDtoNoEmail = createTestUserUpdateDto("Jane", "Smith", LocalDate.of(1995, 5, 15), null);
        User updatedUser = createTestUser(userId, "Jane", "Smith", LocalDate.of(1995, 5, 15), "john.doe@test.com");
        UserResponseDto updatedResponseDto = createTestUserResponseDto(userId, "Jane", "Smith", LocalDate.of(1995, 5, 15),"john.doe@test.com");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser).thenReturn(updatedUser);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(updatedResponseDto);
        when(cacheManager.getCache("usersWithCards")).thenReturn(usersWithCardsCache);
        when(cacheManager.getCache("usersInfo")).thenReturn(usersInfoCache);
        when(userMapper.toUserWithCards(any(User.class))).thenReturn(testUserWithCardsDto);

        UserResponseDto result = userService.updateUser(userId, updateDtoNoEmail);

        assertThat(result).isEqualTo(updatedResponseDto);
        verify(userRepository, times(2)).save(any(User.class));
        verify(usersWithCardsCache).put(eq(userId), any());
        verify(usersInfoCache, never()).evict(anyString());
        verify(usersInfoCache).put(eq("john.doe@test.com"), any());
    }

    @ParameterizedTest
    @MethodSource("updateUserPartialUpdateProvider")
    void updateUser_WhenPartialUpdate_ThenUpdatesOnlyProvidedFields(UserUpdateDto partialUpdate) {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser).thenReturn(testUser);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(testUserResponseDto);
        when(cacheManager.getCache("usersWithCards")).thenReturn(usersWithCardsCache);
        when(cacheManager.getCache("usersInfo")).thenReturn(usersInfoCache);
        when(userMapper.toUserWithCards(any(User.class))).thenReturn(testUserWithCardsDto);

        UserResponseDto result = userService.updateUser(userId, partialUpdate);

        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserNotFound_ThenThrowsResourceNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(userId, testUserUpdateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User by id: " + userId + " not found");
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(cacheManager, userMapper);
    }

    @Test
    void updateUser_WhenCacheIsNull_ThenContinuesWithoutCaching() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser).thenReturn(testUser);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(testUserResponseDto);
        when(cacheManager.getCache(anyString())).thenReturn(null);

        UserResponseDto result = userService.updateUser(userId, testUserUpdateDto);

        assertThat(result).isNotNull();
        verify(cacheManager, times(2)).getCache(anyString());
        verifyNoInteractions(usersWithCardsCache, usersInfoCache);
    }

    @Test
    void updateUser_WhenAllFieldsNull_ThenNoFieldsUpdated() {
        Long userId = 1L;
        UserUpdateDto allNullUpdate = createTestUserUpdateDto(null, null, null, null);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser).thenReturn(testUser);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(testUserResponseDto);
        when(cacheManager.getCache("usersWithCards")).thenReturn(usersWithCardsCache);
        when(cacheManager.getCache("usersInfo")).thenReturn(usersInfoCache);
        when(userMapper.toUserWithCards(any(User.class))).thenReturn(testUserWithCardsDto);

        UserResponseDto result = userService.updateUser(userId, allNullUpdate);

        assertThat(result).isNotNull();
        verify(userRepository, times(2)).save(any(User.class));
        verify(usersInfoCache, never()).evict(anyString());
        verify(usersInfoCache).put(eq("john.doe@test.com"), any());
    }

    @Test
    void deleteUser_WhenUserExists_ThenDeletesUserAndEvictsCache() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cacheManager.getCache("usersWithCards")).thenReturn(usersWithCardsCache);
        when(cacheManager.getCache("usersInfo")).thenReturn(usersInfoCache);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
        verify(usersWithCardsCache).evict(userId);
        verify(usersInfoCache).evict(testUser.getEmail());
    }

    @Test
    void deleteUser_WhenUserNotFound_ThenThrowsResourceNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User by id: " + userId + " not found");
        
        verify(userRepository, never()).deleteById(userId);
        verifyNoInteractions(cacheManager);
    }

    @Test
    void deleteUser_WhenCacheIsNull_ThenContinuesWithoutCacheEviction() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cacheManager.getCache(anyString())).thenReturn(null);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
        verify(cacheManager, times(2)).getCache(anyString());
        verifyNoInteractions(usersWithCardsCache, usersInfoCache);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 999L, Long.MAX_VALUE})
    void deleteUser_WhenVariousIds_ThenCallsRepositoryDelete(Long userId) {
        User userToDelete = createTestUser(userId, "Test", "User", LocalDate.now(), "test@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        when(cacheManager.getCache("usersWithCards")).thenReturn(usersWithCardsCache);
        when(cacheManager.getCache("usersInfo")).thenReturn(usersInfoCache);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
        verify(usersWithCardsCache).evict(userId);
        verify(usersInfoCache).evict(userToDelete.getEmail());
    }

    @Test
    void updateUser_WhenRepositoryFailsOnSecondSave_ThenPropagatesException() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser)
                .thenThrow(new RuntimeException("Database error on second save"));
        when(cacheManager.getCache(anyString())).thenReturn(null);

        assertThatThrownBy(() -> userService.updateUser(userId, testUserUpdateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error on second save");
    }

    @Test
    void deleteUser_WhenRepositoryThrowsException_ThenPropagatesException() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cacheManager.getCache(anyString())).thenReturn(null);
        doThrow(new RuntimeException("Database delete error")).when(userRepository).deleteById(userId);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database delete error");
    }

    private static Stream<Arguments> updateUserPartialUpdateProvider() {
        return Stream.of(
                Arguments.of(new UserUpdateDto("UpdatedName", null, null, null)),
                Arguments.of(new UserUpdateDto(null, "UpdatedSurname", null, null)),
                Arguments.of(new UserUpdateDto(null, null, LocalDate.of(2000, 1, 1), null)),
                Arguments.of(new UserUpdateDto(null, null, null, "updated@test.com")),
                Arguments.of(new UserUpdateDto("UpdatedName", "UpdatedSurname", null, null)),
                Arguments.of(new UserUpdateDto("UpdatedName", null, LocalDate.of(2000, 1, 1), null)),
                Arguments.of(new UserUpdateDto(null, "UpdatedSurname", null, "updated@test.com"))
        );
    }

    private User createTestUser(Long id, String name, String surname, LocalDate birthDate, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setBirthDate(birthDate);
        return user;
    }

    private UserCreateDto createTestUserCreateDto(String name, String surname, LocalDate birthDate, String email) {
        return new UserCreateDto(name, surname, birthDate, email);
    }

    private UserUpdateDto createTestUserUpdateDto(String name, String surname, LocalDate birthDate, String email) {
        return new UserUpdateDto(name, surname, birthDate, email);
    }

    private UserResponseDto createTestUserResponseDto(Long id, String name, String surname, LocalDate birthDate, String email) {
        return new UserResponseDto(id, name, surname, birthDate, email);
    }

    private UserWithCardsDto createTestUserWithCardsDto(Long id, String name, String surname, LocalDate birthDate, String email) {
        return new UserWithCardsDto(id, name, surname, birthDate, email, Collections.emptyList());
    }
}