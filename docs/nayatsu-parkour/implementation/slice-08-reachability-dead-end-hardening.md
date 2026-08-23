# Slice 8 - Reachability and Dead-End Hardening

## Status
FIXED

## Reachability audit
O `canReach()` anterior seguia o envelope de geracao: max horizontal da dificuldade e `abs(dy) <= 1` apenas ate distancia 4. Isso tinha risco de falso negativo para atalhos fisicamente plausiveis em queda e em movimento diagonal, porque shortcut prevention precisa responder "o jogador consegue fazer?", nao "o gerador gera exatamente esse offset?".

## Physical reachability contract
- Horizontal: distancia euclidiana X/Z.
- Diagonal: considerada pelo envelope fisico, mesmo que o gerador atual so enumere offsets cardinais.
- Upward: `dy > +1` e inalcançavel; `dy = +1` so e considerado alcançavel ate distancia horizontal 4.
- Same-level: alcançavel ate `shapeForDistance(difficulty.max).distance`.
- Downward: usa margem conservadora de queda de ate +2 blocos horizontais para `dy < 0`, limitada por `min(2, abs(dy))`.
- Difficulty: o maximo base vem de `jumps.yml` via `Difficulty`.

## Candidate universe
`PkJump.candidateUniverse(...)` enumera todos os offsets intencionais do contrato atual de geracao:
- toda distancia inteira entre `difficulty.min` e `difficulty.max`;
- sinais X/Z cardinais;
- variantes verticais `y`, `y+1`, `y-1` quando `shapeForDistance` permite;
- long jump `r >= 5` plano por contrato existente.

Diagonais nao sao geradas pelo contrato atual; elas sao apenas consideradas por `canReach()` para evitar atalhos fisicos.

## Dead-end root cause
Uma escolha valida agora podia ser terminal no proximo passo quando o proximo universo inteiro fosse eliminado por sequence-integrity/UX. Isso nao violava a sequencia imediatamente, mas causava encerramento evitavel se outro candidato atual tivesse continuacao.

## Viability implementation
`PkJump.filterOneStepViableCandidates(...)` faz lookahead puro de profundidade 1:
1. simula `activeTrajectory` apos o current ser completado;
2. anexa o candidato;
3. enumera o proximo universo por `jumps.yml`;
4. aplica UX + sequence-integrity;
5. exige `viableNextCandidates > 0`.

Se todos os candidatos validos sao dead-end, a geracao falha como `ONLY_DEAD_END_CANDIDATES`. Nenhum fallback inseguro foi reintroduzido.

## Side-effect safety
O lookahead usa apenas listas temporarias e `Location`. Ele nao chama `place()`, nao altera `PkPlayer.jumps`, nao grava historico real, nao incrementa score, nao consome sequence id real e nao dispara evento.

## Stress results
Relatorio: `build/reports/nayatsu-parkour/slice-08-sequence-stress.md`.

Resultado local:
- seed: 87234191
- generated transitions: 100000
- shortcut invariant violations: 0
- no valid candidate count: 0
- only dead-end candidates count: 0
- dead-end candidate rejects: 2035
- dead-end terminations before viability: 190
- dead-end terminations after viability: 0
- average candidate universe size: 32.5192
- average surviving before viability: 14.06954
- average surviving after viability: 14.04919
- max candidate evaluation count: 40
- runtime: 25087.4403 ms

## Verdict
- Atalho fisico nao consecutivo continua sendo hard reject.
- Dead-end imediato evitavel nao e selecionavel quando existe alternativa viavel.
- Lateral, diagonal fisica, sharp turn e zig-zag continuam permitidos quando preservam sequencia.
