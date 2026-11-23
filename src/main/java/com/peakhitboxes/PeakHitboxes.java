package com.peakhitboxes;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

@Mod(PeakHitboxes.MOD_ID)
public class PeakHitboxes {
    public static final String MOD_ID = "peakhitboxes";

    public PeakHitboxes() {
        ModLoadingContext ctx = ModLoadingContext.get();
        // Client-only: tell servers we don't care if they have it.
        ctx.registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ctx.registerConfig(ModConfig.Type.CLIENT, HitboxConfig.SPEC, "peakhitboxes-client.toml");
            ctx.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                    () -> (mc, parent) -> new HitboxSettingsScreen(parent));
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        });
    }

    private void clientSetup(FMLClientSetupEvent event) {
        Keybinds.register();
        MinecraftForge.EVENT_BUS.register(new HitboxRenderer());
        MinecraftForge.EVENT_BUS.register(new Keybinds());
        MinecraftForge.EVENT_BUS.register(new ElytraPitchHud());
        MinecraftForge.EVENT_BUS.register(new PvpHud());
    }
}
