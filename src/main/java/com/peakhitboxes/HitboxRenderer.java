package com.peakhitboxes;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HitboxRenderer {

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (HitboxConfig.HIDE_VANILLA.get() && mc.getEntityRenderDispatcher().shouldRenderHitBoxes())
            mc.getEntityRenderDispatcher().setRenderHitBoxes(false);
        if (!HitboxConfig.ENABLED.get()) return;

        float pt = event.getPartialTicks();
        MatrixStack ms = event.getMatrixStack();
        Vector3d cam = mc.gameRenderer.getMainCamera().getPosition();
        Vector3d eyes = mc.player.getEyePosition(pt);

        double range = HitboxConfig.RANGE.get();
        double maxDistSq = HitboxConfig.MAX_DISTANCE.get() * HitboxConfig.MAX_DISTANCE.get();
        int base = HitboxConfig.parseColor(HitboxConfig.BASE_COLOR.get());
        int inRange = HitboxConfig.parseColor(HitboxConfig.RANGE_COLOR.get());
        int hit = HitboxConfig.parseColor(HitboxConfig.HIT_COLOR.get());
        int eyeCol = HitboxConfig.parseColor(HitboxConfig.EYE_COLOR.get());
        int lookCol = HitboxConfig.parseColor(HitboxConfig.LOOK_COLOR.get());

        IRenderTypeBuffer.Impl buffers = mc.renderBuffers().bufferSource();
        RenderType type = HitboxRenderType.lines(HitboxConfig.LINE_WIDTH.get(), HitboxConfig.THROUGH_WALLS.get());
        IVertexBuilder vb = buffers.getBuffer(type);

        ms.pushPose();
        ms.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f m = ms.last().pose();

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player && (!HitboxConfig.SHOW_SELF.get() || mc.options.getCameraType().isFirstPerson())) continue;
            if (e.isInvisibleTo(mc.player)) continue;
            if (HitboxConfig.LIVING_ONLY.get() && !(e instanceof LivingEntity)) continue;
            if (e.distanceToSqr(mc.player) > maxDistSq) continue;

            // Interpolate so boxes don't jitter behind moving entities.
            double ix = MathHelper.lerp(pt, e.xOld, e.getX()) - e.getX();
            double iy = MathHelper.lerp(pt, e.yOld, e.getY()) - e.getY();
            double iz = MathHelper.lerp(pt, e.zOld, e.getZ()) - e.getZ();
            AxisAlignedBB box = e.getBoundingBox().move(ix, iy, iz);

            int color = base;
            if (HitboxConfig.USE_TYPE_COLORS.get()) {
                EntityClassification cat = e.getType().getCategory();
                if (e instanceof PlayerEntity) color = HitboxConfig.parseColor(HitboxConfig.PLAYER_COLOR.get());
                else if (cat == EntityClassification.MONSTER) color = HitboxConfig.parseColor(HitboxConfig.HOSTILE_COLOR.get());
                else if (e instanceof LivingEntity) color = HitboxConfig.parseColor(HitboxConfig.PASSIVE_COLOR.get());
            }
            if (HitboxConfig.USE_TEAM_COLORS.get() && e instanceof PlayerEntity && e.getTeam() != null)
                color = 0xFF000000 | e.getTeamColor();
            boolean hurt = e instanceof LivingEntity && ((LivingEntity) e).hurtTime > 0;
            if (HitboxConfig.USE_HIT_COLOR.get() && hurt) color = hit;
            else if (HitboxConfig.USE_TARGET_COLOR.get() && e == mc.crosshairPickEntity) color = HitboxConfig.parseColor(HitboxConfig.TARGET_COLOR.get());
            else if (HitboxConfig.USE_RANGE_COLOR.get() && e != mc.player && distanceToBox(eyes, box) <= range) color = inRange;
            if (HitboxConfig.FADE_WITH_DISTANCE.get()) {
                double f = 1.0 - Math.sqrt(e.distanceToSqr(mc.player) / maxDistSq) * 0.85;
                int a = (int) (((color >>> 24) & 255) * f);
                color = (a << 24) | (color & 0xFFFFFF);
            }

            drawBox(vb, m, box, color);

            if (e instanceof LivingEntity) {
                double eyeY = box.minY + e.getEyeHeight();
                if (HitboxConfig.SHOW_EYE_LINE.get()) {
                    AxisAlignedBB eb = new AxisAlignedBB(box.minX, eyeY - 0.01, box.minZ, box.maxX, eyeY + 0.01, box.maxZ);
                    drawBox(vb, m, eb, eyeCol);
                }
                if (HitboxConfig.SHOW_LOOK_LINE.get()) {
                    Vector3d look = e.getViewVector(pt).scale(HitboxConfig.LOOK_LENGTH.get());
                    double cx = (box.minX + box.maxX) / 2, cz = (box.minZ + box.maxZ) / 2;
                    line(vb, m, cx, eyeY, cz, cx + look.x, eyeY + look.y, cz + look.z, lookCol);
                }
            }
        }
        ms.popPose();
        buffers.endBatch(type);
    }

    /** Distance from a point to the closest point of a box (how vanilla reach effectively works). */
    private static double distanceToBox(Vector3d p, AxisAlignedBB b) {
        double dx = Math.max(Math.max(b.minX - p.x, 0), p.x - b.maxX);
        double dy = Math.max(Math.max(b.minY - p.y, 0), p.y - b.maxY);
        double dz = Math.max(Math.max(b.minZ - p.z, 0), p.z - b.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static void drawBox(IVertexBuilder vb, Matrix4f m, AxisAlignedBB b, int c) {
        double x0 = b.minX, y0 = b.minY, z0 = b.minZ, x1 = b.maxX, y1 = b.maxY, z1 = b.maxZ;
        // bottom
        line(vb, m, x0, y0, z0, x1, y0, z0, c); line(vb, m, x1, y0, z0, x1, y0, z1, c);
        line(vb, m, x1, y0, z1, x0, y0, z1, c); line(vb, m, x0, y0, z1, x0, y0, z0, c);
        // top
        line(vb, m, x0, y1, z0, x1, y1, z0, c); line(vb, m, x1, y1, z0, x1, y1, z1, c);
        line(vb, m, x1, y1, z1, x0, y1, z1, c); line(vb, m, x0, y1, z1, x0, y1, z0, c);
        // pillars
        line(vb, m, x0, y0, z0, x0, y1, z0, c); line(vb, m, x1, y0, z0, x1, y1, z0, c);
        line(vb, m, x1, y0, z1, x1, y1, z1, c); line(vb, m, x0, y0, z1, x0, y1, z1, c);
    }

    private static void line(IVertexBuilder vb, Matrix4f m, double ax, double ay, double az,
                             double bx, double by, double bz, int argb) {
        int a = (argb >>> 24) & 255, r = (argb >> 16) & 255, g = (argb >> 8) & 255, bl = argb & 255;
        vb.vertex(m, (float) ax, (float) ay, (float) az).color(r, g, bl, a).endVertex();
        vb.vertex(m, (float) bx, (float) by, (float) bz).color(r, g, bl, a).endVertex();
    }
}
