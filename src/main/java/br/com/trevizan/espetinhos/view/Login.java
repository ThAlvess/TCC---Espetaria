package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.UsuarioDAO;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.util.LogoNitida;
import br.com.trevizan.espetinhos.util.PadraoComponentes;
import br.com.trevizan.espetinhos.util.PadraoTela;
import br.com.trevizan.espetinhos.util.SessaoUsuario;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author thiag
 */
public class Login extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(Login.class.getName());

    /**
     * Creates new form Login
     */
    public Login() {

        initComponents();

        // Logo + cartão de login sempre no centro da janela (maximizada ou não)
        centralizarConteudo();

        // Permite pressionar ENTER para executar o login
        getRootPane().setDefaultButton(jButton2);

        // Centraliza a janela
        setLocationRelativeTo(null);

        // Botão de "olho" dentro do campo para mostrar/ocultar a senha
        br.com.trevizan.espetinhos.util.PadraoComponentes.habilitarMostrarSenha(jPasswordField1);
    }

    /**
     * Reorganiza a tela de login em tempo de execução (o initComponents()
     * gerado pelo NetBeans não é alterado):
     * <ul>
     *   <li>logo + cartão centralizados na janela com GridBagLayout; o
     *       tamanho original da janela passa a ser o tamanho mínimo;</li>
     *   <li>fundo bege, cartão branco arredondado, botões e campos
     *       estilizados com {@link PadraoComponentes};</li>
     *   <li>logo desenhada com {@link LogoNitida}.</li>
     * </ul>
     */
    private void centralizarConteudo() {
        java.awt.Dimension tamanhoOriginal = getSize();

        // ---------- logo em alta resolução ----------
        javax.swing.Icon logoNitida = LogoNitida.criar(177);
        if (logoNitida != null) {
            Logo.setIcon(logoNitida);
        }
        Logo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        // ---------- textos ----------
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        jLabel1.setForeground(PadraoTela.COR_TITULO);
        jLabel2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabel2.setForeground(PadraoTela.COR_SUBTITULO);
        for (javax.swing.JLabel rotulo : new javax.swing.JLabel[]{jLabelUsuário, jLabelSenha}) {
            rotulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            rotulo.setForeground(PadraoComponentes.COR_TEXTO);
        }

        // ---------- campos e botões ----------
        PadraoComponentes.aplicarEstiloCampo(jTextField1);
        PadraoComponentes.aplicarEstiloCampo(jPasswordField1);
        PadraoComponentes.aplicarEstiloPrimario(jButton2);   // Entrar
        PadraoComponentes.aplicarEstiloSecundario(jButton1); // Esqueci a senha

        // ---------- cartão branco arredondado ----------
        javax.swing.JPanel cartao = PadraoComponentes.criarCartao();
        cartao.setLayout(new javax.swing.BoxLayout(cartao, javax.swing.BoxLayout.Y_AXIS));
        cartao.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 46, 30, 46));
        java.awt.Dimension tamanhoCartao = new java.awt.Dimension(449, 350);
        cartao.setPreferredSize(tamanhoCartao);
        cartao.setMinimumSize(tamanhoCartao);
        cartao.setMaximumSize(tamanhoCartao);
        cartao.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        java.awt.Dimension alturaCampo = new java.awt.Dimension(Integer.MAX_VALUE, PadraoComponentes.ALTURA_BOTAO);

        javax.swing.JPanel linhaBotoes = new javax.swing.JPanel(new java.awt.GridLayout(1, 2, 16, 0));
        linhaBotoes.setOpaque(false);
        linhaBotoes.add(jButton2);
        linhaBotoes.add(jButton1);
        linhaBotoes.setMaximumSize(alturaCampo);
        linhaBotoes.setPreferredSize(new java.awt.Dimension(100, PadraoComponentes.ALTURA_BOTAO));

        // todos alinhados à esquerda no BoxLayout, ocupando a largura toda;
        // os títulos centralizam o próprio texto
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        javax.swing.JComponent[] componentes = {
            jLabel1, jLabel2, jLabelUsuário, jTextField1, jLabelSenha, jPasswordField1, linhaBotoes
        };
        for (javax.swing.JComponent componente : componentes) {
            componente.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        }
        jLabel1.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, jLabel1.getPreferredSize().height));
        jLabel2.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, jLabel2.getPreferredSize().height));
        jTextField1.setMaximumSize(alturaCampo);
        jPasswordField1.setMaximumSize(alturaCampo);
        jTextField1.setPreferredSize(new java.awt.Dimension(100, PadraoComponentes.ALTURA_BOTAO));
        jPasswordField1.setPreferredSize(new java.awt.Dimension(100, PadraoComponentes.ALTURA_BOTAO));

        cartao.add(jLabel1);
        cartao.add(javax.swing.Box.createVerticalStrut(4));
        cartao.add(jLabel2);
        cartao.add(javax.swing.Box.createVerticalStrut(22));
        cartao.add(jLabelUsuário);
        cartao.add(javax.swing.Box.createVerticalStrut(6));
        cartao.add(jTextField1);
        cartao.add(javax.swing.Box.createVerticalStrut(14));
        cartao.add(jLabelSenha);
        cartao.add(javax.swing.Box.createVerticalStrut(6));
        cartao.add(jPasswordField1);
        cartao.add(javax.swing.Box.createVerticalStrut(26));
        cartao.add(linhaBotoes);
        cartao.add(javax.swing.Box.createVerticalGlue());
        for (java.awt.Component filho : cartao.getComponents()) {
            if (filho instanceof javax.swing.JComponent jc) {
                jc.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            }
        }

        // ---------- bloco centralizado ----------
        javax.swing.JPanel bloco = new javax.swing.JPanel();
        bloco.setOpaque(false);
        bloco.setLayout(new javax.swing.BoxLayout(bloco, javax.swing.BoxLayout.Y_AXIS));
        bloco.add(Logo);
        bloco.add(javax.swing.Box.createVerticalStrut(10));
        bloco.add(cartao);

        java.awt.Container conteudo = getContentPane();
        conteudo.removeAll();
        conteudo.setBackground(PadraoComponentes.COR_FUNDO);
        conteudo.setLayout(new java.awt.GridBagLayout());
        conteudo.add(bloco, new java.awt.GridBagConstraints());

        setMinimumSize(tamanhoOriginal);
        setSize(tamanhoOriginal);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PainelCartao = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelUsuário = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabelSenha = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        Logo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setName("Frame"); // NOI18N

        PainelCartao.setBackground(new java.awt.Color(255, 255, 255));
        PainelCartao.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        PainelCartao.setPreferredSize(new java.awt.Dimension(450, 400));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(25, 100, 25));
        jLabel1.setText("BEM-VINDO!");

        jLabel2.setForeground(new java.awt.Color(25, 100, 25));
        jLabel2.setText("Faça login preenchendo os dados do seu usuário abaixo:");

        jLabelUsuário.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabelUsuário.setText("Usuário:");

        jTextField1.setBackground(new java.awt.Color(242, 242, 242));
        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        jLabelSenha.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabelSenha.setText("Senha:");

        jPasswordField1.setBackground(new java.awt.Color(242, 242, 242));

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jButton1.setText("Esqueci a senha");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setBackground(new java.awt.Color(0, 102, 0));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Entrar");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        javax.swing.GroupLayout PainelCartaoLayout = new javax.swing.GroupLayout(PainelCartao);
        PainelCartao.setLayout(PainelCartaoLayout);
        PainelCartaoLayout.setHorizontalGroup(
            PainelCartaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PainelCartaoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(177, 177, 177))
            .addGroup(PainelCartaoLayout.createSequentialGroup()
                .addGroup(PainelCartaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PainelCartaoLayout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addComponent(jLabel2))
                    .addGroup(PainelCartaoLayout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(PainelCartaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabelUsuário)
                            .addComponent(jTextField1)
                            .addComponent(jLabelSenha)
                            .addComponent(jPasswordField1)
                            .addGroup(PainelCartaoLayout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        PainelCartaoLayout.setVerticalGroup(
            PainelCartaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PainelCartaoLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(24, 24, 24)
                .addComponent(jLabelUsuário)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabelSenha)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(PainelCartaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 70, Short.MAX_VALUE))
        );

        Logo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/TrevizanPequeno.png"))); // NOI18N
        Logo.setAlignmentY(0.0F);
        Logo.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 1, 1, 1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(291, 291, 291)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(PainelCartao, javax.swing.GroupLayout.PREFERRED_SIZE, 449, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Logo))
                .addContainerGap(340, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(Logo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PainelCartao, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(131, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void jTextField1ActionPerformed(
            java.awt.event.ActionEvent evt
    ) {//GEN-FIRST:event_jTextField1ActionPerformed

        // Ao pressionar Enter no usuário, passa para a senha
        jPasswordField1.requestFocus();

    }//GEN-LAST:event_jTextField1ActionPerformed


    private void jButton1ActionPerformed(
            java.awt.event.ActionEvent evt
    ) {//GEN-FIRST:event_jButton1ActionPerformed

        JOptionPane.showMessageDialog(
                this,
                "Peça ao administrador para resetar a sua senha\n"
                        + "(tela Usuários → botão \"Resetar senha\").\n\n"
                        + "Depois, entre com a senha padrão e crie uma senha nova.",
                "Esqueci a senha",
                JOptionPane.INFORMATION_MESSAGE
        );

    }//GEN-LAST:event_jButton1ActionPerformed


    private void jButton2ActionPerformed(
            java.awt.event.ActionEvent evt
    ) {//GEN-FIRST:event_jButton2ActionPerformed

        String login =
                jTextField1.getText().trim();

        String senha =
                new String(
                        jPasswordField1.getPassword()
                );

        if (login.isEmpty() || senha.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Preencha o usuário e a senha.",
                    "Campos obrigatórios",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            UsuarioDAO usuarioDAO =
                    new UsuarioDAO();

            Usuario usuario =
                    usuarioDAO.autenticar(
                            login,
                            senha
                    );

            if (usuario != null) {

                // troca obrigatória marcada no banco (usuário novo ou senha resetada)
                // ou login com a senha padrão: abre a criação da senha pessoal
                if (usuario.isTrocaSenhaObrigatoria()
                        || br.com.trevizan.espetinhos.util.SenhaUtil.SENHA_PADRAO.equals(senha)) {
                    boolean senhaCriada = TrocarSenhaDialog.mostrar(this, usuario, true);
                    if (!senhaCriada) {
                        jPasswordField1.setText("");
                        jPasswordField1.requestFocusInWindow();
                        return;
                    }
                }

                JOptionPane.showMessageDialog(
                        this,
                        "Bem-vindo, "
                                + usuario.getNome()
                                + "!"
                );

                SessaoUsuario.login(usuario);

                abrirMainScreen(usuario);

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Usuário ou senha inválidos.",
                        "Erro de autenticação",
                        JOptionPane.ERROR_MESSAGE
                );

                // Mantém o usuário digitado e limpa só a senha,
                // já deixando o cursor nela para digitar de novo
                jPasswordField1.setText("");
                jPasswordField1.requestFocusInWindow();
            }

        } catch (RuntimeException e) {

            // Pega a causa original do erro para mostrar ao usuário
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível acessar o banco de dados.\n"
                            + "Verifique a conexão com o MySQL.\n\n"
                            + "Detalhe: " + causa.getMessage(),
                    "Erro de conexão",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }

    }//GEN-LAST:event_jButton2ActionPerformed



    /**
     * Mantém o JFrame do Login aberto e substitui
     * seu conteúdo pela MainScreen.
     *
     * A MainScreen deve estender JPanel.
     */
    private void abrirMainScreen(Usuario usuarioLogado) {

        MainScreen mainScreen =
                new MainScreen(usuarioLogado);

        setContentPane(mainScreen);

        setTitle("Espetinhos Trevizan");

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        revalidate();
        repaint();
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        // Ativa o FlatLaf
        com.formdev.flatlaf.FlatLightLaf.setup();

        java.awt.EventQueue.invokeLater(
                () -> {

                    Login login =
                            new Login();

                    login.setVisible(true);
                }
        );
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Logo;
    private javax.swing.JPanel PainelCartao;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelSenha;
    private javax.swing.JLabel jLabelUsuário;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}