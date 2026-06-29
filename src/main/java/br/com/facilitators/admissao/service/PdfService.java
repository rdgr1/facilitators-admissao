package br.com.facilitators.admissao.service;

import br.com.facilitators.admissao.dto.AdmissaoDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    // Paleta da marca Facilitators
    private static final Color VERDE_ESCURO = new Color(0x76, 0x8A, 0x42);
    private static final Color VERDE_CLARO  = new Color(0xA7, 0xB6, 0x6B);
    private static final Color MARFIM       = new Color(0xF4, 0xF6, 0xEC);
    private static final Color BORDA        = new Color(0xC8, 0xD4, 0x9A);
    private static final Color CINZA_LABEL  = new Color(0x6B, 0x7A, 0x50);

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  "Cp1252", 13, Font.NORMAL, Color.WHITE);
    private static final Font F_SECAO  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  "Cp1252",  8, Font.NORMAL, Color.WHITE);
    private static final Font F_LABEL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  "Cp1252",  7, Font.NORMAL, CINZA_LABEL);
    private static final Font F_VALOR  = FontFactory.getFont(FontFactory.HELVETICA,       "Cp1252",  9, Font.NORMAL, Color.BLACK);
    private static final Font F_THEAD  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  "Cp1252",  8, Font.NORMAL, Color.WHITE);
    private static final Font F_SIG    = FontFactory.getFont(FontFactory.HELVETICA,       "Cp1252",  7, Font.NORMAL, CINZA_LABEL);

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] gerarPdf(AdmissaoDTO dto) throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36, 36, 50, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            addTitulo(doc, dto);

            // Empresa
            gap(doc, 4);
            addSec(doc, "DADOS DA EMPRESA");
            PdfPTable tEmp = grid4();
            String prazo = v(dto.getPrazoContrato());
            campo(tEmp, "Empresa",        v(dto.getEmpresa()),         "Funcao",         v(dto.getFuncao()),       false);
            campo(tEmp, "Data Admissao",  data(dto.getDataAdmissao()), "Salario (R$)",   v(dto.getSalario()),      true);
            campo(tEmp, "Horario Inicio", v(dto.getHorarioInicio()),   "Horario Fim",    v(dto.getHorarioFim()),  false);
            campo(tEmp, "Intervalo",      v(dto.getIntervalo()),       "Prazo Contrato", prazo.isBlank() ? "" : prazo + " dias", true);
            doc.add(tEmp);

            // Candidato
            gap(doc, 6);
            addSec(doc, "DADOS DO CANDIDATO");
            PdfPTable tCand = grid4();
            campo(tCand, "Nome Completo",  v(dto.getNome()),              "Sexo",         v(dto.getSexo()),              false);
            campo(tCand, "Escolaridade",   v(dto.getEscolaridade()),      "Cor/Raca",     v(dto.getCor()),               true);
            campo(tCand, "Tipo Sanguineo", v(dto.getTipoSanguineo()),     "Estado Civil", v(dto.getEstadoCivil()),       false);
            campo(tCand, "Endereco",       v(dto.getEndereco()),          "CEP",          v(dto.getCep()),               true);
            campo(tCand, "Cidade",         v(dto.getCidade()),            "UF",           v(dto.getUf()),                false);
            campo(tCand, "Telefone",       v(dto.getTelefone()),          "Idade",        v(dto.getIdade()),             true);
            campo(tCand, "Nacionalidade",  v(dto.getNacionalidade()),     "Data Nasc.",   data(dto.getDataNascimento()), false);
            campo(tCand, "Local Nasc.",    v(dto.getLocalNascimento()),   "UF Nasc.",     v(dto.getUfNascimento()),      true);
            campo(tCand, "Conjuge",        v(dto.getConjuge()),           "Pai",          v(dto.getPai()),               false);
            campo(tCand, "Mae",            v(dto.getMae()),               " ",            "",                            true);
            doc.add(tCand);

            // Dependentes
            List<AdmissaoDTO.Dependente> deps = dto.getDependentes();
            if (deps != null && !deps.isEmpty()) {
                gap(doc, 6);
                addSec(doc, "DEPENDENTES");
                PdfPTable tDep = new PdfPTable(new float[]{3, 2, 2, 2});
                tDep.setWidthPercentage(100);
                for (String h : new String[]{"Nome", "Parentesco", "Data Nasc.", "CPF"}) {
                    PdfPCell c = cellBase(new Phrase(h, F_THEAD));
                    c.setBackgroundColor(VERDE_CLARO);
                    tDep.addCell(c);
                }
                boolean alt = false;
                for (AdmissaoDTO.Dependente d : deps) {
                    Color bg = alt ? MARFIM : Color.WHITE;
                    tDep.addCell(valCell(v(d.getNome()), bg));
                    tDep.addCell(valCell(v(d.getParentesco()), bg));
                    tDep.addCell(valCell(data(d.getDataNasc()), bg));
                    tDep.addCell(valCell(v(d.getCpf()), bg));
                    alt = !alt;
                }
                doc.add(tDep);
            }

            // Documentos
            gap(doc, 6);
            addSec(doc, "DOCUMENTOS");
            PdfPTable tDoc = grid4();
            campo(tDoc, "CTPS No",      v(dto.getCtpsNum()),    "CTPS Serie",   v(dto.getCtpsSerie()),   false);
            campo(tDoc, "CTPS UF",      v(dto.getCtpsUf()),     "CTPS Data",    data(dto.getCtpsData()), true);
            campo(tDoc, "RG No",        v(dto.getRgNum()),      "RG Data",      data(dto.getRgData()),   false);
            campo(tDoc, "RG Orgao",     v(dto.getRgOrgao()),    "CPF",          v(dto.getCpf()),         true);
            campo(tDoc, "PIS",          v(dto.getPis()),        "Reservista",   v(dto.getReservista()), false);
            campo(tDoc, "Titulo No",    v(dto.getTituloNum()),  "Titulo Secao", v(dto.getTituloSecao()), true);
            campo(tDoc, "Titulo Zona",  v(dto.getTituloZona()), " ",           "",                      false);
            doc.add(tDoc);

            gap(doc, 24);
            addAssinaturas(doc);

            doc.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar PDF", e);
        }
    }

    // ─── Builders ─────────────────────────────────────────────

    private void addTitulo(Document doc, AdmissaoDTO dto) throws DocumentException {
        String texto = "FICHA DE ADMISSAO" +
                (dto.getEmpresa() != null && !dto.getEmpresa().isBlank()
                 ? "  |  " + dto.getEmpresa().toUpperCase() : "");
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell c = cellBase(new Phrase(texto, F_TITULO));
        c.setBackgroundColor(VERDE_ESCURO);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPaddingTop(10);
        c.setPaddingBottom(10);
        t.addCell(c);
        doc.add(t);
    }

    private void addSec(Document doc, String titulo) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(2);
        t.setSpacingAfter(0);
        PdfPCell c = cellBase(new Phrase(titulo, F_SECAO));
        c.setBackgroundColor(VERDE_ESCURO);
        c.setPaddingTop(4);
        c.setPaddingBottom(4);
        c.setPaddingLeft(6);
        t.addCell(c);
        doc.add(t);
    }

    private PdfPTable grid4() throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{1.8f, 2.8f, 1.8f, 2.8f});
        t.setWidthPercentage(100);
        t.setSpacingBefore(0);
        return t;
    }

    private void campo(PdfPTable t, String l1, String v1, String l2, String v2, boolean alt) {
        Color bg = alt ? MARFIM : Color.WHITE;
        t.addCell(lblCell(l1, bg));
        t.addCell(valCell(v1, bg));
        t.addCell(lblCell(l2, bg));
        t.addCell(valCell(v2, bg));
    }

    private PdfPCell lblCell(String text, Color bg) {
        PdfPCell c = cellBase(new Phrase(text, F_LABEL));
        c.setBackgroundColor(bg);
        return c;
    }

    private PdfPCell valCell(String text, Color bg) {
        String display = (text == null || text.isBlank()) ? " " : text;
        PdfPCell c = cellBase(new Phrase(display, F_VALOR));
        c.setBackgroundColor(bg);
        return c;
    }

    private PdfPCell cellBase(Phrase phrase) {
        PdfPCell c = new PdfPCell(phrase);
        c.setBorderColor(BORDA);
        c.setPadding(3);
        return c;
    }

    private void addAssinaturas(Document doc) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{1, 1});
        t.setWidthPercentage(70);
        t.setHorizontalAlignment(Element.ALIGN_CENTER);
        for (String label : new String[]{"Empregado", "Empregador"}) {
            PdfPCell c = new PdfPCell(new Phrase(label, F_SIG));
            c.setBorder(Rectangle.TOP);
            c.setBorderColorTop(VERDE_ESCURO);
            c.setBorderWidthTop(1.5f);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPaddingTop(5);
            c.setPaddingBottom(3);
            t.addCell(c);
        }
        doc.add(t);
    }

    private void gap(Document doc, float pt) throws DocumentException {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(pt);
        doc.add(p);
    }

    // ─── Util ─────────────────────────────────────────────────

    private String v(String s) {
        return s != null ? s.trim() : "";
    }

    private String data(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return "";
        try {
            return LocalDate.parse(isoDate.trim()).format(BR_DATE);
        } catch (Exception e) {
            return isoDate.trim();
        }
    }
}