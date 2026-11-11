Declaración de uso de inteligencia artificial

La consigna permite usar IA como apoyo, siempre que se declare la herramienta, la finalidad y el grado de intervención, y que se adjunte evidencia del intercambio. La IA no reemplaza la autoría ni la defensa: tengo que poder explicar y justificar todo lo que entrego.

En mi caso el uso fue acotado y pasivo. El diseño del sistema base, el diagnóstico, la elección y el orden de los refactorings, las pruebas y la redacción del informe los hice yo. La IA apareció de dos formas concretas: el autocompletado del editor y tres consultas puntuales de repaso.

| Campo | Declaración |
|---|---|
| Herramienta | GitHub Copilot dentro de Visual Studio Code, como autocompletado en línea, y Google Gemini para consultas puntuales de repaso (tres consultas). |
| Finalidad | **Copilot:** completar líneas repetitivas de Java (getters, constructores, armado de cadenas de impresión, esqueleto de los bucles de los scripts) y recordar la firma de las aserciones de JUnit. **Gemini:** aclarar la diferencia entre calidad externa e interna, el significado de deuda técnica según el apunte y qué se entiende por Feature Envy, para no confundir términos al redactar. |
| Grado de intervención | **Bajo.** Copilot sugería líneas que acepté o corregí una por una; las decisiones de diseño (partir `GestorSolicitudes`, usar enums en lugar de Strategy, escribir el golden master antes de tocar el código) fueron mías y las sostengo en las secciones 5, 6 y 9. Los documentos los redacté yo a partir de lo que ya había ejecutado. |
| Verificación | Nada quedó librado a la confianza en la sugerencia: cada cambio se comprobó con la suite completa en verde, con `git diff` contra el commit anterior y con la comparación de las seis salidas contra `estado-inicial`. Si una sugerencia no compilaba o movía la salida, la descartaba. |
| Evidencia | El registro de consultas de abajo y las capturas del panel de Copilot en el anexo. |

Una aclaración sobre la autoría: como el autocompletado propone código que se acepta con Tab, separar "lo mío" de "lo sugerido" no es binario. Mi criterio para esta declaración es que una línea cuenta como asistida solo cuando la acepté sin cambios y resolvía algo que ya tenía decidido; si la sugerencia me obligó a pensarla, la reescribí. Por eso el grado de intervención es bajo y no medio.

## Registro de consultas

| # | Fecha | Herramienta | Consulta | Para qué | Cómo se usó |
|---|---|---|---|---|---|
| 1 | 16/10/2026 | Gemini | «diferencia entre calidad externa y calidad interna en ingeniería de software» | Fijar los dos conceptos antes de escribir la sección 3 | Quedó la distinción del apunte; la sección la redacté con el ejemplo del cambio de tipo de solicitud |
| 2 | 18/10/2026 | Gemini | «qué es Feature Envy y por qué se corrige con Move Method» | Confirmar que el smell 9 estaba bien nombrado | El smell y su tratamiento quedaron como estaban; fue una consulta de control |
| 3 | 25/10/2026 | Gemini | «firma de assertThrows en JUnit 4.13» | Recordar la firma para las pruebas de excepción | La prueba de `SolicitudException` la escribí con esa firma |
| 4 | Durante todo el trabajo | Copilot (VS Code) | Autocompletado en línea, sin prompts escritos | Getters, constructores, bucles de los scripts, formato de impresión | Acepté líneas sueltas y las revisé con `git diff` |

## Una sugerencia aceptada y una descartada, como muestra

```java
// Aceptada en CsvSolicitudRepository (mecánica, ya sabía qué quería)
return String.join(";", campos);

// Descartada: proponía Optional en todos los desde() de los enums
// y preferí dejarlo para el ticket de mejora (ADR-6, R-5)
```

## Reflexión sobre el uso responsable (Unidad 5)
- La IA sirve para ordenar y recordar detalles, pero no tiene el contexto de la cátedra ni valida por sí sola. Toda sugerencia se contrastó con evidencia ejecutable (pruebas y comparación contra la versión inicial), no con la confianza en el texto generado.
- Los datos de ejemplo son **sintéticos** (sin datos personales reales).

