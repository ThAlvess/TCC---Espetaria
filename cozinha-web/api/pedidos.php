
<?php

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');

try {
    require 'C:/xampp/trevizan-config/conexao.php';
    /** @var PDO $pdo */

    $sql = "
        SELECT
            c.id_comanda,
            c.tipo_atendimento,
            c.nome_cliente,
            c.data_abertura,
            m.numero AS numero_mesa,
            ic.id_item_comanda,
            
            ic.quantidade,
            ic.observacao,
            ic.data_envio_cozinha,
            GREATEST(
                0,
                TIMESTAMPDIFF(
                SECOND,
                ic.data_envio_cozinha,
                NOW()
                )
            ) AS tempo_espera_segundos,
            p.nome AS nome_produto

        FROM item_comanda ic
        INNER JOIN comanda c
            ON c.id_comanda = ic.id_comanda
        INNER JOIN produto p
            ON p.id_produto = ic.id_produto
        LEFT JOIN mesa m
            ON m.id_mesa = c.id_mesa
        WHERE c.status = 'ABERTA'
          AND ic.status_item = 'PENDENTE'
          AND p.local_preparo = 'COZINHA'
        ORDER BY c.data_abertura ASC,
                 c.id_comanda ASC,
                 ic.id_item_comanda ASC
    ";

    $stmt = $pdo->query($sql);

    $pedidos = [];

    while ($linha = $stmt->fetch()) {
        $id = (int) $linha['id_comanda'];

        if (!isset($pedidos[$id])) {
            $pedidos[$id] = [
                'id_comanda' => $id,
                'tipo_atendimento' => $linha['tipo_atendimento'],
                'nome_cliente' => $linha['nome_cliente'],
                'numero_mesa' => $linha['numero_mesa'] !== null
                    ? (int) $linha['numero_mesa']
                    : null,
                'data_abertura' => $linha['data_abertura'],
                'itens' => []
            ];
        }

        $pedidos[$id]['itens'][] = [
            'id_item_comanda' => (int) $linha['id_item_comanda'],
            'nome_produto' => $linha['nome_produto'],
            'quantidade' => (int) $linha['quantidade'],
            'observacao' => $linha['observacao'],
            'data_envio_cozinha' => $linha['data_envio_cozinha'],
            'tempo_espera_segundos' => $linha['tempo_espera_segundos'] !== null
                ? (int) $linha['tempo_espera_segundos']
                : null
        ];
    }

    echo json_encode(
        array_values($pedidos),
        JSON_UNESCAPED_UNICODE | JSON_INVALID_UTF8_SUBSTITUTE
    );

} catch (Throwable $e) {
    error_log($e->getMessage());

    http_response_code(500);

    echo json_encode([
        'erro' => 'Não foi possível consultar os pedidos.'
    ]);
}
