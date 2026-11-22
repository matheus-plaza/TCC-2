"""Gráficos de dose dos laudos sintéticos, a partir do resultado de referência."""
import csv
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.lines import Line2D

RAIZ = Path(__file__).resolve().parents[2]
REFERENCIA = RAIZ / "TCC-BACK/src/test/resources/validacao/referencia-laudos-sinteticos.csv"
DESTINO = Path(__file__).resolve().parent / "graficos"

# Latossolos na linha de cima, Nitossolo e Argissolo na de baixo
SOLOS = [
    ("lv-argiloso", "Latossolo Vermelho\nmuito argiloso (Cascavel)"),
    ("lv-medio", "Latossolo Vermelho\ntextura média (Paranavaí)"),
    ("lb", "Latossolo Bruno\n(Guarapuava)"),
    ("nitossolo", "Nitossolo Vermelho\n(Londrina)"),
    ("argissolo", "Argissolo Vermelho-Amarelo\n(Umuarama)"),
]
CONDICOES = ["muito-baixa", "baixa", "media", "alta", "muito-alta"]
ROTULO_CONDICAO = ["Muito\nbaixa", "Baixa", "Média", "Alta", "Muito\nalta"]
SERIE_1, SERIE_2 = ("#2a78d6", "o", "-"), ("#eb6834", "s", "--")
TINTA, TINTA_SECUNDARIA, GRADE = "#0b0b0b", "#52514e", "#e4e3df"

with REFERENCIA.open(encoding="utf-8") as entrada:
    linhas = list(csv.DictReader(entrada, delimiter=";"))


def valor(solo, cultura, campo):
    return [next(float(l[campo]) for l in linhas
                 if l["solo"] == solo and l["condicao"] == c and l["cultura"] == cultura) for c in CONDICOES]


def centro(solo, cultura, prefixo):
    return [(a + b) / 2 for a, b in zip(valor(solo, cultura, prefixo + "_min"), valor(solo, cultura, prefixo + "_max"))]


def figura(titulo, eixo_y, series, nome):
    fig, eixos = plt.subplots(2, 3, figsize=(10, 6.4), sharey=True)
    topo = max(max(dados(solo)) for solo, _ in SOLOS for _, dados, _ in series) * 1.1 or 1
    for ax, (solo, rotulo) in zip(eixos.flat, SOLOS):
        for _, dados, (cor, marcador, traco) in series:
            ax.plot(range(len(CONDICOES)), dados(solo), color=cor, marker=marcador, linestyle=traco,
                    linewidth=2, markersize=7, markeredgecolor="white", markeredgewidth=1)
        ax.set_title(rotulo, fontsize=9, color=TINTA, loc="left")
        ax.set_xticks(range(len(CONDICOES)), ROTULO_CONDICAO)
        ax.set_ylim(0, topo)
        ax.tick_params(colors=TINTA_SECUNDARIA, labelsize=7.5)
        ax.grid(axis="y", color=GRADE, linewidth=0.8)
        ax.set_axisbelow(True)
        for lado in ("top", "right"):
            ax.spines[lado].set_visible(False)
        for lado in ("left", "bottom"):
            ax.spines[lado].set_color(GRADE)
    for ax in eixos[:, 0]:
        ax.set_ylabel(eixo_y, fontsize=8.5, color=TINTA_SECUNDARIA)

    legenda = eixos[1, 2]
    legenda.axis("off")
    if len(series) > 1:
        alcas = [Line2D([], [], color=c, marker=m, linestyle=t, linewidth=2, markersize=7, markeredgecolor="white")
                 for _, _, (c, m, t) in series]
        legenda.legend(alcas, [n for n, _, _ in series], loc="center left", fontsize=9, frameon=False)
    legenda.text(0.02, 0.12, "Eixo X: condição de fertilidade.\nMesma escala em todos os painéis.",
                 fontsize=8, color=TINTA_SECUNDARIA, transform=legenda.transAxes)

    fig.suptitle(titulo, x=0.06, ha="left", fontsize=11, color=TINTA)
    fig.subplots_adjust(hspace=0.55, wspace=0.12, top=0.86)
    fig.savefig(DESTINO / nome, dpi=300, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print(DESTINO / nome)


DESTINO.mkdir(exist_ok=True)
plt.rcParams["font.family"] = "DejaVu Sans"

figura("Calcário recomendado (PRNT 100%)", "Calcário (t/ha)", [
    ("Soja (V2 = 60%)", lambda s: valor(s, "Soja", "nc"), SERIE_1),
    ("Milho e trigo (V2 = 70%)", lambda s: valor(s, "Milho", "nc"), SERIE_2),
], "calcario-por-condicao.png")

figura("Gesso agrícola recomendado (igual para as três culturas)", "Gesso (kg/ha)", [
    ("Gesso", lambda s: valor(s, "Soja", "gesso"), SERIE_1),
], "gesso-por-condicao.png")

figura("Adubação da soja, 3,5 t/ha (centro da faixa do manual)", "Dose (kg/ha)", [
    ("P₂O₅", lambda s: centro(s, "Soja", "p2o5"), SERIE_1),
    ("K₂O", lambda s: centro(s, "Soja", "k2o"), SERIE_2),
], "adubacao-soja-por-condicao.png")
