# Declaración de uso de Inteligencia Artificial

> La consigna permite la IA como apoyo, siempre que se declare **herramienta, finalidad y grado de intervención** y se adjunte evidencia.
> **La IA no reemplaza la autoría ni la defensa:** el equipo debe comprender y justificar todo lo entregado.

| Campo | Declaración |
|---|---|
| Herramienta | Claude (Anthropic), usado como asistente de programación y estudio. |
| Finalidad | (1) Comprender la consigna y mapearla con los apuntes; (2) proponer un sistema base con deuda técnica realista; (3) proponer el diagnóstico, los refactorings, las pruebas y la documentación; (4) generar scripts de evidencia (métricas, comparación antes/después, mutación, experimento OCP); (5) auditar el trabajo contra la consigna antes de entregarlo. |
| Grado de intervención | **Alto en este borrador inicial** (la IA escribió el código y los documentos). **[COMPLETAR: qué revisó, modificó y decidió cada integrante]**. |
| Verificación humana | **[COMPLETAR]**: ejecutar `./scripts/test.sh` y `./scripts/comparar-versiones.sh`, leer cada commit con `git show`, y poder explicar cada refactoring. |
| Evidencia del intercambio | Registro de prompts (abajo) + conversación completa en el ZIP de anexos. |

## Registro de prompts

| # | Fecha | Herramienta | Prompt (resumen fiel) | Para qué | Resultado / cómo se usó |
|---|---|---|---|---|---|
| 1 | Sesión 16–18/10/2026 | Claude | Se adjuntaron la consigna del TPI, el PDF oficial, el modelo de presentación (ZIP) y los apuntes de la materia. Pedido textual: “analiza en profundidad el trabajo y el material de estudio, explícamelo y hazlo”. | Entender qué se pide y construir el TPI. | Mapa consigna → apuntes; sistema base `estado-inicial`; diagnóstico de 13 hallazgos; red de seguridad (*golden master*); 6 refactorings; 85 pruebas; code review con 3 correcciones; scripts de evidencia; CI; informe PDF y guía de defensa. |
| 2 | 07/11/2026 | Claude | Consulta sobre las reglas de la consigna: “¿El TPI debe ser sí o sí en grupo? ¿Qué cantidad es la mínima?” | Aclarar la modalidad de entrega. | Respuesta basada en la consigna: grupos de hasta 4; individual con justificación/autorización de la cátedra. |
| 3 | 07/11/2026 | Claude | Prompt de auditoría (texto completo en `anexo/conversaciones-ia/`): comparar la consigna con la respuesta, verificar el nivel operativo de cada concepto, revisar definiciones contra su definición técnica exacta, buscar lo que falta, ordenar correcciones por impacto en la nota y hacer una pasada final como corrector exigente; “no me des el 10 fácil”. | Auditar el trabajo antes de entregarlo. | Lista de 14 correcciones; origen de la segunda ronda de code review (R-10 a R-15). |
| 4 | 07/11/2026 | Claude | “Resuelve a la perfección lo más idóneo que puedas… y lo que no, plantéalo bien detallado: qué se necesita y de dónde se saca.” | Aplicar las correcciones posibles y documentar las pendientes. | Commits `499c5ab`, `bd1b3f5`, `c09c380`, `daa974e`; documentos `docs/08` y `PENDIENTES_PARA_EL_EQUIPO.md`. |
| 5 | **[COMPLETAR]** | | **[COMPLETAR: cada prompt propio del equipo, de cada integrante]** | | |

> Recomendación: agregar acá **todas** las consultas que haga cada integrante (incluso las de estudio) y pegar la conversación completa en `anexo/conversaciones-ia/`.

## Reflexión sobre el uso responsable (Unidad 5)
- La IA es buena para ordenar, generar variantes y detectar patrones; **no tiene el contexto de la cátedra ni valida por sí sola**. Toda salida se contrastó con
  evidencia ejecutable (pruebas y comparación contra la versión inicial), no con la confianza en el texto generado.
- Los datos de ejemplo son **sintéticos** (sin datos personales reales).
