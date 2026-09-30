package ru.example.rbac.web;

import ru.example.rbac.exception.NotFoundException;
import ru.example.rbac.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @Test
    void grant_returns204() throws Exception {
        mockMvc.perform(post("/api/roles/1/permissions/2"))
                .andExpect(status().isNoContent());

        verify(roleService).grantPermission(1L, 2L);
    }

    @Test
    void grant_notFound_returns404() throws Exception {
        doThrow(new NotFoundException("role not found: 99"))
                .when(roleService).grantPermission(99L, 1L);

        mockMvc.perform(post("/api/roles/99/permissions/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value("role not found: 99"));
    }
}
