package com.peakhitboxes;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Shows "[ 40° ]" + elytra icon under the crosshair while gliding. */
public class ElytraPitchHud {
    private static final ItemStack ELYTRA = new ItemStack(Items.ELYTRA);

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.options.hideGui || !HitboxConfig.ELYTRA_ENABLED.get() || !p.isFallFlying()) return;

        MatrixStack ms = e.getMatrixStack();
        float pitch = -p.xRot; // MC: negative xRot = looking up
        int shown = Math.round(pitch);
        double min = Math.min(HitboxConfig.ELYTRA_GOOD_MIN.get(), HitboxConfig.ELYTRA_GOOD_MAX.get());
        double max = Math.max(HitboxConfig.ELYTRA_GOOD_MIN.get(), HitboxConfig.ELYTRA_GOOD_MAX.get());
        int color = (pitch >= min && pitch <= max)
                ? HitboxConfig.parseColor(HitboxConfig.ELYTRA_GOOD_COLOR.get())
                : HitboxConfig.parseColor(HitboxConfig.ELYTRA_BAD_COLOR.get());

        String text = "[ " + shown + "\u00b0 ]";
        int w = e.getWindow().getGuiScaledWidth(), h = e.getWindow().getGuiScaledHeight();
        boolean icon = HitboxConfig.ELYTRA_ICON.get();
        int total = mc.font.width(text) + (icon ? 18 : 0);
        int x = w / 2 - total / 2 + HitboxConfig.ELYTRA_X.get();
        int y = h / 2 + HitboxConfig.ELYTRA_Y.get();
        mc.font.drawShadow(ms, text, x, y, color);
        if (icon) mc.getItemRenderer().renderGuiItem(ELYTRA, x + mc.font.width(text) + 2, y - 4);

        if (HitboxConfig.ELYTRA_SHOW_SPEED.get()) {
            Vector3d v = p.getDeltaMovement();
            String spd = String.format("%.1f b/s", v.length() * 20.0);
            mc.font.drawShadow(ms, spd, w / 2f - mc.font.width(spd) / 2f + HitboxConfig.ELYTRA_X.get(), y + 12, 0xFFAAAAAA);
        }
    }
}
