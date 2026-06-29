package br.com.facilitators.admissao.controller;

import br.com.facilitators.admissao.dto.AdmissaoDTO;
import br.com.facilitators.admissao.service.DocxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/admissao")
public class AdmissaoController {

    @Autowired
    private DocxService docxService;

    /**
     * Recebe o JSON do formulário, gera o .docx preenchido e retorna como download.
     *
     * POST /api/admissao/gerar
     * Content-Type: application/json
     * Body: { "nome": "João Silva", "cpf": "000.000.000-00", ... }
     *
     * Response: arquivo .docx para download
     */
    @PostMapping(
        value = "/gerar",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> gerarFicha(@RequestBody AdmissaoDTO dto) throws IOException {
        byte[] docBytes = docxService.preencher(dto);

        String nomeArquivo = "ficha_admissao_"
            + (dto.getNome() != null ? dto.getNome().replaceAll("\\s+", "_") : "preenchida")
            + ".docx";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            .contentLength(docBytes.length)
            .body(docBytes);
    }

    /**
     * Health check simples.
     * GET /api/admissao/health → { "status": "ok" }
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"ok\"}");
    }
}
