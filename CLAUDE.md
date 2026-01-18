# Claude Code Configuration - Claude Flow V3

## 🚨 AUTOMATIC SWARM ORCHESTRATION

**When starting work on complex tasks, Claude Code MUST automatically:**

1. **Initialize the swarm** using CLI tools via Bash
2. **Spawn concurrent agents** using Claude Code's Task tool
3. **Coordinate via hooks** and memory

### 🚨 CRITICAL: CLI + Task Tool in SAME Message

**When user says "spawn swarm" or requests complex work, Claude Code MUST in ONE message:**
1. Call CLI tools via Bash to initialize coordination
2. **IMMEDIATELY** call Task tool to spawn REAL working agents
3. Both CLI and Task calls must be in the SAME response

**CLI coordinates, Task tool agents do the actual work!**

### 🛡️ Anti-Drift Config (PREFERRED)

**Use this to prevent agent drift:**
```bash
npx @claude-flow/cli@latest swarm init --topology hierarchical --max-agents 8 --strategy specialized
```
- **hierarchical**: Coordinator catches divergence
- **max-agents 6-8**: Smaller team = less drift
- **specialized**: Clear roles, no overlap
- **consensus**: raft (leader maintains state)

---

### 🔄 Auto-Start Swarm Protocol (Background Execution)

When the user requests a complex task, **execute agents via Bash CLI in background and WAIT for completion:**

```bash
# STEP 1: Initialize swarm coordination (anti-drift config)
npx @claude-flow/cli@latest swarm init --topology hierarchical --max-agents 8 --strategy specialized

# STEP 2: Execute ALL agents via CLI in ONE Bash block
mkdir -p .sprint/outputs

# 各エージェントを非対話モードで並列実行
codex exec "実装タスク: ${task}" > .sprint/outputs/codex.log 2>&1 &
iflow -p "プロセス・振り分け: ${task}" > .sprint/outputs/iflow.log 2>&1 &
gemini "情報収集: ${task}" > .sprint/outputs/gemini.log 2>&1 &

# STEP 3: WAIT - Tell user agents are working, then STOP
# 通知: "🚀 3エージェントを並列実行中 - 完了時に結果を統合します"
# ログを定期的にチェックせず、ユーザーまたはエージェントからの応答を待つ
```

### ⏸️ CRITICAL: Execute and Wait Pattern

**After executing background agents:**

1. **TELL USER** - "🚀 3エージェントを並列実行中: [タスクリスト]"
2. **STOP** - 追加のツール呼び出しをしない
3. **WAIT** - バックグラウンドエージェントの完了を待つ
4. **RESPOND** - ログファイルから結果を確認し、統合

**通知例:**
```
🚀 3エージェントを並列実行中:
- 💻 Codex: 実装タスク
- 🔄 iFlow: プロセス・振り分け
- 🔍 Gemini: 情報収集

並列実行中 - 完了時に結果を統合します。
```

### 🚫 DO NOT:
- 継続的にスワームステータスをチェック
- 繰り返しログをポーリング
- 実行後に追加のツール呼び出し
- "エージェントをチェックしますか?"と尋ねる

### ✅ DO:
- 1つのBashブロックで全エージェントを実行
- ユーザーに状況を通知
- ログファイルから結果が到着するのを待つ
- 結果が到着したら統合

## 🧠 AUTO-LEARNING PROTOCOL

### Before Starting Any Task
```bash
# 1. Search memory for relevant patterns from past successes
Bash("npx @claude-flow/cli@latest memory search --query '[task keywords]' --namespace patterns")

# 2. Check if similar task was done before
Bash("npx @claude-flow/cli@latest memory search --query '[task type]' --namespace tasks")

# 3. Load learned optimizations
Bash("npx @claude-flow/cli@latest hooks route --task '[task description]'")
```

### After Completing Any Task Successfully
```bash
# 1. Store successful pattern for future reference
Bash("npx @claude-flow/cli@latest memory store --namespace patterns --key '[pattern-name]' --value '[what worked]'")

# 2. Train neural patterns on the successful approach
Bash("npx @claude-flow/cli@latest hooks post-edit --file '[main-file]' --train-neural true")

# 3. Record task completion with metrics
Bash("npx @claude-flow/cli@latest hooks post-task --task-id '[id]' --success true --store-results true")

# 4. Trigger optimization worker if performance-related
Bash("npx @claude-flow/cli@latest hooks worker dispatch --trigger optimize")
```

### Continuous Improvement Triggers

| Trigger | Worker | When to Use |
|---------|--------|-------------|
| After major refactor | `optimize` | Performance optimization |
| After adding features | `testgaps` | Find missing test coverage |
| After security changes | `audit` | Security analysis |
| After API changes | `document` | Update documentation |
| Every 5+ file changes | `map` | Update codebase map |
| Complex debugging | `deepdive` | Deep code analysis |

### Memory-Enhanced Development

