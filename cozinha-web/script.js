
const container = document.getElementById("pedidos");

const itensConhecidos = new Set();
const destaquesAte = new Map();

let primeiraConsulta = true;
let consultaEmAndamento = false;

const DURACAO_DESTAQUE = 10000;


const totalPedidos = document.getElementById("total-pedidos");
const relogio = document.getElementById("relogio");
const indicadorConexao = document.getElementById("indicador-conexao");
const textoConexao = document.getElementById("texto-conexao");

function atualizarRelogio() {
    relogio.textContent = new Date().toLocaleTimeString("pt-BR", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit"
    });
}

function atualizarConexao(conectado) {
    indicadorConexao.classList.toggle("online", conectado);
    indicadorConexao.classList.toggle("offline", !conectado);

    textoConexao.textContent = conectado
        ? "Conectado"
        : "Sem conexão com o servidor";
}

atualizarRelogio();
setInterval(atualizarRelogio, 1000);

function criarElemento(tag, texto, classe) {
    const elemento = document.createElement(tag);

    if (texto !== undefined && texto !== null) {
        elemento.textContent = texto;
    }

    if (classe) {
        elemento.className = classe;
    }

    return elemento;
}

function formatarHorario(data) {
    if (!data) return "--:--";

    const partes = data.split(" ");
    return partes.length > 1
        ? partes[1].substring(0, 5)
        : "--:--";
}

function identificarPedido(pedido) {
    if (pedido.tipo_atendimento === "MESA") {
        return `MESA ${pedido.numero_mesa ?? "?"}`;
    }

    return pedido.tipo_atendimento;
}


function converterDataMySQL(data) {
    if (!data) return null;

    const dataFormatada = data.replace(" ", "T");
    const resultado = new Date(dataFormatada);

    return Number.isNaN(resultado.getTime())
        ? null
        : resultado;
}


function obterTempoMaiorEspera(itens) {
    const tempos = itens
        .map(item => item.tempo_espera_segundos)
        .filter(valor => valor !== null && valor !== undefined)
        .map(Number)
        .filter(valor => Number.isFinite(valor) && valor >= 0);

    return tempos.length > 0
        ? Math.max(...tempos)
        : null;
}



function atualizarCronometros() {
    document.querySelectorAll(".cronometro").forEach(elemento => {

        const tempoInicial = Number(elemento.dataset.tempoInicial);
        const recebidoEm = Number(elemento.dataset.recebidoEm);

        if (
            !elemento.dataset.tempoInicial ||
            !elemento.dataset.recebidoEm ||
            !Number.isFinite(tempoInicial) ||
            !Number.isFinite(recebidoEm)
        ) {
            elemento.textContent = "Horário indisponível";
            return;
        }

        const tempoDecorrido =
            (performance.now() - recebidoEm) / 1000;

        const totalSegundos = Math.max(
            0,
            Math.floor(tempoInicial + tempoDecorrido)
        );

        const minutos = Math.floor(totalSegundos / 60);
        const segundos = totalSegundos % 60;

        elemento.textContent =
            `${String(minutos).padStart(2, "0")}:` +
            `${String(segundos).padStart(2, "0")}`;

        elemento.classList.remove(
            "tempo-verde",
            "tempo-amarelo",
            "tempo-vermelho"
        );

        if (minutos < 10) {
            elemento.classList.add("tempo-verde");
        } else if (minutos < 20) {
            elemento.classList.add("tempo-amarelo");
        } else {
            elemento.classList.add("tempo-vermelho");
        }
    });
}



async function carregarPedidos() {

    if (consultaEmAndamento) return;
        consultaEmAndamento = true;

    try {
        const resposta = await fetch("api/pedidos.php", {
            cache: "no-store"
        });

        if (!resposta.ok) {
            throw new Error("Erro HTTP: " + resposta.status);
        }

        const pedidos = await resposta.json();

        if (!Array.isArray(pedidos)) {
            throw new Error("Resposta inválida da API");
        }

        totalPedidos.textContent = pedidos.length;
        atualizarConexao(true);
        
        const agora = Date.now();
        const idsAtuais = new Set();

        pedidos.forEach(pedido => {
            let possuiItemNovo = false;

            pedido.itens.forEach(item => {
                const idItem = Number(item.id_item_comanda);
                idsAtuais.add(idItem);

                if (!primeiraConsulta && !itensConhecidos.has(idItem)) {
            possuiItemNovo = true;
                }

                itensConhecidos.add(idItem);
            });

            if (possuiItemNovo) {
                destaquesAte.set(
            pedido.id_comanda,
            agora + DURACAO_DESTAQUE
                );
            }
        });

        for (const id of itensConhecidos) {
            if (!idsAtuais.has(id)) {
        itensConhecidos.delete(id);
            }
        }

        for (const [idComanda, limite] of destaquesAte) {
            if (agora >= limite) {
                destaquesAte.delete(idComanda);
            }
        }

        primeiraConsulta = false;


        const fragmento = document.createDocumentFragment();

        if (pedidos.length === 0) {
            fragmento.appendChild(
                criarElemento("p", "Nenhum pedido pendente.")
            );
        }

        pedidos.forEach(pedido => {
            
        const tiposPermitidos = ["MESA", "RETIRADA", "DELIVERY"];

        const tipo = tiposPermitidos.includes(pedido.tipo_atendimento)
            ? pedido.tipo_atendimento
            : "MESA";

        const card = criarElemento(
            "article",
            null,
            `pedido pedido-${tipo.toLowerCase()}`
        );
        if ((destaquesAte.get(pedido.id_comanda) ?? 0) > Date.now()) {
            card.classList.add("pedido-novo");
        }


            const titulo = criarElemento(
                "h2",
                identificarPedido(pedido)
            );

            const cliente = criarElemento(
                "p",
                `Cliente: ${pedido.nome_cliente || "Não informado"}`
            );

            const horario = criarElemento(
                "p",
                `Comanda #${pedido.id_comanda} - ${formatarHorario(pedido.data_abertura)}`
            );

            const tempoEspera = obterTempoMaiorEspera(pedido.itens);

            const cronometro = criarElemento(
                "div",
                "--:--",
                "cronometro"
            );

            if (tempoEspera !== null) {
                cronometro.dataset.tempoInicial = String(tempoEspera);
                cronometro.dataset.recebidoEm = String(performance.now());
            }

            const lista = criarElemento("ul");

            pedido.itens.forEach(item => {
                const linha = criarElemento("li");

                linha.appendChild(
                    criarElemento(
                        "strong",
                        `${item.quantidade}x ${item.nome_produto}`
                    )
                );

                if (item.observacao) {
                    linha.appendChild(
                        criarElemento(
                            "p",
                            `Obs: ${item.observacao}`,
                            "observacao"
                        )
                    );
                }

                lista.appendChild(linha);
            });

            card.append(titulo, cliente, horario, cronometro, lista);
            fragmento.appendChild(card);
        });

        container.replaceChildren(fragmento);
        atualizarCronometros();

    } catch (erro) {
        console.error("Erro ao carregar pedidos:", erro);
        atualizarConexao(false);

        // Mantém os pedidos anteriores na tela se
        // houver uma falha temporária de conexão.
        if (!container.querySelector(".pedido")) {
            container.replaceChildren(
                criarElemento(
                    "p",
                    "Não foi possível carregar os pedidos."
                )
            );
        }
    }finally {
        consultaEmAndamento = false;
    }
}

carregarPedidos();
setInterval(carregarPedidos, 3000);
setInterval(atualizarCronometros, 1000);
