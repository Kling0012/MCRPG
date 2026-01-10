# データベース設計（最終版）

## データ保存方式: **SQLite + 3層キャッシュ + YAML設定**

| データ種別 | 保存方式 | 理由 |
|-----------|----------|------|
| プレイヤーデータ | SQLite | 50-150人対応、ACID保証 |
| クラス・スキル設定 | YAML | ホットリロード、人間可読 |
| 独自通貨 | SQLite | トランザクション必要 |
| オークション | SQLite | 複雑クエリ、同時実行制御 |
| トレード履歴 | SQLite | 監査用 |
| キャッシュ層 | ConcurrentHashMap | 高速アクセス |

## 最終データベーススキーマ

### プレイヤーデータ（バニラLV/EXP統合）
```sql
CREATE TABLE player_data (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    class_id VARCHAR(50),              -- 現在のクラスID
    class_rank INTEGER DEFAULT 1,       -- クラスランク(1-6)
    first_join DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### ステータス（自動+手動配分）
```sql
CREATE TABLE player_stats (
    uuid VARCHAR(36) PRIMARY KEY,
    strength_base INTEGER DEFAULT 0,    -- 手動配分
    intelligence_base INTEGER DEFAULT 0,
    spirit_base INTEGER DEFAULT 0,
    vitality_base INTEGER DEFAULT 0,
    dexterity_base INTEGER DEFAULT 0,
    strength_auto INTEGER DEFAULT 0,    -- 自動配分
    intelligence_auto INTEGER DEFAULT 0,
    spirit_auto INTEGER DEFAULT 0,
    vitality_auto INTEGER DEFAULT 0,
    dexterity_auto INTEGER DEFAULT 0,
    available_points INTEGER DEFAULT 0, -- 未使用ポイント
    FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
);
```

### スキル習得状況
```sql
CREATE TABLE player_skills (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid VARCHAR(36) NOT NULL,
    skill_id VARCHAR(50) NOT NULL,
    skill_level INTEGER DEFAULT 1,
    unlocked_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(uuid, skill_id),
    FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
);
```

### 独自通貨（ゴールド）
```sql
CREATE TABLE player_currency (
    uuid VARCHAR(36) PRIMARY KEY,
    gold_balance REAL DEFAULT 0.0,
    total_earned REAL DEFAULT 0.0,
    total_spent REAL DEFAULT 0.0,
    FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
);
```

### オークション（入札システム）
```sql
CREATE TABLE auction_listings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    seller_uuid VARCHAR(36) NOT NULL,
    item_data TEXT NOT NULL,            -- シリアライズされたItemStack
    starting_price REAL NOT NULL,       -- 最低価格
    current_bid REAL,                   -- 現在の最高入札
    current_bidder VARCHAR(36),         -- 最高入札者UUID
    duration_seconds INTEGER NOT NULL,  -- 30-180秒
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (seller_uuid) REFERENCES player_data(uuid)
);
```

### 入札履歴
```sql
CREATE TABLE auction_bids (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    auction_id INTEGER NOT NULL,
    bidder_uuid VARCHAR(36) NOT NULL,
    bid_amount REAL NOT NULL,
    bid_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auction_listings(id) ON DELETE CASCADE
);
```

### トレード履歴
```sql
CREATE TABLE trade_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player1_uuid VARCHAR(36) NOT NULL,
    player2_uuid VARCHAR(36) NOT NULL,
    player1_items TEXT,                 -- シリアライズされたアイテム
    player2_items TEXT,
    gold_amount1 REAL DEFAULT 0.0,
    gold_amount2 REAL DEFAULT 0.0,
    trade_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player1_uuid) REFERENCES player_data(uuid),
    FOREIGN KEY (player2_uuid) REFERENCES player_data(uuid)
);
```

### MythicMobsドロップ記録
```sql
CREATE TABLE mythic_drops (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_uuid VARCHAR(36) NOT NULL,
    mob_id VARCHAR(100) NOT NULL,
    item_data TEXT NOT NULL,
    dropped_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_claimed BOOLEAN DEFAULT FALSE,
    expires_at DATETIME,                -- 独占期限（例: 5分）
    FOREIGN KEY (player_uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
);
```

### クラス変更履歴
```sql
CREATE TABLE class_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid VARCHAR(36) NOT NULL,
    old_class_id VARCHAR(50),
    new_class_id VARCHAR(50) NOT NULL,
    old_rank INTEGER,
    new_rank INTEGER NOT NULL,
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    change_reason VARCHAR(100),         -- 'levelup', 'command', 'item', etc.
    FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
);
```

## インデックス（パフォーマンス最適化）
```sql
CREATE INDEX idx_player_stats_uuid ON player_stats(uuid);
CREATE INDEX idx_player_skills_uuid ON player_skills(uuid);
CREATE INDEX idx_auction_active ON auction_listings(is_active, expires_at);
CREATE INDEX idx_auction_seller ON auction_listings(seller_uuid);
CREATE INDEX idx_auction_bids ON auction_bids(auction_id);
CREATE INDEX idx_mythic_drops_player ON mythic_drops(player_uuid, is_claimed);
```

## キャッシュ戦略（3層構成）

- **L1キャッシュ**: オンラインプレイヤー全データ（ConcurrentHashMap）
- **L2キャッシュ**: 高頻度アクセスデータ（Caffeine、最大1000エントリ、5分TTL）
- **L3**: SQLiteデータベース

**キャッシュヒット率目標**: 95%以上（50-150人規模）
