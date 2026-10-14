
package br.com.trevizan.espetinhos.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Regras e criptografia das senhas dos usuários.
 *
 * <p>O banco guarda apenas o hash BCrypt da senha (formato {@code $2a$...}).
 * Para conferir uma senha, o BCrypt calcula o hash do que foi digitado com o
 * mesmo salt e compara os dois.</p>
 */
public final class SenhaUtil {

    /** Senha que todo usuário novo (ou com senha resetada) recebe. */
    public static final String SENHA_PADRAO = "trevizan2026";

    /** Tamanho mínimo da senha pessoal. */
    public static final int TAMANHO_MINIMO = 6;

    private SenhaUtil() {
        // Impede a criação de instâncias.
    }

    public static String gerarHash(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException(
                    "A senha não pode estar vazia."
            );
        }

        return BCrypt.hashpw(senha, BCrypt.gensalt(12));
    }

    public static boolean verificarSenha(
            String senhaDigitada,
            String hashArmazenado
    ) {
        if (senhaDigitada == null || hashArmazenado == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(
                    senhaDigitada,
                    hashArmazenado
            );
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Indica se o valor gravado no banco já é um hash BCrypt. */
    public static boolean ehHashBCrypt(String senhaGravada) {
        return senhaGravada != null
                && (senhaGravada.startsWith("$2a$")
                || senhaGravada.startsWith("$2b$")
                || senhaGravada.startsWith("$2y$"));
    }

    /**
     * Confere a senha digitada com o valor gravado no banco. Aceita o hash
     * BCrypt e, para usuários antigos, a senha ainda gravada em texto puro.
     */
    public static boolean conferir(String senhaDigitada, String senhaGravada) {
        if (senhaDigitada == null || senhaGravada == null) {
            return false;
        }
        if (ehHashBCrypt(senhaGravada)) {
            return verificarSenha(senhaDigitada, senhaGravada);
        }
        return senhaGravada.equals(senhaDigitada);
    }

    /**
     * Valida a nova senha pessoal.
     *
     * @return a mensagem de erro para mostrar ao usuário, ou {@code null} se
     *         a senha estiver válida
     */
    public static String validarNovaSenha(String novaSenha, String confirmacao) {
        if (novaSenha == null || novaSenha.isEmpty()) {
            return "Informe a nova senha.";
        }
        if (novaSenha.length() < TAMANHO_MINIMO) {
            return "A senha deve ter no mínimo " + TAMANHO_MINIMO + " caracteres.";
        }
        if (novaSenha.isBlank() || !novaSenha.equals(novaSenha.strip())) {
            return "A senha não pode começar ou terminar com espaço.";
        }
        if (SENHA_PADRAO.equals(novaSenha)) {
            return "A nova senha não pode ser igual à senha padrão.";
        }
        if (!novaSenha.equals(confirmacao)) {
            return "A confirmação não confere com a nova senha.";
        }
        return null;
    }
}
