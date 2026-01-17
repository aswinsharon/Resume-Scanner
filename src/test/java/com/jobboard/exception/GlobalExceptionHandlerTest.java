package com.jobboard.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { GlobalExceptionHandlerTest.TestController.class, GlobalExceptionHandler.class })
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldHandleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @WithMockUser
    void shouldHandleBadRequestException() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Bad request"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @WithMockUser
    void shouldHandleUnauthorizedException() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @WithMockUser
    void shouldHandleConflictException() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Conflict"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @WithMockUser
    void shouldHandleFileProcessingException() throws Exception {
        mockMvc.perform(get("/test/file-processing"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("FILE_PROCESSING_ERROR"))
                .andExpect(jsonPath("$.message").value("File processing error"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @WithMockUser
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @WithMockUser
    void shouldHandleInvalidJson() throws Exception {
        mockMvc.perform(post("/test/json")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists());
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/resource-not-found")
        public void resourceNotFound() {
            throw new ResourceNotFoundException("Resource not found");
        }

        @GetMapping("/bad-request")
        public void badRequest() {
            throw new BadRequestException("Bad request");
        }

        @GetMapping("/unauthorized")
        public void unauthorized() {
            throw new UnauthorizedException("Unauthorized");
        }

        @GetMapping("/conflict")
        public void conflict() {
            throw new ConflictException("Conflict");
        }

        @GetMapping("/file-processing")
        public void fileProcessing() {
            throw new FileProcessingException("File processing error");
        }

        @GetMapping("/generic-error")
        public void genericError() {
            throw new RuntimeException("Generic error");
        }
    }
}