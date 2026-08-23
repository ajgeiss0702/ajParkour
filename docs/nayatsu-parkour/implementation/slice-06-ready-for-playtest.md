# Slice 6 - Gameplay Validation Prep

## Status
READY FOR PLAYTEST

## Objetivo
Preparar o fork para validacao humana e tuning sem escolher valores subjetivos automaticamente.

## Mudancas
- Nenhuma mudanca de gameplay nesta slice.
- Consolidado o estado de build/test/benchmark e as variantes configuraveis para playtest.

## Arquivos principais
- `build/libs/ajParkour-2.12.11.jar` gerado localmente por `./gradlew.bat clean build`.
- `build/reports/nayatsu-parkour/slice-1-generation-benchmark.md` regenerado pela suite.

## Testes
`./gradlew.bat clean build` verde.

Resultado atual:
- 25 testes
- 0 failures
- 0 errors
- 0 skipped

## Benchmarks
Benchmark local minimo de candidatos:
- samples: 10000
- candidate_count: 13
- p50_ns: 600
- p95_ns: 1800
- p99_ns: 3800
- max_ns: 1601900

Limitacao: nao substitui Paper benchmark, TPS/MSPT, packet/chunk cost ou crowding real.

## Variantes prontas para playtest
Editar `config.yml` ou config de staging antes de iniciar novas sessoes:

```yaml
nayatsu-generation:
  enabled: true
  recent-history-size: 3
  anti-u-turn:
    enabled: true
```

```yaml
nayatsu-generation:
  enabled: true
  recent-history-size: 5
  anti-u-turn:
    enabled: true
```

```yaml
nayatsu-generation:
  enabled: true
  recent-history-size: 7
  anti-u-turn:
    enabled: true
```

Controle / rollback local:

```yaml
nayatsu-generation:
  enabled: false
```

## Decisoes aplicadas
- Nao fechar HYP-001 sem comparar 3/5/7.
- Nao escolher radius de recent-region sem evidencia.
- Nao alterar BUG-002/BUG-003.
- Nao implementar player-specific trajectory.

## Invariantes protegidos
- Kill switch desliga o guard anti-U-turn.
- Historico recente por si so nao altera selecao.
- Progressao continua AS-IS corrigida apenas pelos fixes seguros de Slice 2A.

## Findings
- BUG-002: `DEFERRED_BEHAVIOR_SENSITIVE`.
- BUG-003: `DEFERRED_BEHAVIOR_SENSITIVE`.
- EC-050/052/054: `RUNTIME_VALIDATION_REQUIRED`.
- HYP-001: `PLAYTEST_REQUIRED`.
- HYP-002: `PLAYTEST_REQUIRED`.
- HYP-003: `DEFERRED_DECISION`.
- HYP-006: `DEFERRED_DECISION` / `RUNTIME_VALIDATION`.

## Duvidas / itens adiados
- Escolher `recent-history-size`.
- Decidir se anti-U-turn melhora clareza sem reduzir dificuldade percebida.
- Definir recent-region radius.
- Executar Paper/crowding validation.
- Decidir qualquer telemetria estruturada permanente.

## Resultado
READY FOR PLAYTEST.

## Proxima slice
Rodar playtest conforme `docs/nayatsu-parkour/07-playtest-protocol.md`. Proxima implementacao deve aguardar evidencia humana ou runtime para tuning/decisoes pendentes.
