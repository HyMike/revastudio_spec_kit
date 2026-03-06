package com.revature.revastudio.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.mockito.Mockito.mock;

import com.revature.revastudio.services.EmployeeMetricsService;
import com.revature.revastudio.util.RetrieveUser;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = EmployeeMetricsControllerTest.TestSecurityConfiguration.class)
class EmployeeMetricsControllerTest {

        private MockMvc mockMvc;

        @Autowired
    private EmployeeMetricsService employeeMetricsService;

        @Autowired
    private RetrieveUser retrieveUser;

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private FilterChainProxy springSecurityFilterChain;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                    .addFilters(springSecurityFilterChain)
                                .build();
        }

    @Test
    void getSalesMetrics_returnsOkForEmployee() throws Exception {
        UUID userId = UUID.randomUUID();
        when(retrieveUser.getUser()).thenReturn(userId);
        when(employeeMetricsService.getMetricsForUser(userId)).thenReturn(null);

            mockMvc.perform(get("/employee/sales-metrics")
                            .header("X-Test-User", "employee-user")
                            .header("X-Test-Role", "EMPLOYEE"))
            .andExpect(status().isOk());
    }

    @Test
    void getSalesMetrics_returnsUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/employee/sales-metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSalesMetrics_returnsForbiddenForNonEmployee() throws Exception {
        mockMvc.perform(get("/employee/sales-metrics")
                .header("X-Test-User", "customer-user")
                .header("X-Test-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

        @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfiguration {

                @Bean
                EmployeeMetricsService employeeMetricsService() {
                        return mock(EmployeeMetricsService.class);
                }

                @Bean
                RetrieveUser retrieveUser() {
                        return mock(RetrieveUser.class);
                }

                @Bean
                EmployeeMetricsController employeeMetricsController(EmployeeMetricsService employeeMetricsService,
                                                                                                                        RetrieveUser retrieveUser) {
                        return new EmployeeMetricsController(employeeMetricsService, retrieveUser);
                }

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .addFilterBefore(new TestAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint((request, response, authException) ->
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                            .accessDeniedHandler((request, response, accessDeniedException) ->
                                    response.sendError(HttpServletResponse.SC_FORBIDDEN)));
            return http.build();
        }

        static class TestAuthenticationFilter extends OncePerRequestFilter {

            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String username = request.getHeader("X-Test-User");
                String role = request.getHeader("X-Test-Role");

                if (username != null && role != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

                filterChain.doFilter(request, response);
            }
        }
    }
}