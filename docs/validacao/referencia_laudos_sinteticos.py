"""Resultado de referência dos laudos sintéticos, com as tabelas do manual transcritas aqui, sem usar o sistema."""
import csv
from decimal import Decimal, ROUND_HALF_UP
from fractions import Fraction
from pathlib import Path

RAIZ = Path(__file__).resolve().parents[2]
LAUDOS = RAIZ / "TCC-BACK/src/main/resources/validacao/laudos-sinteticos.csv"
DESTINO = RAIZ / "TCC-BACK/src/test/resources/validacao/referencia-laudos-sinteticos.csv"

CLASSES = ["Muito baixo", "Baixo", "Médio", "Alto", "Muito alto", "Condição a evitar"]

# Início de cada classe; um valor na lacuna entre duas classes fica na de baixo (dados-do-manual.md, seção 7)
P_POR_ARGILA = [  # Tabela 3.3, mg/dm³: argila < 25%, de 25 a 40% e > 40%
    ["0", "6", "13", "19", "24.01", "120.01"],
    ["0", "4", "9", "13", "18.01", "90.01"],
    ["0", "3", "7", "10", "12.01", "60.01"],
]
K_NA_CTC = ["0", "0.5", "1.1", "2.1", "3.01", "10.01"]  # Tabela 3.4, % de K na CTC
MO_PORCENTAGEM = ["0", "0.7", "1.5", "2.5", "3.41"]  # Tabela 3.2, M.O. em %
MICRO = {  # Tabela 3.5, mg/dm³; o boro de exatamente 0,10 já é Baixo
    "zn": ["0", "0.4", "0.9", "1.3", "10.01", "30.01"],
    "b": ["0", "0.10", "0.21", "0.31", "0.61", "2.01"],
    "cu": ["0", "0.2", "0.6", "0.9", "3.01", "20.01"],
    "mn": ["0", "5", "16", "31", "100.01", "200.01"],
}
# Tabela 9.13: B, Cu, Mn, Zn
DOSE_MICRO_SOJA = {"Muito baixo": (1.5, 1.0, 6.0, 2.0), "Baixo": (1.5, 1.0, 6.0, 2.0), "Médio": (1.0, 0.5, 3.0, 1.5)}

CULTURAS = {
    "Soja": {"v2": 60, "limite": 50, "rendimento": "3.5", "anterior": "Trigo"},
    "Milho": {"v2": 70, "limite": 60, "rendimento": "7.5", "anterior": "Soja"},
    "Trigo": {"v2": 70, "limite": 60, "rendimento": "3.0", "anterior": "Soja"},
}

P2O5 = {
    "Soja": ["101-120", "81-100", "61-80", "41-60", "20-40", "0"],  # Tabela 9.11, 3 a 4 t/ha
    "Milho": ["111-130", "91-110", "71-90", "51-70", "30-50", "0"],  # Tabela 8.22, < 8 t/ha
    "Trigo": ["61-80", "51-60", "41-50", "31-40", "31-40", "0"],  # Tabela 8.30, 2,5 a 3,5 t/ha
}
K2O = {
    "Soja": ["111-130", "91-110", "71-90", "51-70", "0", "0"],  # Tabela 9.12, 3 a 4 t/ha
    "Milho": ["101-130", "71-100", "41-70", "21-40", "20", "0"],  # Tabela 8.23, < 8 t/ha
    "Trigo": ["51-60", "41-50", "31-40", "21-30", "10-20", "0"],  # Tabela 8.31, 2,5 a 3,5 t/ha
}
N_MILHO_LEGUMINOSA = "20-60"  # Tabela 8.21, < 8 t/ha
N_TRIGO_LEGUMINOSA = {"Até Médio": "51-70", "Alto": "31-50", "Muito alto": "31-40"}  # Tabela 8.29, 2,5 a 3,5 t/ha

# Gessagem, p. 56
DOSES_GESSO = [(20, 700), (40, 1200), (60, 2200), (None, 3200)]


def classe(valor, inicios):
    return CLASSES[max(i for i, inicio in enumerate(inicios) if valor >= Fraction(inicio))]


