<!--
Sync Impact Report
- Version change: N/A → 1.0.0
- Modified principles: None (first version)
- Added sections: Core Principles, Technology Stack Requirements, Development Workflow, Governance
- Removed sections: None
- Templates requiring updates: plan-template.md (✅), spec-template.md (✅), tasks-template.md (✅)
- Follow-up TODOs: TODO(RATIFICATION_DATE): original adoption date unknown
-->

# graalvm Constitution

## Core Principles

### I. Library-First
Every feature MUST start as a standalone library. Libraries MUST be self-contained, independently testable, and documented. Each library MUST have a clear purpose and avoid organizational-only code.

### II. CLI Interface
Every library MUST expose functionality via a CLI. Text in/out protocol: stdin/args → stdout, errors → stderr. Libraries MUST support both JSON and human-readable formats.

### III. Test-First (NON-NEGOTIABLE)
Test-driven development (TDD) is mandatory. Tests MUST be written and approved before implementation. The red-green-refactor cycle MUST be strictly enforced.

### IV. Integration Testing
Integration tests are REQUIRED for new library contracts, contract changes, inter-service communication, and shared schemas. All integration points MUST be covered by automated tests.

### V. Observability & Versioning
Structured logging is REQUIRED for all libraries. Semantic versioning (MAJOR.MINOR.PATCH) MUST be used for all releases. Simplicity is prioritized: unnecessary complexity is prohibited.

## Technology Stack Requirements
JVM-based languages (Java, Kotlin, Scala) are preferred. All code MUST be compatible with GraalVM. Build and deployment MUST use Gradle. Compliance with GraalVM runtime standards is mandatory.

## Development Workflow
All code changes MUST undergo code review. Automated tests MUST pass before merging. Deployment approval is REQUIRED from maintainers. Quality gates MUST be enforced for every pull request.

## Governance
This constitution supersedes all other practices. Amendments REQUIRE documentation, approval, and a migration plan. All PRs and reviews MUST verify compliance with these principles. Use the README for runtime development guidance.

**Version**: 1.0.0 | **Ratified**: TODO(RATIFICATION_DATE): original adoption date unknown | **Last Amended**: 2025-09-22
