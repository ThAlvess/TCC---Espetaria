package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.UsuarioDAO;
import br.com.trevizan.espetinhos.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.ParseException;
import java.util.List;

public class UsuarioPanel extends javax.swing.JPanel {

    private JTextField txtBusca;
    private JButton btnCadastrar, btnEditar;
    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private int linhaSelecionada = -1;
    private UsuarioDAO usuarioDAO;

    public UsuarioPanel() {
        // Inicializa o objeto de acesso a dados dos usuários
        usuarioDAO = new UsuarioDAO();

        // Define o gerenciador de layout principal do painel como BorderLayout com espaçamento
        setLayout(new BorderLayout(10, 15));
        // Define a cor de fundo padrão idêntica à tela de mesas
        setBackground(new Color(238, 232, 227));
        // Aplica uma margem interna de preenchimento (padding) nas bordas do painel
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Painel superior para o título e o subtítulo (padrão visual da tela de mesas)
        JPanel panelTopo = new JPanel();
        panelTopo.setLayout(new BoxLayout(panelTopo, BoxLayout.Y_AXIS));
        panelTopo.setOpaque(false);

        JLabel lblTitulo = new JLabel("CADASTRO DE USUÁRIOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(40, 40, 40));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Gerencie o acesso, perfis e credenciais dos operadores do sistema.");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(120, 120, 120));
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitulo.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        panelTopo.add(lblTitulo);
        panelTopo.add(lblSubtitulo);
        add(panelTopo, BorderLayout.NORTH);

