# ============================================
# RPGPlugin API Denizen サンプルスクリプト
# ============================================
#
# このファイルはDenizenからRPGPluginのAPIを使用する方法を示しています。
#
# 前提条件:
# - Denizenプラグインがインストールされていること
# - RPGPluginがインストールされていること
# - プレイヤーが rpgplugin.api 権限を持っていること
#
# ============================================

# ============================================
# 基本例: プレイヤー情報を表示
# ============================================

test_rpg_command:
  type: task
  script:
    - define level <player.tag[rpg.level]>
    - narrate "あなたのLV: <green>%level%"

    - define class <player.tag[rpg.class]>
    - narrate "クラス: <green>%class%"

    - define str <player.tag[rpg.stat[STR]]>
    - narrate "STR: <red>%str%"

    - define gold <player.tag[rpg.gold]>
    - narrate "所持金: <gold>%gold% G"

# ============================================
# 例1: レベル操作
# ============================================

set_my_level_command:
  type: task
  script:
    - define level <c.args.get[1]>
    - execute as_server "rpg api set_level %player% %level%"
    - narrate "<green>レベルを %level% に設定しました！"

# ============================================
# 例2: ステータス表示
# ============================================

show_my_stats_command:
  type: task
  script:
    - narrate "<gold>===== <yellow>あなたのステータス<gold> ====="

    - define str <player.tag[rpg.stat[STR]]>
    - narrate "<red>STR: %str%"

    - define int <player.tag[rpg.stat[INT]]>
    - narrate "<blue>INT: %int%"

    - define spi <player.tag[rpg.stat[SPI]]>
    - narrate "<light_purple>SPI: %spi%"

    - define vit <player.tag[rpg.stat[VIT]]>
    - narrate "<green>VIT: %vit%"

    - define dex <player.tag[rpg.stat[DEX]]>
    - narrate "<aqua>DEX: %dex%"

# ============================================
# 新機能: ステータスポイント配分
# ============================================

allocate_stat_command:
  type: task
  script:
    - define stat <c.args.get[1]>
    - define amount <c.args.get[2]>
    
    # 利用可能ポイント確認
    - execute as_server "rpg api get_available_points %player%"
    
    # ポイント配分実行
    - execute as_server "rpg api add_stat_point %player% %stat% %amount%"
    - narrate "<green>%stat% に %amount% ポイント配分しました"

check_available_points_command:
  type: task
  script:
    - execute as_server "rpg api get_available_points %player%"

# ============================================
# 新機能: スキルポイント管理
# ============================================

check_skill_points_command:
  type: task
  script:
    - execute as_server "rpg api get_skill_points %player%"

give_skill_points_command:
  type: task
  script:
    - define target <server.match_player[<c.args.get[1]>].get>
    - define amount <c.args.get[2]>
    - execute as_server "rpg api add_skill_points %target% %amount%"
    - narrate "<green>%target% に %amount% スキルポイントを付与しました！"

learn_skill_with_points_command:
  type: task
  script:
    - define skillId <c.args.get[1]>
    - execute as_server "rpg api unlock_skill_with_points %player% %skillId%"
    - narrate "<green>スキル %skillId% をポイント消費で習得しました！"

# ============================================
# 例3: クラス操作
# ============================================

my_class_command:
  type: task
  script:
    - define class <player.tag[rpg.class]>
    - narrate "現在のクラス: <green>%class%"

change_class_command:
  type: task
  script:
    - define classId <c.args.get[1]>
    - execute as_server "rpg api set_class %player% %classId%"
    - narrate "<green>クラスを %classId% に変更しました！"

# ============================================
# 新機能: 条件チェック付きクラス変更
# ============================================

try_change_class_command:
  type: task
  script:
    - define classId <c.args.get[1]>
    
    # クラス変更可能かチェック
    - execute as_server "rpg api can_change_class %player% %classId%"
    
    # 条件チェック付きクラス変更を試行
    - execute as_server "rpg api try_change_class %player% %classId%"
    - narrate "<green>クラス変更を試行しました: %classId%"

