# Declaración de uso de Inteligencia Artificial

> La consigna permite la IA como apoyo, siempre que se declare **herramienta, finalidad y grado de intervención** y se adjunte evidencia.
> **La IA no reemplaza la autoría ni la defensa:** el equipo debe comprender y justificar todo lo entregado.

| Campo | Declaración |
|---|---|
| Herramienta | Claude (Anthropic), usado como asistente de programación y estudio. |
| Finalidad | (1) Comprender la consigna y mapearla con los apuntes; (2) proponer un sistema base con deuda técnica realista; (3) proponer el diagnóstico, los refactorings, las pruebas y la documentación; (4) generar scripts de evidencia (métricas, comparación antes/después). |
| Grado de intervención | **Alto en este borrador inicial** (la IA escribió el código y los documentos). **[COMPLETAR: qué revisó, modificó y decidió cada integrante]**. |
| Verificación humana | **[COMPLETAR]**: ejecutar `./scripts/test.sh` y `./scripts/comparar-versiones.sh`, leer cada commit con `git show`, y poder explicar cada refactoring. |
| Evidencia del intercambio | Registro de prompts (abajo) + conversación completa en el ZIP de anexos. |

## Registro de prompts

| # | Fecha | Herramienta | Prompt (resumen fiel) | Para qué | Resultado / cómo se usó |
|---|---|---|---|---|---|
| 1 | Sesión 16–18/10/2026 | Claude | Se adjuntaron la consigna del TPI, el PDF oficial, el modelo de presentación (ZIP) y los apuntes de la materia. Pedido textual: “analiza en profundidad el trabajo y el material de estudio, explícamelo y hazlo”. | Entender qué se pide y construir el TPI. | Mapa consigna → apuntes; sistema base `estado-inicial`; diagnóstico de 13 hallazgos; red de seguridad (*golden master*); 6 refactorings; 85 pruebas; code review con 3 correcciones; scripts de evidencia; CI; informe PDF y guía de defensa. |
| 2 | **[COMPLETAR]** | | **[COMPLETAR: cada prompt propio del equipo]** | | |

> Recomendación: agregar acá **todas** las consultas que haga cada integrante (incluso las de estudio) y pegar la conversación completa en `anexo/conversaciones-ia/`.

## Reflexión sobre el uso responsable (Unidad 5)
- La IA es buena para ordenar, generar variantes y detectar patrones; **no tiene el contexto de la cátedra ni valida por sí sola**. Toda salida se contrastó con
  evidencia ejecutable (pruebas y comparación contra la versión inicial), no con la confianza en el texto generado.
- Los datos de ejemplo son **sintéticos** (sin datos personales reales).
