package br.com.trevizan.espetinhos.util;

import br.com.trevizan.espetinhos.model.ProdutoVendido;
import br.com.trevizan.espetinhos.model.ResumoPeriodo;
import br.com.trevizan.espetinhos.model.ResumoRelatorio;
import br.com.trevizan.espetinhos.model.VendaDetalhada;
import br.com.trevizan.espetinhos.util.PlanilhaXlsx.Aba;
import br.com.trevizan.espetinhos.util.PlanilhaXlsx.Estilo;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static br.com.trevizan.espetinhos.util.PlanilhaXlsx.data;
import static br.com.trevizan.espetinhos.util.PlanilhaXlsx.dataHora;
import static br.com.trevizan.espetinhos.util.PlanilhaXlsx.formula;
import static br.com.trevizan.espetinhos.util.PlanilhaXlsx.numero;
import static br.com.trevizan.espetinhos.util.PlanilhaXlsx.texto;
import static br.com.trevizan.espetinhos.util.PlanilhaXlsx.vazio;

/**
 * Gera a planilha Excel (.xlsx) da tela de Relatórios, com os mesmos dados
 * do PDF, mas em formato editável:
 * <ul>
 *   <li><b>Resumo</b>: indicadores (com o período anterior e a variação),
 *       valores por forma de pagamento e por categoria;</li>
 *   <li><b>Evolução</b>: cada ponto do gráfico (hora, dia ou mês), com o
 *       ponto correspondente do período anterior;</li>
 *   <li><b>Vendas</b> / <b>Resumo semanal</b> / <b>Resumo mensal</b>:
 *       o detalhamento, conforme o tamanho do período;</li>
 *   <li><b>Produtos</b>: quantidade e valor de cada produto.</li>
 * </ul>
 * Valores monetários ficam como números (formato R$) e os totais são
 * fórmulas SUM, então dá para filtrar, somar e fazer gráficos no Excel.
 */
public class RelatorioExcelExporter {

    private static final DateTimeFormatter FMT_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final DateTimeFormatter FMT_DIA_MES = DateTimeFormatter.ofPattern("dd/MM");

    private final DadosRelatorio dados;

    public RelatorioExcelExporter(DadosRelatorio dados) {
        this.dados = dados;
    }

    public void exportar(File destino) throws IOException {
        PlanilhaXlsx planilha = new PlanilhaXlsx();
        planilha.setTitulo("Relatório de Vendas - " + dados.getPeriodo().getTipo().getNome()
                + " - " + dados.getPeriodo().getDescricao());
        planilha.setAutor(dados.getGeradoPor() != null ? dados.getGeradoPor() : "Trevizan Espetinhos");

        abaResumo(planilha.novaAba("Resumo"));
        abaEvolucao(planilha.novaAba("Evolução"));
        switch (dados.getDetalhamento()) {
            case POR_VENDA -> abaVendas(planilha.novaAba("Vendas"));
            case POR_SEMANA -> abaResumoPeriodos(planilha.novaAba("Resumo semanal"), true);
            case POR_MES -> abaResumoPeriodos(planilha.novaAba("Resumo mensal"), false);
        }
        abaProdutos(planilha.novaAba("Produtos"));

        planilha.salvar(destino);
    }

    // ------------------------------------------------------------------
    // Aba "Resumo"
    // ------------------------------------------------------------------