can_change_class_command:
  type: task
  script:
    - define classId <c.args.get[1]>
    - execute as_server "rpg api can_change_class %player% %classId%"

upgrade_class_command:
  type: task
  script:
    - execute as_server "rpg api upgrade_class %player%"
    - narrate "<green>クラスアップを実行しました！"

# ============================================
# 例4: スキル操作
# ============================================

has_skill_command:
  type: task
  script:
    - define skillId <c.args.get[1]>
    - define hasSkill <player.tag[rpg.has_skill[%skillId%]]>
    - narrate "スキル習得: <green>%hasSkill%"

unlock_skill_command:
  type: task
  script:
    - define skillId <c.args.get[1]>
    - execute as_server "rpg api unlock_skill %player% %skillId%"
    - narrate "<green>スキル %skillId% を習得しました！"

cast_skill_command:
  type: task
  script:
    - define skillId <c.args.get[1]>
    - execute as_server "rpg api cast_skill %player% %skillId%"
    - narrate "<green>スキル %skillId% を使用しました！"

# ============================================
# 例5: 経済操作
# ============================================

my_gold_command:
  type: task
  script:
    - define gold <player.tag[rpg.gold]>
    - narrate "所持金: <gold>%gold% G"

give_gold_command:
  type: task
  script:
    - define target <server.match_player[<c.args.get[1]>].get>
    - define amount <c.args.get[2]>
    - execute as_server "rpg api give_gold %target% %amount%"
    - narrate "<green>%target% に %amount%G を付与しました！"

take_gold_command:
  type: task
  script:
    - define target <server.match_player[<c.args.get[1]>].get>
    - define amount <c.args.get[2]>
    - execute as_server "rpg api take_gold %target% %amount%"
    - narrate "<green>%target% から %amount%G を剥奪しました！"

transfer_gold_command:
  type: task
  script:
    - define to <server.match_player[<c.args.get[1]>].get>
    - define amount <c.args.get[2]>
    - execute as_server "rpg api transfer_gold %player% %to% %amount%"
    - narrate "<green>%to% に %amount%G を転送しました！"

# ============================================
# 例6: ダメージ計算
# ============================================

test_damage_command:
  type: task
  script:
    - define target <server.match_player[<c.args.get[1]>].get>
    - execute as_server "rpg api calculate_damage %player% %target%"

# ============================================
# 例7: クエスト完了報酬
# ============================================

quest_complete_command:
  type: task
  script:
    - define questId <c.args.get[1]>

    - if %questId% == tutorial:
        - execute as_server "rpg api give_gold %player% 100"
        - execute as_server "rpg api unlock_skill %player% power_strike"
        - narrate "<green>チュートリアル完了！100Gとスキルを獲得しました！"
    - else if %questId% == boss_defeat:
        - execute as_server "rpg api give_gold %player% 500"
        - narrate "<green>ボス撃破！500Gを獲得しました！"

# ============================================
# 例8: イベント連携
# ============================================

player_joins_event:
  type: world
  events:
    - player joins
  script:
    - wait 2s
    - narrate "<gold>===== <yellow>RPGシステムへようこそ！<gold> =====" context:%player%
    - narrate "<gray>/testrpg - ステータスを確認" context:%player%
    - narrate "<gray>/myclass - クラスを確認" context:%player%
    - narrate "<gray>/mygold - 所持金を確認" context:%player%

player_dies_event:
  type: world
  events:
    - player dies
  script:
    - define gold <context.damager.tag[rpg.gold]>
    - define penalty <math.mul[%gold%, 0.1]>
    - execute as_server "rpg api take_gold %context.damager% %penalty%"
    - narrate "<red>死亡ペナルティ: %penalty%Gを失いました" context:%context.damager%

# ============================================
# 例9: ショップシステム連携
# ============================================

