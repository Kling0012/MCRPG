package com.example.rpgplugin.core.dependency;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.rpgclass.RPGClass;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.stats.Stat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * PlaceholderAPI Expansion for RPGPlugin
 *
 * <p>プレイヤーのRPGステータス情報をPlaceholderAPI経由で提供します。</p>
 *
 * <p>提供プレースホルダー:</p>
 * <ul>
 *   <li>%rpgplugin_level% - プレイヤーレベル</li>
 *   <li>%rpgplugin_class% - 現在のクラス名</li>
 *   <li>%rpgplugin_class_rank% - クラスランク</li>
 *   <li>%rpgplugin_mana% - 現在のマナ</li>
 *   <li>%rpgplugin_max_mana% - 最大マナ</li>
 *   <li>%rpgplugin_mana_percent% - マナパーセンテージ（0-100）</li>
 *   <li>%rpgplugin_stat_<stat>% - ステータス値（strength, intelligence, spirit, vitality, dexterity）</li>
 *   <li>%rpgplugin_available_points% - 使用可能ステータスポイント</li>
 *   <li>%rpgplugin_skill_count% - 習得スキル数</li>
 *   <li>%rpgplugin_has_skill_<skillname>% - スキル所持チェック（true/false）</li>
 *   <li>%rpgplugin_skill_level_<skillname>% - スキルレベル</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class PlaceholderHook extends PlaceholderExpansion {

	private final RPGPlugin plugin;

	/**
	 * コンストラクタ
	 *
	 * @param plugin プラグインインスタンス
	 */
	public PlaceholderHook(RPGPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	@NotNull
	public String getAuthor() {
		return String.join(", ", plugin.getDescription().getAuthors());
	}

	@Override
	@NotNull
	public String getIdentifier() {
		return "rpgplugin";
	}

	@Override
	@NotNull
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
		if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
			return "";
		}

		PlayerManager playerManager = plugin.getPlayerManager();
		RPGPlayer rpgPlayer = playerManager.getRPGPlayer(offlinePlayer.getUniqueId());

		if (rpgPlayer == null) {
			return "";
		}

		// プレースホルダーをパース
		return parsePlaceholder(rpgPlayer, params);
	}

	/**
	 * プレースホルダーをパースして値を返します
	 *
	 * @param rpgPlayer RPGプレイヤー
	 * @param params    パラメータ
	 * @return プレースホルダーの値、無効な場合はnull
	 */
	private String parsePlaceholder(RPGPlayer rpgPlayer, String params) {
		// レベル関連
		if (params.equalsIgnoreCase("level")) {
			return String.valueOf(rpgPlayer.getLevel());
		}

		// クラス関連
		if (params.equalsIgnoreCase("class")) {
			ClassManager classManager = plugin.getClassManager();
			String classId = rpgPlayer.getClassId();
			if (classId == null || classId.isEmpty()) {
				return "なし";
			}
			Optional<RPGClass> rpgClass = classManager.getClass(classId);
			return rpgClass.map(RPGClass::getName).orElse(classId);
		}

		if (params.equalsIgnoreCase("class_id")) {
			String classId = rpgPlayer.getClassId();
			return classId != null && !classId.isEmpty() ? classId : "なし";
		}

		if (params.equalsIgnoreCase("class_rank")) {
			return String.valueOf(rpgPlayer.getClassRank());
		}

		// マナ関連
		if (params.equalsIgnoreCase("mana")) {
			return String.valueOf(rpgPlayer.getCurrentMana());
		}

		if (params.equalsIgnoreCase("max_mana")) {
			return String.valueOf(rpgPlayer.getMaxMana());
		}

		if (params.equalsIgnoreCase("mana_percent")) {
			double ratio = rpgPlayer.getManaRatio();
			return String.format("%.1f", ratio * 100);
		}

		if (params.equalsIgnoreCase("mana_bar")) {
			double ratio = rpgPlayer.getManaRatio();
			int bars = (int) (ratio * 10);
			return "▮".repeat(Math.max(0, bars)) + "▯".repeat(Math.max(0, 10 - bars));
		}

		// ステータスポイント関連
		if (params.equalsIgnoreCase("available_points")) {
			return String.valueOf(rpgPlayer.getAvailablePoints());
		}

		// ステータス値（%rpgplugin_stat_strength 等）
		if (params.startsWith("stat_")) {
			String statName = params.substring(5);
			try {
				Stat stat = Stat.fromShortName(statName.toUpperCase());
				int value = rpgPlayer.getFinalStat(stat);
				return String.valueOf(value);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

		// 基礎ステータス値（%rpgplugin_base_stat_strength 等）
		if (params.startsWith("base_stat_")) {
			String statName = params.substring(10);
			try {
				Stat stat = Stat.fromShortName(statName.toUpperCase());
				int value = rpgPlayer.getBaseStat(stat);
				return String.valueOf(value);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

		// スキル関連
		SkillManager skillManager = plugin.getSkillManager();
		var bukkitPlayer = rpgPlayer.getBukkitPlayer();

		if (params.equalsIgnoreCase("skill_count")) {
			if (bukkitPlayer == null) {
				return "0";
			}
			var playerSkills = skillManager.getPlayerSkillData(bukkitPlayer);
			return playerSkills != null ? String.valueOf(playerSkills.getAcquiredSkills().size()) : "0";
		}

		// スキル所持チェック（%rpgplugin_has_skill_<skillname>%）
		if (params.startsWith("has_skill_")) {
			if (bukkitPlayer == null) {
				return "false";
			}
			String skillName = params.substring(10);
			boolean hasSkill = skillManager.hasSkill(bukkitPlayer, skillName);
			return String.valueOf(hasSkill);
		}

		// スキルレベル（%rpgplugin_skill_level_<skillname>%）
		if (params.startsWith("skill_level_")) {
			if (bukkitPlayer == null) {
				return "0";
			}
			String skillName = params.substring(12);
			int level = skillManager.getSkillLevel(bukkitPlayer, skillName);
			return String.valueOf(level);
		}

		// 認識されないプレースホルダー
		return null;
	}
}
