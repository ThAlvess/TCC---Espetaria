package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.PadraoJPanel;

public class MainScreen extends javax.swing.JPanel {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainScreen.class.getName());
    private java.awt.CardLayout cardLayout;
    
    public MainScreen() {
        initComponents();

        CenterPanel.add(new PadraoJPanel(), "mesas");
        CenterPanel.add(new PadraoJPanel(), "historico");
        CenterPanel.add(new PadraoJPanel(), "caixa");
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
        ativarBotao(btnMesas);
        cardLayout.show(CenterPanel, "mesas");
    }                          

    private void btnHistoricoActionPerformed(java.awt.event.ActionEvent evt) {                                             
        ativarBotao(btnHistorico);
        cardLayout.show(CenterPanel, "historico");
    }                                            

    private void btnCaixaActionPerformed(java.awt.event.ActionEvent evt) {                          
        ativarBotao(btnCaixa);
        cardLayout.show(CenterPanel, "caixa");
    }                          

    private void btnProdutosActionPerformed(java.awt.event.ActionEvent evt) {                                          
        ativarBotao(btnProdutos);
        cardLayout.show(CenterPanel, "produtos");
    }                                         

    private void btnRelatoriosActionPerformed(java.awt.event.ActionEvent evt) {                                              
        ativarBotao(btnRelatorios);
        cardLayout.show(CenterPanel, "relatorios");
    }                                             

    private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {                          
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