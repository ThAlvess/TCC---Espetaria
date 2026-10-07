package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PadraoJPanel;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.dao.CaixaDAO;
import java.math.BigDecimal;
import javax.swing.JOptionPane;

public class MainScreen extends javax.swing.JPanel {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainScreen.class.getName());
    private java.awt.CardLayout cardLayout;
    private final Usuario usuarioLogado;
    private Caixa caixaPanel;
    
    /**
     * Creates new form MainScreen
     */
    public MainScreen(Usuario usuarioLogado) {
        initComponents();
        this.usuarioLogado = usuarioLogado;
        verificarAberturaCaixa();
        CenterPanel.add(new MesaPanel(usuarioLogado), "mesas");
        CenterPanel.add(new HistoricoPanel(), "historico");
        caixaPanel = new Caixa();
        CenterPanel.add(caixaPanel, "caixa");
        CenterPanel.add(new ProdutoPanel(), "produtos");
        CenterPanel.add(new Relatorio(), "relatorios");
        CenterPanel.add(new UsuarioPanel(), "usuarios");
        // CenterPanel.add(new UsuarioPanel(), "usuarios"); // Descomente e adicione quando criar a tela de usuários
        
        cardLayout = (java.awt.CardLayout) CenterPanel.getLayout();
        cardLayout.show(CenterPanel, "mesas");
        
        javax.swing.ImageIcon iconeOriginal = (javax.swing.ImageIcon) Logo.getIcon();
        if (iconeOriginal != null) {
            int novaLargura = 120;
            java.awt.Image imagemRedimensionada = iconeOriginal.getImage().getScaledInstance(novaLargura, -1, java.awt.Image.SCALE_SMOOTH);
            Logo.setIcon(new javax.swing.ImageIcon(imagemRedimensionada));
        }
        
        // Aplica o formato arredondado em todos os botões do menu lateral
        btnMesas.putClientProperty("JButton.buttonType", "roundRect");
        btnHistorico.putClientProperty("JButton.buttonType", "roundRect");
        btnCaixa.putClientProperty("JButton.buttonType", "roundRect");
        btnProdutos.putClientProperty("JButton.buttonType", "roundRect");
        btnRelatorios.putClientProperty("JButton.buttonType", "roundRect");
        btnUsuarios.putClientProperty("JButton.buttonType", "roundRect");
        // Adicionado para arredondar o novo botão

        configurarPermissoes();
        ativarBotao(btnMesas);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        PainelMargem = new javax.swing.JPanel();
        LateralPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
                g2.dispose();
            }
        };
        Logo = new javax.swing.JLabel();
        btnMesas = new javax.swing.JButton();
        btnHistorico = new javax.swing.JButton();
        btnCaixa = new javax.swing.JButton();
        btnProdutos = new javax.swing.JButton();
        btnRelatorios = new javax.swing.JButton();
        btnUsuarios = new javax.swing.JButton(); // Renomeado de jButton1 para btnUsuarios
        CenterPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(-60, 0, getWidth() + 30, getHeight(), 30, 30);
                g2.dispose();
            }
        };

        setBackground(new java.awt.Color(235, 225, 226));
        setLayout(new java.awt.BorderLayout());

        PainelMargem.setBackground(new java.awt.Color(235, 225, 226));
        PainelMargem.setLayout(new java.awt.BorderLayout());

        LateralPanel.setBackground(new java.awt.Color(255, 255, 255));
        LateralPanel.setOpaque(false);
        LateralPanel.setPreferredSize(new java.awt.Dimension(200, 600));

        Logo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/TrevizanPequeno.png")));
        Logo.setAlignmentY(0.0F);
        Logo.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 1, 1, 1));

        btnMesas.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnMesas.setForeground(new java.awt.Color(25, 100, 25));
        btnMesas.setText("Mesas");
        btnMesas.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnMesas.addActionListener(this::btnMesasActionPerformed);

        btnHistorico.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnHistorico.setForeground(new java.awt.Color(25, 100, 25));
        btnHistorico.setText("Histórico");
        btnHistorico.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnHistorico.addActionListener(this::btnHistoricoActionPerformed);

        btnCaixa.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnCaixa.setForeground(new java.awt.Color(25, 100, 25));
        btnCaixa.setText("Caixa");
        btnCaixa.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnCaixa.addActionListener(this::btnCaixaActionPerformed);

        btnProdutos.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnProdutos.setForeground(new java.awt.Color(25, 100, 25));
        btnProdutos.setText("Produtos");
        btnProdutos.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnProdutos.addActionListener(this::btnProdutosActionPerformed);

        btnRelatorios.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnRelatorios.setForeground(new java.awt.Color(25, 100, 25));
        btnRelatorios.setText("Relatórios");
        btnRelatorios.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnRelatorios.addActionListener(this::btnRelatoriosActionPerformed);

        // Configuração visual e de evento do novo botão de Usuários
        btnUsuarios.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnUsuarios.setForeground(new java.awt.Color(25, 100, 25));
        btnUsuarios.setText("Usuários");
        btnUsuarios.setMargin(new java.awt.Insets(2, 15, 2, 15));
        btnUsuarios.addActionListener(this::btnUsuariosActionPerformed);

        javax.swing.GroupLayout LateralPanelLayout = new javax.swing.GroupLayout(LateralPanel);
        LateralPanel.setLayout(LateralPanelLayout);
        LateralPanelLayout.setHorizontalGroup(
            LateralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LateralPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(LateralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(Logo)
                    .addComponent(btnMesas, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCaixa, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRelatorios, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        LateralPanelLayout.setVerticalGroup(
            LateralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LateralPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Logo)
                .addGap(18, 18, 18)
                .addComponent(btnMesas, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCaixa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnRelatorios, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(88, Short.MAX_VALUE))
        );

        PainelMargem.add(LateralPanel, java.awt.BorderLayout.WEST);

        CenterPanel.setBackground(new java.awt.Color(245, 242, 238));
        CenterPanel.setOpaque(false);
        CenterPanel.setLayout(new java.awt.CardLayout());
        PainelMargem.add(CenterPanel, java.awt.BorderLayout.CENTER);

        add(PainelMargem, java.awt.BorderLayout.CENTER);
    }

    private void verificarAberturaCaixa() {

        String perfil = usuarioLogado.getPerfil();

        // Somente Caixa e Administrador podem iniciar o caixa
        if (!"Caixa".equalsIgnoreCase(perfil)
                && !"Administrador".equalsIgnoreCase(perfil)) {
            return;
        }

        CaixaDAO caixaDAO = new CaixaDAO();

        br.com.trevizan.espetinhos.model.Caixa caixaAberto =
                caixaDAO.buscarCaixaAberto();

        if (caixaAberto != null) {

            // O caixa aberto pertence ao dia atual.
            // Continua utilizando normalmente.
            if (caixaDAO.caixaAbertoEhDeHoje(caixaAberto)) {
                return;
            }

            // Existe um caixa aberto de um dia anterior.
            BigDecimal totalVendas =
                    caixaDAO.calcularTotalVendas(caixaAberto.getIdCaixa());

            BigDecimal sangrias =
                    caixaDAO.calcularSangrias(caixaAberto.getIdCaixa());

            BigDecimal valorFinal =
                    caixaAberto.getValorInicial()
                            .add(totalVendas)
                            .subtract(sangrias);

            caixaDAO.fecharCaixa(
                    caixaAberto.getIdCaixa(),
                    valorFinal
            );

            JOptionPane.showMessageDialog(
                    this,
                    "O caixa anterior foi fechado automaticamente.\n\n"
                            + "Data: "
                            + caixaAberto.getDataHoraAbertura()
                            .toLocalDate()
                            .format(
                                    java.time.format.DateTimeFormatter
                                            .ofPattern("dd/MM/yyyy")
                            )
                            + "\nValor final: R$ "
                            + valorFinal.setScale(
                            2,
                            java.math.RoundingMode.HALF_UP
                    ),
                    "Fechamento automático",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        while (true) {

            String valorInformado = javax.swing.JOptionPane.showInputDialog(
                    this,
                    "Informe o valor inicial do caixa:",
                    "Abertura de Caixa",
                    javax.swing.JOptionPane.QUESTION_MESSAGE
            );

            // Usuário clicou em Cancelar
            if (valorInformado == null) {
                return;
            }

            valorInformado = valorInformado
                    .trim()
                    .replace(",", ".");

            try {

                java.math.BigDecimal valorInicial =
                        new java.math.BigDecimal(valorInformado);

                if (valorInicial.compareTo(java.math.BigDecimal.ZERO) < 0) {
                    javax.swing.JOptionPane.showMessageDialog(
                            this,
                            "O valor inicial não pode ser negativo.",
                            "Valor inválido",
                            javax.swing.JOptionPane.WARNING_MESSAGE
                    );
                    continue;
                }

                br.com.trevizan.espetinhos.model.Caixa novoCaixa =
                        new br.com.trevizan.espetinhos.model.Caixa();

                novoCaixa.setUsuarioAbertura(usuarioLogado);
                novoCaixa.setValorInicial(valorInicial);

                caixaDAO.abrirCaixa(novoCaixa);

                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Caixa aberto com sucesso!\n"
                                + "Valor inicial: R$ "
                                + valorInicial.setScale(
                                2,
                                java.math.RoundingMode.HALF_UP
                        ),
                        "Caixa aberto",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE
                );

                break;

            } catch (NumberFormatException e) {

                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Informe um valor válido.\nExemplo: 150,00",
                        "Valor inválido",
                        javax.swing.JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    private boolean usuarioEhAdministrador() {
        return usuarioLogado != null
                && usuarioLogado.getPerfil() != null
                && "Administrador".equalsIgnoreCase(usuarioLogado.getPerfil());
    }

    private boolean usuarioPodeAcessarCaixa() {
        if (usuarioLogado == null || usuarioLogado.getPerfil() == null) {
            return false;
        }

        String perfil = usuarioLogado.getPerfil();

        return "Caixa".equalsIgnoreCase(perfil)
                || "Administrador".equalsIgnoreCase(perfil);
    }

    private void configurarPermissoes() {

        String perfil = usuarioLogado.getPerfil();

        // Administrador pode acessar tudo
        if ("Administrador".equalsIgnoreCase(perfil)) {
            return;
        }

        // Caixa: Mesas + Caixa
        if ("Caixa".equalsIgnoreCase(perfil)) {

            btnMesas.setVisible(true);
            btnCaixa.setVisible(true);

            btnHistorico.setVisible(false);
            btnProdutos.setVisible(false);
            btnRelatorios.setVisible(false);
            btnUsuarios.setVisible(false);

            return;
        }

        // Atendente: somente Mesas
        if ("Atendente".equalsIgnoreCase(perfil)) {

            btnMesas.setVisible(true);

            btnHistorico.setVisible(false);
            btnCaixa.setVisible(false);
            btnProdutos.setVisible(false);
            btnRelatorios.setVisible(false);
            btnUsuarios.setVisible(false);

            return;
        }

        // Segurança para perfil desconhecido
        btnHistorico.setVisible(false);
        btnCaixa.setVisible(false);
        btnProdutos.setVisible(false);
        btnRelatorios.setVisible(false);
        btnUsuarios.setVisible(false);
    }

    private void resetarBotoes() {
        java.awt.Color verdeEscuro = new java.awt.Color(25, 100, 25);
        java.awt.Color branco = java.awt.Color.WHITE;
        
        // Incluído btnUsuarios no array para que ele também resete a cor ao trocar de aba
        javax.swing.JButton[] botoes = {btnMesas, btnHistorico, btnCaixa, btnProdutos, btnRelatorios, btnUsuarios};
        
        for (javax.swing.JButton b : botoes) {
            b.setBackground(branco);
            b.setForeground(verdeEscuro);
            
            b.putClientProperty("JButton.buttonType", "roundRect");
            b.putClientProperty("JComponent.arc", 999); 
            b.putClientProperty("JButton.borderColor", verdeEscuro); 
        }
    }

    private void ativarBotao(javax.swing.JButton botaoAtivo) {
        resetarBotoes();
        java.awt.Color verdeEscuro = new java.awt.Color(25, 100, 25);
        botaoAtivo.setBackground(verdeEscuro); 
        botaoAtivo.setForeground(java.awt.Color.WHITE);
    }
    
    private void btnMesasActionPerformed(java.awt.event.ActionEvent evt) {
        configurarPermissoes();
        ativarBotao(btnMesas);
        cardLayout.show(CenterPanel, "mesas");
    }                          

    private void btnHistoricoActionPerformed(java.awt.event.ActionEvent evt) {

        if (!usuarioEhAdministrador()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Apenas o Administrador possui acesso a esta função.",
                    "Acesso negado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        ativarBotao(btnHistorico);
        cardLayout.show(CenterPanel, "historico");
    }                                            

    private void btnCaixaActionPerformed(java.awt.event.ActionEvent evt) {

        if (!usuarioPodeAcessarCaixa()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seu usuário não possui permissão para acessar o Caixa.",
                    "Acesso negado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        ativarBotao(btnCaixa);
        caixaPanel.atualizarDados();
        cardLayout.show(CenterPanel, "caixa");
    }                          

    private void btnProdutosActionPerformed(java.awt.event.ActionEvent evt) {

        if (!usuarioEhAdministrador()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Apenas o Administrador possui acesso a esta função.",
                    "Acesso negado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }


        ativarBotao(btnProdutos);
        cardLayout.show(CenterPanel, "produtos");
    }                                         

    private void btnRelatoriosActionPerformed(java.awt.event.ActionEvent evt) {

        if (!usuarioEhAdministrador()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Apenas o Administrador possui acesso a esta função.",
                    "Acesso negado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        ativarBotao(btnRelatorios);
        cardLayout.show(CenterPanel, "relatorios");
    }                                             

    private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {

        if (!usuarioEhAdministrador()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Apenas o Administrador possui acesso a esta função.",
                    "Acesso negado",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

    ativarBotao(btnUsuarios);
    cardLayout.show(CenterPanel, "usuarios");
}
    // Variables declaration - do not modify                     
    private javax.swing.JPanel CenterPanel;
    private javax.swing.JPanel LateralPanel;
    private javax.swing.JLabel Logo;
    private javax.swing.JPanel PainelMargem;
    private javax.swing.JButton btnCaixa;
    private javax.swing.JButton btnHistorico;
    private javax.swing.JButton btnMesas;
    private javax.swing.JButton btnProdutos;
    private javax.swing.JButton btnRelatorios;
    private javax.swing.JButton btnUsuarios; // Substituiu o jButton1
    // End of variables declaration                   
}