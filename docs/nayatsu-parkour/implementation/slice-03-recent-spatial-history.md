# Slice 3 - Recent Spatial History

## Status
GO

## Objetivo
Adicionar historico espacial recente por jogador como estado separado, sem mudar selecao, scoring, dificuldade ou progressao.

## Mudancas
- Adicionado `recentJumpHistory` por `PkPlayer`.
- Tamanho inicial: 5, tratado como default experimental de HYP-001.
- Alimentacao ocorre em `madeIt()`, no consumo de `jumps[0]`, antes de remover o bloco antigo.
- Historico armazena copia da `Location` consumida para evitar mutacao externa.
- Limpeza ocorre em `end()`, cobrindo quit/kick e disable/restart que convergem para fim da sessao.
- Exposto snapshot package-private para uso futuro do Slice 4.

## Arquivos principais
- `src/main/java/us/ajg0702/parkour/game/PkPlayer.java`
- `src/test/java/us/ajg0702/parkour/game/PkPlayerRecentHistoryTest.java`

## Testes
- `recentHistoryRetainsLastFiveConsumedJumpsInOrder`
- `recentHistoryStoresLocationCopies`
- `recentHistoryCanBeClearedOnSessionEnd`

## Benchmarks
Benchmark dedicado nao executado. A operacao adicionada e limitada a janela de 5 itens por progresso, nao roda no `PlayerMoveEvent` quando nao ha progresso e nao participa da selecao.

## Decisoes aplicadas
- DEC-005: memoria espacial recente e estado separado, nao derivado de `jumps`.
- HYP-001: tamanho 5 permanece experimental e nao fecha tuning.
- DEC-010: nenhuma mudanca na autoridade de progressao.

## Invariantes protegidos
- Selecao de candidatos inalterada.
- Scoring inalterado.
- Dificuldade inalterada.
- Historico e por jogador, nao global.
- Player-specific blocks/rendering nao implementado; HYP-006 permanece aberto.

## Findings
- EC-050: `RUNTIME_VALIDATION`. Reload de areas com sessoes ativas permanece comportamento AS-IS; historico fica preso ao `PkPlayer` ativo.
- EC-052: `RUNTIME_VALIDATION`. Death segue sem handler dedicado.
- EC-054: `RUNTIME_VALIDATION`. Disable/restart chama `end()` e limpa historico; validacao Paper/manual ainda pertence ao gate de runtime.
- Crowding/multiplayer: `BACKLOG` para Slice 4/5. O historico atual e self-history; trajetorias alheias continuam tratadas pelo scoring AS-IS.

## Duvidas / itens adiados
- Instrumentacao `distance_to_recent_*` / `recent_region_reentry` fica para Slice 4/5 quando houver guard/config/metricas.
- Comparacao 3/5/7 fica para Slice 6/playtest.
- Radius de recent-region permanece indefinido.

## Resultado
GO.

## Proxima slice
Slice 4 - Trajectory UX Guard, mas apenas a parte objetivamente executavel sem escolher tuning subjetivo. Valores behavior-sensitive devem ficar configuraveis/deferred.
