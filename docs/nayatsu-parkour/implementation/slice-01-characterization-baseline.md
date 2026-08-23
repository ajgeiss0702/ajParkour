# Slice 1 - Characterization Baseline

## Status
GO

## Objetivo
Criar a primeira base automatizada de characterization tests para comportamentos AS-IS criticos do ajParkour 2.12.11, sem alterar gameplay.

## Mudancas
- Adicionado suporte a testes com JUnit 5 e Mockito.
- Extraidos seams package-private em `PkPlayer` para zona de progresso e predicado de queda.
- Extraidos seams package-private em `PkJump` para shape distancia/altura e geracao da lista de candidatos.
- Adicionados testes de progressao, queda, candidatos, clamp/flat, obstaculo e score fora da area.
- Adicionado benchmark minimo de geracao de candidatos em `build/reports/nayatsu-parkour/slice-1-generation-benchmark.md`.

## Arquivos principais
- `build.gradle.kts`
- `src/main/java/us/ajg0702/parkour/game/PkPlayer.java`
- `src/main/java/us/ajg0702/parkour/game/PkJump.java`
- `src/test/java/us/ajg0702/parkour/game/PkPlayerProgressionCharacterizationTest.java`
- `src/test/java/us/ajg0702/parkour/game/PkJumpCandidateCharacterizationTest.java`
- `src/test/java/us/ajg0702/parkour/game/PkJumpScoreCharacterizationTest.java`
- `src/test/java/us/ajg0702/parkour/game/PkJumpGenerationBenchmarkTest.java`

## Testes
`./gradlew.bat test` verde: 14 testes.

Cobertura automatizada inicial:
- CHAR-001, CHAR-002, CHAR-003, CHAR-004 parcialmente por seam X/Z.
- CHAR-006 por `shapeForDistance`.
- CHAR-007 por score fora da area.
- CHAR-008 por shape flat em distancia longa.
- CHAR-011 por candidatos cardinais.
- CHAR-014 por rejeicao de coluna bloqueada.
- CHAR-015 parcialmente por predicado de queda.

## Benchmarks
Benchmark minimo gerado durante `test`.

Ambiente:
- commit base de trabalho: branch `nayatsu/slice-1-characterization`
- Java: runtime do Android Studio JBR usado pelo Gradle wrapper
- OS: Windows
- samples: 10000
- metodologia: medir `shapeForDistance` + `candidateLocations`
- resultado observado: `candidate_count=13`, p50 600 ns, p95 1800 ns, p99 2800 ns, max 123700 ns

Limitacao: ainda nao mede o construtor completo de `PkJump` com world/block scoring e crowding.

## Decisoes aplicadas
- DEC-008: nenhum fix de correctness misturado.
- DEC-009: baseline inicial criado antes de mudancas funcionais.
- DEC-010: progressao apenas caracterizada, nao corrigida.

## Invariantes protegidos
- Progressao continua baseada apenas na zona horizontal X/Z estrita `< 0.8`.
- Y e grounded state continuam fora da decisao de progresso.
- Queda continua usando bloco atual, flying e topo da janela +3.
- Candidatos continuam cardinais e com a duplicata AS-IS.
- Distancia `r >= 5` continua clamped em 5 e flat.
- Candidato fora da area continua usando o bug `score =- 10`.

## Findings
- NEW FINDING / TECHNICAL DEBT: a cobertura Slice 1 ainda nao executa `PkPlayer` completo porque o construtor acopla Bukkit scheduler, DB, inventario, eventos, particles e bloco real. Se mais CHAR tests precisarem cobrir lifecycle completo, sera necessario MockBukkit/Paper harness ou seams adicionais.
- NEW FINDING / PERFORMANCE: o benchmark atual cobre a parte pura da geracao de candidatos, nao o hot path completo de `PkJump`.

## Duvidas / itens adiados
- EC-050/EC-052/EC-054 continuam exigindo teste manual em servidor/Paper.
- CHAR-005, CHAR-010, CHAR-012, CHAR-013, CHAR-016, CHAR-017, CHAR-018, CHAR-019 e CHAR-020 seguem para expansao de harness; nao foram corrigidos nem alterados.
- Baseline multiplayer de runtime/crowding real continua dependente de harness Paper.

## Resultado
GO para uma baseline automatizada inicial, suficiente para iniciar correcoes isoladas de Slice 2 sobre BUG-001 com contrato antigo protegido.

## Proxima slice
Slice 2 - Upstream Correctness Fixes, iniciando por BUG-001 (`score =- 10` -> `score -= 10`) com novo teste de contrato correto e commit separado.
