# Slice 6A - Persistent Trajectory Fix

## Status
FIXED

## Root cause
- `PkPlayer.madeIt()` gravava `jumps.get(0).getTo()` no historico recente, mas o bloco completado pelo player e `jumps.get(1).getTo()`.
- O guard de `PkJump` barrava apenas anti-U-turn exato. Ele nao usava o historico recente para rejeitar retorno para regioes ja ocupadas.
- O fallback antigo podia preservar o pool original quando todos os candidatos fossem filtrados, reintroduzindo candidatos invalidos.
- Configs antigas sem `nayatsu-generation.enabled` desligavam o guard por default.

## Runtime path
`PkPlayer.checkMadeIt()` chama `madeIt()`, que incrementa score, grava historico, remove o bloco antigo, usa o ultimo bloco vivo como `prevJump`, constroi `new PkJump(this, prevJump)`, chama `place()` e adiciona o jump novo.

Dentro de `PkJump`, o caminho corrigido e:

```text
candidateLocations(...)
  -> filterNayatsuUxGuardCandidates(...)
  -> getBlockScore(...) somente para diagnostico/scoring legacy
  -> selectHighestEligibleScores(...)
  -> selected candidate
```

## Fix
- Historico recente agora recebe o bloco que acabou de ser completado.
- UX Guard agora rejeita hard:
  - reversao anti-U-turn;
  - candidato cuja distancia horizontal para uma posicao recente seja menor ou igual a distancia do salto atual.
- Scoring legacy e yaw rodam depois do filtro; candidato rejeitado nao participa do pool selecionavel.
- Se nenhum candidato guardado sobreviver, o gerador tenta outra amostra ate 8 vezes preservando distancia/altura de `jumps.yml`.
- Se ainda nao houver candidato valido, a geracao falha explicitamente em vez de escolher geometria invalida.
- `nayatsu-generation.enabled` ausente agora liga o guard; rollback requer `enabled: false` explicito.
- Telemetria temporaria fica ligada por default quando `telemetry.enabled` esta ausente.

## Diagnostics
Com telemetria ativa, cada geracao loga:

```text
generation=<id> current=<xyz> previous=<xyz> movementVector=<xyz> playerYaw=<yaw> recentHistory=[...]
generation=<id> candidate=<xyz> distanceFromCurrent=<n> nearestRecentDistance=<n> turnAngle=<n> yawDelta=NA antiUTurn=<PASS|REJECT> recentRegion=<PASS|REJECT> visualSeparation=<PASS|REJECT> originalEligibility=PASS final=<ACCEPT|REJECT> reason=<...> score=<...>
generation=<id> SELECTED candidate=<xyz> eligibleCandidates=<n> rejectedCandidates=<n> fallbackUsed=false score=<...>
```

## Tests
- Reversao anti-U-turn e filtrada quando ha alternativas.
- Pool todo invalido nao volta por fallback relaxado.
- Sem direcao horizontal anterior nao rejeita por anti-U-turn.
- Retorno para regiao recente e rejeitado mesmo sem U-turn exato.
- Historico carregado por multiplas geracoes rejeita retorno ambiguo.
- Candidato rejeitado nao vence mesmo tendo maior score legacy.
- Config ausente liga o guard por default.

## Fallback verdict
Nenhum fallback ainda pode bypassar o UX Guard. O unico fallback remanescente e resampling; ele troca o conjunto de candidatos, nao escolhe candidato rejeitado.
