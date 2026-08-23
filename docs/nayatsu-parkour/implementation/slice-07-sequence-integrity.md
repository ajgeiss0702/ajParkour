# Slice 7 - Sequence Integrity

## Status
FIXED

## Root cause
O hard trajectory guard anterior protegia UX de caminho, mas nao protegia a propriedade de sequencia. Ele rejeitava U-turn, regiao recente e fallback inseguro, mas nao perguntava se uma plataforma futura ativa poderia alcancar diretamente um candidato novo pulando plataformas intermediarias.

## Runtime model
- `jumps[0]`: plataforma atual do jogador (`CURRENT`).
- `jumps[1..]`: plataformas ja geradas e colocadas que ainda estao pendentes (`PENDING`).
- `recentJumpHistory`: historico espacial de plataformas completadas, usado para UX/recent-region, nao para sequencia.
- `activeTrajectory`: lista ordenada derivada de `jumps`, isto e, `CURRENT + PENDING`.
- `immediatePredecessor`: ultimo item de `activeTrajectory`.

## Reachability authority
`PkJumpSequenceIntegrity.canReach(from, to, difficulty)` e a autoridade canonica atual.

Ela considera:
- mesmo mundo;
- distancia horizontal euclidiana entre blocos;
- maximo horizontal vindo de `difficulty.getMax()` e `PkJump.shapeForDistance`;
- delta vertical permitido pelo envelope atual de geracao: `abs(dy) <= 1` ate distancia 4, e `dy == 0` acima de 4.

Isso representa envelope fisico pratico alinhado com `jumps.yml`, nao igualdade com templates cardinais de geracao.

## Sequence invariant
Ao anexar um candidato a `P0 -> P1 -> ... -> Pn`:
- `Pn -> candidate` deve ser alcancavel.
- nenhum `Pi -> candidate`, para `i < n`, pode ser alcancavel.

Falhas:
- `NEXT_NOT_REACHABLE`;
- `NON_ADJACENT_SHORTCUT`.

## Generation search
O resampling de 8 tentativas foi substituido por enumeracao finita de todas as distancias permitidas por `jumps.yml` para a dificuldade efetiva. `NO_VALID_CANDIDATE` agora significa que nenhum candidato no universo permitido sobreviveu aos guards.

## Failure semantics
Durante `madeIt()`, se nao houver continuacao segura, a run e encerrada de forma controlada com log severo e mensagem de encerramento. Nao ha fallback que selecione candidato rejeitado.

## Compatibility
Lateral, diagonal fisica, sharp turn e zig-zag continuam validos quando:
- cada aresta consecutiva e alcancavel;
- nenhuma aresta nao consecutiva e alcancavel.
