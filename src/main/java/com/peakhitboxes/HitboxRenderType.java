package com.peakhitboxes;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;

/** Subclass only to reach RenderState's protected building blocks; builds line types with custom width. */
public final class HitboxRenderType extends RenderType {
    private static final Map<String, RenderType> CACHE = new HashMap<>();

    private HitboxRenderType(String n, net.minecraft.client.renderer.vertex.VertexFormat f, int m, int s, boolean a, boolean b, Runnable r1, Runnable r2) {
        super(n, f, m, s, a, b, r1, r2);
    }

    public static RenderType lines(double width, boolean throughWalls) {
        String key = width + ":" + throughWalls;
        return CACHE.computeIfAbsent(key, k -> create("peakhitboxes_lines_" + k,
                net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
                RenderType.State.builder()
                        .setLineState(new RenderState.LineState(OptionalDouble.of(width)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(throughWalls ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)));
    }
}
