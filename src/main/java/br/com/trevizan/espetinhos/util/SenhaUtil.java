package br.com.trevizan.espetinhos.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Regras e criptografia das senhas dos usuários.
 *
 * <p>O banco guarda apenas um "hash" (resumo criptográfico) gerado com
 * PBKDF2-HMAC-SHA256 e um "salt" aleatório por usuário, no formato:</p>
 *
 * <pre>pbkdf2$&lt;iterações&gt;$&lt;salt em Base64&gt;$&lt;hash em Base64&gt;</pre>
 *
 * <p>Para conferir uma senha, gera o hash do que foi digitado com o mesmo
 * salt e compara os dois. {@link #conferir} também aceita valores gravados
 * em texto puro, que o {@code UsuarioDAO} converte para hash no login.</p>
 */
public final class SenhaUtil {

    /** Senha que todo usuário novo (ou com senha resetada) recebe. */
    public static final String SENHA_PADRAO = "trevizan2026";

    /** Tamanho mínimo da senha pessoal. */
    public static final int TAMANHO_MINIMO = 6;

    private static final String PREFIXO = "pbkdf2";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_HASH = 256;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private SenhaUtil() {
    }

    /** Gera o hash (com salt novo) que será gravado no banco. */
    public static String gerarHash(String senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        ALEATORIO.nextBytes(salt);
        byte[] hash = calcular(senha.toCharArray(), salt, ITERACOES);
        Base64.Encoder base64 = Base64.getEncoder();
        return PREFIXO + "$" + ITERACOES + "$" + base64.encodeToString(salt) + "$" + base64.encodeToString(hash);
    }

    /**
     * Confere se a senha digitada corresponde à que está gravada no banco
     * (aceita o formato com hash e o texto puro).
     */
    public static boolean conferir(String senhaDigitada, String senhaGravada) {
        if (senhaDigitada == null || senhaGravada == null) {
            return false;
        }
        if (!estaCriptografada(senhaGravada)) {
            // valor gravado em texto puro
            return MessageDigest.isEqual(
                    senhaDigitada.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    senhaGravada.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        try {
            String[] partes = senhaGravada.split("\\$");
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] hashGravado = Base64.getDecoder().decode(partes[3]);
            byte[] hashDigitado = calcular(senhaDigitada.toCharArray(), salt, iteracoes);
            return MessageDigest.isEqual(hashGravado, hashDigitado);
        } catch (RuntimeException e) {
            return false; // valor gravado inválido/corrompido
        }
    }

    /** Indica se o valor gravado já está no formato criptografado. */
    public static boolean estaCriptografada(String senhaGravada) {
        return senhaGravada != null && senhaGravada.startsWith(PREFIXO + "$")
                && senhaGravada.split("\\$").length == 4;
    }

    /** Usuário ainda está com a senha padrão e precisa criar a senha pessoal. */
    public static boolean usaSenhaPadrao(String senhaGravada) {
        return conferir(SENHA_PADRAO, senhaGravada);
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

    private static byte[] calcular(char[] senha, byte[] salt, int iteracoes) {
        PBEKeySpec especificacao = new PBEKeySpec(senha, salt, iteracoes, TAMANHO_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacao).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível criptografar a senha.", e);
        } finally {
            especificacao.clearPassword();
        }
    }
}
