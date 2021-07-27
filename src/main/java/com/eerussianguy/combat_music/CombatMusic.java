package com.eerussianguy.combat_music;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Difficulty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.registries.ForgeRegistries;

import static com.eerussianguy.combat_music.CombatMusic.MOD_ID;

@Mod(MOD_ID)
public class CombatMusic
{
    public static final String MOD_ID = "combat_music";
    private static final Logger LOGGER = LogManager.getLogger();

    public int decaySeconds = 0;
    public SimpleSound lastSound;

    public CombatMusic()
    {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        MinecraftForge.EVENT_BUS.addListener(this::onAttack);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);

        CMConfig.init();
    }

    private void onClientTick(final TickEvent.ClientTickEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.END && mc.level != null && mc.level.getGameTime() % 20 == 0)
        {
            SoundHandler manager = mc.getSoundManager();
            if (manager.isActive(lastSound))
            {
                ClientPlayerEntity player = mc.player;
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
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ClientPlayerEntity)
        {
            ClientPlayerEntity player = (ClientPlayerEntity) entity;
            Minecraft mc = Minecraft.getInstance();
            SoundHandler manager = mc.getSoundManager();
            if (mc.options.difficulty != Difficulty.PEACEFUL)
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

    private static int getEntities(ClientPlayerEntity player)
    {
        return player.clientLevel.getEntitiesOfClass(MonsterEntity.class, new AxisAlignedBB(-12D, -10D, -12D, 12D, 10D, 12D).move(player.blockPosition()), mob -> mob.canSee(player)).size();
    }

    private static SimpleSound pickSound(Random rand)
    {
        final List<? extends String> sounds = CMConfig.CLIENT.sounds.get();
        final int idx = rand.nextInt(sounds.size());
        final SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(sounds.get(idx)));
        if (sound == null)
        {
            throw new NullPointerException("Invalid sound event resource location detected: " + sounds.get(idx));
        }
        return SimpleSound.forMusic(sound);
    }
}