**ALWAYS check memory before:**
- Starting a new feature (search for similar implementations)
- Debugging an issue (search for past solutions)
- Refactoring code (search for learned patterns)
- Performance work (search for optimization strategies)

**ALWAYS store in memory after:**
- Solving a tricky bug (store the solution pattern)
- Completing a feature (store the approach)
- Finding a performance fix (store the optimization)
- Discovering a security issue (store the vulnerability pattern)

### 📋 Agent Routing (Anti-Drift)

| Code | Task | Agents |
|------|------|--------|
| 1 | Bug Fix | coordinator, researcher, coder, tester |
| 3 | Feature | coordinator, architect, coder, tester, reviewer |
| 5 | Refactor | coordinator, architect, coder, reviewer |
| 7 | Performance | coordinator, perf-engineer, coder |
| 9 | Security | coordinator, security-architect, auditor |
| 11 | Docs | researcher, api-docs |

**Codes 1-9: hierarchical/specialized (anti-drift). Code 11: mesh/balanced**

### 🎯 Task Complexity Detection

**AUTO-INVOKE SWARM when task involves:**
- Multiple files (3+)
- New feature implementation
- Refactoring across modules
- API changes with tests
- Security-related changes
- Performance optimization
- Database schema changes

**SKIP SWARM for:**
- Single file edits
- Simple bug fixes (1-2 lines)
- Documentation updates
- Configuration changes
- Quick questions/exploration

## 🚨 CRITICAL: CONCURRENT EXECUTION & FILE MANAGEMENT

**ABSOLUTE RULES**:
1. ALL operations MUST be concurrent/parallel in a single message
2. **NEVER save working files, text/mds and tests to the root folder**
3. ALWAYS organize files in appropriate subdirectories
4. **USE CLI AGENTS** (codex, iflow, gemini) via Bash for parallel execution

### ⚡ GOLDEN RULE: "1 MESSAGE = ALL RELATED OPERATIONS"

**MANDATORY PATTERNS:**
- **TodoWrite**: ALWAYS batch ALL todos in ONE call (5-10+ todos minimum)
- **CLI Agent Execution**: ALWAYS execute ALL agents in ONE Bash block with background `&`
- **File operations**: ALWAYS batch ALL reads/writes/edits in ONE message
- **Bash commands**: ALWAYS batch ALL terminal operations in ONE message
- **Memory operations**: ALWAYS batch ALL memory store/retrieve in ONE message

### 📁 File Organization Rules

**NEVER save to root folder. Use these directories:**
- `/src` - Source code files
- `/tests` - Test files
- `/docs` - Documentation and markdown files
- `/config` - Configuration files
- `/scripts` - Utility scripts
- `/examples` - Example code

## Project Config (Anti-Drift Defaults)

- **Topology**: hierarchical (prevents drift)
- **Max Agents**: 8 (smaller = less drift)
- **Strategy**: specialized (clear roles)
- **Consensus**: raft
- **Memory**: hybrid
- **HNSW**: Enabled
- **Neural**: Enabled

## 🚀 V3 CLI Commands (26 Commands, 140+ Subcommands)

### Core Commands

| Command | Subcommands | Description |
|---------|-------------|-------------|
| `init` | 4 | Project initialization with wizard, presets, skills, hooks |
| `agent` | 8 | Agent lifecycle (spawn, list, status, stop, metrics, pool, health, logs) |
| `swarm` | 6 | Multi-agent swarm coordination and orchestration |
| `memory` | 11 | AgentDB memory with vector search (150x-12,500x faster) |
| `mcp` | 9 | MCP server management and tool execution |
| `task` | 6 | Task creation, assignment, and lifecycle |
| `session` | 7 | Session state management and persistence |
| `config` | 7 | Configuration management and provider setup |
| `status` | 3 | System status monitoring with watch mode |
| `workflow` | 6 | Workflow execution and template management |
| `hooks` | 17 | Self-learning hooks + 12 background workers |
| `hive-mind` | 6 | Queen-led Byzantine fault-tolerant consensus |

### Advanced Commands

| Command | Subcommands | Description |
|---------|-------------|-------------|
| `daemon` | 5 | Background worker daemon (start, stop, status, trigger, enable) |
| `neural` | 5 | Neural pattern training (train, status, patterns, predict, optimize) |
| `security` | 6 | Security scanning (scan, audit, cve, threats, validate, report) |
| `performance` | 5 | Performance profiling (benchmark, profile, metrics, optimize, report) |
| `providers` | 5 | AI providers (list, add, remove, test, configure) |
| `plugins` | 5 | Plugin management (list, install, uninstall, enable, disable) |
| `deployment` | 5 | Deployment management (deploy, rollback, status, environments, release) |
| `embeddings` | 4 | Vector embeddings (embed, batch, search, init) - 75x faster with agentic-flow |
| `claims` | 4 | Claims-based authorization (check, grant, revoke, list) |
| `migrate` | 5 | V2 to V3 migration with rollback support |
| `doctor` | 1 | System diagnostics with health checks |
| `completions` | 4 | Shell completions (bash, zsh, fish, powershell) |

