package com.example.rbac.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rbacOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RBAC API")
                        .version("1.0.0")
                        .description("""
                                Мини-сервис RBAC: пользователи, роли, разрешения.
                                Связи many-to-many: User ↔ Role ↔ Permission;
                                разрешения пользователя складываются из его ролей.

                                **GET /api/users** поддерживает фильтрацию по email (частичное, LIKE %...%),
                                роли (точное имя), разрешению (код через любую роль), активности,
                                а также пагинацию (page/size) и сортировку (sort=поле,направление).
                                """)
                        .contact(new Contact().name("RBAC Team")))
                .tags(List.of(
                        new Tag().name("Users")
                                .description("Пользователи: поиск с фильтрацией/пагинацией, создание"),
                        new Tag().name("Roles")
                                .description("Роли: выдача разрешений"),
                        new Tag().name("Import")
                                .description("Тестовые данные: загрузка")));
    }
}
