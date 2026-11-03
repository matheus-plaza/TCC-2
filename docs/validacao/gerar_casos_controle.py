"""
Gera os resultados de referência dos casos de controle das fórmulas.

Faz a conta de forma independente do sistema, com frações exatas, e escreve o CSV lido
por CasosControleTest. Para acrescentar laudos reais anonimizados, inclua-os em CASOS,
ou preencha o CSV diretamente com os valores calculados à mão (seção 3.4 do TCC).

Uso: python docs/validacao/gerar_casos_controle.py
"""
import csv
from decimal import Decimal, ROUND_HALF_UP
from fractions import Fraction
from pathlib import Path

DESTINO = Path(__file__).resolve().parents[2] / "TCC-BACK/src/test/resources/validacao/casos-controle.csv"

# caso, Ca, Mg, K, Al, H+Al, V2, PRNT | camada 20-40: Ca, Mg, K, Al, H+Al, argila
CASOS = [
    ("exemplo-srs-tela-a", "3.2", "1.1", "0.35", "0.1", "4.8", "70", "100", "1.0", "0.4", "0.1", "0.1", "2.0", "45"),
    ("prnt-80", "3.2", "1.1", "0.35", "0.1", "4.8", "70", "80", "1.0", "0.4", "0.1", "0.1", "2.0", "45"),
    ("prnt-125", "2.4", "0.8", "0.21", "0.3", "6.1", "65", "125", "0.9", "0.5", "0.08", "0.2", "3.2", "62"),
    ("trigo-v60", "2.8", "0.9", "0.18", "0.2", "5.4", "60", "100", "0.45", "0.3", "0.05", "0.4", "5.1", "38"),
    ("v1-igual-v2", "5", "1.5", "0.5", "0", "3", "70", "100", "1.2", "0.6", "0.2", "0", "2", "30"),
    ("v1-acima-v2", "6.2", "2.1", "0.62", "0", "2.5", "70", "100", "2.1", "0.9", "0.3", "0", "2.4", "55"),
    ("solo-acido-argiloso", "1.1", "0.4", "0.12", "1.3", "11.2", "70", "100", "0.3", "0.1", "0.04", "1.8", "9.6", "72"),
    ("solo-arenoso", "0.9", "0.3", "0.07", "0.4", "2.6", "65", "90", "0.4", "0.2", "0.03", "0.5", "2.1", "14"),
    ("al-zero", "4.1", "1.3", "0.44", "0", "3.7", "70", "100", "1.5", "0.7", "0.15", "0", "2.8", "48"),
    ("soja-v60", "3.2", "1.1", "0.35", "0.1", "4.8", "60", "100", "1.0", "0.4", "0.1", "0.1", "2.0", "45"),
    ("sub-m-no-limite", "3.5", "1.2", "0.3", "0.1", "4.2", "70", "100", "1.0", "0.4", "0.1", "0.375", "2.4", "40"),
    ("sub-m-acima", "3.5", "1.2", "0.3", "0.1", "4.2", "70", "100", "1.0", "0.4", "0.1", "0.376", "2.4", "40"),
    ("sub-v-baixo-sem-gesso", "3.5", "1.2", "0.3", "0.1", "4.2", "70", "100", "0.6", "0.3", "0.1", "0.1", "5.0", "40"),
    ("sub-ca-no-limite", "3.5", "1.2", "0.3", "0.1", "4.2", "70", "100", "0.5", "1.0", "0.1", "0.1", "1.6", "40"),
    ("sub-ca-abaixo", "3.5", "1.2", "0.3", "0.1", "4.2", "70", "100", "0.49", "1.0", "0.1", "0.1", "1.6", "33"),
    ("argila-20", "2.2", "0.7", "0.25", "0.2", "5.2", "65", "100", "0.7", "0.2", "0.05", "0.6", "4.4", "20"),
    ("argila-40", "2.2", "0.7", "0.25", "0.2", "5.2", "65", "100", "0.7", "0.2", "0.05", "0.6", "4.4", "40"),
    ("argila-60", "2.2", "0.7", "0.25", "0.2", "5.2", "65", "100", "0.7", "0.2", "0.05", "0.6", "4.4", "60"),
    ("argila-decimal", "2.2", "0.7", "0.25", "0.2", "5.2", "65", "100", "0.7", "0.2", "0.05", "0.6", "4.4", "47.5"),
]

# Manual, p. 56: critérios na camada de 20-40 cm e dose por argila (até o limite, inclusive)
LIMITE_M = Fraction("20")
LIMITE_CA = Fraction("0.5")
DOSES_GESSO = [(Fraction(20), Fraction(700)), (Fraction(40), Fraction(1200)), (Fraction(60), Fraction(2200)), (None, Fraction(3200))]


def dose_de_gesso(argila):
    return next(dose for limite, dose in DOSES_GESSO if limite is None or argila <= limite)


def arredondar(valor: Fraction, casas: int) -> str:
    exato = Decimal(valor.numerator) / Decimal(valor.denominator)
    return str(exato.quantize(Decimal(1).scaleb(-casas), rounding=ROUND_HALF_UP))


def calcular(caso):
    nome, *numeros = caso
    ca, mg, k, al, h_al, v2, prnt, ca_s, mg_s, k_s, al_s, h_al_s, argila = [Fraction(n) for n in numeros]

    sb = ca + mg + k
    efetiva = sb + al
    total = sb + h_al
    v1 = sb / total * 100
    m = al / efetiva * 100
    nc = max((v2 - v1) * total / prnt, Fraction(0))

    sb_s = ca_s + mg_s + k_s
    m_s = al_s / (sb_s + al_s) * 100
    indica_gesso = m_s > LIMITE_M or ca_s < LIMITE_CA
    gesso = dose_de_gesso(argila) if indica_gesso else Fraction(0)

    return [nome, *caso[1:],
            arredondar(sb, 2), arredondar(efetiva, 2), arredondar(total, 2),
            arredondar(v1, 1), arredondar(m, 1), arredondar(nc, 2), arredondar(gesso, 2)]


CABECALHO = ["caso", "ca", "mg", "k", "al", "h_al", "v2", "prnt",
             "ca_sub", "mg_sub", "k_sub", "al_sub", "h_al_sub", "argila_sub",
             "sb", "t_efetiva", "t_total", "v1", "m", "nc", "gesso"]

DESTINO.parent.mkdir(parents=True, exist_ok=True)
with DESTINO.open("w", newline="", encoding="utf-8") as saida:
    escritor = csv.writer(saida, delimiter=";", lineterminator="\n")
    escritor.writerow(CABECALHO)
    escritor.writerows(calcular(caso) for caso in CASOS)
print(f"{len(CASOS)} casos em {DESTINO}")
