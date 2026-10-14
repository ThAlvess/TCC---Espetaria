package br.com.trevizan.espetinhos.util;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Campo de texto com máscara de dinheiro ("R$ 0,00").
 *
 * <p>Os números entram pela direita, como em aplicativo de banco: digitar
 * 1, 5, 5 e 0 mostra R$ 0,01 → R$ 0,15 → R$ 1,55 → R$ 15,50. O Backspace
 * apaga o último dígito. Letras e outros símbolos são ignorados. Colar um
 * valor ("15,50", "R$ 1.200,00") também funciona.</p>
 *
 * <p>Uso: {@code CampoMoeda campo = new CampoMoeda();} e
 * {@code BigDecimal valor = campo.getValor();}.</p>
 */
public class CampoMoeda extends JTextField {

    /** Maior valor aceito: R$ 9.999.999,99. */
    private static final long LIMITE_CENTAVOS = 999_999_999L;

    private static final NumberFormat FORMATO =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private long centavos;
    private boolean atualizandoTexto;

    public CampoMoeda() {
        this(BigDecimal.ZERO);
    }

    public CampoMoeda(BigDecimal valorInicial) {
        super(12);
        ((AbstractDocument) getDocument()).setDocumentFilter(new FiltroMoeda());
        PadraoComponentes.aplicarEstiloCampo(this);
        setValor(valorInicial);

        // ao entrar no campo, o valor fica selecionado: digitar substitui o
        // valor sugerido; fora isso, o cursor fica sempre no fim
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(CampoMoeda.this::selectAll);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (getSelectionStart() == getSelectionEnd()) {
                    moverCursorParaOFim();
                }
            }
        });
    }

    /** Valor digitado, com duas casas decimais. */
    public BigDecimal getValor() {
        return BigDecimal.valueOf(centavos, 2);
    }

    /** Define o valor mostrado no campo ({@code null} = zero). */
    public void setValor(BigDecimal valor) {
        long novo = valor == null
                ? 0
                : valor.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValue();
        centavos = Math.max(0, Math.min(novo, LIMITE_CENTAVOS));
        escreverTexto();
    }

    /** Indica se o valor é maior que zero. */
    public boolean temValor() {
        return centavos > 0;
    }

    /** Formata um valor no padrão "R$ 1.234,56". */
    public static String formatar(BigDecimal valor) {
        return FORMATO.format(valor == null ? BigDecimal.ZERO : valor)
                .replace(' ', ' ');
    }

    /**
     * Abre uma janela (OK / Cancelar) pedindo um valor em reais.
     *
     * @param mensagem texto acima do campo (pode ser HTML, começando com
     *                 {@code <html>})
     * @return o valor digitado, ou {@code null} se o usuário cancelar
     */
    public static BigDecimal pedirValor(Component pai, Object mensagem, String titulo,
                                        BigDecimal valorInicial) {
        CampoMoeda campo = new CampoMoeda(valorInicial);

        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.add(mensagem instanceof JComponent
                ? (JComponent) mensagem
                : new JLabel(textoParaRotulo(String.valueOf(mensagem))), BorderLayout.NORTH);
        painel.add(campo, BorderLayout.CENTER);

        focarAoAbrir(campo);

        int opcao = JOptionPane.showConfirmDialog(pai, painel, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        return opcao == JOptionPane.OK_OPTION ? campo.getValor() : null;
    }

    /**
     * Coloca o cursor no componente assim que a janela que o contém abrir
     * (por padrão o JOptionPane deixa o foco no botão OK).
     */
    public static void focarAoAbrir(JComponent componente) {
        componente.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent e) {
                JComponent alvo = e.getComponent();
                alvo.removeAncestorListener(this);
                SwingUtilities.invokeLater(alvo::requestFocusInWindow);
            }

            @Override
            public void ancestorRemoved(AncestorEvent e) {
            }

            @Override
            public void ancestorMoved(AncestorEvent e) {
            }
        });
    }

    /** Textos com quebra de linha viram HTML para o JLabel mostrar várias linhas. */
    private static String textoParaRotulo(String texto) {
        if (!texto.contains("\n") || texto.startsWith("<html>")) {
            return texto;
        }
        String escapado = texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html>" + escapado.replace("\n", "<br>") + "</html>";
    }

    private void moverCursorParaOFim() {
        SwingUtilities.invokeLater(() -> setCaretPosition(getDocument().getLength()));
    }

    private void escreverTexto() {
        atualizandoTexto = true;
        try {
            setText(formatar(getValor()));
        } finally {
            atualizandoTexto = false;
        }
        moverCursorParaOFim();
    }

    private void adicionarDigitos(String texto) {
        if (texto == null) {
            return;
        }
        for (char c : texto.toCharArray()) {
            if (Character.isDigit(c)) {
                long novo = centavos * 10 + (c - '0');
                if (novo > LIMITE_CENTAVOS) {
                    break;
                }
                centavos = novo;
            }
        }
    }

    /**
     * Intercepta toda digitação, colagem e exclusão: em vez de mexer no
     * texto, altera o valor em centavos e reescreve o campo formatado.
     */
    private class FiltroMoeda extends DocumentFilter {

        @Override
        public void insertString(FilterBypass fb, int offset, String texto, AttributeSet attr)
                throws BadLocationException {
            if (atualizandoTexto) {
                super.insertString(fb, offset, texto, attr);
                return;
            }
            adicionarDigitos(texto);
            reescrever(fb);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int tamanho, String texto, AttributeSet attrs)
                throws BadLocationException {
            if (atualizandoTexto) {
                super.replace(fb, offset, tamanho, texto, attrs);
                return;
            }
            // texto todo selecionado e substituído: começa do zero
            if (tamanho > 0 && tamanho >= fb.getDocument().getLength()) {
                centavos = 0;
            }
            adicionarDigitos(texto);
            reescrever(fb);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int tamanho) throws BadLocationException {
            if (atualizandoTexto) {
                super.remove(fb, offset, tamanho);
                return;
            }
            if (tamanho >= fb.getDocument().getLength()) {
                centavos = 0;           // apagou tudo
            } else {
                centavos = centavos / 10; // Backspace/Delete: tira o último dígito
            }
            reescrever(fb);
        }

        private void reescrever(FilterBypass fb) throws BadLocationException {
            // grava o valor formatado direto no documento (sem passar pelo filtro)
            fb.replace(0, fb.getDocument().getLength(), formatar(getValor()), null);
            moverCursorParaOFim();
        }
    }
}
