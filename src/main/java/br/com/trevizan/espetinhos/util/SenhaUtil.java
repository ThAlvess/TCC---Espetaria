
package br.com.trevizan.espetinhos.util;

import org.mindrot.jbcrypt.BCrypt;

public final class SenhaUtil {

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
}
