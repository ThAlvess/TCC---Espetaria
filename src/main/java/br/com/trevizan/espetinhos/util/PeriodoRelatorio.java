package br.com.trevizan.espetinhos.util;

import br.com.trevizan.espetinhos.dao.RelatorioDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Período selecionado na tela de Relatórios (filtros Diário, Mensal, Anual e
 * Personalizado) e as regras que dependem dele: granularidade do gráfico de
 * evolução, textos de cabeçalho, nome sugerido dos arquivos exportados e o
 * <b>período anterior</b> usado na comparação dos cartões.
 *
 * Regras do período anterior (sempre com o mesmo "tamanho" do atual, para a
 * comparação ser justa):
 * <ul>
 *   <li><b>Diário:</b> o dia imediatamente anterior (ex.: 27/09 com 26/09).
 *       Se o dia escolhido é hoje, compara até o mesmo horário.</li>
 *   <li><b>Mensal:</b> o mês anterior. Se o mês escolhido ainda está em
 *       andamento, compara só os mesmos dias (ex.: 01 a 27/09 com 01 a 27/08).</li>
 *   <li><b>Anual (últimos 12 meses):</b> os 12 meses anteriores a esses.</li>
 *   <li><b>Personalizado:</b> sem comparação — os cartões e o gráfico
 *       mostram só o intervalo De–até escolhido.</li>
 * </ul>
 */
public class PeriodoRelatorio {

    public enum Tipo {
        DIARIO("Diário"),
        MENSAL("Mensal"),
        ANUAL("Anual"),
        PERSONALIZADO("Personalizado");

        private final String nome;

        Tipo(String nome) {
            this.nome = nome;
        }

        public String getNome() {
            return nome;
        }
    }

    /** Personalizado com até esta quantidade de dias usa o gráfico por dia; acima disso, por mês. */
    public static final int MAXIMO_DIAS_GRAFICO_DIARIO = 62;

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DIA_MES = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FMT_ANO_MES = DateTimeFormatter.ofPattern("yyyy-MM");

    private final Tipo tipo;
    private final LocalDateTime inicio;
    private final LocalDateTime fim;

    public PeriodoRelatorio(Tipo tipo, LocalDateTime inicio, LocalDateTime fim) {
        this.tipo = tipo;
        this.inicio = inicio;
        this.fim = fim;
    }

    // ------------------------------------------------------------------
    // Criação a partir dos filtros da tela
    // ------------------------------------------------------------------

    public static PeriodoRelatorio diario(LocalDate dia) {
        return new PeriodoRelatorio(Tipo.DIARIO, dia.atStartOfDay(), fimDoDia(dia));
    }

    public static PeriodoRelatorio mensal(YearMonth mes) {
        return new PeriodoRelatorio(Tipo.MENSAL, mes.atDay(1).atStartOfDay(), fimDoDia(mes.atEndOfMonth()));
    }

    /** Últimos 12 meses: do dia 1º de 11 meses atrás até hoje. */
    public static PeriodoRelatorio anual(LocalDate hoje) {
        LocalDate inicioJanela = hoje.minusMonths(11).withDayOfMonth(1);
        return new PeriodoRelatorio(Tipo.ANUAL, inicioJanela.atStartOfDay(), fimDoDia(hoje));
    }

    /** Intervalo livre (datas inclusivas). Se vierem invertidas, são trocadas. */
    public static PeriodoRelatorio personalizado(LocalDate de, LocalDate ate) {
        if (ate.isBefore(de)) {
            LocalDate troca = de;
            de = ate;
            ate = troca;
        }
        return new PeriodoRelatorio(Tipo.PERSONALIZADO, de.atStartOfDay(), fimDoDia(ate));
    }

    private static LocalDateTime fimDoDia(LocalDate dia) {
        return dia.atTime(LocalTime.of(23, 59, 59));
    }

    // ------------------------------------------------------------------
    // Regras
    // ------------------------------------------------------------------

    /** Quantidade de dias do período (inclusive). */
    public long getQuantidadeDias() {
        return ChronoUnit.DAYS.between(inicio.toLocalDate(), fim.toLocalDate()) + 1;
    }

    /** Agrupamento do gráfico de evolução: por hora, por dia ou por mês. */
    public RelatorioDAO.Granularidade getGranularidade() {
        return switch (tipo) {
            case DIARIO -> RelatorioDAO.Granularidade.HORA;
            case MENSAL -> RelatorioDAO.Granularidade.DIA;
            case ANUAL -> RelatorioDAO.Granularidade.MES;
            case PERSONALIZADO -> {
                long dias = getQuantidadeDias();
                if (dias == 1) {
                    yield RelatorioDAO.Granularidade.HORA;
                } else if (dias <= MAXIMO_DIAS_GRAFICO_DIARIO) {
                    yield RelatorioDAO.Granularidade.DIA;
                } else {
                    yield RelatorioDAO.Granularidade.MES;
                }
            }
        };
    }

    /** Fim do período sem passar de "agora" (dias futuros ainda não têm venda). */
    public LocalDateTime getFimEfetivo(LocalDateTime agora) {
        return fim.isAfter(agora) ? agora : fim;
    }

