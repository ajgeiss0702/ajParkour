# Slice 2A - Safe Correctness Closure

## Status
GO WITH DEFERRED ITEMS

## Objetivo
Fechar o escopo executavel e seguro de correctness da Slice 2 sem resolver itens behavior-sensitive.

## Mudancas
- BUG-005: renome local `ydist` para `zdist` no predicado de progresso extraido em Slice 1.
- Sem mudanca funcional: a condicao continua comparando X/Z contra o centro de `jumps[1]` com tolerancia estrita `< 0.8`.
- BUG-001 ja foi corrigido no commit `dbfeb53`.

## Arquivos principais
- `src/main/java/us/ajg0702/parkour/game/PkPlayer.java`

## Testes
Suite esperada: `./gradlew.bat test` e `./gradlew.bat build`.

## Benchmarks
Nao aplicavel para BUG-005: renome local sem mudanca de bytecode relevante para algoritmo, complexidade, alocacao ou numero de candidatos.

## Decisoes aplicadas
- DEC-008: correctness fixes isolados.
- Regra de continuidade: somente `BLOCKING_NOW` interrompe execucao.

## Invariantes protegidos
- Progressao continua sem Y/grounded/velocity/origem.
- BUG-002 e BUG-003 nao foram alterados.
- Dificuldade e yaw weighting permanecem como no estado atual.

## Findings
- BUG-002: `DEFERRED / BEHAVIOR-SENSITIVE`. Nao corrigir sem baseline/decisao de yaw.
- BUG-003: `DEFERRED / BEHAVIOR-SENSITIVE`. Nao alterar clamp `r = 5` sem config real/decisao; pode afetar Expert/custom configs.
- EC-050: `RUNTIME_VALIDATION REQUIRED`. Nao bloqueia desenvolvimento local independente.
- EC-052: `RUNTIME_VALIDATION REQUIRED`. Nao bloqueia desenvolvimento local independente.
- EC-054: `RUNTIME_VALIDATION REQUIRED`. Nao bloqueia desenvolvimento local independente.

## Duvidas / itens adiados
- BUG-002 fica para Slice 2B/deferred behavior-sensitive.
- BUG-003 fica para Slice 2B/deferred behavior-sensitive.
- Validacoes Paper/manual ficam registradas para gate de runtime, nao para bloquear Slice 3 local.

## Resultado
GO WITH DEFERRED ITEMS.

## Proxima slice
Slice 3 - Recent Spatial History, desde que implementada sem tuning subjetivo e sem mudar selecao de candidatos.
