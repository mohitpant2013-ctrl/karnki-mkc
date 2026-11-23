package com.peakhitboxes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static KeyBinding TOGGLE, SETTINGS, PITCH_SNAP;

    static void register() {
        TOGGLE = new KeyBinding("key.peakhitboxes.toggle", GLFW.GLFW_KEY_H, "key.categories.peakhitboxes");
        SETTINGS = new KeyBinding("key.peakhitboxes.config", GLFW.GLFW_KEY_J, "key.categories.peakhitboxes");
        ClientRegistry.registerKeyBinding(TOGGLE);
        ClientRegistry.registerKeyBinding(SETTINGS);
        PITCH_SNAP = new KeyBinding("key.peakhitboxes.pitchsnap", GLFW.GLFW_KEY_G, "key.categories.peakhitboxes");
        ClientRegistry.registerKeyBinding(PITCH_SNAP);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        while (TOGGLE.consumeClick()) {
            boolean v = !HitboxConfig.ENABLED.get();
            HitboxConfig.ENABLED.set(v);
            HitboxConfig.SPEC.save();
            mc.player.displayClientMessage(new StringTextComponent("Peak Hitboxes: " +
                    (v ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF")), true);
        }
        while (PITCH_SNAP.consumeClick()) {
            if (mc.player.isFallFlying()) mc.player.xRot = (float) -HitboxConfig.ELYTRA_LOCK_ANGLE.get().doubleValue();
        }
        while (SETTINGS.consumeClick()) mc.setScreen(new HitboxSettingsScreen(null));
    }
}
