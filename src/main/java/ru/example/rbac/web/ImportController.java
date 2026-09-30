package ru.example.rbac.web;

import ru.example.rbac.dto.ImportSummary;
import ru.example.rbac.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/import", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    @Operation(summary = "Загрузить тестовые данные",
            description = "Идемпотентная загрузка из db/import-data.json: "
                    + "существующие сущности обновляются, отсутствующие создаются.")
    @ApiResponse(responseCode = "200", description = "Данные загружены")
    public ImportSummary load() {
        return importService.load();
    }
}
