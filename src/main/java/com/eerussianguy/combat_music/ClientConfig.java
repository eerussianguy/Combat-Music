package com.eerussianguy.combat_music;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import net.minecraftforge.common.ForgeConfigSpec;

import static com.eerussianguy.combat_music.CombatMusic.MOD_ID;

public class ClientConfig
{
    public final ForgeConfigSpec.IntValue minPursuitEntities;
    public final ForgeConfigSpec.IntValue decayTime;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> sounds;

    ClientConfig(ForgeConfigSpec.Builder innerBuilder)
    {
        Function<String, ForgeConfigSpec.Builder> builder = name -> innerBuilder.translation(MOD_ID + ".config.server." + name);

        innerBuilder.push("general");
        minPursuitEntities = builder.apply("minPursuitEntities").comment("Minimum number of pursuing mobs to trigger a music sequence").defineInRange("minPursuitEntities", 3, 1, Integer.MAX_VALUE);
        decayTime = builder.apply("decayTime").comment("Seconds without mobs around needed for music to stop itself.").defineInRange("decayTime", 20, 1, Integer.MAX_VALUE);
        sounds = builder.apply("sounds").comment("Resource locations of sounds to play. Separate with commas.").defineList("sounds", () -> Arrays.asList("minecraft:music_disc.pigstep", "minecraft:music_disc.mellohi"), o -> o instanceof String);
        innerBuilder.pop();
    }
}
