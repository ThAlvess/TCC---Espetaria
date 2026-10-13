package br.com.trevizan.espetinhos.util;

import br.com.trevizan.espetinhos.PanelArredondado;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Filtro de datas das telas de histórico: um cartão branco arredondado com
 * "De [data] até [data]" e o botão "Limpar".
 *
 * <ul>
 *   <li>As duas datas começam vazias: a tela mostra todos os registros.</li>
 *   <li>Só "De" preenchido: tudo a partir daquela data. Só "até": tudo até
 *       aquela data (inclusive o dia inteiro).</li>
 *   <li>Se o início ficar depois do fim, a outra data acompanha, para o
 *       intervalo nunca ficar invertido.</li>
 *   <li>O botão "Limpar" apaga as duas datas e volta a mostrar tudo.</li>
 * </ul>
 *
 * <p>Uso:</p>
 * <pre>
 *   FiltroPeriodo filtro = new FiltroPeriodo(this::recarregar);
 *   ...
 *   if (filtro.contem(comanda.getDataFechamento())) { ... }
 * </pre>
 */
public class FiltroPeriodo extends JPanel {

    private static final Color VERDE = PadraoComponentes.VERDE;
    private static final Color VERDE_CLARO = PadraoComponentes.VERDE_CLARO;
    private static final Font FONTE_ROTULO = new Font("Segoe UI", Font.BOLD, 13);

    private final Runnable aoMudar;
    private final DatePicker dataDe;
    private final DatePicker dataAte;
    private final JButton btnLimpar;
    private boolean ajustandoDatas;

    /** @param aoMudar executado sempre que uma das datas muda */
    public FiltroPeriodo(Runnable aoMudar) {
        this.aoMudar = aoMudar;
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        dataDe = criarDatePicker();
        dataDe.addDateChangeListener(e -> aoMudarData(true));

        dataAte = criarDatePicker();
        dataAte.addDateChangeListener(e -> aoMudarData(false));

        JPanel conteudo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        conteudo.setOpaque(false);
        conteudo.add(new JLabel(PadraoComponentes.iconeCalendario(VERDE)));
        conteudo.add(criarRotulo("De"));
        conteudo.add(dataDe);
        conteudo.add(criarRotulo("até"));
        conteudo.add(dataAte);

        // "Limpar" apaga as duas datas; fica desabilitado enquanto não houver data preenchida
        btnLimpar = new JButton("Limpar");
        btnLimpar.setFont(FONTE_ROTULO);
        btnLimpar.setForeground(VERDE);
        btnLimpar.setBackground(Color.WHITE);
        btnLimpar.setFocusPainted(false);
        btnLimpar.setRequestFocusEnabled(false);
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLimpar.setToolTipText("Apagar as datas e mostrar todos os registros");
        btnLimpar.putClientProperty("FlatLaf.style", "arc: " + PadraoComponentes.ARCO
                + "; borderColor: #196419; hoverBackground: #deebde; margin: 2,12,2,12");
        btnLimpar.setEnabled(false);
        btnLimpar.addActionListener(e -> limpar());
        conteudo.add(btnLimpar);

        // espaçamento lateral igual ao raio, para o conteúdo ficar fora da curva do canto
        int raio = PadraoComponentes.RAIO_CARTAO;
        PanelArredondado cartao = new PanelArredondado(raio);
        cartao.setBackground(Color.WHITE);
        cartao.setLayout(new GridBagLayout());
        cartao.setBorder(BorderFactory.createEmptyBorder(6, raio, 6, raio));
        cartao.add(conteudo, new GridBagConstraints());
        add(cartao);
    }

    /**
     * Indica se a data/hora está dentro do intervalo escolhido (sem datas
     * preenchidas, aceita tudo).
     */
    public boolean contem(LocalDateTime dataHora) {
        LocalDate de = dataDe.getDate();
        LocalDate ate = dataAte.getDate();
        if (de == null && ate == null) {
            return true;
        }
        if (dataHora == null) {
            return false;
        }
        LocalDate dia = dataHora.toLocalDate();
        return (de == null || !dia.isBefore(de)) && (ate == null || !dia.isAfter(ate));
    }

    /** Apaga as duas datas (volta a mostrar todos os registros). */
    public void limpar() {
        ajustandoDatas = true;
        try {
            dataDe.clear();
            dataAte.clear();
        } finally {
            ajustandoDatas = false;
        }
        atualizarBotaoLimpar();
        if (aoMudar != null) {
            aoMudar.run();
        }
    }

    private void atualizarBotaoLimpar() {
        btnLimpar.setEnabled(dataDe.getDate() != null || dataAte.getDate() != null
                || !dataDe.getText().isBlank() || !dataAte.getText().isBlank());
    }

    private void aoMudarData(boolean mudouInicio) {
        if (ajustandoDatas) {
            return;
        }
        atualizarBotaoLimpar();
        LocalDate de = dataDe.getDate();
        LocalDate ate = dataAte.getDate();
        if (de != null && ate != null && de.isAfter(ate)) {
            ajustandoDatas = true;
            try {
                if (mudouInicio) {
                    dataAte.setDate(de);
                } else {
                    dataDe.setDate(ate);
                }
            } finally {
                ajustandoDatas = false;
            }
        }
        if (aoMudar != null) {
            aoMudar.run();
        }
    }

    private DatePicker criarDatePicker() {
        DatePickerSettings configuracao = new DatePickerSettings(new Locale("pt", "BR"));
        configuracao.setColor(DatePickerSettings.DateArea.CalendarBackgroundSelectedDate, VERDE);
        configuracao.setColor(DatePickerSettings.DateArea.BackgroundTodayLabel, VERDE_CLARO);
        configuracao.setColor(DatePickerSettings.DateArea.TextFieldBackgroundValidDate, Color.WHITE);
        configuracao.setColor(DatePickerSettings.DateArea.BackgroundMonthAndYearNavigationButtons, Color.WHITE);
        configuracao.setFormatForDatesCommonEra("dd/MM/yyyy");
        configuracao.setAllowEmptyDates(true);

        DatePicker picker = new DatePicker(configuracao);
        picker.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        picker.getComponentDateTextField().setToolTipText("Deixe em branco para não limitar a data");
        return picker;
    }

    private JLabel criarRotulo(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(FONTE_ROTULO);
        rotulo.setForeground(VERDE);
        return rotulo;
    }
}