    private void abaResumo(Aba aba) {
        aba.larguras(30, 20, 20, 14);

        aba.linha(texto("Trevizan Espetinhos — Relatório de Vendas (" + dados.getPeriodo().getTipo().getNome() + ")",
                Estilo.TITULO));
        aba.linha(texto("Período: " + dados.getPeriodo().getDescricao(), Estilo.SUBTITULO));
        if (dados.temComparacao()) {
            aba.linha(texto("Comparado com: " + dados.getPeriodoAnterior().getDescricaoComparacao(), Estilo.SUBTITULO));
        }
        aba.linha(texto("Gerado em " + dados.getGeradoEm().format(FMT_DATA_HORA)
                + (dados.getGeradoPor() != null ? " por " + dados.getGeradoPor() : ""), Estilo.SUBTITULO));
        aba.linhaVazia();

        // --- indicadores
        ResumoRelatorio atual = dados.getResumo();
        ResumoRelatorio anterior = dados.getResumoAnterior();
        boolean compara = dados.temComparacao();

        if (compara) {
            aba.linha(texto("Indicador", Estilo.CABECALHO), texto("Período atual", Estilo.CABECALHO),
                    texto("Período anterior", Estilo.CABECALHO), texto("Variação", Estilo.CABECALHO));
        } else {
            aba.linha(texto("Indicador", Estilo.CABECALHO), texto("Valor", Estilo.CABECALHO));
        }

        linhaIndicador(aba, "Faturamento total", atual.getFaturamentoTotal(),
                compara ? anterior.getFaturamentoTotal() : null, Estilo.MOEDA, compara);
        linhaIndicador(aba, "Quantidade de vendas", BigDecimal.valueOf(atual.getQuantidadeVendas()),
                compara ? BigDecimal.valueOf(anterior.getQuantidadeVendas()) : null, Estilo.INTEIRO, compara);
        linhaIndicador(aba, "Ticket médio", atual.getTicketMedio(),
                compara ? anterior.getTicketMedio() : null, Estilo.MOEDA, compara);
        aba.linha(texto("Pagamento principal", Estilo.TEXTO_NEGRITO),
                texto(textoOuTraco(atual.getFormaPagamentoPrincipal()), Estilo.PADRAO),
                compara ? texto(textoOuTraco(anterior.getFormaPagamentoPrincipal()), Estilo.PADRAO) : null);
        aba.linhaVazia();

        // --- formas de pagamento e categorias
        tabelaValores(aba, "Forma de pagamento", dados.getTotaisPorFormaPagamento());
        aba.linhaVazia();
        tabelaValores(aba, "Categoria", dados.getVendasPorCategoria());
    }

