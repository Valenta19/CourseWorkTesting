package com.skypro.simplebanking.controller;

import com.skypro.simplebanking.entity.User;

import com.skypro.simplebanking.repository.UserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.Base64Utils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withUsername("postgres")
            .withPassword("Valenta2001!");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void createUserTestWithRoleUser() throws Exception {
        User user = new User("user", passwordEncoder.encode("Valenta2001!"));
        user = userRepository.save(user);
        String base64Encoded = Base64Utils.encodeToString((user.getUsername() + ":" + "Valenta2001!")
                .getBytes(StandardCharsets.UTF_8));
        JSONObject userRequest = new JSONObject();
        userRequest.put("username", "user");
        userRequest.put("password", passwordEncoder.encode("Valenta2001!"));
        mockMvc.perform(post("/user/")
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + base64Encoded)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(userRequest)))
                .andExpect(status().isForbidden());

    }
    @Test
    void createUserTestWithRoleAdmin() throws Exception {
        JSONObject createUserRequest = new JSONObject();
        createUserRequest.put("username", "admin");
        createUserRequest.put("password", "Valenta2001!");
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserRequest.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsersTest() throws Exception {
        User user = new User("user", passwordEncoder.encode("Valenta2001!"));
        user = userRepository.save(user);
        String base64Encoded = Base64Utils.encodeToString((user.getUsername() + ":" + "Valenta2001!")
                .getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/user/list/").header(HttpHeaders.AUTHORIZATION, "Basic " + base64Encoded))
                .andExpect(status().isOk());
    }

    @Test
    void getMyProfileTest() throws Exception {
        User user = new User("user", passwordEncoder.encode("Valenta2001!"));
        user = userRepository.save(user);
        String base64Encoded = Base64Utils.encodeToString((user.getUsername() + ":" + "Valenta2001!")
                .getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/user/me/").header(HttpHeaders.AUTHORIZATION, "Basic " + base64Encoded))
                .andExpect(status().isOk());

    }

}