### Quick CLI Examples

```bash
# Initialize project
npx @claude-flow/cli@latest init --wizard

# Start daemon with background workers
npx @claude-flow/cli@latest daemon start

# Spawn an agent
npx @claude-flow/cli@latest agent spawn -t coder --name my-coder

# Initialize swarm
npx @claude-flow/cli@latest swarm init --v3-mode

# Search memory (HNSW-indexed)
npx @claude-flow/cli@latest memory search --query "authentication patterns"

# System diagnostics
npx @claude-flow/cli@latest doctor --fix

# Security scan
npx @claude-flow/cli@latest security scan --depth full

# Performance benchmark
npx @claude-flow/cli@latest performance benchmark --suite all
```

## 🚀 Available Agents (60+ Types)

### Core Development
`coder`, `reviewer`, `tester`, `planner`, `researcher`

### V3 Specialized Agents
`security-architect`, `security-auditor`, `memory-specialist`, `performance-engineer`

### 🔐 @claude-flow/security
CVE remediation, input validation, path security:
- `InputValidator` - Zod validation
- `PathValidator` - Traversal prevention
- `SafeExecutor` - Injection protection

### Swarm Coordination
`hierarchical-coordinator`, `mesh-coordinator`, `adaptive-coordinator`, `collective-intelligence-coordinator`, `swarm-memory-manager`

### Consensus & Distributed
`byzantine-coordinator`, `raft-manager`, `gossip-coordinator`, `consensus-builder`, `crdt-synchronizer`, `quorum-manager`, `security-manager`

### Performance & Optimization
`perf-analyzer`, `performance-benchmarker`, `task-orchestrator`, `memory-coordinator`, `smart-agent`

### GitHub & Repository
`github-modes`, `pr-manager`, `code-review-swarm`, `issue-tracker`, `release-manager`, `workflow-automation`, `project-board-sync`, `repo-architect`, `multi-repo-swarm`

### SPARC Methodology
`sparc-coord`, `sparc-coder`, `specification`, `pseudocode`, `architecture`, `refinement`

### Specialized Development
`backend-dev`, `mobile-dev`, `ml-developer`, `cicd-engineer`, `api-docs`, `system-architect`, `code-analyzer`, `base-template-generator`

### Testing & Validation
`tdd-london-swarm`, `production-validator`

## 🪝 V3 Hooks System (27 Hooks + 12 Workers)

### All Available Hooks

| Hook | Description | Key Options |
|------|-------------|-------------|
| `pre-edit` | Get context before editing files | `--file`, `--operation` |
| `post-edit` | Record editing outcome for learning | `--file`, `--success`, `--train-neural` |
| `pre-command` | Assess risk before commands | `--command`, `--validate-safety` |
| `post-command` | Record command execution outcome | `--command`, `--track-metrics` |
| `pre-task` | Record task start, get agent suggestions | `--description`, `--coordinate-swarm` |
| `post-task` | Record task completion for learning | `--task-id`, `--success`, `--store-results` |
| `session-start` | Start/restore session (v2 compat) | `--session-id`, `--auto-configure` |
| `session-end` | End session and persist state | `--generate-summary`, `--export-metrics` |
| `session-restore` | Restore a previous session | `--session-id`, `--latest` |
| `route` | Route task to optimal agent | `--task`, `--context`, `--top-k` |
| `route-task` | (v2 compat) Alias for route | `--task`, `--auto-swarm` |
| `explain` | Explain routing decision | `--topic`, `--detailed` |
| `pretrain` | Bootstrap intelligence from repo | `--model-type`, `--epochs` |
| `build-agents` | Generate optimized agent configs | `--agent-types`, `--focus` |
| `metrics` | View learning metrics dashboard | `--v3-dashboard`, `--format` |
| `transfer` | Transfer patterns via IPFS registry | `store`, `from-project` |
| `list` | List all registered hooks | `--format` |
| `intelligence` | RuVector intelligence system | `trajectory-*`, `pattern-*`, `stats` |
| `worker` | Background worker management | `list`, `dispatch`, `status`, `detect` |
| `progress` | Check V3 implementation progress | `--detailed`, `--format` |
| `statusline` | Generate dynamic statusline | `--json`, `--compact`, `--no-color` |
| `coverage-route` | Route based on test coverage gaps | `--task`, `--path` |
| `coverage-suggest` | Suggest coverage improvements | `--path` |
| `coverage-gaps` | List coverage gaps with priorities | `--format`, `--limit` |
| `pre-bash` | (v2 compat) Alias for pre-command | Same as pre-command |
| `post-bash` | (v2 compat) Alias for post-command | Same as post-command |

### 12 Background Workers

