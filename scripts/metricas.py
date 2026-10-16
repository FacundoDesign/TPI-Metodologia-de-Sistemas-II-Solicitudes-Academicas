#!/usr/bin/env python3
"""Metricas simples de calidad interna, reproducibles (sin herramientas externas).

Uso: python3 scripts/metricas.py [directorio_fuentes]   (por defecto src/main/java)

Definiciones (heuristicas, validas para el estilo de formato del proyecto: 4 espacios, llaves K&R):
  LOC            lineas no vacias ni de comentario
  metodo mas largo   lineas entre la firma y su llave de cierre (indentacion 4)
  decisiones     ocurrencias de if / else if / for / while / case / && / || / ?  (aprox. complejidad ciclomatica)
  literales      comparaciones contra strings literales: .equals("...")
  static mutable campos estaticos no final
  catch vacios   bloques catch sin ninguna sentencia
  params max     mayor cantidad de parametros en un metodo
"""
import re
import sys
from pathlib import Path

raiz = Path(sys.argv[1] if len(sys.argv) > 1 else "src/main/java")
firma = re.compile(r"^    (?:public |private |protected )?(?:static )?(?:final )?[\w<>\[\], ?]+ (\w+)\((.*)\)(?: throws [\w, ]+)? \{\s*$")
no_metodo = {"if", "for", "while", "switch", "catch", "else", "return", "new"}

tot = dict(archivos=0, loc=0, metodos=0, decisiones=0, literales=0, static_mut=0, catch_vacios=0)
mas_largo = (0, "")
params_max = (0, "")
detalle = []
for f in sorted(raiz.rglob("*.java")):
    txt = f.read_text(encoding="utf-8")
    lineas = txt.splitlines()
    loc = sum(1 for l in lineas if l.strip() and not l.strip().startswith(("//", "/*", "*", "*/")))
    sin_com = "\n".join(l for l in lineas if not l.strip().startswith(("//", "*", "/*")))
    dec = len(re.findall(r"\b(?:if|for|while|case)\b|&&|\|\||\?(?!\w*>)", sin_com))
    lit = len(re.findall(r'\.equals\("', sin_com))
    stat = len(re.findall(r"^\s*(?:public |private |protected )?static (?!final)(?![\w<>\[\], ?]+ \w+\()[\w<>\[\], ?]+ \w+\s*(?:=|;)", sin_com, re.M))
    cat = len(re.findall(r"catch \([^)]*\) \{\s*\}", sin_com))
    metodos = 0
    i = 0
    while i < len(lineas):
        m = firma.match(lineas[i])
        if m and m.group(1) not in no_metodo and not lineas[i].strip().startswith(("else", "} ")):
            j = i
            while j < len(lineas) and lineas[j] != "    }":
                j += 1
            largo = j - i + 1
            metodos += 1
            if largo > mas_largo[0]:
                mas_largo = (largo, f"{f.stem}.{m.group(1)}")
            p = m.group(2).strip()

            n = len([x for x in re.split(r",(?![^<]*>)", p) if x.strip()]) if p else 0
            if n > params_max[0]:
                params_max = (n, f"{f.stem}.{m.group(1)}")
            i = j
        i += 1
    detalle.append((f.name, loc, metodos, dec, lit, stat, cat))
    tot["archivos"] += 1
    tot["loc"] += loc
    tot["metodos"] += metodos
    tot["decisiones"] += dec
    tot["literales"] += lit
    tot["static_mut"] += stat
    tot["catch_vacios"] += cat

print(f"{'archivo':36} {'LOC':>5} {'metod':>6} {'decis':>6} {'liter':>6} {'stat':>5} {'catchV':>6}")
for d in detalle:
    print(f"{d[0]:36} {d[1]:>5} {d[2]:>6} {d[3]:>6} {d[4]:>6} {d[5]:>5} {d[6]:>6}")
print("-" * 76)
print(f"archivos={tot['archivos']}  LOC={tot['loc']}  metodos={tot['metodos']}  decisiones={tot['decisiones']}")
print(f"comparaciones con literal={tot['literales']}  campos static mutables={tot['static_mut']}  catch vacios={tot['catch_vacios']}")
print(f"metodo mas largo: {mas_largo[1]} ({mas_largo[0]} lineas)   max parametros: {params_max[1]} ({params_max[0]})")
if tot["archivos"]:
    print(f"LOC promedio por archivo={tot['loc']/tot['archivos']:.1f}  decisiones por archivo={tot['decisiones']/tot['archivos']:.1f}")
