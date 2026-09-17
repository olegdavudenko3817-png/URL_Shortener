package com.example.url_shortener.auth;

import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestAuthenticationEntryPointTest {
    @Test
    void shouldReturnUnauthorizedJsonResponse() throws IOException, ServletException {

        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(JsonMapper.builder().build());

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Invalid JWT"));
        assertEquals(401, response.getStatus());

        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType().split(";")[0]);
        assertEquals("{\"error\":\"Invalid or expired token\"}", response.getContentAsString());
    }
}