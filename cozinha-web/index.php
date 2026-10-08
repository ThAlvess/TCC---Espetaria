
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Cozinha - Espetinhos Trevizan</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>

<header class="topo">
    <div class="identidade">
        <h1>ESPETINHOS TREVIZAN</h1>
        <p>PAINEL DE PEDIDOS • COZINHA</p>
    </div>

    <div class="informacoes">
        <div class="resumo">
            <span id="total-pedidos">0</span>
            <span>pedidos pendentes</span>
        </div>

        <div class="relogio" id="relogio">--:--:--</div>

        <div class="conexao">
            <span id="indicador-conexao"
                  class="indicador"></span>
            <span id="texto-conexao">Conectando...</span>
        </div>
    </div>
</header>

<main>
    <div id="pedidos" aria-live="polite">
        <p>Carregando pedidos...</p>
    </div>
</main>

<script src="script.js"></script>
</body>
</html>
