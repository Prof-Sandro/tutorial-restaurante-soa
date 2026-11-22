// Finalidade: Criar app.js e ligá-lo ao HTML para consultar o catálogo público.
'use strict';
const el = id => document.getElementById(id);
let dados = { categorias: [], produtos: [] }, categoria = null;
const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const numero = new URLSearchParams(location.search).get('mesa');
if (numero && /^[0-9]{1,4}$/.test(numero) && Number(numero) > 0) {
    el('mesa').textContent = `Mesa ${String(Number(numero)).padStart(2, '0')}`;
    el('qr').href = `../api/public/qr?mesa=${Number(numero)}`;
    el('qr').hidden = false;
}
function texto(tag, valor, classe) {
    const n = document.createElement(tag); n.textContent = valor;
    if (classe) n.className = classe;
    return n;
}
function renderizar() {
    const termo = el('busca').value.toLocaleLowerCase('pt-BR').trim();
    const lista = dados.produtos.filter(p => (categoria === null || p.categoriaId === categoria) && `${p.nome} ${p.descricao}`.toLocaleLowerCase('pt-BR').includes(termo));
    el('produtos').replaceChildren();
    for (const p of lista) {
        const card = document.createElement('article'); card.className = 'prato';
        const img = document.createElement('img');
        img.src = typeof p.imagem === 'string' && (/^assets\/[\w-]+\.(png|jpe?g|webp|svg)$/.test(p.imagem) || /^https:\/\//i.test(p.imagem)) ? p.imagem : 'https://raw.githubusercontent.com/Prof-Sandro/restaurante-imagens/main/produtos/macarrao_espaguete.jpg';
        img.alt = p.nome; img.loading = 'lazy'; img.referrerPolicy = 'no-referrer';
        img.addEventListener('error', () => { if (img.src !== 'https://raw.githubusercontent.com/Prof-Sandro/restaurante-imagens/main/produtos/macarrao_espaguete.jpg') img.src = 'https://raw.githubusercontent.com/Prof-Sandro/restaurante-imagens/main/produtos/macarrao_espaguete.jpg'; }, { once: true });
        const detalhes = texto('div', '', 'detalhes');
        if (!p.disponivel) detalhes.append(texto('span', 'Indisponível no momento', 'indisponivel'));
        detalhes.append(texto('h3', p.nome), texto('p', p.descricao), texto('p', p.porcao, 'porcao'), texto('p', moeda.format(p.preco), 'preco'));
        card.append(img, detalhes); el('produtos').append(card);
    }
    el('status').textContent = lista.length ? `${lista.length} ${lista.length === 1 ? 'opção' : 'opções'} para você.` : 'Nenhum prato encontrado. Experimente outra busca ou categoria.';
}
function filtros() {
    el('categorias').replaceChildren();
    for (const c of [{ id: null, nome: 'Todo o cardápio' }, ...dados.categorias]) {
        const b = texto('button', c.nome); b.type = 'button'; b.setAttribute('aria-pressed', String(c.id === categoria));
        b.addEventListener('click', () => { categoria = c.id; filtros(); renderizar();[...el('categorias').children].find(x => x.textContent === c.nome)?.focus(); });
        el('categorias').append(b);
    }
}
async function carregar() {
    el('tentar').hidden = true; el('status').textContent = 'Buscando os sabores de hoje…';
    const controle = new AbortController(); const tempo = setTimeout(() => controle.abort(), 10000);
    try {
        const resposta = await fetch('../api/public/cardapio', { signal: controle.signal, headers: { Accept: 'application/json' }, credentials: 'omit' });
        if (!resposta.ok) throw new Error('Cardápio indisponível');
        dados = await resposta.json();
        if (!Array.isArray(dados.categorias) || !Array.isArray(dados.produtos)) throw new Error('Resposta inválida');
        categoria = null; filtros(); renderizar();
    } catch {
        el('status').textContent = 'Não conseguimos carregar o cardápio. Verifique sua conexão e tente novamente.';
        el('tentar').hidden = false;
    } finally { clearTimeout(tempo); }
}
el('busca').addEventListener('input', renderizar);
el('tentar').addEventListener('click', carregar);
carregar();
