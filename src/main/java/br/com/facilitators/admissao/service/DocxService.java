package br.com.facilitators.admissao.service;

import br.com.facilitators.admissao.dto.AdmissaoDTO;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Lê o template ficha_admissao_template.docx de resources/templates/,
 * substitui todos os placeholders ${campo} pelos valores do DTO
 * e retorna os bytes do .docx preenchido.
 *
 * Estratégia de substituição:
 *   O Apache POI pode fragmentar um único parágrafo em vários XWPFRun
 *   por causa de formatação. Para resolver isso, reconstruímos o texto
 *   completo do parágrafo, substituímos, e reescrevemos no primeiro run
 *   (preservando a formatação original). Os runs excedentes são limpos.
 */
@Service
public class DocxService {

    @Value("${app.docx.template}")
    private Resource templateResource;

    public byte[] preencher(AdmissaoDTO dto) throws IOException {
        Map<String, String> valores = dto.toPlaceholders();

        try (InputStream is = templateResource.getInputStream();
             XWPFDocument doc = new XWPFDocument(is);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Parágrafos soltos (fora de tabela)
            for (XWPFParagraph p : doc.getParagraphs()) {
                substituirNoParagrafo(p, valores);
            }

            // Parágrafos dentro de tabelas
            for (XWPFTable tabela : doc.getTables()) {
                for (XWPFTableRow linha : tabela.getRows()) {
                    for (XWPFTableCell celula : linha.getTableCells()) {
                        for (XWPFParagraph p : celula.getParagraphs()) {
                            substituirNoParagrafo(p, valores);
                        }
                    }
                }
            }

            // Parágrafos em cabeçalho e rodapé
            for (XWPFHeader header : doc.getHeaderList()) {
                for (XWPFParagraph p : header.getParagraphs()) {
                    substituirNoParagrafo(p, valores);
                }
            }
            for (XWPFFooter footer : doc.getFooterList()) {
                for (XWPFParagraph p : footer.getParagraphs()) {
                    substituirNoParagrafo(p, valores);
                }
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Reconstrução segura de parágrafo fragmentado pelo POI.
     *
     * Problema clássico: o texto "${nome}" pode estar espalhado em 3 runs:
     *   run[0] = "${no"   run[1] = "m"   run[2] = "e}"
     * Aqui concatenamos tudo, substituímos e jogamos de volta no run[0].
     */
    private void substituirNoParagrafo(XWPFParagraph paragrafo, Map<String, String> valores) {
        List<XWPFRun> runs = paragrafo.getRuns();
        if (runs == null || runs.isEmpty()) return;

        // 1. Constrói o texto completo do parágrafo
        StringBuilder sb = new StringBuilder();
        for (XWPFRun r : runs) {
            String t = r.getText(0);
            sb.append(t != null ? t : "");
        }

        String textoOriginal = sb.toString();
        String textoFinal = textoOriginal;

        // 2. Aplica todas as substituições
        boolean algumaMudanca = false;
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            if (textoFinal.contains(entry.getKey())) {
                textoFinal = textoFinal.replace(entry.getKey(), entry.getValue());
                algumaMudanca = true;
            }
        }

        // 3. Se mudou algo, reescreve: texto completo no run[0], "" nos demais
        if (algumaMudanca) {
            for (int i = 0; i < runs.size(); i++) {
                runs.get(i).setText(i == 0 ? textoFinal : "", 0);
            }
        }
    }
}
