package battlepass.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import battlepass.main.Battlepass;

public class InventoryUtils {

    public enum BorderAnimationType {
        RAINBOW_LOOP,
        PING_PONG,
        FLASHING
    }

    public static class BattlepassInvHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static Inventory fillInventory(Inventory inv, boolean infinite, @Nonnull Material... mat) {
        int size = inv.getSize();
        int matLen = mat.length;
        if (matLen == 0)
            return inv;
        for (int i = 0; i < size; i++) {
            int matIndex = infinite ? i % matLen : i;
            if (matIndex >= matLen)
                break;
            ItemStack item = new ItemStack(mat[matIndex]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
        return inv;
    }

    public static Inventory createBorder(Inventory inv, Material mat, boolean checkered, @Nullable Material mat2) {
        int size = inv.getSize();
        for (int i = 0; i < size; i++) {
            boolean isBorder = false;
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == (size / 9) - 1 || col == 0 || col == 8) {
                isBorder = true;
            }
            if (isBorder) {
                Material useMat = mat;
                if (checkered && mat2 != null) {
                    useMat = ((row + col) % 2 == 0) ? mat : mat2;
                }
                ItemStack pane = new ItemStack(useMat);
                ItemMeta meta = pane.getItemMeta();
                meta.setDisplayName(" ");
                pane.setItemMeta(meta);
                inv.setItem(i, pane);
            }
        }
        return inv;
    }

    public static void playBorderAnimation(Player player, BorderAnimationType type, @Nullable int... skippedSlots) {
        List<Integer> borderSlots = new ArrayList<>(Arrays.asList(
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                17, 26, 35, 44,
                53, 52, 51, 50, 49, 48, 47, 46, 45,
                36, 27, 18, 9));

        if (skippedSlots != null && skippedSlots.length > 0) {
            borderSlots.removeAll(Arrays.stream(skippedSlots).boxed().toList());
        }

        switch (type) {
            case RAINBOW_LOOP -> startRainbowLoop(player, borderSlots);
            case PING_PONG -> startPingPong(player, borderSlots);
            case FLASHING -> startFlashing(player, borderSlots);
            default -> throw new IllegalArgumentException("Unknown animation type: " + type);
        }
    }

    private static void startRainbowLoop(Player player, List<Integer> borders) {
        new BukkitRunnable() {
            int tick = 0;
            final Inventory inv = player.getOpenInventory().getTopInventory();
            final Material[] colors = {
                    Material.RED_STAINED_GLASS_PANE,
                    Material.ORANGE_STAINED_GLASS_PANE,
                    Material.YELLOW_STAINED_GLASS_PANE,
                    Material.LIME_STAINED_GLASS_PANE,
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    Material.BLUE_STAINED_GLASS_PANE,
                    Material.PURPLE_STAINED_GLASS_PANE,
                    Material.PINK_STAINED_GLASS_PANE
            };

            @Override
            public void run() {
                if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof BattlepassInvHolder)) {
                    this.cancel();
                    return;
                }

                for (int i = 0; i < borders.size(); i++) {
                    int slot = borders.get(i);
                    Material mat = colors[(tick + i) % colors.length];
                    ItemStack pane = new ItemStack(mat);
                    ItemMeta meta = pane.getItemMeta();
                    meta.setDisplayName("§r");
                    pane.setItemMeta(meta);
                    inv.setItem(slot, pane);
                }

                tick++;
            }
        }.runTaskTimer(Battlepass.getInstance(), 0L, 2L);
    }

    private static void startPingPong(Player player, List<Integer> borders) {
    new BukkitRunnable() {
        final Inventory inv = player.getOpenInventory().getTopInventory();
        final Material highlight = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        final Material filler = Material.BLACK_STAINED_GLASS_PANE;

        int currentIndex = 0;
        boolean forward = true;
        final int startingIndex;

        // Find starting position (5th slot of first row = slot 4)
        {
            int startSlot = 4;
            int foundIndex = 0;
            for (int i = 0; i < borders.size(); i++) {
                if (borders.get(i) == startSlot) {
                    foundIndex = i;
                    break;
                }
            }
            startingIndex = foundIndex;
            currentIndex = startingIndex;
        }

        @Override
        public void run() {
            if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof BattlepassInvHolder)) {
                this.cancel();
                return;
            }

            // Set all border slots to filler first
            for (int i = 0; i < borders.size(); i++) {
                int slot = borders.get(i);
                Material mat = (i == currentIndex) ? highlight : filler;

                ItemStack pane = new ItemStack(mat);
                ItemMeta meta = pane.getItemMeta();
                meta.setDisplayName("§r");
                pane.setItemMeta(meta);
                inv.setItem(slot, pane);
            }

            // Advance to next position
            if (forward) {
                currentIndex++;
                // If we've gone full circle back to start, reverse direction
                if (currentIndex >= borders.size()) {
                    currentIndex = 0; // Wrap to beginning
                }
                // Check if we're back at starting position after going around
                if (currentIndex == startingIndex && currentIndex != 0) {
                    forward = false;
                    currentIndex--; // Start going backward
                }
            } else {
                currentIndex--;
                if (currentIndex < 0) {
                    currentIndex = borders.size() - 1; // Wrap to end
                }
                // Check if we're back at starting position from reverse direction
                if (currentIndex == startingIndex) {
                    forward = true;
                    currentIndex++; // Start going forward again
                }
            }
        }
    }.runTaskTimer(Battlepass.getInstance(), 0L, 2L);
}

    private static void startFlashing(Player player, List<Integer> borders) {
        new BukkitRunnable() {
            int tick = 0;
            final Inventory inv = player.getOpenInventory().getTopInventory();
            final Material[] flashColors = {
                    Material.RED_STAINED_GLASS_PANE,
                    Material.YELLOW_STAINED_GLASS_PANE,
                    Material.LIME_STAINED_GLASS_PANE,
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    Material.MAGENTA_STAINED_GLASS_PANE
            };

            @Override
            public void run() {
                if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof BattlepassInvHolder)) {
                    this.cancel();
                    return;
                }

                Material mat = flashColors[tick % flashColors.length];
                ItemStack pane = new ItemStack(mat);
                ItemMeta meta = pane.getItemMeta();
                meta.setDisplayName("§r");
                pane.setItemMeta(meta);

                for (int slot : borders) {
                    inv.setItem(slot, pane);
                }

                tick++;
            }
        }.runTaskTimer(Battlepass.getInstance(), 0L, 10L); // Slightly slower for flash effect
    }
}
