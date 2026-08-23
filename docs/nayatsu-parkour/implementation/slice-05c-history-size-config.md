# Slice 5C - Recent History Size Config

## Status
GO

## Objetivo
Conectar `nayatsu-generation.recent-history-size` ao historico espacial para permitir variantes 3/5/7 sem nova build.

## Mudancas
- `PkPlayer` passa a ler `recent-history-size` no inicio da sessao.
- Default permanece 5.
- Valores numericos em YAML e strings numericas sao aceitos pelo helper de config.

## Arquivos principais
- `src/main/java/us/ajg0702/parkour/game/NayatsuGenerationConfig.java`
- `src/main/java/us/ajg0702/parkour/game/PkPlayer.java`
- `src/test/java/us/ajg0702/parkour/game/NayatsuGenerationConfigTest.java`

## Testes
- `recentHistorySizeAcceptsNumberOrStringWithFallback`
- Testes de historico de Slice 3 continuam protegendo limite por parametro.

## Benchmarks
Nao aplicavel separadamente. A janela continua pequena e configuravel; comparacao 3/5/7 e parte de playtest/tuning.

## Decisoes aplicadas
- HYP-001 continua aberto.
- O valor 5 e default experimental, nao decisao final.

## Invariantes protegidos
- Mudar o tamanho do historico nao muda selecao sozinho.
- Config e lida por nova sessao; nao reescreve historico de sessao ja ativa.

## Findings
- Escolha final 3/5/7: `PLAYTEST_REQUIRED`.

## Duvidas / itens adiados
- Fechar HYP-001 fica para Slice 6.

## Resultado
GO.

## Proxima slice
Slice 6 preparatoria / READY FOR PLAYTEST.
