package com.skypro.simplebanking.controller;


import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.AccountRepository;
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
import org.springframework.util.Base64Utils;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;
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
    void getUserAccountTest() throws Exception {
        User user = new User("user", passwordEncoder.encode("Valenta2001!"));
        user = userRepository.save(user);
        Account account = new Account(AccountCurrency.USD, 1L, user);
        account = accountRepository.save(account);
        String base64Encoded = Base64Utils.encodeToString((user.getUsername() + ":" + "Valenta2001!")
                .getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/account/" + account.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + base64Encoded))
                .andExpect(status().isOk());
    }

    @Test
    void depositToAccountTest() throws Exception {
        User user = new User("user", passwordEncoder.encode("Valenta2001!"));
        user = userRepository.save(user);
        Account account = accountRepository.save(new Account(1L, user));
        JSONObject balanceChangeRequest = new JSONObject();
        balanceChangeRequest.put("amount", 1L);
        String base64Encoded = Base64Utils.encodeToString((user.getUsername() + ":" + "Valenta2001!")
                .getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post("/account/deposit/" + account.getId()).
                        header(HttpHeaders.AUTHORIZATION, "Basic " + base64Encoded).contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(balanceChangeRequest)))
                .andExpect(status().isOk());

    }

    @Test
    void withdrawFromAccountTest() throws Exception {
        User user = new User("user", passwordEncoder.encode("Valenta2001!"));
        user = userRepository.save(user);
        Account account = accountRepository.save(new Account(1L, user));
        JSONObject balanceChangeRequest = new JSONObject();
        balanceChangeRequest.put("amount", 1L);
        String base64Encoded = Base64Utils.encodeToString((user.getUsername() + ":" + "Valenta2001!")
                .getBytes(StandardCharsets.UTF_8));


        mockMvc.perform(post("/account/withdraw/" + account.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + base64Encoded).contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(balanceChangeRequest)))
                .andExpect(status().isOk());
    }
}