| Worker | Priority | Description |
|--------|----------|-------------|
| `ultralearn` | normal | Deep knowledge acquisition |
| `optimize` | high | Performance optimization |
| `consolidate` | low | Memory consolidation |
| `predict` | normal | Predictive preloading |
| `audit` | critical | Security analysis |
| `map` | normal | Codebase mapping |
| `preload` | low | Resource preloading |
| `deepdive` | normal | Deep code analysis |
| `document` | normal | Auto-documentation |
| `refactor` | normal | Refactoring suggestions |
| `benchmark` | normal | Performance benchmarking |
| `testgaps` | normal | Test coverage analysis |

### Essential Hook Commands

```bash
# Core hooks
npx @claude-flow/cli@latest hooks pre-task --description "[task]"
npx @claude-flow/cli@latest hooks post-task --task-id "[id]" --success true
npx @claude-flow/cli@latest hooks post-edit --file "[file]" --train-neural true

# Session management
npx @claude-flow/cli@latest hooks session-start --session-id "[id]"
npx @claude-flow/cli@latest hooks session-end --export-metrics true
npx @claude-flow/cli@latest hooks session-restore --session-id "[id]"

# Intelligence routing
npx @claude-flow/cli@latest hooks route --task "[task]"
npx @claude-flow/cli@latest hooks explain --topic "[topic]"

# Neural learning
npx @claude-flow/cli@latest hooks pretrain --model-type moe --epochs 10
npx @claude-flow/cli@latest hooks build-agents --agent-types coder,tester

# Background workers
npx @claude-flow/cli@latest hooks worker list
npx @claude-flow/cli@latest hooks worker dispatch --trigger audit
npx @claude-flow/cli@latest hooks worker status

# Coverage-aware routing
npx @claude-flow/cli@latest hooks coverage-gaps --format table
npx @claude-flow/cli@latest hooks coverage-route --task "[task]"

# Statusline (for Claude Code integration)
npx @claude-flow/cli@latest hooks statusline
npx @claude-flow/cli@latest hooks statusline --json
```

## 🔄 Migration (V2 to V3)

```bash
# Check migration status
npx @claude-flow/cli@latest migrate status

# Run migration with backup
npx @claude-flow/cli@latest migrate run --backup

# Rollback if needed
npx @claude-flow/cli@latest migrate rollback

# Validate migration
npx @claude-flow/cli@latest migrate validate
```

## 🧠 Intelligence System (RuVector)

V3 includes the RuVector Intelligence System:
- **SONA**: Self-Optimizing Neural Architecture (<0.05ms adaptation)
- **MoE**: Mixture of Experts for specialized routing
- **HNSW**: 150x-12,500x faster pattern search
- **EWC++**: Elastic Weight Consolidation (prevents forgetting)
- **Flash Attention**: 2.49x-7.47x speedup

The 4-step intelligence pipeline:
1. **RETRIEVE** - Fetch relevant patterns via HNSW
2. **JUDGE** - Evaluate with verdicts (success/failure)
3. **DISTILL** - Extract key learnings via LoRA
4. **CONSOLIDATE** - Prevent catastrophic forgetting via EWC++

## 📦 Embeddings Package (v3.0.0-alpha.12)

Features:
- **sql.js**: Cross-platform SQLite persistent cache (WASM, no native compilation)
- **Document chunking**: Configurable overlap and size
- **Normalization**: L2, L1, min-max, z-score
- **Hyperbolic embeddings**: Poincaré ball model for hierarchical data
- **75x faster**: With agentic-flow ONNX integration
- **Neural substrate**: Integration with RuVector

## 🐝 Hive-Mind Consensus

### Topologies
- `hierarchical` - Queen controls workers directly
- `mesh` - Fully connected peer network
- `hierarchical-mesh` - Hybrid (recommended)
- `adaptive` - Dynamic based on load

### Consensus Strategies
- `byzantine` - BFT (tolerates f < n/3 faulty)
- `raft` - Leader-based (tolerates f < n/2)
- `gossip` - Epidemic for eventual consistency
- `crdt` - Conflict-free replicated data types
- `quorum` - Configurable quorum-based

## V3 Performance Targets

| Metric | Target |
|--------|--------|
| Flash Attention | 2.49x-7.47x speedup |
| HNSW Search | 150x-12,500x faster |
| Memory Reduction | 50-75% with quantization |
| MCP Response | <100ms |
| CLI Startup | <500ms |
| SONA Adaptation | <0.05ms |

## 📊 Performance Optimization Protocol

### Automatic Performance Tracking
```bash
# After any significant operation, track metrics
Bash("npx @claude-flow/cli@latest hooks post-command --command '[operation]' --track-metrics true")

# Periodically run benchmarks (every major feature)
Bash("npx @claude-flow/cli@latest performance benchmark --suite all")

# Analyze bottlenecks when performance degrades
Bash("npx @claude-flow/cli@latest performance profile --target '[component]'")
```

