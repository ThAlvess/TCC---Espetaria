package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.UsuarioDAO;
import br.com.trevizan.espetinhos.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioPanel extends javax.swing.JPanel {

    private JTextField txtNome, txtUsuario, txtBusca;
    private JFormattedTextField txtCpf;
    private JPasswordField txtSenha, txtConfirmaSenha;
    private JComboBox<String> cbPerfil;
    private JButton btnCadastrar, btnAtualizar, btnInativar;
    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    private int linhaSelecionada = -1;
    private UsuarioDAO usuarioDAO;
    private final Map<Integer, Boolean> senhasVisiveis = new HashMap<>();

    public UsuarioPanel() {
        // Inicializa o objeto de acesso a dados dos usuários
        usuarioDAO = new UsuarioDAO();

        // Define o gerenciador de layout principal do painel como BorderLayout com espaçamento
        setLayout(new BorderLayout(10, 10));
        // Define a cor de fundo padrão do painel
        setBackground(new Color(245, 242, 238));
        // Aplica uma margem interna de preenchimento (padding) nas bordas do painel
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Cria e estiliza o título principal exibido no topo do painel
        JLabel lblTitulo = new JLabel("CADASTRO DE USUÁRIOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(25, 100, 25));
        // Adiciona o título na região superior (NORTH) do painel principal
        add(lblTitulo, BorderLayout.NORTH);

        // Cria o painel central utilizando GridBagLayout para organizar o formulário e a tabela estruturalmente
        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cria o painel do formulário com posicionamento absoluto (null layout) para os campos de entrada
        JPanel panelForm = new JPanel(null);
        panelForm.setPreferredSize(new Dimension(0, 140));
        panelForm.setBackground(Color.WHITE);
        // Remove a borda separada para fundir visualmente com a tabela
        panelForm.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(180, 180, 180)));

        // Cria o rótulo e o campo de texto para o Nome do usuário
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setBounds(30, 20, 50, 25);
        txtNome = new JTextField();
        txtNome.setBounds(80, 20, 180, 25);

        // Cria o rótulo para o campo de CPF
        JLabel lblCpf = new JLabel("CPF:");
        lblCpf.setBounds(312, 20, 40, 25);
        
        // Tenta configurar a máscara de formatação para o CPF no padrão ###.###.###-## com caractere de preenchimento '_'
        try {
            MaskFormatter maskCpf = new MaskFormatter("###.###.###-##");
            maskCpf.setPlaceholderCharacter('_');
            txtCpf = new JFormattedTextField(maskCpf);
        } catch (ParseException e) {
            // Caso ocorra erro ao gerar a máscara, instancia um campo formatado padrão sem máscara
            txtCpf = new JFormattedTextField();
        }
        txtCpf.setBounds(350, 20, 180, 25);

        // Cria o rótulo e o campo de texto para o login do Usuário
        JLabel lblUsuario = new JLabel("Usuário:");
        lblUsuario.setBounds(30, 60, 50, 25);
        txtUsuario = new JTextField();
        txtUsuario.setBounds(80, 60, 180, 25);

        // Cria o rótulo e o campo de senha para o cadastro
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setBounds(300, 60, 50, 25);
        txtSenha = new JPasswordField();
        txtSenha.setBounds(350, 60, 180, 25);

        // Cria o rótulo e a caixa de seleção (ComboBox) para definir o perfil de acesso
        JLabel lblPerfil = new JLabel("Perfil:");
        lblPerfil.setBounds(30, 100, 50, 25);
        cbPerfil = new JComboBox<>(new String[]{"Caixa", "Gerente", "Administrador"});
        cbPerfil.setBounds(80, 100, 180, 25);

        // Cria o rótulo e o campo de confirmação de senha
        JLabel lblConfirmaSenha = new JLabel("Confirmar:");
        lblConfirmaSenha.setBounds(280, 100, 70, 25);
        txtConfirmaSenha = new JPasswordField();
        txtConfirmaSenha.setBounds(350, 100, 180, 25);

        // Adiciona todos os componentes visuais de entrada e rótulos dentro do painel do formulário
        panelForm.add(lblNome); panelForm.add(txtNome);
        panelForm.add(lblCpf); panelForm.add(txtCpf);
        panelForm.add(lblUsuario); panelForm.add(txtUsuario);
        panelForm.add(lblPerfil); panelForm.add(cbPerfil);
        panelForm.add(lblSenha); panelForm.add(txtSenha);
        panelForm.add(lblConfirmaSenha); panelForm.add(txtConfirmaSenha);

        // Configura as restrições do GridBagLayout para posicionar o painel do formulário na linha superior
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.0;
        panelCentro.add(panelForm, gbc);

        // Cria o painel contentor que englobará a barra de busca e a tabela de listagem de usuários
        JPanel panelTabelaContainer = new JPanel(new BorderLayout(5, 5));
        panelTabelaContainer.setBackground(Color.WHITE);
        // Borda unificada apenas nas laterais e embaixo
        panelTabelaContainer.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(180, 180, 180)));

        // Cria o painel superior da tabela contendo os botões de ação à esquerda e o campo de busca à direita
        JPanel panelBusca = new JPanel(new BorderLayout());
        panelBusca.setOpaque(false);
        panelBusca.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, -5));

        // Painel esquerdo para os botões Cadastrar, Salvar e Inativar alinhados lado a lado
        JPanel panelBotoesEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBotoesEsquerda.setOpaque(false);

        btnCadastrar = criarBotaoEstilizado("Cadastrar", new Color(25, 100, 25), Color.WHITE);
        btnAtualizar = criarBotaoEstilizado("Salvar", new Color(30, 100, 180), Color.WHITE);
        btnInativar = criarBotaoEstilizado("Inativar", new Color(220, 130, 20), Color.WHITE);

        panelBotoesEsquerda.add(btnCadastrar);
        panelBotoesEsquerda.add(btnAtualizar);
        panelBotoesEsquerda.add(btnInativar);

        // Painel direito para o campo de busca posicionado na ponta direita
        JPanel panelBuscaDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelBuscaDireita.setOpaque(false);
        panelBuscaDireita.add(new JLabel("Buscar Usuário:"));
        txtBusca = new JTextField(20);
        panelBuscaDireita.add(txtBusca);

        panelBusca.add(panelBotoesEsquerda, BorderLayout.WEST);
        panelBusca.add(panelBuscaDireita, BorderLayout.EAST);

        panelTabelaContainer.add(panelBusca, BorderLayout.NORTH);

        // Define o modelo da tabela customizado: permite editar a senha apenas se o olhinho daquela linha estiver destravado (visível)
        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Nome", "Usuário", "CPF", "Perfil", "Senha"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 5) {
                    int modelRow = tabelaUsuarios.convertRowIndexToModel(row);
                    return senhasVisiveis.getOrDefault(modelRow, false);
                }
                return true; // Demais colunas editáveis normalmente
            }
        };

        // Inicializa a JTable associada ao modelo de dados criado
        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setRowHeight(25);
        tabelaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Alinha os títulos (cabeçalhos) das colunas para a esquerda
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) tabelaUsuarios.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        // Controla a largura da coluna PERFIL (índice 4)
        tabelaUsuarios.getColumnModel().getColumn(4).setPreferredWidth(240);
        tabelaUsuarios.getColumnModel().getColumn(4).setMinWidth(90);
        tabelaUsuarios.getColumnModel().getColumn(4).setMaxWidth(240);

        // Define o renderizador customizado na coluna de senha para mascarar os caracteres e desenhar o ícone de visualização
        tabelaUsuarios.getColumnModel().getColumn(5).setCellRenderer(new SenhaCellRenderer());

        // Adiciona um ouvinte de eventos de mouse na tabela para detectar cliques específicos no ícone do olho da coluna de senha
        tabelaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = tabelaUsuarios.columnAtPoint(e.getPoint());
                int row = tabelaUsuarios.rowAtPoint(e.getPoint());
                // Verifica se o clique ocorreu na coluna da senha (índice 5) e em uma linha válida
                if (col == 5 && row != -1) {
                    int modelRow = tabelaUsuarios.convertRowIndexToModel(row);
                    Rectangle cellRect = tabelaUsuarios.getCellRect(row, col, false);
                    // Identifica se o clique foi efetuado na extremidade direita da célula (onde o ícone do olho é desenhado)
                    if (e.getX() >= (cellRect.x + cellRect.width - 30)) {
                        boolean visivelAtual = senhasVisiveis.getOrDefault(modelRow, false);
                        if (visivelAtual) {
                            // Se a senha já estava visível, oculta-la novamente e repintar a tabela
                            senhasVisiveis.put(modelRow, false);
                            tabelaUsuarios.repaint();
                        } else {
                            // Se estiver oculta, exige a senha do administrador antes de exibi-la e permitir a edição
                            if (validarSenhaAdministrador()) {
                                senhasVisiveis.put(modelRow, true);
                                tabelaUsuarios.repaint();
                            } else {
                                JOptionPane.showMessageDialog(UsuarioPanel.this, "Senha de Administrador incorreta!", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });

        // Configura o mecanismo de ordenação dinâmico (RowSorter) para filtrar e ordenar as linhas da tabela
        sorter = new TableRowSorter<>(modeloTabela);
        tabelaUsuarios.setRowSorter(sorter);

        // Adiciona a tabela dentro de um painel com barras de rolagem (JScrollPane)
        JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);
        panelTabelaContainer.add(scrollPane, BorderLayout.CENTER);

        // Posiciona o container da tabela na parte inferior central do GridBagLayout
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        panelCentro.add(panelTabelaContainer, gbc);

        // Adiciona o painel centralizado no layout principal do componente
        add(panelCentro, BorderLayout.CENTER);

        // Realiza a carga inicial dos dados dos usuários na tabela
        carregarTabela();

        // Adiciona um listener de componente para recarregar a tabela e limpar os campos sempre que o painel for exibido
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                carregarTabela();
                limparCampos();
            }
        });

        // Adiciona um ouvinte de documento no campo de busca para aplicar o filtro de texto em tempo real na tabela
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
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
                }
            }
        });

        // Associa as ações de clique aos respectivos métodos de controle dos botões
        btnCadastrar.addActionListener(e -> cadastrarUsuario());
        btnAtualizar.addActionListener(e -> atualizarUsuario());
        btnInativar.addActionListener(e -> inativarUsuario());

        // Adiciona um ouvinte de seleção na tabela apenas para capturar a linha selecionada, sem preencher o formulário superior
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

    // Método auxiliar padronizado para criar botões com propriedades visuais customizadas
    private JButton criarBotaoEstilizado(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    // Método responsável por buscar os usuários no banco de dados e popular as linhas da tabela
    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        senhasVisiveis.clear();
        List<Usuario> lista = usuarioDAO.listar();
        if (lista != null) {
            for (Usuario u : lista) {
                Object[] linha = new Object[6];
                linha[0] = u.getIdUsuario();
                linha[1] = u.getNome();
                linha[2] = u.getLogin();
                
                // Formata o CPF recuperado do banco para o padrão visual de pontos e traço na tabela
                String cpfBruto = u.getCpf();
                if (cpfBruto != null) {
                    cpfBruto = cpfBruto.replaceAll("\\D", "");
                    if (cpfBruto.length() == 11) {
                        cpfBruto = cpfBruto.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
                    }
                }
                linha[3] = cpfBruto;
                
                linha[4] = u.getPerfil();
                linha[5] = u.getSenha();
                modeloTabela.addRow(linha);
            }
        }
    }

    // Método acionado ao clicar em Cadastrar: valida os campos, exige a senha do admin e persiste o novo usuário
    private void cadastrarUsuario() {
        String nome = txtNome.getText().trim();
        String usuarioLogin = txtUsuario.getText().trim();
        String cpf = txtCpf.getText().trim();
        String senha = new String(txtSenha.getPassword()).trim();
        String confirmaSenha = new String(txtConfirmaSenha.getPassword()).trim();

        // Reseta as bordas dos campos para o padrão visual original
        txtNome.setBorder(UIManager.getBorder("TextField.border"));
        txtCpf.setBorder(UIManager.getBorder("TextField.border"));
        txtUsuario.setBorder(UIManager.getBorder("TextField.border"));
        txtSenha.setBorder(UIManager.getBorder("TextField.border"));
        txtConfirmaSenha.setBorder(UIManager.getBorder("TextField.border"));

        // Valida se o campo Nome foi preenchido corretamente
        if (nome.isEmpty()) {
            txtNome.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, "Preencha o campo Nome!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Valida se o campo CPF está completo ou vazio
        if (cpf.contains("_") || cpf.isEmpty()) {
            txtCpf.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, "Preencha o campo CPF corretamente!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Valida se o campo de login do usuário foi preenchido
        if (usuarioLogin.isEmpty()) {
            txtUsuario.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, "Preencha o campo Usuário!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Valida se o campo de senha foi preenchido
        if (senha.isEmpty()) {
            txtSenha.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, "Preencha o campo Senha!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Valida se o campo de confirmação de senha foi preenchido
        if (confirmaSenha.isEmpty()) {
            txtConfirmaSenha.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, "Preencha o campo Confirmar Senha!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Valida se as senhas coincidem
        if (!senha.equals(confirmaSenha)) {
            txtSenha.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            txtConfirmaSenha.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, "As senhas não coincidem!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Valida se a estrutura matemática do CPF é válida
        if (!isCpfValido(cpf)) {
            txtCpf.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, "CPF inválido!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Verifica se o CPF informado já se encontra cadastrado no sistema
        if (isCpfCadastrado(cpf, -1)) {
            txtCpf.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, "Este CPF já está cadastrado no sistema!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verifica o status atual do login informado no banco de dados
        String statusUsuario = usuarioDAO.verificarStatusLogin(usuarioLogin);

        if (statusUsuario != null) {
            if (statusUsuario.equalsIgnoreCase("inativo")) {
                JOptionPane.showMessageDialog(this, "Não é possível cadastrar esse usuário, pois foi inativado", "Aviso", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Usuário " + usuarioLogin + " já cadastrado", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        // Exige a confirmação da senha do administrador antes de prosseguir com a inserção no banco de dados
        if (!validarSenhaAdministrador()) {
            JOptionPane.showMessageDialog(this, "Senha de Administrador incorreta!", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Instancia o objeto Usuario com os dados validados do formulário
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setLogin(usuarioLogin);
        u.setCpf(cpf);
        u.setSenha(senha);
        u.setPerfil(cbPerfil.getSelectedItem().toString());
        u.setStatus("ativo");

        // Executa o cadastro no banco de dados através do DAO
        usuarioDAO.cadastrar(u);
        JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso!");
        carregarTabela();
        limparCampos();
    }

    // Método acionado ao clicar em Salvar para atualizar as informações do usuário selecionado (incluindo alteração de senha pela tabela)
    private void atualizarUsuario() {
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para alterar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Exige a senha do administrador para autorizar a modificação dos dados
        if (!validarSenhaAdministrador()) {
            JOptionPane.showMessageDialog(this, "Senha de Administrador incorreta!", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = Integer.parseInt(modeloTabela.getValueAt(linhaSelecionada, 0).toString());

        String nome = String.valueOf(modeloTabela.getValueAt(linhaSelecionada, 1));
        String usuarioLogin = String.valueOf(modeloTabela.getValueAt(linhaSelecionada, 2));
        
        Object cpfObj = modeloTabela.getValueAt(linhaSelecionada, 3);
        String cpf = (cpfObj != null && !cpfObj.toString().equalsIgnoreCase("null")) ? cpfObj.toString() : "";
        
        String perfil = String.valueOf(modeloTabela.getValueAt(linhaSelecionada, 4));
        
        // Pega o valor atualizado da senha diretamente da tabela (caso tenha sido editada após destravada pelo olhinho)
        Object senhaObj = modeloTabela.getValueAt(linhaSelecionada, 5);
        String senhaFinal = (senhaObj != null) ? senhaObj.toString().trim() : "";

        if (cpf.isEmpty() || cpf.contains("_")) {
            JOptionPane.showMessageDialog(this, "O CPF não pode ficar vazio ou incompleto!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isCpfValido(cpf)) {
            JOptionPane.showMessageDialog(this, "CPF inválido!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (isCpfCadastrado(cpf, id)) {
            JOptionPane.showMessageDialog(this, "Este CPF já está cadastrado para outro usuário!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (nome.isEmpty() || usuarioLogin.isEmpty() || senhaFinal.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Os campos Nome, Usuário e Senha não podem ficar vazios!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNome(nome);
        u.setLogin(usuarioLogin);
        u.setCpf(cpf);
        u.setPerfil(perfil);
        u.setSenha(senhaFinal);
        u.setStatus("ativo");

        usuarioDAO.atualizar(u);

        JOptionPane.showMessageDialog(this, "Usuário alterado com sucesso!");
        carregarTabela();
        limparCampos();
    }

    // Método acionado ao clicar em Inativar para desativar o registro de usuário selecionado
    private void inativarUsuario() {
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para inativar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Exige a senha do administrador para autorizar a inativação
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

    // Limpa todos os campos do formulário e redefine as seleções e bordas visuais
    private void limparCampos() {
        txtNome.setText("");
        txtCpf.setValue(null);
        txtUsuario.setText("");
        txtSenha.setText("");
        txtConfirmaSenha.setText("");
        txtNome.setBorder(UIManager.getBorder("TextField.border"));
        txtCpf.setBorder(UIManager.getBorder("TextField.border"));
        txtUsuario.setBorder(UIManager.getBorder("TextField.border"));
        txtSenha.setBorder(UIManager.getBorder("TextField.border"));
        txtConfirmaSenha.setBorder(UIManager.getBorder("TextField.border"));
        cbPerfil.setSelectedIndex(0);
        tabelaUsuarios.clearSelection();
        linhaSelecionada = -1;
    }

    // Valida mathematicalmente os dígitos verificadores do CPF informado
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

    // Verifica se o CPF digitado já existe cadastrado em outra linha da tabela
    private boolean isCpfCadastrado(String cpfCandidato, int idAtual) {
        cpfCandidato = cpfCandidato.replaceAll("\\D", "");
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            Object idObj = modeloTabela.getValueAt(i, 0);
            Object cpfObj = modeloTabela.getValueAt(i, 3);
            
            if (idObj != null && cpfObj != null) {
                int idLinha = Integer.parseInt(idObj.toString());
                String cpfLinha = cpfObj.toString().replaceAll("\\D", "");
                
                if (!cpfLinha.isEmpty() && cpfLinha.equals(cpfCandidato)) {
                    if (idLinha != idAtual) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Abre uma caixa de diálogo solicitando a senha do administrador e a valida
    private boolean validarSenhaAdministrador() {
        JPasswordField senhaField = new JPasswordField();
        Object[] mensagem = {"Digite a senha do Administrador:", senhaField};
        
        // Configura o componente que receberá o foco inicial assim que a caixa de diálogo abrir
        senhaField.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                senhaField.requestFocusInWindow();
            }
            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}
        });

        int opcao = JOptionPane.showConfirmDialog(this, mensagem, "Segurança", JOptionPane.OK_CANCEL_OPTION);
        if (opcao == JOptionPane.OK_OPTION) {
            return new String(senhaField.getPassword()).equals("admin123");
        }
        return false;
    }

    // Cria um ícone vetorial customizado em formato de olho para representar a visualização da senha
    private static Icon criarIconeOlho() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.DARK_GRAY);

                GeneralPath p = new GeneralPath();
                p.moveTo(x + 1, y + 8);
                p.quadTo(x + 10, y + 1, x + 19, y + 8);
                p.quadTo(x + 10, y + 15, x + 1, y + 8);
                g2.fill(p);

                g2.setColor(c != null ? c.getBackground() : Color.WHITE);
                g2.fill(new Ellipse2D.Double(x + 6, y + 4, 8, 8));

                g2.setColor(Color.DARK_GRAY);
                g2.fill(new Ellipse2D.Double(x + 8, y + 6, 4, 4));

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 20; }

            @Override
            public int getIconHeight() { return 16; }
        };
    }

    // Classe interna para renderizar o componente visual da célula de senha na JTable
    private class SenhaCellRenderer implements TableCellRenderer {
        private JPanel panel;
        private JLabel lblSenha;
        private JLabel lblOlho;

        public SenhaCellRenderer() {
            panel = new JPanel(new BorderLayout());
            panel.setOpaque(true);
            lblSenha = new JLabel("••••••••");
            lblSenha.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            
            lblOlho = new JLabel(criarIconeOlho());
            lblOlho.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            
            panel.add(lblSenha, BorderLayout.CENTER);
            panel.add(lblOlho, BorderLayout.EAST);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            boolean visivel = senhasVisiveis.getOrDefault(modelRow, false);
            
            if (visivel) {
                lblSenha.setText(value != null ? value.toString() : "");
            } else {
                lblSenha.setText("••••••••");
            }

            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
                lblSenha.setForeground(table.getSelectionForeground());
            } else {
                panel.setBackground(table.getBackground());
                lblSenha.setForeground(table.getForeground());
            }
            return panel;
        }
    }
}