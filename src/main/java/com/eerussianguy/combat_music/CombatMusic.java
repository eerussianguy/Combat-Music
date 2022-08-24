package com.eerussianguy.combat_music;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import static com.eerussianguy.combat_music.CombatMusic.MOD_ID;

@Mod(MOD_ID)
public class CombatMusic
{
    public static final String MOD_ID = "combat_music";

    public int decaySeconds = 0;
    public SimpleSoundInstance lastSound;

    public CombatMusic()
    {
        MinecraftForge.EVENT_BUS.addListener(this::onAttack);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);

        CMConfig.init();
    }

    private void onClientTick(final TickEvent.ClientTickEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.END && mc.level != null && mc.level.getGameTime() % 20 == 0)
        {
            SoundManager manager = mc.getSoundManager();
            if (manager.isActive(lastSound))
            {
                LocalPlayer player = mc.player;
                if (player != null && getEntities(player) == 0)
                {
                    decaySeconds++;
                }
                if (decaySeconds > CMConfig.CLIENT.decayTime.get())
                {
                    manager.stop(lastSound);
                }
            }
        }
    }

    private void onAttack(final LivingAttackEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (entity instanceof LocalPlayer player)
        {
            Minecraft mc = Minecraft.getInstance();
            SoundManager manager = mc.getSoundManager();
            if (mc.level != null && mc.level.getDifficulty() != Difficulty.PEACEFUL)
            {
                if (getEntities(player) > CMConfig.CLIENT.minPursuitEntities.get())
                {
                    if (!manager.isActive(lastSound))
                    {
                        mc.getMusicManager().stopPlaying();
                        lastSound = pickSound(entity.getRandom());
                        manager.play(lastSound);
                    }
                    decaySeconds = 0;
                }
            }
        }
    }

    private static int getEntities(LocalPlayer player)
    {
        return player.clientLevel.getEntitiesOfClass(Monster.class, new AABB(-12D, -10D, -12D, 12D, 10D, 12D).move(player.blockPosition()), mob -> mob.canAttack(player, TargetingConditions.DEFAULT)).size();
    }

    private static SimpleSoundInstance pickSound(RandomSource rand)
    {
        final List<? extends String> sounds = CMConfig.CLIENT.sounds.get();
        final int idx = rand.nextInt(sounds.size());
        final SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(sounds.get(idx)));
        if (sound == null)
        {
            throw new NullPointerException("Invalid sound event resource location detected: " + sounds.get(idx));
        }
        return SimpleSoundInstance.forMusic(sound);
    }
}
