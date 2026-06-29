package br.com.facilitators.admissao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa todos os dados do formulário de admissão.
 * Cada campo mapeia diretamente para um placeholder no template .docx.
 *
 * Placeholders no template têm o formato: ${nomeDoCampo}
 */
@Data
public class AdmissaoDTO {

    // === Seção: Empresa ===
    private String empresa;
    private String funcao;
    private String dataAdmissao;
    private String salario;
    private String horarioInicio;
    private String horarioFim;
    private String intervalo;
    private String prazoContrato; // "30", "45", "60" ou "90"

    // === Seção: Dados do Candidato ===
    private String nome;
    private String sexo;           // "Feminino" | "Masculino"
    private String escolaridade;
    private String cor;
    private String tipoSanguineo;
    private String endereco;
    private String cidade;
    private String uf;
    private String cep;
    private String telefone;
    private String estadoCivil;
    private String idade;
    private String nacionalidade;
    private String dataNascimento;
    private String localNascimento;
    private String ufNascimento;
    private String conjuge;
    private String pai;
    private String mae;

    // === Seção: Dependentes ===
    @JsonProperty("dependentes")
    private List<Dependente> dependentes;

    // === Seção: Documentos ===
    private String ctpsNum;
    private String ctpsSerie;
    private String ctpsUf;
    private String ctpsData;
    private String rgNum;
    private String rgData;
    private String rgOrgao;
    private String tituloNum;
    private String tituloSecao;
    private String tituloZona;
    private String cpf;
    private String pis;
    private String reservista;

    /**
     * Gera o mapa de substituição: placeholder → valor real.
     * Usado pelo DocxService para localizar e substituir no XML do .docx.
     */
    public Map<String, String> toPlaceholders() {
        Map<String, String> m = new LinkedHashMap<>();

        // Empresa
        m.put("${empresa}",        safe(empresa));
        m.put("${funcao}",         safe(funcao));
        m.put("${dataAdmissao}",   formatDate(dataAdmissao));
        m.put("${salario}",        safe(salario));
        m.put("${horarioInicio}",  safe(horarioInicio));
        m.put("${horarioFim}",     safe(horarioFim));
        m.put("${intervalo}",      safe(intervalo));
        m.put("${prazoContrato}",  safe(prazoContrato));

        // Candidato
        m.put("${nome}",           safe(nome));
        m.put("${sexo}",           safe(sexo));
        m.put("${escolaridade}",   safe(escolaridade));
        m.put("${cor}",            safe(cor));
        m.put("${tipoSanguineo}",  safe(tipoSanguineo));
        m.put("${endereco}",       safe(endereco));
        m.put("${cidade}",         safe(cidade));
        m.put("${uf}",             safe(uf));
        m.put("${cep}",            safe(cep));
        m.put("${telefone}",       safe(telefone));
        m.put("${estadoCivil}",    safe(estadoCivil));
        m.put("${idade}",          safe(idade));
        m.put("${nacionalidade}",  safe(nacionalidade));
        m.put("${dataNascimento}", formatDate(dataNascimento));
        m.put("${localNasc}",      safe(localNascimento));
        m.put("${ufNasc}",         safe(ufNascimento));
        m.put("${conjuge}",        safe(conjuge));
        m.put("${pai}",            safe(pai));
        m.put("${mae}",            safe(mae));

        // Dependentes (apenas o primeiro no template estático;
        // para múltiplos, o DocxService repete o bloco de parágrafo)
        if (dependentes != null && !dependentes.isEmpty()) {
            Dependente d = dependentes.get(0);
            m.put("${dep1Nome}",       safe(d.getNome()));
            m.put("${dep1Parentesco}", safe(d.getParentesco()));
            m.put("${dep1Nasc}",       formatDate(d.getDataNasc()));
            m.put("${dep1Cpf}",        safe(d.getCpf()));
        } else {
            m.put("${dep1Nome}", "");
            m.put("${dep1Parentesco}", "");
            m.put("${dep1Nasc}", "");
            m.put("${dep1Cpf}", "");
        }

        // Documentos
        m.put("${ctpsNum}",    safe(ctpsNum));
        m.put("${ctpsSerie}",  safe(ctpsSerie));
        m.put("${ctpsUf}",     safe(ctpsUf));
        m.put("${ctpsData}",   formatDate(ctpsData));
        m.put("${rgNum}",      safe(rgNum));
        m.put("${rgData}",     formatDate(rgData));
        m.put("${rgOrgao}",    safe(rgOrgao));
        m.put("${tituloNum}",  safe(tituloNum));
        m.put("${tituloSecao}",safe(tituloSecao));
        m.put("${tituloZona}", safe(tituloZona));
        m.put("${cpf}",        safe(cpf));
        m.put("${pis}",        safe(pis));
        m.put("${reservista}", safe(reservista));

        return m;
    }

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return "";
        try {
            return LocalDate.parse(isoDate.trim()).format(BR_DATE);
        } catch (Exception e) {
            return isoDate.trim();
        }
    }

    private String safe(String val) {
        return val != null ? val.trim() : "";
    }

    // === Inner class ===
    @Data
    public static class Dependente {
        private String nome;
        private String parentesco;
        private String dataNasc;
        private String cpf;
    }
}