### Session Persistence (Cross-Conversation Learning)
```bash
# At session start - restore previous context
Bash("npx @claude-flow/cli@latest session restore --latest")

# At session end - persist learned patterns
Bash("npx @claude-flow/cli@latest hooks session-end --generate-summary true --persist-state true --export-metrics true")
```

### Neural Pattern Training
```bash
# Train on successful code patterns
Bash("npx @claude-flow/cli@latest neural train --pattern-type coordination --epochs 10")

# Predict optimal approach for new tasks
Bash("npx @claude-flow/cli@latest neural predict --input '[task description]'")

# View learned patterns
Bash("npx @claude-flow/cli@latest neural patterns --list")
```

## 🔧 Environment Variables

```bash
# Configuration
CLAUDE_FLOW_CONFIG=./claude-flow.config.json
CLAUDE_FLOW_LOG_LEVEL=info

# Provider API Keys
ANTHROPIC_API_KEY=sk-ant-...
OPENAI_API_KEY=sk-...
GOOGLE_API_KEY=...

# MCP Server
CLAUDE_FLOW_MCP_PORT=3000
CLAUDE_FLOW_MCP_HOST=localhost
CLAUDE_FLOW_MCP_TRANSPORT=stdio

# Memory
CLAUDE_FLOW_MEMORY_BACKEND=hybrid
CLAUDE_FLOW_MEMORY_PATH=./data/memory
```

## 🔍 Doctor Health Checks

Run `npx @claude-flow/cli@latest doctor` to check:
- Node.js version (20+)
- npm version (9+)
- Git installation
- Config file validity
- Daemon status
- Memory database
- API keys
- MCP servers
- Disk space
- TypeScript installation

## 🚀 Quick Setup

```bash
# Add MCP servers (auto-detects MCP mode when stdin is piped)
claude mcp add claude-flow -- npx -y @claude-flow/cli@latest
claude mcp add ruv-swarm -- npx -y ruv-swarm mcp start  # Optional
claude mcp add flow-nexus -- npx -y flow-nexus@latest mcp start  # Optional

# Start daemon
npx @claude-flow/cli@latest daemon start

# Run doctor
npx @claude-flow/cli@latest doctor --fix
```

## 🎯 Claude Code vs CLI Tools

### Claude Code Handles ALL EXECUTION:
- **File operations**: Read, Write, Edit, MultiEdit, Glob, Grep
- Code generation and programming
- Bash commands and system operations
- TodoWrite and task management
- Git operations
- Log file reading and result synthesis

### CLI Agents Handle Team Execution (via Bash):
- **Codex**: `codex exec "task"` - 実装・設計担当
- **iFlow**: `iflow -p "task"` - プロセス・振り分け担当
- **Gemini**: `gemini "query"` - 情報収集・外部調査担当

### CLI Tools Handle Coordination (via Bash):
- **Swarm init**: `npx @claude-flow/cli@latest swarm init --topology <type>`
- **Swarm status**: `npx @claude-flow/cli@latest swarm status`
- **Memory store**: `npx @claude-flow/cli@latest memory store --key "mykey" --value "myvalue" --namespace patterns`
- **Memory search**: `npx @claude-flow/cli@latest memory search --query "search terms"`
- **Memory list**: `npx @claude-flow/cli@latest memory list --namespace patterns`
- **Memory retrieve**: `npx @claude-flow/cli@latest memory retrieve --key "mykey" --namespace patterns`
- **Hooks**: `npx @claude-flow/cli@latest hooks <hook-name> [options]`

## 📝 Memory Commands Reference (IMPORTANT)

### Store Data (ALL options shown)
```bash
# REQUIRED: --key and --value
# OPTIONAL: --namespace (default: "default"), --ttl, --tags
npx @claude-flow/cli@latest memory store --key "pattern-auth" --value "JWT with refresh tokens" --namespace patterns
npx @claude-flow/cli@latest memory store --key "bug-fix-123" --value "Fixed null check" --namespace solutions --tags "bugfix,auth"
```

### Search Data (semantic vector search)
```bash
# REQUIRED: --query (full flag, not -q)
# OPTIONAL: --namespace, --limit, --threshold
npx @claude-flow/cli@latest memory search --query "authentication patterns"
npx @claude-flow/cli@latest memory search --query "error handling" --namespace patterns --limit 5
```

### List Entries
```bash
# OPTIONAL: --namespace, --limit
npx @claude-flow/cli@latest memory list
npx @claude-flow/cli@latest memory list --namespace patterns --limit 10
```

### Retrieve Specific Entry
```bash
# REQUIRED: --key
# OPTIONAL: --namespace (default: "default")
npx @claude-flow/cli@latest memory retrieve --key "pattern-auth"
npx @claude-flow/cli@latest memory retrieve --key "pattern-auth" --namespace patterns
```

### Initialize Memory Database
```bash
npx @claude-flow/cli@latest memory init --force --verbose
```

