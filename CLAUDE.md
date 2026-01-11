# CLAUDE.md
### 重要!!!1つのメッセージで複数のタスク呼び出しを実行する!!!!
作業開始と終了時にMemoryMCPを参照/更新する
1つのメッセージで複数のタスク呼び出しを実行する
並列ワーカーを優先して利用する
ファイル確認や現状確認，調査はSerenaMCPを積極的に利用する
Skillで参照するべきものが無いか，確認してから作業を開始する
必要と判断した事項，重要情報，は必ずMemoryMCPで保存する
積極的にMemoryMCPを参照する

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java 21 development workspace using DevContainer for consistent development environment setup.

### Development Environment

- **Java Version**: 21 (OpenJDK from Microsoft DevContainer)
- **Base Image**: `mcr.microsoft.com/devcontainers/java:1-21-bullseye`
- **Package Managers**: Maven and Gradle are available but not pre-installed in the DevContainer (configure as needed)

## Spec Workflow System

This project uses a structured specification workflow system located in `.spec-workflow/`. The system includes templates for requirements, design, and task management.

### Key Templates

1. **Requirements Document** (`.spec-workflow/templates/requirements-template.md`):
   - Defines user stories with acceptance criteria
   - Specifies non-functional requirements including architecture, performance, security, reliability, and usability
   - **Critical**: All requirements must follow the "WHEN [event] THEN [system] SHALL [response]" format for acceptance criteria

2. **Design Document** (`.spec-workflow/templates/design-template.md`):
   - Describes overall architecture and design patterns
   - **Code Reuse Analysis**: Must identify existing components to leverage, extend, or integrate
   - **Integration Points**: Must document how new features connect with existing systems
   - **Modular Design Principles**:
     - Single File Responsibility: Each file handles one specific concern
     - Component Isolation: Small, focused components over monolithic files
     - Service Layer Separation: Data access, business logic, and presentation layers must be separated
     - Utility Modularity: Break utilities into focused, single-purpose modules

3. **Tasks Document** (`.spec-workflow/templates/tasks-template.md`):
   - Detailed task breakdown with specific prompts for each task
   - Each task includes:
     - File path
     - Implementation details
     - Existing code to leverage (reuse requirement)
     - Requirements reference
     - Specialized prompt with role, task, restrictions, and success criteria

4. **Technology Stack** (`.spec-workflow/templates/tech-template.md`):
   - Documents core technologies, dependencies, and architecture patterns
   - Includes development workflow, code quality tools, and deployment considerations

5. **Project Structure** (`.spec-workflow/templates/structure-template.md`):
   - Defines directory organization and naming conventions
   - Specifies import patterns and code organization principles
   - **Core Principles**: Single Responsibility, Modularity, Testability, Consistency

6. **Product Overview** (`.spec-workflow/templates/product-template.md`):
   - Documents product purpose, target users, and key features
   - Defines business objectives and success metrics

### Custom Templates

Custom templates can be created in `.spec-workflow/user-templates/` to override default templates. Template files must have the exact same name as the default they replace:
- `requirements-template.md`
- `design-template.md`
- `tasks-template.md`
- `tech-template.md`
- `structure-template.md`
- `product-template.md`

### Development Workflow

When implementing features in this project:

1. **Use Spec Workflow**: Leverage the structured templates in `.spec-workflow/templates/` for requirements, design, and tasks
2. **Follow Modular Design Principles**:
   - Single Responsibility: Each file should have one clear purpose
   - Component Isolation: Create small, focused components
   - Service Layer Separation: Separate data access, business logic, and presentation
   - Code Reuse: Always identify and leverage existing components before creating new code
3. **Document Integration Points**: Clearly document how new code integrates with existing systems
4. **Maintain Consistency**: Follow patterns established in the codebase for naming, structure, and organization

## Setting Up Java Build System

This project currently has no build system configured. When setting up:

### Using Maven:
```bash
# Create pom.xml with project configuration
mvn archetype:generate -DgroupId=com.example -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
# Build
mvn clean install
# Run tests
mvn test
# Single test
mvn test -Dtest=MyTestClass#testMethod
```

### Using Gradle:
```bash
# Initialize Gradle project
gradle init --type java-application
# Build
./gradlew build
# Run tests
./gradlew test
# Single test
./gradlew test --tests MyTestClass.testMethod
```

## Directory Structure

```
/workspaces/java/
├── .devcontainer/           # DevContainer configuration
│   └── devcontainer.json    # Java 21 environment setup
├── .github/                 # GitHub configuration
│   └── dependabot.yml       # Dependency update automation
└── .spec-workflow/          # Specification workflow templates
    ├── templates/           # Default templates (requirements, design, tasks, etc.)
    ├── user-templates/      # Custom template overrides
    ├── specs/               # Generated specification documents
    ├── steering/            # Technical and product steering documents
    ├── approvals/           # Approval workflow artifacts
    └── archive/             # Archived specifications
```

## Common Commands

*Note: Build commands depend on the build system (Maven/Gradle) chosen for the project.*

```bash
# Java version check
java -version

# Maven (if configured)
mvn clean compile    # Compile the project
mvn clean package    # Package the project
mvn test            # Run all tests
mvn test -Dtest=ClassName  # Run specific test class

# Gradle (if configured)
./gradlew build     # Build the project
./gradlew test      # Run all tests
./gradlew test --tests ClassName  # Run specific test class
```

## Architecture Principles

Based on the Spec Workflow templates, this project emphasizes:

