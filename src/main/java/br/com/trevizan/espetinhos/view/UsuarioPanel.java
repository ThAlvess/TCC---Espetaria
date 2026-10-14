package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.UsuarioDAO;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.util.PadraoComponentes;
import br.com.trevizan.espetinhos.util.PadraoTela;
import br.com.trevizan.espetinhos.util.SenhaUtil;
import br.com.trevizan.espetinhos.util.SessaoUsuario;

import javax.swing.*;
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
    private JButton btnCadastrar, btnEditar, btnResetarSenha;
    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private int linhaSelecionada = -1;
    private UsuarioDAO usuarioDAO;
    private JLabel lblResumo;

    public UsuarioPanel() {
        // Inicializa o objeto de acesso a dados dos usuários
        usuarioDAO = new UsuarioDAO();

        // Define o gerenciador de layout principal do painel como BorderLayout com espaçamento
        setLayout(new BorderLayout(10, 15));
        // Define a cor de fundo padrão idêntica à tela de mesas
        setBackground(new Color(238, 232, 227));
        // Aplica uma margem interna de preenchimento (padding) nas bordas do painel
        // (margens do padrão de telas: título na mesma posição da tela de Relatórios)
        setBorder(BorderFactory.createEmptyBorder(
                PadraoTela.MARGEM_TOPO, PadraoTela.MARGEM_LATERAL, 20, PadraoTela.MARGEM_LATERAL));

        // Painel superior para o título e o subtítulo (padrão visual da tela de mesas)
        JPanel panelTopo = new JPanel();
        panelTopo.setLayout(new BoxLayout(panelTopo, BoxLayout.Y_AXIS));
        panelTopo.setOpaque(false);

        JLabel lblTitulo = PadraoTela.criarTitulo("CADASTRO DE USUÁRIOS");
        JLabel lblSubtitulo = PadraoTela.criarSubtitulo("Gerencie o acesso, perfis e credenciais dos operadores do sistema.");

        panelTopo.add(lblTitulo);
        panelTopo.add(Box.createVerticalStrut(PadraoTela.ESPACO_TITULO_SUBTITULO));
        panelTopo.add(lblSubtitulo);
        add(panelTopo, BorderLayout.NORTH);

        // Barra de ações: pesquisa à esquerda e botões à direita
        JPanel panelCentro = new JPanel(new BorderLayout(0, 16));
        panelCentro.setOpaque(false);
        panelCentro.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JPanel panelBusca = new JPanel(new BorderLayout(12, 0));
        panelBusca.setOpaque(false);

        txtBusca = PadraoComponentes.criarCampoPesquisa("Pesquisar por nome, usuário, CPF ou perfil...");
        panelBusca.add(txtBusca, BorderLayout.CENTER);

        btnCadastrar = PadraoComponentes.criarBotaoPrimario("Cadastrar", PadraoComponentes.iconeMais(Color.WHITE));
        btnEditar = PadraoComponentes.criarBotaoSecundario("Editar usuário", PadraoComponentes.iconeEditar(PadraoComponentes.VERDE));
        btnResetarSenha = PadraoComponentes.criarBotaoSecundario("Resetar senha", PadraoComponentes.iconeChave(PadraoComponentes.VERDE));
        btnResetarSenha.setToolTipText("Volta a senha do usuário selecionado para a senha padrão ("
                + SenhaUtil.SENHA_PADRAO + ")");

        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBotoes.setOpaque(false);
        panelBotoes.add(btnResetarSenha);
        panelBotoes.add(Box.createHorizontalStrut(10));
        panelBotoes.add(btnEditar);
        panelBotoes.add(Box.createHorizontalStrut(10));
        panelBotoes.add(btnCadastrar);

        // GridBagLayout mantém os botões na altura padrão, centralizados na linha
        JPanel panelBotoesCentralizados = new JPanel(new GridBagLayout());
        panelBotoesCentralizados.setOpaque(false);
        panelBotoesCentralizados.add(panelBotoes);
        panelBusca.add(panelBotoesCentralizados, BorderLayout.EAST);

        panelCentro.add(panelBusca, BorderLayout.NORTH);

        // Define o modelo da tabela (agora somente leitura)
        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Status", "Nome", "Usuário", "CPF", "Perfil"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Estilo da tabela (zebrado, sem grade)
        PadraoComponentes.estilizarTabela(tabelaUsuarios);
        PadraoComponentes.alinharColuna(tabelaUsuarios, 0, SwingConstants.CENTER);
        tabelaUsuarios.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());

        tabelaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabelaUsuarios.getColumnModel().getColumn(0).setMaxWidth(90);

        tabelaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabelaUsuarios.getColumnModel().getColumn(1).setMinWidth(80);
        tabelaUsuarios.getColumnModel().getColumn(1).setMaxWidth(130);

        tabelaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(150);
        tabelaUsuarios.getColumnModel().getColumn(5).setMinWidth(100);
        tabelaUsuarios.getColumnModel().getColumn(5).setMaxWidth(200);

        sorter = new TableRowSorter<>(modeloTabela);
        tabelaUsuarios.setRowSorter(sorter);

        lblResumo = new JLabel(" ");
        lblResumo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblResumo.setForeground(PadraoComponentes.COR_TEXTO_SECUNDARIO);

        panelCentro.add(PadraoComponentes.criarCartaoTabela("Usuários cadastrados", tabelaUsuarios, lblResumo),
                BorderLayout.CENTER);

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
                    sorter.setRowFilter(regexFilterSafe(texto.trim()));
                }
                atualizarResumo();
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
        btnResetarSenha.addActionListener(e -> resetarSenha());

        // Duplo clique em uma linha também abre a edição
        tabelaUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tabelaUsuarios.rowAtPoint(e.getPoint()) >= 0) {
                    abrirJanelaEdicao();
                }
            }
        });

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

    /** Quantidade de usuários que estão aparecendo na tabela (respeita a pesquisa). */
    private void atualizarResumo() {
        if (lblResumo == null) {
            return;
        }
        int quantidade = tabelaUsuarios.getRowCount();
        lblResumo.setText(quantidade + (quantidade == 1 ? " usuário" : " usuários"));
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        List<Usuario> lista = usuarioDAO.listar();
        if (lista != null) {
            for (Usuario u : lista) {
                Object[] linha = new Object[6];
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
                modeloTabela.addRow(linha);
            }
        }
        atualizarResumo();
    }

    /**
     * Pede a senha do administrador logado antes de ações sensíveis
     * (cadastrar, editar ou resetar senha).
     *
     * @return {@code true} se a senha conferiu
     */
    private boolean confirmarAdministrador() {
        JPasswordField txtSenhaAdmin = new JPasswordField(15);
        PadraoComponentes.habilitarMostrarSenha(txtSenhaAdmin);
        Object[] mensagemAdmin = {
            "Digite a sua senha de administrador para continuar:", txtSenhaAdmin
        };

        int opcaoAdmin = JOptionPane.showConfirmDialog(
            this,
            mensagemAdmin,
            "Confirmação de Segurança",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (opcaoAdmin != JOptionPane.OK_OPTION) {
            return false;
        }

        // confere a senha de quem está logado (se for administrador)
        Usuario logado = SessaoUsuario.getUsuarioLogado();
        String loginAdmin = logado != null && "Administrador".equalsIgnoreCase(logado.getPerfil())
                ? logado.getLogin()
                : "admin";

        String senhaAdmin = new String(txtSenhaAdmin.getPassword());
        Usuario adminAutenticado = usuarioDAO.autenticar(loginAdmin, senhaAdmin);

        if (adminAutenticado == null || !"Administrador".equalsIgnoreCase(adminAutenticado.getPerfil())) {
            JOptionPane.showMessageDialog(this, "Senha incorreta ou usuário sem privilégios de Administrador!", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /** Campo de nome que não aceita números. */
    private JTextField criarCampoNome(String valorInicial) {
        JTextField campo = new JTextField(22);
        campo.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                if (str == null) return;
                if (!str.matches(".*\\d.*")) {
                    super.insertString(offset, str, attr);
                }
            }
        });
        campo.setText(valorInicial);
        return campo;
    }

    private JFormattedTextField criarCampoCpf(String valorInicial) {
        JFormattedTextField campo;
        try {
            MaskFormatter maskCpf = new MaskFormatter("###.###.###-##");
            maskCpf.setPlaceholderCharacter('_');
            campo = new JFormattedTextField(maskCpf);
        } catch (ParseException e) {
            campo = new JFormattedTextField();
        }
        campo.setColumns(22);
        if (valorInicial != null && !valorInicial.isEmpty()) {
            campo.setText(valorInicial);
        }
        return campo;
    }

    /**
     * Monta o formulário (rótulo + campo por linha) usado dentro das janelas
     * padrão de Cadastrar e Editar, com um aviso sobre a senha no final.
     */
    private JPanel montarFormulario(String[] rotulos, JComponent[] campos, String avisoSenha) {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < rotulos.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            formulario.add(new JLabel(rotulos[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formulario.add(campos[i], gbc);
        }

        JLabel aviso = new JLabel("<html><div style='width:260px'>" + avisoSenha + "</div></html>");
        gbc.gridx = 0;
        gbc.gridy = rotulos.length;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 0, 0);
        formulario.add(aviso, gbc);
        return formulario;
    }

    private void abrirJanelaCadastro() {
        // Confirmação de Segurança do Administrador antes de abrir a janela
        if (!confirmarAdministrador()) {
            return;
        }

        JTextField txtNome = criarCampoNome("");
        JFormattedTextField txtCpf = criarCampoCpf("");
        JTextField txtUsuario = new JTextField(22);
        JComboBox<String> cbPerfil = new JComboBox<>(new String[]{"Atendente", "Caixa", "Administrador"});

        // o usuário é cadastrado com a senha padrão e cria a senha pessoal no primeiro login
        JPanel formulario = montarFormulario(
                new String[]{"Nome:", "CPF:", "Usuário:", "Perfil:"},
                new JComponent[]{txtNome, txtCpf, txtUsuario, cbPerfil},
                "Senha inicial: <b>" + SenhaUtil.SENHA_PADRAO + "</b>. "
                        + "No primeiro acesso o usuário vai criar a própria senha.");

        while (true) {
            SwingUtilities.invokeLater(txtNome::requestFocusInWindow);
            int opcao = JOptionPane.showConfirmDialog(this, formulario, "Cadastrar Usuário",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcao != JOptionPane.OK_OPTION) {
                return;
            }

            String nome = txtNome.getText().trim();
            String cpf = txtCpf.getText().replaceAll("\\D", "");
            String login = txtUsuario.getText().trim();
            String perfil = (String) cbPerfil.getSelectedItem();

            if (nome.isEmpty() || login.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!", "Aviso", JOptionPane.WARNING_MESSAGE);
                continue; // reabre a janela com o que já foi digitado
            }

            Usuario u = new Usuario();
            u.setNome(nome);
            u.setCpf(cpf);
            u.setLogin(login);
            u.setSenha(SenhaUtil.SENHA_PADRAO); // gravada criptografada pelo DAO
            u.setPerfil(perfil);
            u.setAtivo(true);

            try {
                usuarioDAO.cadastrar(u);
                JOptionPane.showMessageDialog(this,
                        "Usuário cadastrado com sucesso!\n\n"
                                + "Login: " + login + "\n"
                                + "Senha inicial: " + SenhaUtil.SENHA_PADRAO + "\n\n"
                                + "No primeiro acesso será pedida uma nova senha.");
                carregarTabela();
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao cadastrar usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
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
        if (!confirmarAdministrador()) {
            return;
        }

        // Pega os dados atuais da linha selecionada
        int id = Integer.parseInt(modeloTabela.getValueAt(linhaSelecionada, 0).toString());
        String statusAtual = modeloTabela.getValueAt(linhaSelecionada, 1).toString();
        String loginAtual = modeloTabela.getValueAt(linhaSelecionada, 3).toString();
        Object cpfTabela = modeloTabela.getValueAt(linhaSelecionada, 4);
        String cpfAtual = cpfTabela != null ? cpfTabela.toString().replaceAll("\\D", "") : "";
        String perfilAtual = String.valueOf(modeloTabela.getValueAt(linhaSelecionada, 5));

        // busca o nome no banco (a senha não é carregada)
        Usuario usuarioBanco = usuarioDAO.buscarPorId(id);
        String nomeAtual = usuarioBanco != null && usuarioBanco.getNome() != null ? usuarioBanco.getNome() : "";

        JTextField editNome = criarCampoNome(nomeAtual);
        JFormattedTextField editCpf = criarCampoCpf(cpfAtual);
        JTextField editLogin = new JTextField(loginAtual, 22);
        JComboBox<String> editPerfil = new JComboBox<>(new String[]{"Atendente", "Caixa", "Administrador"});
        editPerfil.setSelectedItem(perfilAtual);
        JComboBox<String> editStatus = new JComboBox<>(new String[]{"Ativo", "Inativo"});
        editStatus.setSelectedItem(statusAtual);

        JPanel formulario = montarFormulario(
                new String[]{"Nome:", "CPF:", "Usuário:", "Perfil:", "Status:"},
                new JComponent[]{editNome, editCpf, editLogin, editPerfil, editStatus},
                "A senha é pessoal e não pode ser vista nem alterada aqui. "
                        + "Para bloquear o acesso, mude o status para <b>Inativo</b>.");

        while (true) {
            int opcao = JOptionPane.showConfirmDialog(this, formulario, "Editar Usuário",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcao != JOptionPane.OK_OPTION) {
                return;
            }

            String novoNome = editNome.getText().trim();
            String novoCpf = editCpf.getText().replaceAll("\\D", "");
            String novoLogin = editLogin.getText().trim();
            String novoPerfil = (String) editPerfil.getSelectedItem();
            boolean novoStatus = "Ativo".equalsIgnoreCase((String) editStatus.getSelectedItem());

            if (novoNome.isEmpty() || novoLogin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!", "Aviso", JOptionPane.WARNING_MESSAGE);
                continue; // reabre a janela com o que já foi digitado
            }

            try {
                Usuario u = new Usuario();
                u.setIdUsuario(id);
                u.setNome(novoNome);
                u.setCpf(novoCpf);
                u.setLogin(novoLogin);
                u.setPerfil(novoPerfil);
                u.setAtivo(novoStatus);

                usuarioDAO.alterar(u); // não altera a senha
                JOptionPane.showMessageDialog(this, "Alterações salvas com sucesso!");
                carregarTabela();
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Volta a senha do usuário selecionado para a senha padrão. O
     * administrador não vê nem escolhe a senha: no próximo login o usuário
     * é obrigado a criar uma senha pessoal nova.
     */
    private void resetarSenha() {
        int viewRow = tabelaUsuarios.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecione o usuário que vai ter a senha resetada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int linha = tabelaUsuarios.convertRowIndexToModel(viewRow);
        int id = Integer.parseInt(modeloTabela.getValueAt(linha, 0).toString());
        String nome = String.valueOf(modeloTabela.getValueAt(linha, 2));

        int resposta = JOptionPane.showConfirmDialog(this,
                "Resetar a senha de " + nome + "?\n\n"
                        + "A senha volta a ser a padrão (" + SenhaUtil.SENHA_PADRAO + ") e,\n"
                        + "no próximo login, o usuário terá que criar uma nova senha.",
                "Resetar senha", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        if (!confirmarAdministrador()) {
            return;
        }

        try {
            usuarioDAO.redefinirSenhaPadrao(id);
            JOptionPane.showMessageDialog(this,
                    "Senha de " + nome + " resetada.\nSenha temporária: " + SenhaUtil.SENHA_PADRAO,
                    "Resetar senha", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao resetar a senha: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Status em verde (Ativo) ou vermelho (Inativo)
    private static class StatusCellRenderer extends PadraoComponentes.RendererCelula {
        StatusCellRenderer() {
            super(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(getFont().deriveFont(Font.BOLD));
            setForeground("Inativo".equals(value) ? PadraoComponentes.VERMELHO : PadraoComponentes.VERDE);
            return this;
        }
    }
}
