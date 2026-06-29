package br.com.facilitators.admissao.controller;

import br.com.facilitators.admissao.dto.AdmissaoDTO;
import br.com.facilitators.admissao.service.DocxService;
import br.com.facilitators.admissao.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/admissao")
public class AdmissaoController {

    @Autowired
    private DocxService docxService;

    @Autowired
    private PdfService pdfService;

    @Value("${app.contato.whatsapp:}")
    private String contatoWhatsapp;

    @Value("${app.contato.email:}")
    private String contatoEmail;

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
     * POST /api/admissao/gerar-pdf
     * Mesma entrada que /gerar, retorna PDF.
     */
    @PostMapping(value = "/gerar-pdf", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/pdf")
    public ResponseEntity<byte[]> gerarFichaPdf(@RequestBody AdmissaoDTO dto) throws IOException {
        byte[] pdfBytes = pdfService.gerarPdf(dto);
        String nomeArquivo = "ficha_admissao_"
                + (dto.getNome() != null ? dto.getNome().replaceAll("\\s+", "_") : "preenchida")
                + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.parseMediaType("application/pdf"))
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    /**
     * GET /api/admissao/mailto-link?nome=X&empresa=Y
     * Retorna URL mailto: usando o e-mail configurado em app.contato.email.
     */
    @GetMapping("/mailto-link")
    public ResponseEntity<Map<String, String>> mailtoLink(
            @RequestParam(required = false, defaultValue = "") String nome,
            @RequestParam(required = false, defaultValue = "") String empresa) {

        String subject = "Ficha de Admissao - " + nome +
                         (empresa.isBlank() ? "" : " | " + empresa);
        String body = "Ola,\n\nSegue em anexo a ficha de admissao de " + nome + ".\n\nAtenciosamente.";
        String url = "mailto:" + enc(contatoEmail) +
                     "?subject=" + enc(subject) + "&body=" + enc(body);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * GET /api/admissao/whatsapp-link?nome=X&empresa=Y
     * Retorna URL wa.me/<numero> usando o número configurado em app.contato.whatsapp.
     */
    @GetMapping("/whatsapp-link")
    public ResponseEntity<Map<String, String>> whatsappLink(
            @RequestParam(required = false, defaultValue = "") String nome,
            @RequestParam(required = false, defaultValue = "") String empresa) {

        String msg = "Ola! Segue a ficha de admissao de *" + nome + "*" +
                     (empresa.isBlank() ? "" : " — " + empresa) +
                     ". Faca o download do arquivo e nos envie por aqui.";
        String url = "https://wa.me/" + contatoWhatsapp + "?text=" + enc(msg);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Health check simples.
     * GET /api/admissao/health → { "status": "ok" }
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"ok\"}");
    }

    private String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
