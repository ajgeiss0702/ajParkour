# Slice 5A - Config / Kill Switch

## Status
GO WITH DEFERRED ITEMS

## Objetivo
Adicionar o namespace `nayatsu-generation.*` e colocar o UX Guard atras de kill switch, preservando rollback para comportamento build B quando a chave esta ausente ou desabilitada.

## Mudancas
- Adicionado `NayatsuGenerationConfig`.
- `nayatsu-generation.enabled` ausente resolve como `false`.
- `nayatsu-generation.anti-u-turn.enabled` ausente resolve como `true` quando o namespace geral esta habilitado.
- O filtro anti-U-turn de Slice 4A so roda quando `nayatsu-generation.enabled=true`.
- Adicionado namespace default em `config.yml` para novas instalacoes.

## Arquivos principais
- `src/main/java/us/ajg0702/parkour/game/NayatsuGenerationConfig.java`
- `src/main/java/us/ajg0702/parkour/game/PkJump.java`
- `src/main/resources/config.yml`
- `src/test/java/us/ajg0702/parkour/game/NayatsuGenerationConfigTest.java`

## Testes
- `missingGenerationEnabledDefaultsToDisabledForOldConfigs`
- `explicitGenerationEnabledAcceptsBooleanOrString`
- `nestedAntiUTurnDefaultsToEnabledWhenNamespaceIsEnabled`

## Benchmarks
Benchmark dedicado nao executado. A checagem de config ocorre apenas na construcao de `PkJump`, nao em todo `PlayerMoveEvent`.

## Decisoes aplicadas
- Kill switch `enabled: false` aproxima comportamento build B.
- Chave ausente = false para rollback/backward compatibility.
- Telemetria permanece opt-in.

## Invariantes protegidos
- Config antiga sem `nayatsu-generation` nao ativa guards.
- Dificuldade, scoring base e progressao permanecem separados.
- O guard anti-U-turn continua com fallback local para nao esvaziar candidatos.

## Findings
- Fallback observavel completo (`fallback_used/fallback_level`) ainda nao esta implementado: `BACKLOG` para Slice 5B, porque exige definir formato de metrica/log e ponto de emissao.
- Reload/restart Paper real: `RUNTIME_VALIDATION`.
- Multiplayer/crowding fallback real: `RUNTIME_VALIDATION`.

## Duvidas / itens adiados
- Ordem completa de relaxamento de fallback permanece `DEFERRED_DECISION`.
- Recent-region config existe, mas radius `-1` significa indefinido/desabilitado ate decisao/tuning.
- Metricas consolidadas ficam para Slice 5B/6.

## Resultado
GO WITH DEFERRED ITEMS.

## Proxima slice
Slice 5B ou Slice 6 preparatoria, dependendo de haver decisao objetiva para formato de fallback/metricas. Caso contrario, preparar relatorio e parar em itens deferred/playtest.