1. **Modular Architecture**: Components should be isolated and reusable
2. **Service Layer Pattern**: Clear separation between data access, business logic, and presentation
3. **Code Reuse**: Always leverage existing components before creating new ones
4. **Single Responsibility**: Each file and component should have one clear purpose
5. **Testability**: Structure code to be easily testable with clear boundaries
6. **Documentation-First**: Use structured templates for requirements, design, and tasks

## Development Guidelines

When adding new features or making changes:

1. **Check for Existing Code**: Always search for existing utilities, services, or patterns before creating new code
2. **Use Spec Workflow Templates**: Follow the structured approach in `.spec-workflow/templates/`
3. **Document Integration**: Clearly explain how new code connects with existing systems in design documents
4. **Maintain Modularity**: Keep components small and focused
5. **Write Tests**: Ensure all code is testable and covered by tests

---

## RPGPlugin Project Specifics

### プロジェクト概要

Minecraft Java RPG Plugin - Paper API 1.20.6対応のRPGシステムプラグイン

### コアシステム

| システム | 説明 | 場所 |
|---------|------|------|
| **Component System V5** | コンポーネントベースのスキルシステム | `skill/component/` |
| **Class System** | クラス・ジョブシステム | `rpgclass/` |
| **Stat System** | ステータス管理 | `stats/` |
| **Skill System** | スキル実行・管理 | `skill/` |
| **Damage System** | ダメージ計算・修正 | `damage/` |
| **Storage** | データ永続化 | `storage/` |

### V5 コンポーネントシステム

スキルは以下のコンポーネントタイプで構成されます：

- **Trigger**: `CAST`, `CROUCH`, `LAND`, `DEATH`, `KILL`, `PHYSICAL_DEALT`, `PHYSICAL_TAKEN`, `LAUNCH`, `ENVIRONMENTAL`
- **Target**: `SELF`, `SINGLE`, `CONE`, `SPHERE`, `SECTOR`, `AREA`, `LINE`, `NEAREST_HOSTILE`
- **Condition**: `health`, `chance`, `mana`, `biome`, `class`, `time`, `armor`, `fire`, `water`, `combat`, `potion`, `status`, `tool`, `event`
- **Mechanic**: `damage`, `heal`, `push`, `fire`, `message`, `potion`, `lightning`, `sound`, `command`, `explosion`, `speed`, `particle`, `launch`, `delay`, `cleanse`, `channel`
- **Cost**: `MANA`, `HP`, `STAMINA`, `ITEM`
- **Cooldown**: `COOLDOWN`
- **Filter**: `entity_type`, `group`

### 外部ディレクトリ構造（plugins/MCRPG/）

```
plugins/MCRPG/
├── config.yml              # メイン設定
├── skills/
│   ├── active/            # アクティブスキル（trigger: CAST）
│   ├── passive/           # パッシブスキル（イベントトリガー）
│   └── README.txt
├── classes/               # クラス定義YAML
├── templates/
│   ├── skills/
│   │   └── skill_template.yml  # V5テンプレート（単一）
│   └── classes/           # クラステンプレート
├── mobs/                  # MythicMobs連携
├── exp/                   # 経験値設定
└── data/
    └── database.db        # SQLiteデータベース
```

### 関連修正チェックリスト

システム修正時に必ず確認する関連箇所：

#### 1. コンポーネント追加時
- [ ] `ComponentRegistry.java` にコンポーネントを登録
- [ ] `SkillLoader.java` の `parseComponent()` にタイプを追加
- [ ] `SkillLoader.java` の `parseComponentSettings()` にキーを除外追加
- [ ] YAMLテンプレート `skill_template.yml` に説明を追加
- [ ] `COMPONENT_SYSTEM_V5.md` ドキュメント更新
- [ ] テストケース追加

#### 2. テンプレートファイル変更時
- [ ] `ResourceSetupManager.java` の `copyResourceFromJar()` パス確認
- [ ] README.txt 内容の更新（`createSkillsReadme()`, `createClassesReadme()`）
- [ ] `docs/YAML_REFERENCE.md` 更新
- [ ] YAMLエディタ（tools/yaml-editor）のスキーマ更新

#### 3. ローダー変更時
- [ ] YAMLキー変更ならテンプレートファイル更新
- [ ] 新しい必須フィールドならバリデーション更新
- [ ] オプションフィールドならデフォルト値確認

#### 4. ドキュメント更新時
- [ ] `docs/COMPONENT_SYSTEM_V5.md` - コンポーネント仕様
- [ ] `docs/YAML_REFERENCE.md` - YAML形式リファレンス
- [ ] MemoryMCP に重要な変更を保存

### 参照ドキュメント

| ドキュメント | 説明 |
|------------|------|
| `COMPONENT_SYSTEM_V5.md` | V5コンポーネントシステム完全リファレンス |
| `YAML_REFERENCE.md` | YAML形式詳細仕様 |
| `SKRIPT_REFLECT.md` | Skript連携手引き |
| `API_DOCUMENTATION.md` | Java APIドキュメント |

### 重要なクラス

| クラス | 説明 |
|-------|------|
| `ResourceSetupManager` | 外部ディレクトリ・テンプレート作成 |
| `ComponentRegistry` | コンポーネントのファクトリレジストリ |
| `SkillLoader` | スキルYAMLローダー |
| `ClassLoader` | クラスYAMLローダー |
| `ConsistencyValidator` | クラス・スキル整合性検証 |
