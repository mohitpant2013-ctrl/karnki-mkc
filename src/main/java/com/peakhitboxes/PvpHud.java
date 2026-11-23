package com.peakhitboxes;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.Deque;

/** "Peak PvP": combo counter, reach of last hit, CPS, and a target HUD. */
public class PvpHud {
    private final Deque<Long> clicks = new ArrayDeque<>();
    private int combo = 0, bestCombo = 0;
    private long lastHitTime = 0, targetSince = 0;
    private double lastReach = 0;
    private LivingEntity target;
    private int lastHurtTime = 0;

    @SubscribeEvent
    public void onClick(InputEvent.ClickInputEvent e) {
        if (e.isAttack()) clicks.addLast(Util.getMillis());
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (e.getPlayer() != mc.player) return;
        Entity t = e.getTarget();
        AxisAlignedBB b = t.getBoundingBox();
        Vector3d eye = mc.player.getEyePosition(1f);
        double dx = Math.max(Math.max(b.minX - eye.x, 0), eye.x - b.maxX);
        double dy = Math.max(Math.max(b.minY - eye.y, 0), eye.y - b.maxY);
        double dz = Math.max(Math.max(b.minZ - eye.z, 0), eye.z - b.maxZ);
        lastReach = Math.sqrt(dx * dx + dy * dy + dz * dz);
        long now = Util.getMillis();
        if (t instanceof LivingEntity && ((LivingEntity) t).hurtTime == 0) { // only count real hits, not spam into i-frames
            combo = (now - lastHitTime < 2500) ? combo + 1 : 1;
            bestCombo = Math.max(bestCombo, combo);
        }
        lastHitTime = now;
        if (t instanceof LivingEntity) { target = (LivingEntity) t; targetSince = now; }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (e.phase != TickEvent.Phase.END || mc.player == null) return;
        int ht = mc.player.hurtTime;
        if (ht > 0 && lastHurtTime == 0) combo = 0; // you got hit -> combo broken
        lastHurtTime = ht;
        if (Util.getMillis() - lastHitTime > 2500) combo = 0;
        long cut = Util.getMillis() - 1000;
        while (!clicks.isEmpty() && clicks.peekFirst() < cut) clicks.pollFirst();
        if (target != null && (!target.isAlive() || Util.getMillis() - targetSince > 5000)) target = null;
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.options.renderDebug || !HitboxConfig.PVP_ENABLED.get()) return;
        MatrixStack ms = e.getMatrixStack();
        int col = HitboxConfig.parseColor(HitboxConfig.PVP_TEXT_COLOR.get());
        int x = HitboxConfig.PVP_X.get(), y = HitboxConfig.PVP_Y.get();

        if (HitboxConfig.PVP_COMBO.get()) { mc.font.drawShadow(ms, "Combo: " + combo + "  (best " + bestCombo + ")", x, y, col); y += 10; }
        if (HitboxConfig.PVP_REACH.get()) { mc.font.drawShadow(ms, String.format("Reach: %.2f", lastReach), x, y, col); y += 10; }
        if (HitboxConfig.PVP_CPS.get()) { mc.font.drawShadow(ms, "CPS: " + clicks.size(), x, y, col); y += 10; }

        if (HitboxConfig.PVP_TARGET_HUD.get() && target != null) {
            y += 4;
            int w = 120;
            AbstractGui.fill(ms, x, y, x + w, y + 32, 0x90000000);
            mc.font.drawShadow(ms, target.getDisplayName().getString(), x + 4, y + 4, 0xFFFFFFFF);
            float hp = target.getHealth(), max = Math.max(1f, target.getMaxHealth());
            float pct = Math.min(1f, hp / max);
            int barCol = pct > 0.6f ? 0xFF55FF55 : pct > 0.3f ? 0xFFFFDD33 : 0xFFFF4444;
            AbstractGui.fill(ms, x + 4, y + 15, x + w - 4, y + 19, 0xFF333333);
            AbstractGui.fill(ms, x + 4, y + 15, x + 4 + (int) ((w - 8) * pct), y + 19, barCol);
            String info = String.format("%.1f\u2764  %d armor", hp / 2f, target.getArmorValue());
            mc.font.drawShadow(ms, info, x + 4, y + 22, 0xFFDDDDDD);
            String dist = String.format("%.1fm", target.distanceTo(mc.player));
            mc.font.drawShadow(ms, dist, x + w - 4 - mc.font.width(dist), y + 4, 0xFFAAAAAA);
        }
    }
}