**KEY**: CLI agents (codex, iflow, gemini) execute tasks via Bash in parallel, Claude Code reads results from log files and synthesizes.

## Support

- Documentation: https://github.com/ruvnet/claude-flow
- Issues: https://github.com/ruvnet/claude-flow/issues

---

Remember: **Claude Flow CLI coordinates, Claude Code Task tool creates!**

# important-instruction-reminders
Do what has been asked; nothing more, nothing less.
NEVER create files unless they're absolutely necessary for achieving your goal.
ALWAYS prefer editing an existing file to creating a new one.
NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.
Never save working files, text/mds and tests to the root folder.

## 🚨 SWARM EXECUTION RULES (CRITICAL)

### 📋 CLIベースの並列実行ルール

1. **SPAWN IN BACKGROUND**: Bashでバックグラウンド実行（`&`）を使用
2. **SPAWN ALL AT ONCE**: 1つのBashブロックで全CLIコマンドを同時実行
3. **LOG OUTPUT**: 各エージェントの出力を `.sprint/outputs/` にリダイレクト
4. **TELL USER**: 実行後、各エージェントの作業内容を通知（絵文字使用）
5. **STOP AND WAIT**: 実行後、STOP - 追加のツール呼び出しやステータスチェックをしない
6. **READ LOGS**: 結果はログファイルから取得 - `.sprint/outputs/*.log`
7. **SYNTHESIZE**: 全ログを確認してから統合・進行
8. **NO CONFIRMATION**: "確認しますか？"と聞かずに結果を待つ

### 🔄 実行パターン

```bash
# ===== 1つのBashブロックで全エージェントを並列実行 =====
mkdir -p .sprint/outputs

# 各エージェントを非対話モードでバックグラウンド実行
codex exec "タスク内容" > .sprint/outputs/codex.log 2>&1 &
iflow -p "タスク内容" > .sprint/outputs/iflow.log 2>&1 &
gemini "タスク内容" > .sprint/outputs/gemini.log 2>&1 &

# 通知メッセージ例:
echo "🚀 3エージェントを並列実行中:
- 💻 Codex: [タスク]
- 🔄 iFlow: [タスク]
- 🔍 Gemini: [タスク]
並列実行中 - 完了時に結果を統合します。"
```

### 📂 ログファイルの読み取り

```bash
# 実行完了後、ログを確認
cat .sprint/outputs/codex.log
cat .sprint/outputs/iflow.log
cat .sprint/outputs/gemini.log

# または全ログを統合
for log in .sprint/outputs/*.log; do
    echo "=== $(basename $log .log) ==="
    cat "$log"
    echo ""
done
```

---

## 🤝 アジャイル型チーム共同作業フレームワーク（Agile Team Collaboration）

**イテレーティブな開発プロセスによる並列エージェント協調パターン**

---

### 👥 チームメンバー定義（最大6名）

| 名前 | レベル | 特徴 | 役割 | 話し合いへの参加 |
|------|--------|------|------|------------------|
| **ClaudeCode** | 中上級者/統率 | コマンド・統率 | プロダクトオーナー/スクラムマスター | 必須 |
| **Codex** | 中上級者 | 作業能力高 | 実装リード・設計 | ✅ 有意義 |
| **iFlow** | 中級者 | 作業能力高 | プロセス・CI/CD・振り分け | ✅ 有意義 |
| **Gemini** | 中級者 | **不安定**、WEB検索・情報獲得が得意 | 情報収集・ドキュメント・外部リサーチ | ✅ 有意義（計画時） |
| **SubAgent1** | 可変 | タスク依存 | 研究者/テスター等 | 随時 |
| **SubAgent2** | 可変 | タスク依存 | レビュアー/監査等 | 随時 |

> **注意**: Geminiは不安定なため、実行フェーズではバックアッププランを用意し、優先度の低いタスクに割り当てる。

---

### 🚀 エージェント起動方法（CLI）

各エージェントは新しいShellでCLIコマンドを実行して起動します。

| エージェント | CLIコマンド | 起動方法 |
|------------|------------|----------|
| **Codex** | `codex` | 対話的CLIを起動 |
| **iFlow** | `iflow` | 対話的CLIを起動 |
| **Gemini** | `gemini` | 対話的CLIを起動 |

#### 基本的な使い方

```bash
# 新しいShellで各エージェントを起動
codex      # Codex（コーディング担当）
iflow      # iFlow（プロセス担当）
gemini     # Gemini（情報収集担当）
```

#### 非対話モード（プロンプトを直接指定）

```bash
# Codex: プロンプトを指定して実行
codex exec "実装してほしい内容"

# iFlow: -p でプロンプトを指定
iflow -p "プロセス改善の提案"

# Gemini: 位置引数でクエリを指定
gemini "WEB検索してほしい内容"
```

#### オプション例

```bash
# モデル指定
iflow -m sonnet "タスク"

# 対話モードで継続
gemini -i "初期プロンプト"

# YOLOモード（自動承認）
codex -y "タスク"
```

