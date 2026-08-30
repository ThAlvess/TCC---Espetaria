package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.UsuarioDAO;
import br.com.trevizan.espetinhos.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsuarioPanel extends javax.swing.JPanel {

    private JTextField txtNome, txtCpf, txtUsuario, txtBusca;
    private JPasswordField txtSenha;
    private JComboBox<String> cbPerfil;
    private JButton btnCadastrar, btnExcluir, btnLimpar, btnAtualizar, btnInativar;
    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private int linhaSelecionada = -1;
    private UsuarioDAO usuarioDAO;

    public UsuarioPanel() {
        usuarioDAO = new UsuarioDAO();

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 242, 238));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("CADASTRO DE USUÁRIOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(25, 100, 25));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel panelForm = new JPanel(null);
        panelForm.setPreferredSize(new Dimension(0, 140));
        panelForm.setBackground(Color.WHITE);
        panelForm.setBorder(BorderFactory.createEtchedBorder());

        JLabel lblNome = new JLabel("Nome:");
        lblNome.setBounds(30, 20, 50, 25);
        txtNome = new JTextField();
        txtNome.setBounds(80, 20, 200, 25);

        JLabel lblCpf = new JLabel("CPF:");
        lblCpf.setBounds(300, 20, 40, 25);
        txtCpf = new JTextField();
        txtCpf.setBounds(340, 20, 150, 25);

        JLabel lblUsuario = new JLabel("Usuário:");
        lblUsuario.setBounds(30, 60, 50, 25);
        txtUsuario = new JTextField();
        txtUsuario.setBounds(80, 60, 200, 25);

        JLabel lblPerfil = new JLabel("Perfil:");
        lblPerfil.setBounds(300, 60, 40, 25);
        cbPerfil = new JComboBox<>(new String[]{"Administrador", "Cliente"});
        cbPerfil.setBounds(340, 60, 150, 25);

        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setBounds(30, 100, 50, 25);
        txtSenha = new JPasswordField();
        txtSenha.setBounds(80, 100, 200, 25);

        btnCadastrar = criarBotaoEstilizado("Cadastrar", new Color(25, 100, 25), Color.WHITE);
        btnCadastrar.setBounds(340, 100, 100, 25);

        panelForm.add(lblNome); panelForm.add(txtNome);
        panelForm.add(lblCpf); panelForm.add(txtCpf);
        panelForm.add(lblUsuario); panelForm.add(txtUsuario);
        panelForm.add(lblPerfil); panelForm.add(cbPerfil);
        panelForm.add(lblSenha); panelForm.add(txtSenha);
        panelForm.add(btnCadastrar);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.0;
        panelCentro.add(panelForm, gbc);

        JPanel panelTabelaContainer = new JPanel(new BorderLayout(5, 5));
        panelTabelaContainer.setBackground(Color.WHITE);
        panelTabelaContainer.setBorder(BorderFactory.createEtchedBorder());

        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusca.setOpaque(false);
        panelBusca.add(new JLabel("Buscar Usuário:"));
        txtBusca = new JTextField(20);
        panelBusca.add(txtBusca);

        btnAtualizar = criarBotaoEstilizado("Atualizar", new Color(30, 100, 180), Color.WHITE);
        btnExcluir = criarBotaoEstilizado("Excluir", new Color(180, 40, 40), Color.WHITE);
        btnInativar = criarBotaoEstilizado("Inativar", new Color(220, 130, 20), Color.WHITE);
        btnLimpar = criarBotaoEstilizado("Limpar", new Color(120, 120, 120), Color.WHITE);

        panelBusca.add(btnAtualizar);
        panelBusca.add(btnExcluir);
        panelBusca.add(btnInativar);
        panelBusca.add(btnLimpar);

        panelTabelaContainer.add(panelBusca, BorderLayout.NORTH);

        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Nome", "Usuário", "CPF", "Perfil", "Status", "Senha"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setRowHeight(25);
        tabelaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);
        panelTabelaContainer.add(scrollPane, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        panelCentro.add(panelTabelaContainer, gbc);

        add(panelCentro, BorderLayout.CENTER);

        carregarTabela();

        btnCadastrar.addActionListener(e -> cadastrarUsuario());
        btnExcluir.addActionListener(e -> excluirUsuario());
        btnLimpar.addActionListener(e -> limparCampos());
        btnAtualizar.addActionListener(e -> atualizarUsuario());
        btnInativar.addActionListener(e -> inativarUsuario());

        tabelaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaUsuarios.getSelectedRow() != -1) {
                linhaSelecionada = tabelaUsuarios.getSelectedRow();
                preencherCamposComSelecao();
            }
        });
    }

    private JButton criarBotaoEstilizado(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        List<Usuario> lista = usuarioDAO.listar();
        if (lista != null) {
            for (int i = 0; i < lista.size(); i++) {
                Usuario u = lista.get(i);
                Object[] linha = new Object[7];
                linha[0] = u.getId();
                linha[1] = u.getNome();
                linha[2] = u.getUsuario();
                linha[3] = u.getCpf();
                linha[4] = u.getPerfil();
                linha[5] = u.getStatus();
                linha[6] = u.getSenha();
                modeloTabela.addRow(linha);
            }
        }
    }

    private void preencherCamposComSelecao() {
        if (linhaSelecionada != -1) {
            txtNome.setText(modeloTabela.getValueAt(linhaSelecionada, 1) != null ? modeloTabela.getValueAt(linhaSelecionada, 1).toString() : "");
            txtUsuario.setText(modeloTabela.getValueAt(linhaSelecionada, 2) != null ? modeloTabela.getValueAt(linhaSelecionada, 2).toString() : "");
            txtCpf.setText(modeloTabela.getValueAt(linhaSelecionada, 3) != null ? modeloTabela.getValueAt(linhaSelecionada, 3).toString() : "");
            
            String perfil = modeloTabela.getValueAt(linhaSelecionada, 4) != null ? modeloTabela.getValueAt(linhaSelecionada, 4).toString() : "";
            for (int i = 0; i < cbPerfil.getItemCount(); i++) {
                if (cbPerfil.getItemAt(i).equalsIgnoreCase(perfil)) {
                    cbPerfil.setSelectedIndex(i);
                    break;
                }
            }

            txtSenha.setText(modeloTabela.getValueAt(linhaSelecionada, 6) != null ? modeloTabela.getValueAt(linhaSelecionada, 6).toString() : "");
        }
    }

    private void cadastrarUsuario() {
        String cpf = txtCpf.getText().trim();
        if (!cpf.isEmpty() && !isCpfValido(cpf)) {
            JOptionPane.showMessageDialog(this, "CPF inválido!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario u = new Usuario();
        u.setNome(txtNome.getText());
        u.setUsuario(txtUsuario.getText());
        u.setCpf(cpf);
        u.setSenha(new String(txtSenha.getPassword()));
        u.setPerfil(cbPerfil.getSelectedItem().toString());
        u.setStatus("ativo");

        usuarioDAO.cadastrar(u);
        JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso no banco de dados!");
        carregarTabela();
        limparCampos();
    }

    private void atualizarUsuario() {
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para atualizar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(modeloTabela.getValueAt(linhaSelecionada, 0).toString());

        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            nome = modeloTabela.getValueAt(linhaSelecionada, 1).toString();
        }

        String usuarioLogin = txtUsuario.getText().trim();
        if (usuarioLogin.isEmpty()) {
            usuarioLogin = modeloTabela.getValueAt(linhaSelecionada, 2).toString();
        }

        String cpf = txtCpf.getText().trim();
        if (cpf.isEmpty()) {
            cpf = modeloTabela.getValueAt(linhaSelecionada, 3).toString();
        }

        String perfil = cbPerfil.getSelectedItem().toString();

        String senhaDigitada = new String(txtSenha.getPassword());
        String senhaAntiga = modeloTabela.getValueAt(linhaSelecionada, 6) != null ? modeloTabela.getValueAt(linhaSelecionada, 6).toString() : "";
        String senhaFinal = senhaDigitada.isEmpty() ? senhaAntiga : senhaDigitada;

        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setUsuario(usuarioLogin);
        u.setCpf(cpf);
        u.setPerfil(perfil);
        u.setSenha(senhaFinal);

        usuarioDAO.atualizar(u);
        JOptionPane.showMessageDialog(this, "Usuário atualizado com sucesso no banco de dados!");
        carregarTabela();
        limparCampos();
    }

    private void excluirUsuario() {
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para excluir!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validarSenhaAdministrador()) {
            JOptionPane.showMessageDialog(this, "Senha de Administrador incorreta!", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = Integer.parseInt(modeloTabela.getValueAt(linhaSelecionada, 0).toString());
        int confirmacao = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir este usuário do banco?", "Confirmação", JOptionPane.YES_NO_OPTION);
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            usuarioDAO.excluir(id);
            JOptionPane.showMessageDialog(this, "Usuário excluído com sucesso!");
            carregarTabela();
            limparCampos();
        }
    }

    private void inativarUsuario() {
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para inativar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validarSenhaAdministrador()) {
            JOptionPane.showMessageDialog(this, "Senha de Administrador incorreta!", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = Integer.parseInt(modeloTabela.getValueAt(linhaSelecionada, 0).toString());
        usuarioDAO.inativar(id);
        
        JOptionPane.showMessageDialog(this, "Usuário inativado no banco de dados!");
        carregarTabela();
        limparCampos();
    }

    private void limparCampos() {
        txtNome.setText("");
        txtCpf.setText("");
        txtUsuario.setText("");
        txtSenha.setText("");
        cbPerfil.setSelectedIndex(0);
        tabelaUsuarios.clearSelection();
        linhaSelecionada = -1;
    }

    private boolean isCpfValido(String cpf) {
        cpf = cpf.replaceAll("\\D", "");
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;
        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
            int dig1 = 11 - (soma % 11);
            if (dig1 > 9) dig1 = 0;
            soma = 0;
            for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
            int dig2 = 11 - (soma % 11);
            if (dig2 > 9) dig2 = 0;
            return (dig1 == (cpf.charAt(9) - '0')) && (dig2 == (cpf.charAt(10) - '0'));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validarSenhaAdministrador() {
        JPasswordField senhaField = new JPasswordField();
        Object[] mensagem = {"Digite a senha do Administrador:", senhaField};
        int opcao = JOptionPane.showConfirmDialog(this, mensagem, "Segurança", JOptionPane.OK_CANCEL_OPTION);
        if (opcao == JOptionPane.OK_OPTION) {
            return new String(senhaField.getPassword()).equals("admin123");
        }
        return false;
    }
}