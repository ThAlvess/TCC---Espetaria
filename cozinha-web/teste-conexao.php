
<?php

try {
    require 'C:/xampp/trevizan-config/conexao.php';

    $stmt = $pdo->query("SELECT COUNT(*) FROM comanda");
    $total = $stmt->fetchColumn();

    echo "Conexao realizada com sucesso!";
    echo "<p>Comandas cadastradas: " . (int)$total . "</p>";

} catch (Throwable $e) {
    http_response_code(500);
    echo "Erro ao conectar ou consultar o banco.";
}