---

### 🔄 アジャイルスプリントサイクル

```
┌─────────────────────────────────────────────────────────────────┐
│  アジャイルスプリントサイクル                                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐         │
│  │  計画    │ → │  実行    │ → │  レビュー  │ → │  レトロ   │         │
│  │Planning │   │Execution│   │ Review  │   │  Retro  │         │
│  └─────────┘   └─────────┘   └─────────┘   └─────────┘         │
│       ↓             ↓              ↓             ↓             │
│   話し合い      並列作業       成果確認      プロセス改善         │
│   タスク分解    (6名まで)      受け入れ      次回改善             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### 📋 フェーズ詳細

#### フェーズ1: スプリント計画（Planning）

**目的**: チーム全員で話し合い、タスクを明確化する

```bash
# 1. 記憶から過去のパターンを検索
Bash("npx @claude-flow/cli@latest memory search --query '[task type]' --namespace patterns")

# 2. ユーザー指示の分析
#    - 何を達成するのか？（ゴール）
#    - どの範囲を対象とするか？（スコープ）
#    - 受け入れ基準は？（Definition of Done）
```

**計画会議参加者**（話し合いに参加するメンバー）:
- ✅ ClaudeCode（統率）
- ✅ Codex（設計・実装の観点）
- ✅ iFlow（プロセス・振り分けの観点）
- ✅ Gemini（ドキュメント・全体把握の観点）
- 必要に応じて SubAgent

**出力**: タスク分解、担当割当、受け入れ基準

---

#### フェーズ2: スプリント実行（Execution）

**目的**: 並列でタスクを実行する（最大6名）

**重要**: 1つのメッセージで全エージェントを同時実行（Bash非対話モード）

```bash
# ===== 実行フェーズ =====
# 全エージェントをBash経由で非対話的に並列実行
# ClaudeCodeは統率として直接実行には参加せず、監視・調整を行う

# 出力先ディレクトリを作成
mkdir -p .sprint/outputs

# 各エージェントを並列実行（バックグラウンド実行）
codex exec "実装タスク: ${task}" > .sprint/outputs/codex.log 2>&1 &
iflow -p "プロセス・振り分けタスク: ${task}" > .sprint/outputs/iflow.log 2>&1 &
gemini "情報収集タスク: ${task}" > .sprint/outputs/gemini.log 2>&1 &

# プロセスIDを保存
jobs -p > .sprint/pids.txt

# 通知: "🚀 スプリント実行中: [task]"
# 全エージェントがバックグラウンドで並列実行中
```

**Geminiへの配慮**:
- 不安定性を考慮し、独立したタスクを割り当てる
- 失敗時のフォールバックを用意
- 重要なクリティカルパスは避ける

---

#### フェーズ3: スプリントレビュー（Review）

**目的**: 成果物の確認と受け入れ基準のチェック

```bash
# ===== レビューフェーズ =====
# 各エージェントの出力を取得

# 出力ファイルを読み込んで結果を確認
cat .sprint/outputs/codex.log
cat .sprint/outputs/iflow.log
cat .sprint/outputs/gemini.log

