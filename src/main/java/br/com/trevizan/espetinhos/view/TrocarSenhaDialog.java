package br.com.trevizan.espetinhos.view;

import br.com.trevizan.espetinhos.dao.UsuarioDAO;
import br.com.trevizan.espetinhos.model.Usuario;
import br.com.trevizan.espetinhos.util.PadraoComponentes;
import br.com.trevizan.espetinhos.util.SenhaUtil;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * Janelas (JOptionPane com OK / Cancelar) para o usuário criar ou trocar a
 * própria senha.
 *
 * <ul>
 *   <li><b>Primeiro acesso</b>: aparece logo após o login de quem ainda usa a
 *       senha padrão ({@link SenhaUtil#SENHA_PADRAO}). Não pede a senha atual
 *       e, se for cancelada, o login não é concluído.</li>
 *   <li><b>Troca de senha</b>: aberta pelo menu da engrenagem (qualquer
 *       perfil). Pede a senha atual antes de aceitar a nova.</li>
 * </ul>
 *
 * Se algo estiver errado (senha curta, confirmação diferente...), mostra o
 * aviso e reabre a janela mantendo o que já foi digitado. A senha é sempre
 * gravada criptografada (ver {@link SenhaUtil}).
 */
public final class TrocarSenhaDialog {

    private TrocarSenhaDialog() {
    }

    /**
     * Abre a janela e espera o usuário terminar.
     *
     * @return {@code true} se a senha foi gravada; {@code false} se cancelou
     */
    public static boolean mostrar(Component pai, Usuario usuario, boolean primeiroAcesso) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        String titulo = primeiroAcesso ? "Primeiro acesso" : "Redefinir senha";
        String nome = usuario.getNome() != null ? usuario.getNome() : usuario.getLogin();

        JPasswordField txtSenhaAtual = criarCampoSenha();
        JPasswordField txtNovaSenha = criarCampoSenha();
        JPasswordField txtConfirmacao = criarCampoSenha();

        String explicacao = primeiroAcesso
                ? "<html>Olá, <b>" + nome + "</b>! Você entrou com a senha padrão.<br>"
                        + "Para continuar, crie uma senha pessoal.</html>"
                : "<html>Usuário: <b>" + nome + "</b></html>";
        String regras = "Mínimo de " + SenhaUtil.TAMANHO_MINIMO
                + " caracteres. Não pode ser a senha padrão.";

        Object[] mensagem = primeiroAcesso
                ? new Object[]{explicacao, " ", "Nova senha:", txtNovaSenha,
                    "Confirmar nova senha:", txtConfirmacao, regras}
                : new Object[]{explicacao, " ", "Senha atual:", txtSenhaAtual, "Nova senha:", txtNovaSenha,
                    "Confirmar nova senha:", txtConfirmacao, regras};

        JComponent primeiroCampo = primeiroAcesso ? txtNovaSenha : txtSenhaAtual;

        while (true) {
            // coloca o cursor no primeiro campo assim que a janela abrir
            SwingUtilities.invokeLater(primeiroCampo::requestFocusInWindow);

            int opcao = JOptionPane.showConfirmDialog(pai, mensagem, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (opcao != JOptionPane.OK_OPTION) {
                if (primeiroAcesso) {
                    JOptionPane.showMessageDialog(pai,
                            "Sem criar a senha pessoal não é possível entrar no sistema.",
                            titulo, JOptionPane.WARNING_MESSAGE);
                }
                return false;
            }

            String novaSenha = new String(txtNovaSenha.getPassword());
            String confirmacao = new String(txtConfirmacao.getPassword());
            String erro = null;

            if (!primeiroAcesso) {
                String senhaAtual = new String(txtSenhaAtual.getPassword());
                if (senhaAtual.isEmpty()) {
                    erro = "Informe a senha atual.";
                } else if (!usuarioDAO.conferirSenha(usuario.getIdUsuario(), senhaAtual)) {
                    erro = "A senha atual está incorreta.";
                    txtSenhaAtual.setText("");
                } else if (senhaAtual.equals(novaSenha)) {
                    erro = "A nova senha deve ser diferente da atual.";
                }
            }

            if (erro == null) {
                erro = SenhaUtil.validarNovaSenha(novaSenha, confirmacao);
            }

            if (erro != null) {
                JOptionPane.showMessageDialog(pai, erro, titulo, JOptionPane.WARNING_MESSAGE);
                continue; // reabre a janela com o que já foi digitado
            }

            try {
                usuarioDAO.alterarSenha(usuario.getIdUsuario(), novaSenha);
            } catch (RuntimeException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(pai,
                        "Não foi possível salvar a senha no banco de dados.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            JOptionPane.showMessageDialog(pai,
                    primeiroAcesso ? "Senha criada com sucesso!" : "Senha alterada com sucesso!",
                    titulo, JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
    }

    private static JPasswordField criarCampoSenha() {
        JPasswordField campo = new JPasswordField(20);
        PadraoComponentes.habilitarMostrarSenha(campo);
        return campo;
    }
}