    private void linhaIndicador(Aba aba, String nome, BigDecimal atual, BigDecimal anterior,
            Estilo estilo, boolean compara) {
        if (!compara) {
            aba.linha(texto(nome, Estilo.TEXTO_NEGRITO), numero(atual, estilo));
            return;
        }
        BigDecimal variacao = DadosRelatorio.variacaoPercentual(atual, anterior);
        aba.linha(texto(nome, Estilo.TEXTO_NEGRITO),
                numero(atual, estilo),
                numero(anterior, estilo),
                variacao != null
                        ? numero(variacao.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP), Estilo.VARIACAO)
                        : texto("—", Estilo.PADRAO));
    }

    /** Tabela "Nome | Valor | % do total" com linha de total (fórmula SUM). */
    private void tabelaValores(Aba aba, String titulo, Map<String, BigDecimal> valores) {
        aba.linha(texto(titulo, Estilo.CABECALHO), texto("Valor", Estilo.CABECALHO), texto("% do total", Estilo.CABECALHO));
        if (valores.isEmpty()) {
            aba.linha(texto("Sem dados no período", Estilo.SUBTITULO));
            return;
        }

        BigDecimal total = somar(valores.values());
        int primeira = aba.proximaLinha();
        for (Map.Entry<String, BigDecimal> item : valores.entrySet()) {
            aba.linha(texto(item.getKey(), Estilo.PADRAO),
                    numero(item.getValue(), Estilo.MOEDA),
                    numero(fracao(item.getValue(), total), Estilo.PERCENTUAL));
        }
        int ultima = aba.proximaLinha() - 1;
        aba.linha(texto("Total", Estilo.TOTAL_TEXTO),
                formula("SUM(B" + primeira + ":B" + ultima + ")", total, Estilo.TOTAL_MOEDA),
                numero(total.signum() == 0 ? BigDecimal.ZERO : BigDecimal.ONE, Estilo.TOTAL_PERCENTUAL));
    }

    // ------------------------------------------------------------------
    // Aba "Evolução"
    // ------------------------------------------------------------------

    private void abaEvolucao(Aba aba) {
        String ponto = switch (dados.getDetalhamento()) {
            case POR_VENDA -> "Hora";
            case POR_SEMANA -> "Dia";
            case POR_MES -> "Mês";
        };
        List<BigDecimal> anteriores = dados.getEvolucaoAnterior();
        List<String> rotulosAnteriores = dados.getRotulosEvolucaoAnterior();
        boolean compara = !anteriores.isEmpty();

        if (compara) {
            aba.larguras(14, 18, 18, 20, 14);
            aba.linha(texto(ponto, Estilo.CABECALHO), texto("Faturamento", Estilo.CABECALHO),
                    texto(ponto + " (período anterior)", Estilo.CABECALHO),
                    texto("Faturamento anterior", Estilo.CABECALHO), texto("Variação", Estilo.CABECALHO));
        } else {
            aba.larguras(14, 18);
            aba.linha(texto(ponto, Estilo.CABECALHO), texto("Faturamento", Estilo.CABECALHO));
        }
        aba.congelarLinhas(1);

        if (dados.getEvolucao().isEmpty()) {
            aba.linha(texto("Sem dados no período", Estilo.SUBTITULO));
            return;
        }

        int primeira = aba.proximaLinha();
        int indice = 0;
        BigDecimal totalAtual = BigDecimal.ZERO;
        BigDecimal totalAnterior = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> item : dados.getEvolucao().entrySet()) {
            BigDecimal valor = item.getValue() != null ? item.getValue() : BigDecimal.ZERO;
            totalAtual = totalAtual.add(valor);

            if (compara && indice < anteriores.size()) {
                BigDecimal anterior = anteriores.get(indice) != null ? anteriores.get(indice) : BigDecimal.ZERO;
                totalAnterior = totalAnterior.add(anterior);
                BigDecimal variacao = DadosRelatorio.variacaoPercentual(valor, anterior);
                aba.linha(texto(item.getKey(), Estilo.PADRAO), numero(valor, Estilo.MOEDA),
                        texto(indice < rotulosAnteriores.size() ? rotulosAnteriores.get(indice) : "", Estilo.PADRAO),
                        numero(anterior, Estilo.MOEDA),
                        variacao != null
                                ? numero(variacao.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP), Estilo.VARIACAO)
                                : texto("—", Estilo.PADRAO));
            } else {
                aba.linha(texto(item.getKey(), Estilo.PADRAO), numero(valor, Estilo.MOEDA));
            }
            indice++;
        }

        int ultima = aba.proximaLinha() - 1;
        if (compara) {
            BigDecimal variacaoTotal = DadosRelatorio.variacaoPercentual(totalAtual, totalAnterior);
            aba.linha(texto("Total", Estilo.TOTAL_TEXTO),
                    formula("SUM(B" + primeira + ":B" + ultima + ")", totalAtual, Estilo.TOTAL_MOEDA),
                    texto("", Estilo.TOTAL_TEXTO),
                    formula("SUM(D" + primeira + ":D" + ultima + ")", totalAnterior, Estilo.TOTAL_MOEDA),
                    variacaoTotal != null
                            ? numero(variacaoTotal.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP), Estilo.TOTAL_PERCENTUAL)
                            : texto("", Estilo.TOTAL_TEXTO));
        } else {
            aba.linha(texto("Total", Estilo.TOTAL_TEXTO),
                    formula("SUM(B" + primeira + ":B" + ultima + ")", totalAtual, Estilo.TOTAL_MOEDA));
        }
    }

    // ------------------------------------------------------------------
    // Aba "Vendas" (período de um dia)
    // ------------------------------------------------------------------

    private void abaVendas(Aba aba) {
        aba.larguras(11, 7, 22, 18, 10, 12, 12, 8, 20, 14);
        aba.linha(texto("Comanda", Estilo.CABECALHO), texto("Mesa", Estilo.CABECALHO),
                texto("Cliente", Estilo.CABECALHO), texto("Atendente", Estilo.CABECALHO),
                texto("Abertura", Estilo.CABECALHO), texto("Fechamento", Estilo.CABECALHO),
                texto("Tempo (min)", Estilo.CABECALHO), texto("Itens", Estilo.CABECALHO),
                texto("Pagamento", Estilo.CABECALHO), texto("Total", Estilo.CABECALHO));
        aba.congelarLinhas(1);

        List<VendaDetalhada> vendas = dados.getVendas();
        if (vendas.isEmpty()) {
            aba.linha(texto("Nenhuma venda registrada no período", Estilo.SUBTITULO));
            return;
        }

        int primeira = aba.proximaLinha();
        int itens = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (VendaDetalhada venda : vendas) {
            Duration permanencia = venda.getPermanencia();
            aba.linha(numero(venda.getIdComanda(), Estilo.INTEIRO),
                    numero(venda.getNumeroMesa(), Estilo.INTEIRO),
                    texto(textoOuTraco(venda.getNomeCliente()), Estilo.PADRAO),
                    texto(textoOuTraco(venda.getAtendente()), Estilo.PADRAO),
                    dataHora(venda.getDataAbertura(), Estilo.HORA),
                    dataHora(venda.getDataFechamento(), Estilo.HORA),
                    permanencia != null && !permanencia.isNegative()
                            ? numero(permanencia.toMinutes(), Estilo.INTEIRO) : vazio(Estilo.INTEIRO),
                    numero(venda.getQuantidadeItens(), Estilo.INTEIRO),
                    texto(textoOuTraco(venda.getFormasPagamento()), Estilo.PADRAO),
                    numero(venda.getValorTotal(), Estilo.MOEDA));
            itens += venda.getQuantidadeItens();
            total = total.add(venda.getValorTotal());
        }
        int ultima = aba.proximaLinha() - 1;
        aba.linha(texto("Total — " + vendas.size() + (vendas.size() == 1 ? " venda" : " vendas"), Estilo.TOTAL_TEXTO),
                texto("", Estilo.TOTAL_TEXTO), texto("", Estilo.TOTAL_TEXTO), texto("", Estilo.TOTAL_TEXTO),
                texto("", Estilo.TOTAL_TEXTO), texto("", Estilo.TOTAL_TEXTO), texto("", Estilo.TOTAL_TEXTO),
                formula("SUM(H" + primeira + ":H" + ultima + ")", itens, Estilo.TOTAL_INTEIRO),
                texto("", Estilo.TOTAL_TEXTO),
                formula("SUM(J" + primeira + ":J" + ultima + ")", total, Estilo.TOTAL_MOEDA));
    }

    // ------------------------------------------------------------------
    // Abas "Resumo semanal" / "Resumo mensal"
    // ------------------------------------------------------------------

    private void abaResumoPeriodos(Aba aba, boolean semanal) {
        List<ResumoPeriodo> periodos = dados.getResumoPeriodos();
        BigDecimal total = somar(periodos.stream().map(ResumoPeriodo::getFaturamento).toList());
        int vendas = periodos.stream().mapToInt(ResumoPeriodo::getQuantidadeVendas).sum();
        int dias = periodos.stream().mapToInt(ResumoPeriodo::getDiasComVenda).sum();

        aba.larguras(semanal ? 12 : 18, 12, 12, 10, 10, 16, 14, 14, 11, 12, 16);
        aba.linha(texto(semanal ? "Semana" : "Mês", Estilo.CABECALHO), texto("Início", Estilo.CABECALHO),
                texto("Fim", Estilo.CABECALHO), texto("Dias c/ venda", Estilo.CABECALHO),
                texto("Vendas", Estilo.CABECALHO), texto("Faturamento", Estilo.CABECALHO),
                texto("Ticket médio", Estilo.CABECALHO), texto("Média por dia", Estilo.CABECALHO),
                texto("% do total", Estilo.CABECALHO), texto("Melhor dia", Estilo.CABECALHO),
                texto("Faturamento melhor dia", Estilo.CABECALHO));
        aba.congelarLinhas(1);

        int primeira = aba.proximaLinha();
        for (ResumoPeriodo periodo : periodos) {
            boolean semVenda = periodo.getQuantidadeVendas() == 0;
            aba.linha(texto(periodo.getRotulo(), Estilo.TEXTO_NEGRITO),
                    data(periodo.getDataInicio(), Estilo.DATA),
                    data(periodo.getDataFim(), Estilo.DATA),
                    numero(periodo.getDiasComVenda(), Estilo.INTEIRO),
                    numero(periodo.getQuantidadeVendas(), Estilo.INTEIRO),
                    numero(periodo.getFaturamento(), Estilo.MOEDA),
                    semVenda ? vazio(Estilo.MOEDA) : numero(periodo.getTicketMedio(), Estilo.MOEDA),
                    semVenda ? vazio(Estilo.MOEDA) : numero(periodo.getMediaDiaria(), Estilo.MOEDA),
                    numero(fracao(periodo.getFaturamento(), total), Estilo.PERCENTUAL),
                    periodo.getMelhorDia() != null
                            ? texto(periodo.getMelhorDia().format(FMT_DIA_MES), Estilo.PADRAO) : texto("—", Estilo.PADRAO),
                    periodo.getMelhorDia() != null
                            ? numero(periodo.getFaturamentoMelhorDia(), Estilo.MOEDA) : vazio(Estilo.MOEDA));
        }
        int ultima = aba.proximaLinha() - 1;
        int linhaTotal = ultima + 1;
        if (periodos.isEmpty()) {
            return;
        }
        aba.linha(texto("Total", Estilo.TOTAL_TEXTO),
                data(periodos.get(0).getDataInicio(), Estilo.DATA),
                data(periodos.get(periodos.size() - 1).getDataFim(), Estilo.DATA),
                formula("SUM(D" + primeira + ":D" + ultima + ")", dias, Estilo.TOTAL_INTEIRO),
                formula("SUM(E" + primeira + ":E" + ultima + ")", vendas, Estilo.TOTAL_INTEIRO),
                formula("SUM(F" + primeira + ":F" + ultima + ")", total, Estilo.TOTAL_MOEDA),
                formula("IF(E" + linhaTotal + "=0,0,F" + linhaTotal + "/E" + linhaTotal + ")",
                        dividir(total, vendas), Estilo.TOTAL_MOEDA),
                formula("IF(D" + linhaTotal + "=0,0,F" + linhaTotal + "/D" + linhaTotal + ")",
                        dividir(total, dias), Estilo.TOTAL_MOEDA),
                numero(total.signum() == 0 ? BigDecimal.ZERO : BigDecimal.ONE, Estilo.TOTAL_PERCENTUAL),
                texto("", Estilo.TOTAL_TEXTO),
                texto("", Estilo.TOTAL_TEXTO));
    }

    // ------------------------------------------------------------------
    // Aba "Produtos"
    // ------------------------------------------------------------------

    private void abaProdutos(Aba aba) {
        List<ProdutoVendido> produtos = dados.getProdutos();
        BigDecimal total = somar(produtos.stream().map(ProdutoVendido::getTotal).toList());
        int quantidade = produtos.stream().mapToInt(ProdutoVendido::getQuantidade).sum();

        aba.larguras(9, 28, 16, 12, 14, 16, 11);
        aba.linha(texto("Posição", Estilo.CABECALHO), texto("Produto", Estilo.CABECALHO),
                texto("Categoria", Estilo.CABECALHO), texto("Quantidade", Estilo.CABECALHO),
                texto("Preço médio", Estilo.CABECALHO), texto("Total", Estilo.CABECALHO),
                texto("% do total", Estilo.CABECALHO));
        aba.congelarLinhas(1);

        if (produtos.isEmpty()) {
            aba.linha(texto("Nenhum produto vendido no período", Estilo.SUBTITULO));
            return;
        }

        int primeira = aba.proximaLinha();
        int posicao = 1;
        for (ProdutoVendido produto : produtos) {
            aba.linha(numero(posicao++, Estilo.INTEIRO),
                    texto(produto.getNome(), Estilo.TEXTO_NEGRITO),
                    texto(textoOuTraco(produto.getCategoria()), Estilo.PADRAO),
                    numero(produto.getQuantidade(), Estilo.INTEIRO),
                    numero(dividir(produto.getTotal(), produto.getQuantidade()), Estilo.MOEDA),
                    numero(produto.getTotal(), Estilo.MOEDA),
                    numero(fracao(produto.getTotal(), total), Estilo.PERCENTUAL));
        }
        int ultima = aba.proximaLinha() - 1;
        aba.linha(texto("Total", Estilo.TOTAL_TEXTO), texto("", Estilo.TOTAL_TEXTO), texto("", Estilo.TOTAL_TEXTO),
                formula("SUM(D" + primeira + ":D" + ultima + ")", quantidade, Estilo.TOTAL_INTEIRO),
                texto("", Estilo.TOTAL_TEXTO),
                formula("SUM(F" + primeira + ":F" + ultima + ")", total, Estilo.TOTAL_MOEDA),
                numero(total.signum() == 0 ? BigDecimal.ZERO : BigDecimal.ONE, Estilo.TOTAL_PERCENTUAL));
    }

    // ------------------------------------------------------------------
    // Utilitários
    // ------------------------------------------------------------------

    /** Parte/total como fração (0,25 = 25%), que é como o Excel guarda percentuais. */
    private static BigDecimal fracao(BigDecimal parte, BigDecimal total) {
        if (parte == null || total == null || total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.divide(total, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal dividir(BigDecimal valor, int divisor) {
        if (valor == null || divisor == 0) {
            return BigDecimal.ZERO;
        }
        return valor.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal somar(Iterable<BigDecimal> valores) {
        BigDecimal soma = BigDecimal.ZERO;
        for (BigDecimal valor : valores) {
            if (valor != null) {
                soma = soma.add(valor);
            }
        }
        return soma;
    }

    private static String textoOuTraco(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }
}
