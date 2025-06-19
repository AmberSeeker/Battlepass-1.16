package battlepass.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBallRegistry;

import battlepass.config.BattlePassReward;
import battlepass.config.BattlePassText;
import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import battlepass.utils.Utils;
import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeBPMenu {

    private static final LinkedPageButton previous = LinkedPageButton.builder()
            .title("Previous")
            .display(new ItemStack(Items.ARROW))
            .linkType(LinkType.Previous)
            .build();

    private static final LinkedPageButton next = LinkedPageButton.builder()
            .title("Next")
            .display(new ItemStack(Items.ARROW))
            .linkType(LinkType.Next)
            .build();

    public static void openMenu(Player player) {
        ServerPlayerEntity sp = getForgePlayer(player);
        LinkedPage lp = createBattlepassMenu(player);
        if (sp != null && lp != null) {
            UIManager.openUIForcefully(sp, lp);
            return;
        }
        player.sendMessage("Failed to open BattlepassMenu for you. Contact an administrator.");
    }

    private static LinkedPage createBattlepassMenu(Player player) {
        BattlepassPlayer battlepassPlayer = Battlepass.getInstance().playerDataMap.get(player.getUniqueId());
        if (battlepassPlayer == null)
            return null;

        BattlePassText battlePassText = Battlepass.getInstance().battlePassText;

        ChestTemplate bpTemplate = ChestTemplate.builder(3)
                .fill(fillerButton())
                .rectangle(0, 0, 2, 9, new PlaceholderButton())
                .set(22, getPlayerInfo(player)) // Player info button
                .set(21, previous).set(23, next) // Next/Previous Buttons
                .build();

        LinkedPage bpPage = PaginationHelper.createPagesFromPlaceholders(bpTemplate, getBPTiers(player),
                LinkedPage.builder().title(Utils.toText(battlePassText.battlePassTitle)));

        return bpPage;
    }

    private static Button getPlayerInfo(Player player) {
        BattlepassPlayer bpp = Battlepass.getInstance().playerDataMap.get(player.getUniqueId());
        if (bpp == null) {
            return GooeyButton.of(new ItemStack(Items.SKELETON_SKULL));
        }

        ServerPlayerEntity forgePlayer = getForgePlayer(player);
        GameProfile profile = forgePlayer.getGameProfile();
        ItemStack is = new ItemStack(Items.PLAYER_HEAD);
        CompoundNBT nbt = is.getOrCreateTag();
        nbt.put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), profile));
        is.setTag(nbt);
        double playerLvl = Utils.getLvl(bpp.getXp());
        GooeyButton playerInfo = GooeyButton.builder()
                .title("§e§l" + player.getName() + "'s Battlepass Info")
                .display(is)
                .lore(Arrays.asList(
                        "§bBattlepass Level: " + Utils.getDisplayLvl((int) playerLvl),
                        "§bBattlepass XP: " + Utils.getFormattedLong(bpp.getXp()) + "/"
                                + Utils.getFormattedLong(Utils.xpForLvl(playerLvl + 1.0D))))
                .build();

        return playerInfo;
    }

    private static List<Button> getBPTiers(Player player) {
        BattlepassPlayer bpp = Battlepass.getInstance().playerDataMap.get(player.getUniqueId());
        if (bpp == null) {
            throw new IllegalStateException("BattlepassPlayer data not found for player: " + player.getName());
        }
        double currentLvl = Utils.getLvl(bpp.getXp());
        List<Button> basicButtons = new ArrayList<>();
        for (BattlePassReward reward : Battlepass.getInstance().rewardMap.values()) {
            basicButtons.add(getRewardButton(player, reward, currentLvl, false));
        }

        List<Button> premiumButtons = new ArrayList<>();
        for (BattlePassReward reward : Battlepass.getInstance().premiumRewardMap.values()) {
            premiumButtons.add(getRewardButton(player, reward, currentLvl, true));
        }

        return combineButtons(basicButtons, premiumButtons);
    }

    // private static List<Button> combineButtons(List<Button> basicButtons,
    // List<Button> premiumButtons) {
    // List<Button> combined = new ArrayList<>();
    // int basicIndex = 0;
    // int premiumIndex = 0;

    // while (basicIndex < basicButtons.size() || premiumIndex <
    // premiumButtons.size()) {
    // // Add up to 9 basic buttons
    // for (int i = 0; i < 9 && basicIndex < basicButtons.size(); i++) {
    // combined.add(basicButtons.get(basicIndex++));
    // }

    // // Add up to 9 premium buttons
    // for (int i = 0; i < 9 && premiumIndex < premiumButtons.size(); i++) {
    // combined.add(premiumButtons.get(premiumIndex++));
    // }
    // }

    // return combined;
    // }

    private static List<Button> combineButtons(List<Button> basicButtons, List<Button> premiumButtons) {
        List<Button> combined = new ArrayList<>();
        int basicIndex = 0;
        int premiumIndex = 0;

        while (basicIndex < basicButtons.size() || premiumIndex < premiumButtons.size()) {
            // Fill 9 basic buttons (row 1)
            for (int i = 0; i < 9; i++) {
                if (basicIndex < basicButtons.size()) {
                    combined.add(basicButtons.get(basicIndex++));
                } else {
                    combined.add(GooeyButton.builder().title(Utils.toText("&eKeep Going!"))
                            .display(new ItemStack(Items.YELLOW_STAINED_GLASS_PANE)).build());
                }
            }

            // Fill 9 premium buttons (row 2)
            for (int i = 0; i < 9; i++) {
                if (premiumIndex < premiumButtons.size()) {
                    combined.add(premiumButtons.get(premiumIndex++));
                } else {
                    combined.add(GooeyButton.builder().title(Utils.toText("&eKeep Going!"))
                            .display(new ItemStack(Items.YELLOW_STAINED_GLASS_PANE)).build()); // Fill with empty slot
                }
            }
        }

        return combined;
    }

    private static GooeyButton getRewardButton(Player player, BattlePassReward reward, double currentLvl,
            boolean premium) {
        boolean completed = (currentLvl >= reward.getRequiredLvl());
        List<String> lore = new ArrayList<>(reward.getLore().stream().map(Utils::toText).toList());
        if (!lore.isEmpty()) {
            lore.add("");
        }
        if (premium && !player.hasPermission("battlepass.premium")) {
            lore.add("§c§lLOCKED");
            lore.add(Utils.toText(Battlepass.getInstance().battlePassText.premiumRequiredText));
            ItemStack is = new ItemStack(Items.BARRIER);
            return GooeyButton.builder()
                    .title(Utils.toText(reward.getName()))
                    .display(is)
                    .lore(lore)
                    .build();
        } else if (completed) {
            lore.add("§a§lCOMPLETED");
            ItemStack is = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
            return GooeyButton.builder()
                    .title(Utils.toText(reward.getName()))
                    .display(is)
                    .lore(lore)
                    .build();
        } else {
            lore.add("§c§lLOCKED");
            lore.add("§7You need to reach level " + Utils.getDisplayLvl(reward.getRequiredLvl())
                    + " to claim this reward.");
            ItemStack is = itemLocator(reward.getItemType());
            return GooeyButton.builder()
                    .title(Utils.toText(reward.getName()))
                    .display(is)
                    .lore(lore)
                    .build();
        }
    }

    private static ItemStack itemLocator(String itemType) {
        ItemStack itemStack = new ItemStack(Items.DIAMOND);
        if (itemType == null || itemType.isEmpty()) {
            return itemStack;
        }
        itemType = itemType.toLowerCase();
        if (itemType.startsWith("pixelmon:")) {
            String pokeBallName = itemType.replace("pixelmon:", "");
            if (PokeBallRegistry.getPokeBall(pokeBallName).getValue().isPresent()) {
                itemStack = PokeBallRegistry.getPokeBall(pokeBallName).getValue().get().getBallItem();
            } else {
                itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemType)));
            }
        } else {
            itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemType)));
        }
        return (itemStack == null || itemStack.isEmpty()) ? new ItemStack(Items.DIAMOND) : itemStack;
    }

    private static Button fillerButton() {
        ItemStack is = new ItemStack(Items.RED_STAINED_GLASS_PANE);
        GooeyButton filler = GooeyButton.builder().display(is).title("").build();
        return filler;
    }

    private static final ServerPlayerEntity getForgePlayer(Player player) {

        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getPlayerList().getPlayer(player.getUniqueId());
    }
}
