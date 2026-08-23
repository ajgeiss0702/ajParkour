# Slice 4A - Trajectory UX Guard

## Status
GO WITH DEFERRED ITEMS

## Objetivo
Implementar a parte objetivamente executavel do UX Guard: evitar reversao horizontal exata de 180 graus quando houver alternativas, sem alterar dificuldade, distancia, altura, progressao ou criar UI.

## Mudancas
- Adicionado filtro de self U-turn em `PkJump`.
- O filtro usa o segmento horizontal anterior (`jumps[-2] -> from`) e remove candidatos cujo proximo segmento retorna exatamente na direcao oposta.
- Fallback local: se todos os candidatos seriam removidos, o pool original e preservado.
- Sem recent-region radius.
- Sem guard multiplayer/reservation zone.
- Sem player-specific blocks/rendering.

## Arquivos principais
- `src/main/java/us/ajg0702/parkour/game/PkJump.java`
- `src/test/java/us/ajg0702/parkour/game/PkJumpTrajectoryUxGuardTest.java`

## Testes
- `reverseTurnCandidatesAreFilteredWhenAlternativesRemain`
- `guardKeepsOriginalCandidatesWhenAllWouldBeFiltered`
- `guardDoesNothingWithoutPreviousHorizontalDirection`

## Benchmarks
Suite local continua gerando benchmark minimo de candidatos. Benchmark Paper/crowding real nao executado.

## Decisoes aplicadas
- HYP-002 forma inicial documentada: no modelo cardinal, 0 graus e 90 graus permanecem; 180 graus e tratado como reversao indesejada.
- DEC-003: dificuldade continua autoridade; o filtro nao altera `r`, altura ou limites.
- DEC-007: soft guard nao pode causar dead-end; fallback preserva pool original se necessario.
- DEC-010: nenhuma mudanca em `checkMadeIt`.

## Invariantes protegidos
- Candidatos continuam gerados a partir da dificuldade antes do guard.
- O guard so remove candidatos do pool quando ha alternativa.
- UI/holograma/seta/texto nao foram adicionados.
- Progressao antecipada permanece igual.

## Findings
- HYP-003 / recent-region radius: `DEFERRED_DECISION`.
- Multiplayer reservation/isolation: `DEFERRED_DECISION` / `RUNTIME_VALIDATION`.
- Player-specific trajectory: `DEFERRED_DECISION`, HYP-006 permanece aberto.
- Playtest de legibilidade/dificuldade percebida: `PLAYTEST_REQUIRED`.
- Paper/crowding performance: `RUNTIME_VALIDATION`.

## Duvidas / itens adiados
- Configuracao/kill switch/fallback observavel ficam para Slice 5.
- Metricas `u_turn_rate`, `turn_distribution`, `fallback_used/fallback_level` ficam para Slice 5/6.
- Recent-region guard so deve ser implementado quando radius/tuning estiverem definidos ou configuraveis.

## Resultado
GO WITH DEFERRED ITEMS.

## Proxima slice
Slice 5 - Integration / Config / Fallback, para tornar o guard configuravel/observavel e manter rollback claro.
