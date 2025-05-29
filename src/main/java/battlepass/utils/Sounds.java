package battlepass.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum Sounds {
    LEVEL_UP(Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4F);

    private final float pitch;
    private final int volume;
    private final Sound sound;

    Sounds(Sound sound, int volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static void playSound(Player player, Sounds sound) {
        player.playSound(player.getLocation(), sound.sound, sound.volume, sound.pitch);
    }
}
