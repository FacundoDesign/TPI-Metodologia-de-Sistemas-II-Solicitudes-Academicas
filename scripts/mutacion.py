#!/usr/bin/env python3
"""Mutacion manual: cada 'mutante' introduce UN defecto realista en una copia temporal del codigo de produccion y
ejecuta la suite completa. Si alguna prueba falla el mutante esta MATADO (la suite detecta el defecto); si todas
pasan SOBREVIVE (hay un hueco en las pruebas). Mide si las pruebas realmente protegen el comportamiento.
Uso: python3 scripts/mutacion.py > docs/evidencias/mutacion.txt"""
import re, shutil, subprocess, sys, tempfile
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
P = "src/main/java/ar/edu/solicitudes/"
MUTANTES = [
    ("M01", "Solicitud.java", "prioridad.plazoBaseDias() + tipo.plazoExtraDias()", "prioridad.plazoBaseDias() - tipo.plazoExtraDias()", "el plazo resta el extra del tipo en vez de sumarlo"),
    ("M02", "Prioridad.java", "case BAJA -> MEDIA;", "case BAJA -> ALTA;", "la urgencia sube dos niveles"),
    ("M03", "Estado.java", "nuevo == EN_PROCESO || nuevo == RECHAZADA;", "nuevo == RESUELTA || nuevo == RECHAZADA;", "PENDIENTE puede saltar directo a RESUELTA"),
    ("M04", "Estado.java", "this == RESUELTA || this == RECHAZADA", "this == RESUELTA", "RECHAZADA deja de ser estado final"),
    ("M05", "Solicitud.java", "nuevoEstado == Estado.EN_PROCESO && !tieneResponsable()", "nuevoEstado == Estado.EN_PROCESO && tieneResponsable()", "EN_PROCESO exige lo contrario: que SI haya responsable"),
    ("M06", "Solicitud.java", "if (estaCerrada()) throw", "if (!estaCerrada()) throw", "se puede asignar en cerradas y no en abiertas"),
    ("M07", "Estudiante.java", "legajo.length() != 5", "legajo.length() != 6", "el legajo exige 6 digitos"),
    ('M08', "Estudiante.java", 'email.indexOf("@") >= 1', 'email.indexOf("@") >= 0', "se acepta un email que empieza con @"),
    ("M09", "ReporteSolicitudes.java", "* 10) / 10.0", "* 100) / 100.0", "el promedio se redondea a 2 decimales"),
    ("M10", "TipoSolicitud.java", "EQUIVALENCIA(Prioridad.MEDIA, 5)", "EQUIVALENCIA(Prioridad.MEDIA, 4)", "plazo extra de EQUIVALENCIA incorrecto"),
    ("M11", "SolicitudService.java", "if (s.id() == id) return s;", "if (s.id() != id) return s;", "obtener(id) devuelve cualquier otra solicitud"),
    ("M12", "CsvSolicitudRepository.java", r'.replace(";", "\\;")', "", "no se escapa el ';' (reaparece el defecto latente)"),
    ("M13", "SolicitudService.java", "solicitudes.remove(nueva);", "", "no se revierte el alta si falla el guardado (R-4)"),
    ("M14", "SolicitudService.java", "solicitud.restaurar(anterior);", "", "no se revierte el cambio si falla el guardado (R-4)"),
]

def correr(copia: Path) -> bool:
    """Devuelve 'verde' (todo pasa), 'rojo' (compila pero alguna prueba falla) o 'no-compila'."""
    r = subprocess.run(["bash", "scripts/test.sh"], cwd=copia, capture_output=True, text=True)
    if "BUILD OK" not in r.stdout:
        return "no-compila"
    return "verde" if r.returncode == 0 and "OK (" in r.stdout else "rojo"

def main():
    base = Path(tempfile.mkdtemp())
    try:
        copia = base / "w"
        shutil.copytree(RAIZ, copia, ignore=shutil.ignore_patterns(".git", "build"))
        assert correr(copia) == "verde", "la suite base debe estar en verde antes de mutar"
        print("Suite base: VERDE\n")
        print(f"{'id':4} {'resultado':10} {'archivo':28} mutacion")
        matados = 0
        for mid, archivo, viejo, nuevo, desc in MUTANTES:
            ruta = copia / P / archivo
            original = ruta.read_text(encoding="utf-8")
            assert viejo in original, f"{mid}: patron no encontrado en {archivo}"
            ruta.write_text(original.replace(viejo, nuevo, 1), encoding="utf-8")
            resultado = correr(copia)
            ruta.write_text(original, encoding="utf-8")
            assert resultado != "no-compila", f"{mid}: el mutante no compila (no es valido)"
            estado = "SOBREVIVE" if resultado == "verde" else "MATADO"
            matados += 1 if resultado == "rojo" else 0
            print(f"{mid:4} {estado:10} {archivo:28} {desc}")
        print(f"\nMutantes matados: {matados} de {len(MUTANTES)}  (puntaje de mutacion {100*matados/len(MUTANTES):.0f} %)")
    finally:
        shutil.rmtree(base, ignore_errors=True)

if __name__ == "__main__":
    main()
