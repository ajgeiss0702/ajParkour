# Slice 2 - Upstream Correctness Fixes / BUG-001

## Status
GO

## Objetivo
Corrigir isoladamente BUG-001: `score =- 10` em candidatos fora da area deve ser `score -= 10`, preservando o score acumulado antes da penalidade de area.

## Mudancas
- Corrigido o typo de atribuicao em `PkJump.getBlockScore`.
- Atualizado o teste de score fora da area para o novo contrato correto.
- Nenhuma mudanca em UX Guard, dificuldade, yaw weighting, clamp de distancia ou progressao.

## Arquivos principais
- `src/main/java/us/ajg0702/parkour/game/PkJump.java`
- `src/test/java/us/ajg0702/parkour/game/PkJumpScoreCharacterizationTest.java`

## Testes
`./gradlew.bat test` verde: 14 testes.

Teste novo/alterado:
- `outOfAreaCandidateSubtractsTenWithoutDiscardingPriorScore`

## Benchmarks
Benchmark separado nao executado: a mudanca troca uma atribuicao por uma subtracao no mesmo hot path, sem alterar complexidade, alocacao ou numero de candidatos. A suite continua gerando o baseline minimo de Slice 1 para candidatos.

## Decisoes aplicadas
- DEC-008: fix de correctness isolado.
- DEC-009: sem regressao estrutural de performance esperada.

## Invariantes protegidos
- Candidato fora da area continua penalizado.
- A penalidade de distancia da parede/fora da area continua aplicada depois.
- O score anterior nao e mais descartado silenciosamente.

## Findings
- Nenhum finding bloqueante.

## Duvidas / itens adiados
- BUG-002 permanece sem correcao: depende de baseline/decisao sobre yaw.
- BUG-003 permanece como contrato atual documentado: corrigir clamp pode mudar Expert/custom configs.
- BUG-005 nao foi tocado nesta alteracao.

## Resultado
GO para BUG-001.

## Proxima slice
Ainda dentro de Slice 2, BUG-002/BUG-003 exigem decisao/benchmark antes de correcao. Sem essa decisao, proxima execucao objetiva deve parar ou apenas documentar dead code/contrato atual.
