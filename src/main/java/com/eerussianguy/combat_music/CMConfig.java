package com.eerussianguy.combat_music;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class CMConfig
{
    public static final ClientConfig CLIENT = registerConfig(ClientConfig::new);

    public static void init() {}

    private static <C> C registerConfig(Function<ForgeConfigSpec.Builder, C> factory)
    {
        Pair<C, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(factory);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, specPair.getRight());
        return specPair.getLeft();
    }
}
