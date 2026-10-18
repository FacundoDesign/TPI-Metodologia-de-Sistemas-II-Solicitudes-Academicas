## Qué resuelve este PR (una sola cosa)
Closes #<numero de issue>

## Tipo de cambio
- [ ] refactor (sin cambio de comportamiento)   - [ ] feature   - [ ] bug   - [ ] documentación

## Checklist de code review (docs/05-code-review.md)
- [ ] Compila y pasan todas las pruebas (`./scripts/test.sh` o `mvn verify`)
- [ ] El comportamiento externo no cambió (`./scripts/comparar-versiones.sh`) o el cambio está justificado
- [ ] Nombres descriptivos, funciones pequeñas, sin código muerto
- [ ] Respeta SOLID/DRY (sin estado global, dependencias por interfaz)
- [ ] Errores manejados con excepciones (sin `catch` vacíos)
- [ ] Pruebas nuevas o actualizadas para lo que cambió
- [ ] Documentación actualizada (README / docs)
- [ ] Sin datos personales reales

## Evidencia
<!-- pegar salida de las pruebas, antes/después, capturas -->
