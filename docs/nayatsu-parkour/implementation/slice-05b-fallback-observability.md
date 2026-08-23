# Slice 5B - Fallback Observability

## Status
GO WITH DEFERRED ITEMS

## Objetivo
Tornar observavel o fallback local do anti-U-turn quando ele precisar relaxar o soft guard para evitar pool vazio.

## Mudancas
- `filterReverseTurnCandidates` agora retorna `GuardedCandidates`.
- Quando todos os candidatos seriam filtrados, o pool original e preservado e `fallbackUsed=true`.
- Se `nayatsu-generation.debug-fallback-log=true`, o fallback emite log com `fallback_used=true` e `fallback_level=anti-u-turn-relaxed`.

## Arquivos principais
- `src/main/java/us/ajg0702/parkour/game/PkJump.java`
- `src/main/java/us/ajg0702/parkour/game/NayatsuGenerationConfig.java`
- `src/test/java/us/ajg0702/parkour/game/PkJumpTrajectoryUxGuardTest.java`
- `src/test/java/us/ajg0702/parkour/game/NayatsuGenerationConfigTest.java`

## Testes
- Guard normal nao marca fallback.
- Pool totalmente filtrado preserva candidatos e marca fallback.
- Sem direcao horizontal anterior nao marca fallback.
- Debug fallback log default permanece desligado.

## Benchmarks
Benchmark dedicado nao executado. A alteracao adiciona um objeto pequeno por geracao somente quando o guard esta habilitado.

## Decisoes aplicadas
- DEC-007: soft guard nao causa dead-end.
- `fallback_used/fallback_level` ficam disponiveis via debug log opcional.

## Invariantes protegidos
- Fallback relaxa somente o anti-U-turn.
- Hard constraints, dificuldade e progressao nao mudam.
- Sem telemetria permanente obrigatoria.

## Findings
- Telemetria estruturada completa: `DEFERRED_DECISION`.
- Fallback order completo para multiplos guards: `DEFERRED_DECISION`.
- Crowding/Paper fallback real: `RUNTIME_VALIDATION`.

## Duvidas / itens adiados
- `fallback_level` para future recent-region/multiplayer guards fica para quando esses guards existirem.
- Agregacao de metricas p50/p95 por build fica para Slice 6/pre-playtest.

## Resultado
GO WITH DEFERRED ITEMS.

## Proxima slice
Slice 6 preparatoria: consolidar estado e parar em `READY FOR PLAYTEST` para escolhas de tuning/experiencia humana.