def arredondar(valor, casas):
    exato = Decimal(valor.numerator) / Decimal(valor.denominator)
    return exato.quantize(Decimal(1).scaleb(-casas), rounding=ROUND_HALF_UP)


def faixa(texto):
    minimo, _, maximo = texto.partition("-")
    return minimo, maximo or minimo


def coluna_de_argila(argila):
    return 0 if argila < 25 else 1 if argila <= 40 else 2


def calcular(sup, sub, cultura):
    c = CULTURAS[cultura]
    ca, mg, k, al, h_al = (Fraction(sup[x]) for x in ("ca", "mg", "k", "al", "h_al"))
    sb = ca + mg + k
    t = sb + h_al
    v1 = arredondar(sb / t * 100, 1)
    nc = Fraction(0)
    if v1 < c["limite"]:
        nc = max((c["v2"] * t - 100 * sb) / 100, Fraction(0))

    ca_s, mg_s, k_s, al_s = (Fraction(sub[x]) for x in ("ca", "mg", "k", "al"))
    m_s = al_s / (ca_s + mg_s + k_s + al_s) * 100
    gesso = 0
    if m_s > 20 or ca_s < Fraction("0.5"):
        argila_s = Fraction(sub["argila"])
        gesso = next(dose for limite, dose in DOSES_GESSO if limite is None or argila_s <= limite)

    classe_p = classe(Fraction(sup["p"]), P_POR_ARGILA[coluna_de_argila(Fraction(sup["argila"]))])
    k_ctc = arredondar(k / t * 100, 1)
    classe_k = classe(Fraction(k_ctc), K_NA_CTC)
    classe_mo = classe(Fraction(sup["mo"]) / 10, MO_PORCENTAGEM)

    if cultura == "Soja":
        n = "0"
    elif cultura == "Milho":
        n = N_MILHO_LEGUMINOSA
    else:
        n = N_TRIGO_LEGUMINOSA.get(classe_mo, N_TRIGO_LEGUMINOSA["Até Médio"])
    p_dose = P2O5[cultura][CLASSES.index(classe_p)]
    k_dose = K2O[cultura][CLASSES.index(classe_k)]

    micro = ["", "", "", ""]
    if cultura == "Soja":
        doses = {}
        for nutriente in ("b", "cu", "mn", "zn"):
            cl = classe(Fraction(sup[nutriente]), MICRO[nutriente])
            posicao = ("b", "cu", "mn", "zn").index(nutriente)
            doses[nutriente] = DOSE_MICRO_SOJA.get(cl, (0, 0, 0, 0))[posicao]
        micro = [doses["zn"], doses["b"], doses["cu"], doses["mn"]]

    return [sup["solo"], sup["condicao"], cultura, c["rendimento"], c["anterior"],
            arredondar(sb, 2), arredondar(t, 2), v1, arredondar(nc, 2), gesso,
            classe_p, classe_k, k_ctc, classe_mo, *faixa(n), *faixa(p_dose), *faixa(k_dose), *micro]


CABECALHO = ["solo", "condicao", "cultura", "rendimento", "anterior", "sb", "t_total", "v1", "nc", "gesso",
             "classe_p", "classe_k", "k_ctc", "classe_mo", "n_min", "n_max", "p2o5_min", "p2o5_max",
             "k2o_min", "k2o_max", "zn", "b", "cu", "mn"]

with LAUDOS.open(encoding="utf-8") as entrada:
    linhas = list(csv.DictReader(entrada, delimiter=";"))
camadas = {(l["solo"], l["condicao"], l["camada"]): l for l in linhas}
cenarios = list(dict.fromkeys((l["solo"], l["condicao"]) for l in linhas))

resultados = [calcular(camadas[(s, c, "0-20")], camadas[(s, c, "20-40")], cultura)
              for s, c in cenarios for cultura in CULTURAS]

DESTINO.parent.mkdir(parents=True, exist_ok=True)
with DESTINO.open("w", newline="", encoding="utf-8") as saida:
    escritor = csv.writer(saida, delimiter=";", lineterminator="\n")
    escritor.writerow(CABECALHO)
    escritor.writerows(resultados)
print(f"{len(resultados)} resultados de referência em {DESTINO}")
