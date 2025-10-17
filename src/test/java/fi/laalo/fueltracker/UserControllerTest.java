package fi.laalo.fueltracker;

import fi.laalo.fueltracker.controller.UserController;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    public void testCreateUser() throws Exception {
        // Arrange: Create test user
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");

        // Mock the service to return the user when save is called
        when(userService.createUser(any(User.class))).thenReturn(user);

        // Act & Assert: Perform POST request and verify response
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"name\":\"Test User\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    public void testGetUserById() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");

        when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    public void testGetUserByEmail() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");

        when(userService.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/email")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    public void testGetUserByEmailNotFound() throws Exception {
        // Arrange
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/email")
                .param("email", "nonexistent@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }
}