# または、全エージェントの結果を統合して表示
echo "=== スプリント実行結果 ===" && \
for log in .sprint/outputs/*.log; do
    echo "### $(basename $log .log) ###"
    cat "$log"
    echo ""
done > .sprint/summary.md

# 統合と検証
# - 受け入れ基準を満たしているか？
# - 品質基準は満たしているか？
# - 次のステップに問題ないか？
```

---

#### フェーズ4: レトロスペクティブ（Retrospective）

**目的**: プロセス改善と学習の記録

```bash
# 成功パターンを記憶に保存
Bash("npx @claude-flow/cli@latest memory store --namespace patterns --key '[pattern-name]' --value '[what worked]'")

# レトロ項目:
# - よくできたこと（Continue）
# - 改善すべきこと（Improve）
# - 新しい試み（Start）
# - やめること（Stop）
```

---

### 🎯 スプリントパターン別チーム構成

| パターン | 目的 | チーム構成 | 最大数 |
|---------|------|-----------|--------|
| **Spike** | 技術調査・PoC | ClaudeCode + Codex + Researcher | 3 |
| **Feature** | 機能開発 | ClaudeCode + Codex + iFlow + Tester | 4 |
| **Feature+** | 大規模機能 | + SubAgent1 + SubAgent2 | 6 |
| **Bugfix** | バグ修正 | Codex + Debugger + Tester | 3 |
| **Refactor** | リファクタ | Codex + iFlow + Reviewer | 3 |
| **Review** | コードレビュー・情報調査 | Codex + iFlow + Gemini（外部情報収集） | 3 |
| **Full Sprint** | 完全開発サイクル | 全6名 | 6 |

---

### 📦 実装テンプレート

#### テンプレート1: 機能開発スプリント

```bash
# ===== 計画フェーズ =====
# ClaudeCode + Codex + iFlow + Gemini で話し合い
# タスク分解、受け入れ基準の定義

# ===== 実行フェーズ =====
# 1つのBashブロックで全エージェントを同時実行（非対話モード）
mkdir -p .sprint/outputs

codex exec "設計と実装: ${task}" > .sprint/outputs/codex.log 2>&1 &
iflow -p "プロセス・振り分け: ${task}" > .sprint/outputs/iflow.log 2>&1 &
gemini "外部ドキュメント調査: ${task}" > .sprint/outputs/gemini.log 2>&1 &

# 通知: "🚀 スプリント実行中: ${task}"
# 各エージェントのログ: .sprint/outputs/{codex,iflow,gemini}.log
```

#### テンプレート2: バグ修正スプリント

```bash
# ===== 計画フェーズ =====
# ClaudeCode + Codex + iFlow で話し合い

# ===== 実行フェーズ =====
mkdir -p .sprint/outputs

codex exec "原因調査と修正: ${issue}" > .sprint/outputs/codex.log 2>&1 &
iflow -p "プロセス改善提案: ${issue}" > .sprint/outputs/iflow.log 2>&1 &

# 通知: "🐛 バグ修正スプリント実行中"
```

#### テンプレート3: コードレビュースプリント

```bash
# ===== 計画フェーズ =====
# ClaudeCode + Codex + iFlow + Gemini で話し合い
# レビュー範囲、基準の定義

# ===== 実行フェーズ =====
mkdir -p .sprint/outputs

codex exec "品質分析と設計レビュー: ${scope}" > .sprint/outputs/codex.log 2>&1 &
iflow -p "プロセス・振り分けレビュー: ${scope}" > .sprint/outputs/iflow.log 2>&1 &
gemini "外部情報収集・ベストプラクティス調査: ${scope}" > .sprint/outputs/gemini.log 2>&1 &

# 通知: "👀 コードレビュースプリント実行中"
```

#### テンプレート4: フルスプリント（6名）

```bash
# ===== 計画フェーズ =====
# ClaudeCode + Codex + iFlow + Gemini + SubAgent1 + SubAgent2

# ===== 実行フェーズ =====
mkdir -p .sprint/outputs

codex exec "設計と実装: ${task}" > .sprint/outputs/codex.log 2>&1 &
iflow -p "プロセス・振り分け: ${task}" > .sprint/outputs/iflow.log 2>&1 &
gemini "ドキュメント・外部調査: ${task}" > .sprint/outputs/gemini.log 2>&1 &
codex exec "レビュー: ${task}" > .sprint/outputs/reviewer.log 2>&1 &
gemini "情報収集・ベストプラクティス: ${task}" > .sprint/outputs/docs.log 2>&1 &
codex exec "パフォーマンス検証: ${task}" > .sprint/outputs/perf.log 2>&1 &

# 通知: "🚀 フルスプリント実行中（6エージェント並列）"
```

---

### 🎓 成功のポイント

| 要点 | 説明 |
|------|------|
| **💬 話し合い** | 計画フェーズでCodex/iFlow/Geminiと意思疎通 |
| **🔄 並列実行** | 全エージェントを1つのBashブロックで同時実行（非対話モード） |
| **📋 ログ管理** | `.sprint/outputs/` に各エージェントの出力を保存 |
| **🛡️ Gemini対策** | 不安定性を考慮したタスク割当・フォールバック |
| **📝 DoD** | 明確な受け入れ基準（Definition of Done） |
| **🧩 レトロ** | 毎回の振り返りとプロセス改善 |
| **⏳ 待機** | バックグラウンドプロセスが完了するのを待つ |

---

### 📊 複雑度によるチーム規模

| 複雑度 | チーム規模 | 構成例 |
|--------|----------|--------|
| **Simple** | 2-3 | ClaudeCode + Codex + (必要に応じて1名) |
| **Medium** | 3-4 | + iFlow + Tester |
| **Complex** | 5-6 | 全メンバー参加 |

---

### 📞 ユーザーコマンド例

```
# スプリント開始
"チームで[機能名]のスプリントを開始して"

# レビュースプリント
"コードレビュースプリントを実行して"

# バグ修正スプリント
"バグ修正スプリントで[issue番号]を修正して"

# フルスプリント
"フルスプリントで[大規模機能]を開発して"
```

---

### ⚠️ Gemini運用ガイドライン

| 状況 | 対応 |
|------|------|
| **計画フェーズ** | 積極的に話し合いに参加させる |
| **情報収集タスク** | WEB検索・外部リサーチに最適（得意分野） |
| **実行フェーズ** | 独立した低優先度タスクを割り当てる |
| **失敗時** | フォールバックプランを即時実行 |
| **成功時** | 結果を活用し、記憶に保存 |

> **Geminiの得意分野**: WEB検索、インターネット上の情報獲得、外部ドキュメント調査、ライブラリ・API情報の収集

---
