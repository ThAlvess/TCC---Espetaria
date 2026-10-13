
package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.UsuarioDAO;
import br.com.trevizan.espetinhos.model.Usuario;

import javax.swing.*;
import java.awt.*;

public class TrocaSenhaObrigatoriaDialog extends JDialog {

    private final Usuario usuario;
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final JPasswordField campoSenhaAtual =
            new JPasswordField(20);

    private final JPasswordField campoNovaSenha =
            new JPasswordField(20);

    private final JPasswordField campoConfirmarSenha =
            new JPasswordField(20);

    private boolean senhaAlterada = false;

    public TrocaSenhaObrigatoriaDialog(
            JFrame janelaPai,
            Usuario usuario
    ) {
        super(janelaPai, "Primeiro acesso - Alterar senha", true);

        this.usuario = usuario;

        configurarJanela();
    }

    private void configurarJanela() {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 0, 5, 0);

        JLabel titulo = new JLabel("Alteração obrigatória de senha");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(25, 100, 25));

        gbc.gridy = 0;
        painel.add(titulo, gbc);

        JLabel descricao = new JLabel(
                "<html>Por segurança, você precisa definir uma "
                        + "senha pessoal antes de acessar o sistema.</html>"
        );

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 15, 0);
        painel.add(descricao, gbc);

        gbc.insets = new Insets(5, 0, 5, 0);

        gbc.gridy = 2;
        painel.add(new JLabel("Senha provisória:"), gbc);

        gbc.gridy = 3;
        painel.add(campoSenhaAtual, gbc);

        gbc.gridy = 4;
        painel.add(new JLabel("Nova senha:"), gbc);

        gbc.gridy = 5;
        painel.add(campoNovaSenha, gbc);

        gbc.gridy = 6;
        painel.add(new JLabel("Confirmar nova senha:"), gbc);

        gbc.gridy = 7;
        painel.add(campoConfirmarSenha, gbc);

        JLabel observacao = new JLabel(
                "Mínimo de 6 caracteres."
        );

        gbc.gridy = 8;
        painel.add(observacao, gbc);

        JButton botaoAlterar = new JButton("Alterar senha");
        botaoAlterar.setBackground(new Color(0, 102, 0));
        botaoAlterar.setForeground(Color.WHITE);
        botaoAlterar.setFocusPainted(false);

        botaoAlterar.addActionListener(e -> alterarSenha());

        gbc.gridy = 9;
        gbc.insets = new Insets(20, 0, 5, 0);
        painel.add(botaoAlterar, gbc);

        setContentPane(painel);
        getRootPane().setDefaultButton(botaoAlterar);

        setSize(440, 440);
        setLocationRelativeTo(getOwner());
    }

    private void alterarSenha() {

        String senhaAtual =
                new String(campoSenhaAtual.getPassword());

        String novaSenha =
                new String(campoNovaSenha.getPassword());

        String confirmarSenha =
                new String(campoConfirmarSenha.getPassword());

        if (senhaAtual.isEmpty()
                || novaSenha.isEmpty()
                || confirmarSenha.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Preencha todos os campos.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (novaSenha.length() < 6) {

            JOptionPane.showMessageDialog(
                    this,
                    "A nova senha deve ter no mínimo 6 caracteres.",
                    "Senha inválida",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {

            JOptionPane.showMessageDialog(
                    this,
                    "A confirmação não corresponde à nova senha.",
                    "Senha inválida",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (novaSenha.equals(senhaAtual)) {

            JOptionPane.showMessageDialog(
                    this,
                    "A nova senha deve ser diferente da provisória.",
                    "Senha inválida",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {

            boolean sucesso = usuarioDAO.trocarSenhaObrigatoria(
                    usuario.getIdUsuario(),
                    senhaAtual,
                    novaSenha
            );

            if (sucesso) {

                senhaAlterada = true;
                usuario.setTrocaSenhaObrigatoria(false);

                JOptionPane.showMessageDialog(
                        this,
                        "Senha alterada com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE
                );

                dispose();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Não foi possível alterar a senha. "
                                + "Verifique a senha provisória.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao alterar a senha: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public boolean isSenhaAlterada() {
        return senhaAlterada;
    }
}