shop_command:
  type: task
  script:
    - narrate "<gold>===== <yellow>ショップ<gold> ====="
    - narrate "<green>/shop buy hp - HP回復ポーション (50G)"
    - narrate "<green>/shop buy stat - ステータスポイント (100G)"
    - narrate "<green>/shop buy skill - スキル習得 (200G)"

shop_buy_command:
  type: task
  script:
    - define item <c.args.get[1]>

    - if %item% == hp:
        - define hasEnough <player.tag[rpg.has_gold[50]]>
        - if %hasEnough% == true:
            - execute as_server "rpg api take_gold %player% 50"
            - heal %player%
            - narrate "<green>HP回復ポーションを使用しました！( -50G)"
        - else:
            - narrate "<red>ゴールドが足りません！"

    - else if %item% == stat:
        - define hasEnough <player.tag[rpg.has_gold[100]]>
        - if %hasEnough% == true:
            - execute as_server "rpg api take_gold %player% 100"
            - narrate "<green>ステータスポイントを購入しました！( -100G)"
            - narrate "<gray>/rpg stats でステータスを振ってください"
        - else:
            - narrate "<red>ゴールドが足りません！"

    - else if %item% == skill:
        - define hasEnough <player.tag[rpg.has_gold[200]]>
        - if %hasEnough% == true:
            - execute as_server "rpg api take_gold %player% 200"
            - narrate "<green>スキル習得権を購入しました！( -200G)"
            - narrate "<gray>/unlockskill <スキル名> でスキルを習得してください"
        - else:
            - narrate "<red>ゴールドが足りません！"

# ============================================
# 例10: レベルアップボーナス
# ============================================

level_up_bonus:
  type: world
  events:
    - player levels up
  script:
    - define level <context.new_level>
    - narrate "<gold>===== <yellow>レベルアップ！<gold> =====" context:%player%
    - narrate "<green>おめでとうございます！" context:%player%

    - switch %level%:
        - case 10:
            - execute as_server "rpg api give_gold %player% 100"
            - narrate "<gray>レベル10ボーナス: 100Gを獲得！" context:%player%
        - case 20:
            - execute as_server "rpg api give_gold %player% 200"
            - execute as_server "rpg api unlock_skill %player% power_strike"
            - narrate "<gray>レベル20ボーナス: 200Gとスキルを獲得！" context:%player%
        - case 30:
            - execute as_server "rpg api give_gold %player% 500"
            - narrate "<gray>レベル30ボーナス: 500Gを獲得！" context:%player%

# ============================================
# 例11: クラスアップ自動チェック
# ============================================

check_class_upgrade:
  type: task
  script:
    - narrate "クラスアップ可能かチェック中..."

    # Denizenからは直接ブリッジメソッドを呼べないので
    # コマンド経由でチェック
    - execute as_server "rpg api can_upgrade_class %player%"

# ============================================
# 例12: ギルド/パーティー連携（高度な例）
# ============================================

guild_member_bonus:
  type: task
  script:
    # ギルドメンバーにボーナス付与
    - narrate "ギルドメンバーにボーナスを配布中..."

    # ここでギルドメンバーのリストを取得（仮定）
    # foreach <server.list_online_players>:
    #   - define isGuildMember <player.flag[guild_member]>
    #   - if %isGuildMember%:
    #       - execute as_server "rpg api give_gold %value% 50"
    #       - narrate "ギルドボーナス: 50G" context:%value%

# ============================================
# 例13: PvP報酬システム
# ============================================

pvp_kill_reward:
  type: world
  events:
    - player killed by player
  script:
    # キラーに報酬を与える
    - execute as_server "rpg api give_gold %context.killer% 100"
    - narrate "<green>プレイヤーを撃破！100Gを獲得！" context:%context.killer%

    # 被害者にはペナルティ
    - define gold <context.victim.tag[rpg.gold]>
    - define penalty <math.mul[%gold%, 0.05]>
    - execute as_server "rpg api take_gold %context.victim% %penalty%"
    - narrate "<red>死亡ペナルティ: %penalty%Gを失いました" context:%context.victim%