        // Cria o painel central utilizando GridBagLayout para organizar a tabela estruturalmente
        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cria o painel contentor da tabela com visual de card arredondado e limpo
        JPanel panelTabelaContainer = new JPanel(new BorderLayout(5, 5));
        panelTabelaContainer.setBackground(Color.WHITE);
        panelTabelaContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 215, 210), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Painel superior da tabela contendo os botões de ação e o campo de busca
        JPanel panelBusca = new JPanel(new BorderLayout());
        panelBusca.setOpaque(false);
        panelBusca.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel panelBotoesEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBotoesEsquerda.setOpaque(false);

        btnCadastrar = criarBotaoEstilizado("Cadastrar", new Color(25, 110, 45), Color.WHITE);
        btnEditar = criarBotaoEstilizado("Editar Usuário", new Color(30, 100, 180), Color.WHITE);

        panelBotoesEsquerda.add(btnCadastrar);
        panelBotoesEsquerda.add(btnEditar);

        JPanel panelBuscaDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelBuscaDireita.setOpaque(false);
        
        JLabel lblBusca = new JLabel("Buscar Usuário:");
        lblBusca.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBusca.setForeground(new Color(90, 90, 90));
        panelBuscaDireita.add(lblBusca);
        
        txtBusca = new JTextField(18);
        panelBuscaDireita.add(txtBusca);

        panelBusca.add(panelBotoesEsquerda, BorderLayout.WEST);
        panelBusca.add(panelBuscaDireita, BorderLayout.EAST);

        panelTabelaContainer.add(panelBusca, BorderLayout.NORTH);

        // Define o modelo da tabela (agora somente leitura)
        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Status", "Nome", "Usuário", "CPF", "Perfil", "Senha"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setRowHeight(28);
        tabelaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaUsuarios.setGridColor(new Color(235, 230, 225));
        tabelaUsuarios.getTableHeader().setBackground(new Color(248, 246, 242));
        tabelaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) tabelaUsuarios.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        tabelaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabelaUsuarios.getColumnModel().getColumn(1).setMinWidth(80);
        tabelaUsuarios.getColumnModel().getColumn(1).setMaxWidth(120);

        tabelaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(140);
        tabelaUsuarios.getColumnModel().getColumn(5).setMinWidth(100);
        tabelaUsuarios.getColumnModel().getColumn(5).setMaxWidth(180);

        tabelaUsuarios.getColumnModel().getColumn(6).setCellRenderer(new SenhaOcultaCellRenderer());

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaUsuarios.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 225, 220), 1));
        
        panelTabelaContainer.add(scrollPane, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        panelCentro.add(panelTabelaContainer, gbc);

        add(panelCentro, BorderLayout.CENTER);

        carregarTabela();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                carregarTabela();
                tabelaUsuarios.clearSelection();
                linhaSelecionada = -1;
            }
        });

        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrar(); }

            private void filtrar() {
                String texto = txtBusca.getText();
                if (texto.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(regexFilterSafe(texto));
                }
            }

            private RowFilter<DefaultTableModel, Object> regexFilterSafe(String texto) {
                try {
                    return RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto));
                } catch (Exception ex) {
                    return RowFilter.regexFilter("(?i)" + texto);
                }
            }
        });

        btnCadastrar.addActionListener(e -> abrirJanelaCadastro());
        btnEditar.addActionListener(e -> abrirJanelaEdicao());

        tabelaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = tabelaUsuarios.getSelectedRow();
                if (viewRow != -1) {
                    linhaSelecionada = tabelaUsuarios.convertRowIndexToModel(viewRow);
                } else {
                    linhaSelecionada = -1;
                }
            }
        });
    }

    private JButton criarBotaoEstilizado(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        List<Usuario> lista = usuarioDAO.listar();
        if (lista != null) {
            for (Usuario u : lista) {
                Object[] linha = new Object[7];
                linha[0] = u.getIdUsuario();
                linha[1] = Boolean.TRUE.equals(u.isAtivo()) ? "Ativo" : "Inativo";
                linha[2] = u.getNome();
                linha[3] = u.getLogin();
                
                String cpfBruto = u.getCpf();
                if (cpfBruto != null) {
                    cpfBruto = cpfBruto.replaceAll("\\D", "");
                    if (cpfBruto.length() == 11) {
                        cpfBruto = cpfBruto.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
                    }
                }
                linha[4] = cpfBruto;
                linha[5] = u.getPerfil();
                linha[6] = u.getSenha();
                modeloTabela.addRow(linha);
            }
        }
    }

    private void abrirJanelaCadastro() {
        // Confirmação de Segurança do Administrador antes de abrir a janela
        JPasswordField txtSenhaAdmin = new JPasswordField(15);
        Object[] mensagemAdmin = {
            "Digite a senha do administrador para continuar:", txtSenhaAdmin
        };

        int opcaoAdmin = JOptionPane.showConfirmDialog(
            this, 
            mensagemAdmin, 
            "Confirmação de Segurança", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );

        if (opcaoAdmin != JOptionPane.OK_OPTION) {
            return;
        }

        String senhaAdmin = new String(txtSenhaAdmin.getPassword());
        Usuario adminAutenticado = usuarioDAO.autenticar("admin", senhaAdmin);

        if (adminAutenticado == null || !"Administrador".equalsIgnoreCase(adminAutenticado.getPerfil())) {
            JOptionPane.showMessageDialog(this, "Senha incorreta ou usuário sem privilégios de Administrador!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialogCadastro = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cadastrar Usuário", true);
        dialogCadastro.setSize(420, 360);
        dialogCadastro.setLocationRelativeTo(this);
        dialogCadastro.setLayout(null);
        dialogCadastro.getContentPane().setBackground(Color.WHITE);
        dialogCadastro.setResizable(false);

        JLabel lblCNome = new JLabel("Nome:");
        lblCNome.setBounds(25, 20, 80, 25);
        final JTextField txtNome = new JTextField();
        // Filtro para bloquear números no campo Nome
        txtNome.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                if (str == null) return;
                if (!str.matches(".*\\d.*")) {
                    super.insertString(offset, str, attr);
                }
            }
        });
        txtNome.setBounds(95, 20, 280, 28);

        JLabel lblCCpf = new JLabel("CPF:");
        lblCCpf.setBounds(25, 60, 80, 25);
        JFormattedTextField tempCpf;
        try {
            MaskFormatter maskCpf = new MaskFormatter("###.###.###-##");
            maskCpf.setPlaceholderCharacter('_');
            tempCpf = new JFormattedTextField(maskCpf);
        } catch (ParseException e) {
            tempCpf = new JFormattedTextField();
        }
        final JFormattedTextField txtCpf = tempCpf;
        txtCpf.setBounds(95, 60, 280, 28);

        JLabel lblCUsuario = new JLabel("Usuário:");
        lblCUsuario.setBounds(25, 100, 80, 25);
        final JTextField txtUsuario = new JTextField();
        txtUsuario.setBounds(95, 100, 280, 28);

        JLabel lblCSenha = new JLabel("Senha:");
        lblCSenha.setBounds(25, 140, 80, 25);
        final JPasswordField txtSenha = new JPasswordField();
        txtSenha.setBounds(95, 140, 280, 28);

        JLabel lblCConfirmaSenha = new JLabel("Confirmar:");
        lblCConfirmaSenha.setBounds(25, 180, 80, 25);
        final JPasswordField txtConfirmaSenha = new JPasswordField();
        txtConfirmaSenha.setBounds(95, 180, 280, 28);

        JLabel lblCPerfil = new JLabel("Perfil:");
        lblCPerfil.setBounds(25, 220, 80, 25);
        final JComboBox<String> cbPerfil = new JComboBox<>(new String[]{"Caixa", "Gerente", "Administrador"});
        cbPerfil.setBounds(95, 220, 280, 28);
        cbPerfil.setBackground(Color.WHITE);

        JButton btnSalvarCadastro = criarBotaoEstilizado("Cadastrar", new Color(25, 110, 45), Color.WHITE);
        btnSalvarCadastro.setBounds(145, 275, 120, 35);

        dialogCadastro.add(lblCNome); dialogCadastro.add(txtNome);
        dialogCadastro.add(lblCCpf); dialogCadastro.add(txtCpf);
        dialogCadastro.add(lblCUsuario); dialogCadastro.add(txtUsuario);
        dialogCadastro.add(lblCSenha); dialogCadastro.add(txtSenha);
        dialogCadastro.add(lblCConfirmaSenha); dialogCadastro.add(txtConfirmaSenha);
        dialogCadastro.add(lblCPerfil); dialogCadastro.add(cbPerfil);
        dialogCadastro.add(btnSalvarCadastro);

        btnSalvarCadastro.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            String cpf = txtCpf.getText().replaceAll("\\D", "");
            String login = txtUsuario.getText().trim();
            String senha = new String(txtSenha.getPassword());
            String confirmaSenha = new String(txtConfirmaSenha.getPassword());
            String perfil = (String) cbPerfil.getSelectedItem();

            if (nome.isEmpty() || login.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(dialogCadastro, "Preencha todos os campos obrigatórios!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (senha.length() < 6) {
                JOptionPane.showMessageDialog(dialogCadastro, "A senha deve ter no mínimo 6 caracteres!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!senha.equals(confirmaSenha)) {
                JOptionPane.showMessageDialog(dialogCadastro, "As senhas não coincidem!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Usuario u = new Usuario();
            u.setNome(nome);
            u.setCpf(cpf);
            u.setLogin(login);
            u.setSenha(senha);
            u.setPerfil(perfil);
            u.setAtivo(true);

            try {
                usuarioDAO.cadastrar(u);
                JOptionPane.showMessageDialog(dialogCadastro, "Usuário cadastrado com sucesso!");
                dialogCadastro.dispose();
                carregarTabela();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogCadastro, "Erro ao cadastrar usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogCadastro.setVisible(true);
    }

    private void abrirJanelaEdicao() {
        if (linhaSelecionada == -1 && tabelaUsuarios.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int viewRow = tabelaUsuarios.getSelectedRow();
        if (viewRow != -1) {
            linhaSelecionada = tabelaUsuarios.convertRowIndexToModel(viewRow);
        }

        // Confirmação de Segurança do Administrador
        JPasswordField txtSenhaAdmin = new JPasswordField(15);
        Object[] mensagemAdmin = {
            "Digite a senha do administrador para continuar:", txtSenhaAdmin
        };

        int opcaoAdmin = JOptionPane.showConfirmDialog(
            this, 
            mensagemAdmin, 
            "Confirmação de Segurança", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );

        if (opcaoAdmin != JOptionPane.OK_OPTION) {
            return;
        }

        String senhaAdmin = new String(txtSenhaAdmin.getPassword());
        Usuario adminAutenticado = usuarioDAO.autenticar("admin", senhaAdmin);

        if (adminAutenticado == null || !"Administrador".equalsIgnoreCase(adminAutenticado.getPerfil())) {
            JOptionPane.showMessageDialog(this, "Senha incorreta ou usuário sem privilégios de Administrador!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Pega os dados atuais da linha selecionada
        int id = Integer.parseInt(modeloTabela.getValueAt(linhaSelecionada, 0).toString());
        String statusAtual = modeloTabela.getValueAt(linhaSelecionada, 1).toString();
        String loginAtual = modeloTabela.getValueAt(linhaSelecionada, 3).toString();
        String cpfAtual = modeloTabela.getValueAt(linhaSelecionada, 4).toString().replaceAll("\\D", "");
        String perfilAtual = modeloTabela.getValueAt(linhaSelecionada, 5).toString();
        
        // Pega o nome e a senha real direto do banco para garantir que venham corretos
        Usuario usuarioBanco = usuarioDAO.buscarPorId(id);
        String nomeAtual = usuarioBanco != null && usuarioBanco.getNome() != null ? usuarioBanco.getNome() : "";
        String senhaAtual = usuarioBanco != null ? usuarioBanco.getSenha() : "";

        // Cria a nova janela menor (Dialog) com os campos preenchidos
        JDialog dialogEdicao = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Editar Usuário", true);
        dialogEdicao.setSize(420, 360);
        dialogEdicao.setLocationRelativeTo(this);
        dialogEdicao.setLayout(null);
        dialogEdicao.getContentPane().setBackground(Color.WHITE);
        dialogEdicao.setResizable(false);

        JLabel lblENome = new JLabel("Nome:");
        lblENome.setBounds(25, 20, 80, 25);
        final JTextField editNome = new JTextField();
        // Aplica o documento customizado com o filtro de números e define o valor inicial corretamente
        editNome.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                if (str == null) return;
                if (!str.matches(".*\\d.*")) {
                    super.insertString(offset, str, attr);
                }
            }
        });
        editNome.setText(nomeAtual);
        editNome.setBounds(95, 20, 280, 28);

        JLabel lblECpf = new JLabel("CPF:");
        lblECpf.setBounds(25, 60, 80, 25);
        JFormattedTextField tempCpf;
        try {
            MaskFormatter maskCpf = new MaskFormatter("###.###.###-##");
            maskCpf.setPlaceholderCharacter('_');
            tempCpf = new JFormattedTextField(maskCpf);
        } catch (ParseException e) {
            tempCpf = new JFormattedTextField();
        }
        final JFormattedTextField editCpf = tempCpf;
        editCpf.setText(cpfAtual);
        editCpf.setBounds(95, 60, 280, 28);

        JLabel lblELogin = new JLabel("Usuário:");
        lblELogin.setBounds(25, 100, 80, 25);
        final JTextField editLogin = new JTextField(loginAtual);
        editLogin.setBounds(95, 100, 280, 28);

        JLabel lblESenha = new JLabel("Senha:");
        lblESenha.setBounds(25, 140, 80, 25);
        final JPasswordField editSenha = new JPasswordField(senhaAtual);
        editSenha.setBounds(95, 140, 280, 28);

        JLabel lblEPerfil = new JLabel("Perfil:");
        lblEPerfil.setBounds(25, 180, 80, 25);
        final JComboBox<String> editPerfil = new JComboBox<>(new String[]{"Caixa", "Gerente", "Administrador"});
        editPerfil.setSelectedItem(perfilAtual);
        editPerfil.setBounds(95, 180, 280, 28);

        JLabel lblEStatus = new JLabel("Status:");
        lblEStatus.setBounds(25, 220, 80, 25);
        final JComboBox<String> editStatus = new JComboBox<>(new String[]{"Ativo", "Inativo"});
        editStatus.setSelectedItem(statusAtual);
        editStatus.setBounds(95, 220, 280, 28);

        JButton btnSalvarEdicao = criarBotaoEstilizado("Salvar", new Color(30, 100, 180), Color.WHITE);
        btnSalvarEdicao.setBounds(145, 275, 120, 35);

        dialogEdicao.add(lblENome); dialogEdicao.add(editNome);
        dialogEdicao.add(lblECpf); dialogEdicao.add(editCpf);
        dialogEdicao.add(lblELogin); dialogEdicao.add(editLogin);
        dialogEdicao.add(lblESenha); dialogEdicao.add(editSenha);
        dialogEdicao.add(lblEPerfil); dialogEdicao.add(editPerfil);
        dialogEdicao.add(lblEStatus); dialogEdicao.add(editStatus);
        dialogEdicao.add(btnSalvarEdicao);

        btnSalvarEdicao.addActionListener(e -> {
            String novoNome = editNome.getText().trim();
            String novoCpf = editCpf.getText().replaceAll("\\D", "");
            String novoLogin = editLogin.getText().trim();
            String novaSenha = new String(editSenha.getPassword());
            String novoPerfil = (String) editPerfil.getSelectedItem();
            boolean novoStatus = "Ativo".equalsIgnoreCase((String) editStatus.getSelectedItem());

            if (novoNome.isEmpty() || novoLogin.isEmpty() || novaSenha.isEmpty()) {
                JOptionPane.showMessageDialog(dialogEdicao, "Preencha todos os campos obrigatórios!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (novaSenha.length() < 6) {
                JOptionPane.showMessageDialog(dialogEdicao, "A senha deve ter no mínimo 6 caracteres!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Usuario u = new Usuario();
                u.setIdUsuario(id);
                u.setNome(novoNome);
                u.setCpf(novoCpf);
                u.setLogin(novoLogin);
                u.setSenha(novaSenha);
                u.setPerfil(novoPerfil);
                u.setAtivo(novoStatus);

                usuarioDAO.alterar(u);
                JOptionPane.showMessageDialog(dialogEdicao, "Alterações salvas com sucesso!");
                dialogEdicao.dispose();
                carregarTabela();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogEdicao, "Erro ao atualizar usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogEdicao.setVisible(true);
    }

    // Classe auxiliar interna para renderizar a senha com asteriscos na JTable
    private static class SenhaOcultaCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String senha = value != null ? value.toString() : "";
            String oculto = "•".repeat(senha.length());
            return super.getTableCellRendererComponent(table, oculto, isSelected, hasFocus, row, column);
        }
    }
}