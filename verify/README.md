# verify/ — offline verification scaffolding

These files are **not part of the service** and are not on the Maven build path.
They exist because the environment this project was written in had no access to
Maven Central, so `mvn verify` could not be run there. Rather than ship
unverified code, the main sources were compiled against hand-written stubs and
the domain and service layers were executed directly.

- `stubs/` — 63 minimal stand-ins for the Spring, Jakarta, SLF4J, JUnit, AssertJ
  and Mockito types the sources reference. Enough to type-check, nothing more.
- `DomainHarness.java` — runs the status machine: 43 checks.
- `ServiceHarness.java` — runs `JobApplicationService` against a hand-written
  fake repository: 19 checks.

Reproduce:

```bash
javac -d out $(find verify/stubs -name '*.java') $(find src/main/java -name '*.java')
javac -cp out -d out verify/DomainHarness.java verify/ServiceHarness.java
java -cp out DomainHarness && java -cp out ServiceHarness
```

Once Maven is available this whole directory is redundant — `mvn verify` runs
the four real JUnit classes against the real libraries. Delete it then, or keep
it as a zero-dependency smoke test.
