---
name: local-precheck
description: Run a local Gradle test + SonarQube analysis before committing, so you know your coverage and code quality status in minutes instead of waiting ~10 minutes for the Jenkins pipeline. Trigger this skill whenever the user asks to "check coverage before commit", "run local sonar check", "pre-check my build", or similar.
---

# Local Pre-Commit Sonar Check

## Purpose
Give a fast, local signal on test coverage and SonarQube code-quality issues
(bugs, vulnerabilities, code smells) for the current working tree, before
pushing and waiting on the Jenkins pipeline.

## Prerequisites (one-time setup)

1. SonarQube personal access token (My Account → Security → Generate Token in SonarQube).
2. Project must already have the `org.sonarqube` Gradle plugin configured
   (it is, since Jenkins runs `sonar` as part of the pipeline).
3. SonarQube host URL and project key — these are normally already in
   `gradle.properties` (project-level) since Jenkins uses them.

## Step 1 — Credential check

Check `~/.gradle/gradle.properties` for a line:
```
systemProp.sonar.login=<token>
```

If it is missing:
1. Ask the user to paste their SonarQube personal access token.
2. Append the line above to `~/.gradle/gradle.properties` (create the file
   if it doesn't exist; create the `~/.gradle` directory if needed).
3. Do NOT print the token back to the user or log it anywhere.
4. Confirm the file was written successfully without echoing its contents.

Do not ask for the Sonar host URL or project key — these come from the
project's existing `gradle.properties` / `build.gradle`, which already work
with Jenkins.

## Step 2 — Run tests and generate coverage

```bash
./gradlew clean test jacocoTestReport
```

If this fails, show the test failure summary (failed test names) and stop —
there's no point running Sonar on a build with failing tests. Do not attempt
to interpret or fix failing tests automatically; just report them.

## Step 3 — Run Sonar analysis

```bash
./gradlew sonar
```

This uploads the analysis (including the JaCoCo report from Step 2) to the
SonarQube server using the token from `~/.gradle/gradle.properties`.

Common failure modes and how to handle them:
- **401/403 Unauthorized** → token is invalid or expired. Tell the user
  plainly and offer to re-prompt for a new token (repeat Step 1).
- **Could not resolve host / connection refused** → likely VPN/network issue
  reaching the Sonar server. Tell the user to check connectivity.
- **Plugin not found / task 'sonar' not found** → the `org.sonarqube` plugin
  may not be applied in this module. Show the raw Gradle error.

For all other errors, show the raw Gradle error output rather than guessing.

## Step 4 — Poll for analysis completion

The `./gradlew sonar` task prints a line like:
```
More about the report processing at <SONAR_URL>/api/ce/task?id=<TASK_ID>
```

Extract `SONAR_URL` and `TASK_ID` from this output. Poll:
```bash
curl -s -u "<token>:" "<SONAR_URL>/api/ce/task?id=<TASK_ID>"
```
until `"status"` is `SUCCESS`, `FAILED`, or `CANCELLED` (check every ~5
seconds, timeout after ~2 minutes). If `FAILED`/`CANCELLED`, report this and
stop.

On `SUCCESS`, extract `analysisId` if present, then proceed to Step 5.

## Step 5 — Fetch results

Get the project key from `build.gradle` (`sonar.projectKey` property) or
`gradle.properties`. Then:

```bash
curl -s -u "<token>:" \
  "<SONAR_URL>/api/measures/component?component=<PROJECT_KEY>&metricKeys=coverage,new_coverage,bugs,vulnerabilities,code_smells,uncovered_lines"
```

Also fetch the quality gate status:
```bash
curl -s -u "<token>:" \
  "<SONAR_URL>/api/qualitygates/project_status?projectKey=<PROJECT_KEY>"
```

## Step 6 — Present summary

Present a concise summary, e.g.:

```
Local Pre-Check Results
------------------------
Quality Gate: PASS / FAIL
Coverage:     XX.X%  (uncovered lines: N)
Bugs:         N
Vulnerabilities: N
Code Smells:  N

Dashboard: <SONAR_URL>/dashboard?id=<PROJECT_KEY>
```

If the quality gate FAILS, list which specific conditions failed (from the
`project_status` response's `conditions` array) so the user knows exactly
what to fix before committing.

## Notes / Limitations

- This skill currently covers SonarQube only. Nexus IQ vulnerability
  scanning is a planned follow-up, pending confirmation of how to invoke
  the Nexus IQ scanner without direct CLI jar download access.
- This runs a full project analysis (not diff-only), matching the existing
  Jenkins behaviour, so results are directly comparable.
- Typical run time: a few minutes, dominated by `test` + Sonar processing —
  still much faster than waiting for the full Jenkins pipeline.
