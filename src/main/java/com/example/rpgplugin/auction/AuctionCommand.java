package com.example.rpgplugin.auction;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * オークションコマンドハンドラー
 * /rpg auction コマンドの処理を担当
 */
public class AuctionCommand implements CommandExecutor, TabCompleter {

    private final AuctionManager auctionManager;

    public AuctionCommand(AuctionManager auctionManager) {
        this.auctionManager = auctionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // プレイヤーのみ
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用できます");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                handleListCommand(player);
                break;

            case "bid":
                handleBidCommand(player, args);
                break;

            case "create":
                handleCreateCommand(player, args);
                break;

            case "cancel":
                handleCancelCommand(player, args);
                break;

            case "info":
                handleInfoCommand(player, args);
                break;

            default:
                player.sendMessage(ChatColor.RED + "不明なサブコマンドです");
                showHelp(player);
                break;
        }

        return true;
    }

    /**
     * ヘルプを表示
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=== オークションヘルプ ===");
        player.sendMessage(ChatColor.GOLD + "--- コマンド一覧 ---");
        player.sendMessage(ChatColor.GRAY + "/rpg auction list - アクティブなオークション一覧を表示");
        player.sendMessage(ChatColor.GRAY + "/rpg auction info <ID> - オークション詳細を表示");
        player.sendMessage(ChatColor.GRAY + "/rpg auction bid <ID> <金額> - 入札する");
        player.sendMessage(ChatColor.GRAY + "/rpg auction create <価格> <秒数> - 手持ちアイテムを出品");
        player.sendMessage(ChatColor.GRAY + "/rpg auction cancel <ID> - 自分の出品をキャンセル");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "--- 入札ルール ---");
        player.sendMessage(ChatColor.GRAY + "・開始価格以上である必要があります");
        player.sendMessage(ChatColor.GRAY + "・現在の入札額の10%以上上乗せする必要があります");
        player.sendMessage(ChatColor.GRAY + "・入札があると有効期限が+5秒延長されます");
        player.sendMessage(ChatColor.GRAY + "・出品期間は30-180秒です");
    }

    /**
     * オークション一覧を表示
     */
    private void handleListCommand(Player player) {
        List<Auction> auctions = auctionManager.getActiveAuctions();

        if (auctions.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "=== アクティブなオークション ===");
            player.sendMessage(ChatColor.GRAY + "現在、アクティブなオークションはありません");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "=== アクティブなオークション (" + auctions.size() + ") ===");

        for (Auction auction : auctions) {
            long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), auction.getExpiresAt());

            String itemInfo = auction.getItem().getType().name();
            String bidderInfo = auction.getCurrentBidder() != null ?
                    " (入札あり: " + ChatColor.GOLD + String.format("%.2f", auction.getCurrentBid()) + ChatColor.RESET + ")" :
                    " (" + ChatColor.WHITE + String.format("%.2f", auction.getStartingPrice()) + ChatColor.RESET + ")";

            player.sendMessage(String.format("%s[%d] %s%s %s- %s%s %s残り%ds",
                    ChatColor.GOLD,
                    auction.getId(),
                    ChatColor.WHITE,
                    itemInfo,
                    ChatColor.GRAY,
                    bidderInfo,
                    ChatColor.GREEN,
                    remainingSeconds
            ));
        }
    }

    /**
     * 入札コマンド
     */
    private void handleBidCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "使用法: /rpg auction bid <オークションID> <金額>");
            return;
        }

        // オークションID解析
        int auctionId;
        try {
            auctionId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "無効なオークションIDです");
            return;
        }

        // 金額解析
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "無効な金額です");
            return;
        }

        // 入札処理
        BiddingSystem.BidResult result = auctionManager.placeBid(player, auctionId, amount);

        if (result.isSuccess()) {
            player.sendMessage(ChatColor.GREEN + "✓ " + result.getMessage());
        } else {
            player.sendMessage(ChatColor.RED + "✗ " + result.getMessage());

            if (result.getRequiredAmount() > 0) {
                player.sendMessage(ChatColor.YELLOW + "必要金額: " + String.format("%.2f", result.getRequiredAmount()));
            }
        }
    }

    /**
     * 出品コマンド
     */
    private void handleCreateCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "使用法: /rpg auction create <開始価格> <秒数(30-180)>");
            player.sendMessage(ChatColor.GRAY + "手に持っているアイテムを出品します");
            return;
        }

        // 価格解析
        double startingPrice;
        try {
            startingPrice = Double.parseDouble(args[1]);
            if (startingPrice <= 0) {
                player.sendMessage(ChatColor.RED + "価格は0より大きい必要があります");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "無効な価格です");
            return;
        }

        // 秒数解析
        int durationSeconds;
        try {
            durationSeconds = Integer.parseInt(args[2]);
            if (durationSeconds < 30 || durationSeconds > 180) {
                player.sendMessage(ChatColor.RED + "秒数は30-180の範囲である必要があります");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "無効な秒数です");
            return;
        }

        // 手に持っているアイテムを取得
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "手にアイテムを持っていません");
            return;
        }

        // アイテムを1つ消費
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        // 出品データ作成
        AuctionListing listing = new AuctionListing(
                player.getUniqueId(),
                player.getName(),
                item,
                startingPrice,
                durationSeconds
        );

        // オークション作成
        Auction auction = auctionManager.createAuction(listing);

        if (auction != null) {
            player.sendMessage(ChatColor.GREEN + "✓ オークションを作成しました！");
            player.sendMessage(ChatColor.GRAY + "ID: " + auction.getId());
            player.sendMessage(ChatColor.GRAY + "アイテム: " + item.getType().name());
            player.sendMessage(ChatColor.GRAY + "開始価格: " + String.format("%.2f", startingPrice));
            player.sendMessage(ChatColor.GRAY + "有効期限: " + durationSeconds + "秒");
        } else {
            player.sendMessage(ChatColor.RED + "オークション作成に失敗しました");
            // アイテムを返還
            player.getInventory().addItem(item);
        }
    }

    /**
     * キャンセルコマンド
     */
    private void handleCancelCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "使用法: /rpg auction cancel <オークションID>");
            return;
        }

        int auctionId;
        try {
            auctionId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "無効なオークションIDです");
            return;
        }

        boolean success = auctionManager.cancelAuction(auctionId, player.getUniqueId());

        if (success) {
            player.sendMessage(ChatColor.GREEN + "✓ オークションをキャンセルしました");
        } else {
            player.sendMessage(ChatColor.RED + "キャンセルできませんでした");
            player.sendMessage(ChatColor.GRAY + "自分の出品したオークションのみキャンセルできます");
        }
    }

    /**
     * 詳細表示コマンド
     */
    private void handleInfoCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "使用法: /rpg auction info <オークションID>");
            return;
        }

        int auctionId;
        try {
            auctionId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "無効なオークションIDです");
            return;
        }

        Auction auction = auctionManager.getAuction(auctionId);
        if (auction == null) {
            player.sendMessage(ChatColor.RED + "オークションが見つかりません");
            return;
        }

        long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), auction.getExpiresAt());

        player.sendMessage(ChatColor.YELLOW + "=== オークション詳細 #" + auction.getId() + " ===");
        player.sendMessage(ChatColor.GOLD + "アイテム: " + ChatColor.WHITE + auction.getItem().getType().name());
        player.sendMessage(ChatColor.GOLD + "出品者: " + ChatColor.WHITE + auction.getSellerName());
        player.sendMessage(ChatColor.GOLD + "開始価格: " + ChatColor.WHITE + String.format("%.2f", auction.getStartingPrice()));

        if (auction.getCurrentBidder() != null) {
            player.sendMessage(ChatColor.GOLD + "現在の入札額: " + ChatColor.GREEN + String.format("%.2f", auction.getCurrentBid()));
            player.sendMessage(ChatColor.GOLD + "最終入札者: " + ChatColor.WHITE + auction.getCurrentBidder().toString());
        } else {
            player.sendMessage(ChatColor.GRAY + "入札なし");
        }

        player.sendMessage(ChatColor.GOLD + "有効期限: " + ChatColor.WHITE + remainingSeconds + "秒");
        player.sendMessage(ChatColor.GRAY + "最低次回入札額: " + String.format("%.2f", auction.getMinimumNextBid()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // /rpg auction なので、args[0]がauction、args[1]がサブコマンド
        if (args.length == 1) {
            // サブコマンド補完
            completions.addAll(Arrays.asList("list", "bid", "create", "cancel", "info"));
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "bid":
                case "cancel":
                case "info":
                    if (args.length == 2) {
                        // オークションID補完
                        completions.addAll(auctionManager.getActiveAuctions().stream()
                                .map(auction -> String.valueOf(auction.getId()))
                                .collect(Collectors.toList()));
                    }
                    break;

                case "create":
                    if (args.length == 2) {
                        completions.add("<価格>");
                    } else if (args.length == 3) {
                        completions.addAll(Arrays.asList("30", "60", "90", "120", "180"));
                    }
                    break;

                default:
                    break;
            }
        }

        // 入力された文字列でフィルタリング
        String lastArg = args[args.length - 1];
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(lastArg.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }
}