    public PeriodoRelatorio getPeriodoAnterior() {
        return getPeriodoAnterior(LocalDateTime.now());
    }

    /**
     * Período usado na comparação dos cartões (ver regras no topo da classe).
     * Devolve null quando não há comparação: filtro Personalizado ou
     * período escolhido todo no futuro.
     */
    public PeriodoRelatorio getPeriodoAnterior(LocalDateTime agora) {
        LocalDateTime fimEfetivo = getFimEfetivo(agora);
        if (fimEfetivo.isBefore(inicio)) {
            return null;
        }

        return switch (tipo) {
            case DIARIO -> new PeriodoRelatorio(tipo, inicio.minusDays(1), fimEfetivo.minusDays(1));
            case MENSAL -> {
                YearMonth mesAnterior = YearMonth.from(inicio).minusMonths(1);
                boolean mesCompleto = !fim.isAfter(agora);
                LocalDateTime fimAnterior = mesCompleto
                        ? fimDoDia(mesAnterior.atEndOfMonth())
                        : fimEfetivo.minusMonths(1);
                yield new PeriodoRelatorio(tipo, mesAnterior.atDay(1).atStartOfDay(), fimAnterior);
            }
            case ANUAL -> new PeriodoRelatorio(tipo, inicio.minusMonths(12), fimEfetivo.minusMonths(12));
            // Personalizado não compara: o usuário escolheu exatamente o intervalo que quer ver
            case PERSONALIZADO -> null;
        };
    }

    // ------------------------------------------------------------------
    // Textos
    // ------------------------------------------------------------------

    /** Descrição completa, usada no cabeçalho e rodapé dos arquivos exportados. */
    public String getDescricao() {
        LocalDate de = inicio.toLocalDate();
        LocalDate ate = fim.toLocalDate();
        return switch (tipo) {
            case DIARIO -> de.format(FMT_DATA) + " (" + de.getDayOfWeek().getDisplayName(TextStyle.FULL, PT_BR) + ")";
            case MENSAL -> capitalizar(de.getMonth().getDisplayName(TextStyle.FULL, PT_BR)) + " de " + de.getYear();
            case ANUAL -> "Últimos 12 meses (" + mesAno(de) + " a " + mesAno(ate) + ")";
            case PERSONALIZADO -> de.equals(ate)
                    ? de.format(FMT_DATA)
                    : de.format(FMT_DATA) + " a " + ate.format(FMT_DATA) + " (" + getQuantidadeDias() + " dias)";
        };
    }

    /**
     * Descrição curta deste período quando ele é o "anterior" de uma
     * comparação — aparece nos cartões como "vs. sex. 18/09".
     */
    public String getDescricaoComparacao() {
        LocalDate de = inicio.toLocalDate();
        LocalDate ate = fim.toLocalDate();
        return switch (tipo) {
            case DIARIO -> de.getDayOfWeek().getDisplayName(TextStyle.SHORT, PT_BR).replace(".", "")
                    + ". " + de.format(FMT_DIA_MES);
            case MENSAL -> {
                boolean mesInteiro = de.getDayOfMonth() == 1 && ate.equals(YearMonth.from(de).atEndOfMonth());
                yield mesInteiro
                        ? de.getMonth().getDisplayName(TextStyle.FULL, PT_BR) + "/" + de.getYear()
                        : intervaloCurto(de, ate);
            }
            case ANUAL -> "12 meses anteriores";
            case PERSONALIZADO -> intervaloCurto(de, ate);
        };
    }

    /** Nome sugerido para o arquivo exportado, ex.: Relatorio_Mensal_2026-09.pdf */
    public String getNomeArquivo(String extensao) {
        LocalDate de = inicio.toLocalDate();
        LocalDate ate = fim.toLocalDate();
        String base = switch (tipo) {
            case DIARIO -> "Relatorio_Diario_" + de;
            case MENSAL -> "Relatorio_Mensal_" + de.format(FMT_ANO_MES);
            case ANUAL -> "Relatorio_Anual_" + de.format(FMT_ANO_MES) + "_a_" + ate.format(FMT_ANO_MES);
            case PERSONALIZADO -> de.equals(ate)
                    ? "Relatorio_" + de
                    : "Relatorio_" + de + "_a_" + ate;
        };
        return base + "." + extensao;
    }

    private static String intervaloCurto(LocalDate de, LocalDate ate) {
        if (de.equals(ate)) {
            return de.format(FMT_DIA_MES);
        }
        // com o ano quando o intervalo atravessa anos ou é de outro ano (ex.: 06/04/2025 a 31/12/2025)
        if (de.getYear() != ate.getYear() || de.getYear() != LocalDate.now().getYear()) {
            return de.format(FMT_DATA) + " a " + ate.format(FMT_DATA);
        }
        return de.format(FMT_DIA_MES) + " a " + ate.format(FMT_DIA_MES);
    }

    private static String mesAno(LocalDate data) {
        return data.getMonth().getDisplayName(TextStyle.SHORT, PT_BR).replace(".", "") + "/" + data.getYear();
    }

    private static String capitalizar(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public Tipo getTipo() {
        return tipo;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }
}
