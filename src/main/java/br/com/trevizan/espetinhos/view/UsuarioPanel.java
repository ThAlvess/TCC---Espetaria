package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.UsuarioDAO;
import br.com.trevizan.espetinhos.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.ParseException;
import java.util.List;

public class UsuarioPanel extends javax.swing.JPanel {

    private JTextField txtNome, txtUsuario, txtBusca;
    private JFormattedTextField txtCpf;
    private JPasswordField txtSenha, txtConfirmaSenha;
    private JComboBox<String> cbPerfil;
    private JButton btnCadastrar, btnAtualizar;
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

        // Cria o painel central utilizando GridBagLayout para organizar o formulário e a tabela estruturalmente
        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cria o painel do formulário com estilo de card limpo em fundo branco
        JPanel panelForm = new JPanel(null);
        panelForm.setPreferredSize(new Dimension(0, 135));
        panelForm.setBackground(Color.WHITE);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 215, 210), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Cria os rótulos e os campos de entrada alinhados harmoniosamente
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNome.setForeground(new Color(80, 80, 80));
        lblNome.setBounds(25, 18, 50, 25);
        txtNome = new JTextField();
        txtNome.setBounds(75, 18, 220, 28);

        JLabel lblCpf = new JLabel("CPF:");
        lblCpf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCpf.setForeground(new Color(80, 80, 80));
        lblCpf.setBounds(343, 18, 40, 25);
        
        try {
            MaskFormatter maskCpf = new MaskFormatter("###.###.###-##");
            maskCpf.setPlaceholderCharacter('_');
            txtCpf = new JFormattedTextField(maskCpf);
        } catch (ParseException e) {
            txtCpf = new JFormattedTextField();
        }
        txtCpf.setBounds(370, 18, 170, 28);

        JLabel lblUsuario = new JLabel("Usuário:");
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsuario.setForeground(new Color(80, 80, 80));
        lblUsuario.setBounds(25, 58, 50, 25);
        txtUsuario = new JTextField();
        txtUsuario.setBounds(75, 58, 220, 28);

        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSenha.setForeground(new Color(80, 80, 80));
        lblSenha.setBounds(330, 58, 50, 25);
        txtSenha = new JPasswordField();
        txtSenha.setBounds(370, 58, 170, 28);

        JLabel lblPerfil = new JLabel("Perfil:");
        lblPerfil.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPerfil.setForeground(new Color(80, 80, 80));
        lblPerfil.setBounds(25, 98, 50, 25);
        cbPerfil = new JComboBox<>(new String[]{"Caixa", "Gerente", "Administrador"});
        cbPerfil.setBounds(75, 98, 220, 28);
        cbPerfil.setBackground(Color.WHITE);

        JLabel lblConfirmaSenha = new JLabel("Confirmar:");
        lblConfirmaSenha.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblConfirmaSenha.setForeground(new Color(80, 80, 80));
        lblConfirmaSenha.setBounds(307, 98, 70, 25);
        txtConfirmaSenha = new JPasswordField();
        txtConfirmaSenha.setBounds(370, 98, 170, 28);

        // Adiciona os componentes ao painel do formulário
        panelForm.add(lblNome); panelForm.add(txtNome);
        panelForm.add(lblCpf); panelForm.add(txtCpf);
        panelForm.add(lblUsuario); panelForm.add(txtUsuario);
        panelForm.add(lblPerfil); panelForm.add(cbPerfil);
        panelForm.add(lblSenha); panelForm.add(txtSenha);
        panelForm.add(lblConfirmaSenha); panelForm.add(txtConfirmaSenha);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.0;
        panelCentro.add(panelForm, gbc);

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
        btnAtualizar = criarBotaoEstilizado("Salvar Alterações", new Color(30, 100, 180), Color.WHITE);

        panelBotoesEsquerda.add(btnCadastrar);
        panelBotoesEsquerda.add(btnAtualizar);

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

        // Define o modelo da tabela
        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Status", "Nome", "Usuário", "CPF", "Perfil", "Senha"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; 
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

        JComboBox<String> cbStatusCombo = new JComboBox<>(new String[]{"Ativo", "Inativo"});
        tabelaUsuarios.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(cbStatusCombo));
        tabelaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabelaUsuarios.getColumnModel().getColumn(1).setMinWidth(80);
        tabelaUsuarios.getColumnModel().getColumn(1).setMaxWidth(120);

        JComboBox<String> cbPerfilCombo = new JComboBox<>(new String[]{"Caixa", "Gerente", "Administrador"});
        tabelaUsuarios.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(cbPerfilCombo));
        tabelaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(140);
        tabelaUsuarios.getColumnModel().getColumn(5).setMinWidth(100);
        tabelaUsuarios.getColumnModel().getColumn(5).setMaxWidth(180);

        JPasswordField passwordEditorField = new JPasswordField();
        tabelaUsuarios.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(passwordEditorField));
        tabelaUsuarios.getColumnModel().getColumn(6).setCellRenderer(new SenhaOcultaCellRenderer());

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaUsuarios.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 225, 220), 1));
        
        panelTabelaContainer.add(scrollPane, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        panelCentro.add(panelTabelaContainer, gbc);

        add(panelCentro, BorderLayout.CENTER);

        carregarTabela();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                carregarTabela();
                limparCampos();
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

        btnCadastrar.addActionListener(e -> cadastrarUsuario());
        btnAtualizar.addActionListener(e -> atualizarUsuario());

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

    private void limparCampos() {
        txtNome.setText("");
        txtCpf.setValue(null);
        txtUsuario.setText("");
        txtSenha.setText("");
        txtConfirmaSenha.setText("");
        cbPerfil.setSelectedIndex(0);
        tabelaUsuarios.clearSelection();
        linhaSelecionada = -1;
    }

    private void cadastrarUsuario() {
        String nome = txtNome.getText().trim();
        String cpf = txtCpf.getText().replaceAll("\\D", "");
        String login = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());
        String confirmaSenha = new String(txtConfirmaSenha.getPassword());
        String perfil = (String) cbPerfil.getSelectedItem();

        if (nome.isEmpty() || login.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!senha.equals(confirmaSenha)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem!", "Erro", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso!");
            carregarTabela();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarUsuario() {
        if (linhaSelecionada == -1 && tabelaUsuarios.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para atualizar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int viewRow = tabelaUsuarios.getSelectedRow();
        if (viewRow != -1) {
            linhaSelecionada = tabelaUsuarios.convertRowIndexToModel(viewRow);
        }

        // Cria um campo de senha para ocultar a digitação com pontos
        JPasswordField txtSenhaAdmin = new JPasswordField(15);
        Object[] mensagem = {
            "Digite a senha do administrador para confirmar:", txtSenhaAdmin
        };

        int opcao = JOptionPane.showConfirmDialog(
            this, 
            mensagem, 
            "Confirmação de Segurança", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );

        if (opcao != JOptionPane.OK_OPTION) {
            return; // Usuário cancelou ou fechou a janela
        }

        String senhaAdmin = new String(txtSenhaAdmin.getPassword());

        // Autentica dinamicamente contra o banco de dados usando o login admin e a senha digitada
        Usuario adminAutenticado = usuarioDAO.autenticar("admin", senhaAdmin);

        if (adminAutenticado == null || !"Administrador".equalsIgnoreCase(adminAutenticado.getPerfil())) {
            JOptionPane.showMessageDialog(this, "Senha incorreta ou usuário sem privilégios de Administrador!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(modeloTabela.getValueAt(linhaSelecionada, 0).toString());
            String statusStr = modeloTabela.getValueAt(linhaSelecionada, 1).toString();
            String nome = modeloTabela.getValueAt(linhaSelecionada, 2).toString();
            String login = modeloTabela.getValueAt(linhaSelecionada, 3).toString();
            String cpf = modeloTabela.getValueAt(linhaSelecionada, 4).toString().replaceAll("\\D", "");
            String perfil = modeloTabela.getValueAt(linhaSelecionada, 5).toString();
            String senha = modeloTabela.getValueAt(linhaSelecionada, 6).toString();

            Usuario u = new Usuario();
            u.setIdUsuario(id);
            u.setAtivo("Ativo".equalsIgnoreCase(statusStr));
            u.setNome(nome);
            u.setLogin(login);
            u.setCpf(cpf);
            u.setPerfil(perfil);
            u.setSenha(senha);

            usuarioDAO.alterar(u);
            JOptionPane.showMessageDialog(this, "Alterações salvas com sucesso!");
            carregarTabela();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
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