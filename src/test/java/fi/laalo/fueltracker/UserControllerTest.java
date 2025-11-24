package fi.laalo.fueltracker;

import fi.laalo.fueltracker.controller.UserController;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        return user;
    }

    // Test 1: POST /api/users/register - Register new user
    @Test
    void testRegisterUser() throws Exception {
        // Arrange
        User user = createTestUser();
        when(userService.registerNewUser("test@example.com", "password123")).thenReturn(user);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        System.out.println("✅ POST /api/users/register - Registers new user");
    }

    // Test 2: GET /api/users/me - Get current logged-in user
    @Test
    @WithMockUser(username = "test@example.com")
    void testGetCurrentUser() throws Exception {
        // Arrange
        User user = createTestUser();
        when(userService.getByEmail("test@example.com")).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        System.out.println("✅ GET /api/users/me - Returns current user");
    }

    // Test 3: GET /api/users/{id} - Get user by ID
    @Test
    @WithMockUser
    void testGetUserById() throws Exception {
        // Arrange
        User user = createTestUser();
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        System.out.println("✅ GET /api/users/{id} - Returns user by ID");
    }

    // Test 4: GET /api/users/email - Get user by email
    @Test
    @WithMockUser
    void testGetUserByEmail() throws Exception {
        // Arrange
        User user = createTestUser();
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/email")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        System.out.println("✅ GET /api/users/email - Returns user by email");
    }

    // Test 5: GET /api/users/email - User not found
    @Test
    @WithMockUser
    void testGetUserByEmailNotFound() throws Exception {
        // Arrange
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/email")
                .param("email", "nonexistent@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());

        System.out.println("✅ GET /api/users/email - Returns empty when user not found");
    }
}
