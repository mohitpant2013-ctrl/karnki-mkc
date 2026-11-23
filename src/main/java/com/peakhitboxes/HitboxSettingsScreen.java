package com.peakhitboxes;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Tabbed settings screen: Hitboxes / Colors / Smart Colors / Elytra / PvP. */
public class HitboxSettingsScreen extends Screen {
    private static final String[] TABS = {"Hitboxes", "Colors", "Smart", "Elytra", "PvP"};
    private static int tab = 0;
    private final Screen parent;
    private final List<Runnable> savers = new ArrayList<>();
    private final List<TextFieldWidget> fields = new ArrayList<>();
    private final List<Object[]> swatches = new ArrayList<>();
    private final List<Object[]> labels = new ArrayList<>();
    private int col, row;

    public HitboxSettingsScreen(Screen parent) {
        super(new StringTextComponent("Peak Client Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        savers.clear(); fields.clear(); swatches.clear(); labels.clear(); col = 0; row = 0;
        int tw = 60, tx = width / 2 - (TABS.length * (tw + 2)) / 2;
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            Button b = addButton(new Button(tx + i * (tw + 2), 28, tw, 20, new StringTextComponent((i == tab ? "\u00a7e" : "") + TABS[i]), x -> {
                saveAll(); tab = idx; init(minecraft, width, height);
            }));
            b.active = i != tab;
        }
        switch (tab) {
            case 0:
                toggle("Enabled", HitboxConfig.ENABLED); toggle("Hide vanilla F3+B", HitboxConfig.HIDE_VANILLA);
                toggle("Mobs/players only", HitboxConfig.LIVING_ONLY); toggle("Show own box", HitboxConfig.SHOW_SELF);
                toggle("Through walls", HitboxConfig.THROUGH_WALLS); toggle("Eye line", HitboxConfig.SHOW_EYE_LINE);
                toggle("Look line", HitboxConfig.SHOW_LOOK_LINE); toggle("Fade with distance", HitboxConfig.FADE_WITH_DISTANCE);
                number("Line width", HitboxConfig.LINE_WIDTH); number("Attack range", HitboxConfig.RANGE);
                number("Max distance", HitboxConfig.MAX_DISTANCE); number("Look length", HitboxConfig.LOOK_LENGTH);
                break;
            case 1:
                toggle("Range color", HitboxConfig.USE_RANGE_COLOR); toggle("Hit color", HitboxConfig.USE_HIT_COLOR);
                color("Base color", HitboxConfig.BASE_COLOR); color("In-range color", HitboxConfig.RANGE_COLOR);
                color("Hit color", HitboxConfig.HIT_COLOR); color("Eye line color", HitboxConfig.EYE_COLOR);
                color("Look line color", HitboxConfig.LOOK_COLOR);
                break;
            case 2:
                toggle("Crosshair target color", HitboxConfig.USE_TARGET_COLOR); toggle("Per-type colors", HitboxConfig.USE_TYPE_COLORS);
                toggle("Team colors", HitboxConfig.USE_TEAM_COLORS); skip();
                color("Target color", HitboxConfig.TARGET_COLOR); color("Player color", HitboxConfig.PLAYER_COLOR);
                color("Hostile color", HitboxConfig.HOSTILE_COLOR); color("Passive color", HitboxConfig.PASSIVE_COLOR);
                break;
            case 3:
                toggle("Pitch HUD", HitboxConfig.ELYTRA_ENABLED); toggle("Elytra icon", HitboxConfig.ELYTRA_ICON);
                toggle("Show speed", HitboxConfig.ELYTRA_SHOW_SPEED); skip();
                number("Good pitch min", HitboxConfig.ELYTRA_GOOD_MIN); number("Good pitch max", HitboxConfig.ELYTRA_GOOD_MAX);
                number("Snap angle (G key)", HitboxConfig.ELYTRA_LOCK_ANGLE); integer("Offset X", HitboxConfig.ELYTRA_X);
                integer("Offset Y", HitboxConfig.ELYTRA_Y); skip();
                color("Good pitch color", HitboxConfig.ELYTRA_GOOD_COLOR); color("Normal color", HitboxConfig.ELYTRA_BAD_COLOR);
                break;
            default:
                toggle("PvP HUD", HitboxConfig.PVP_ENABLED); toggle("Combo counter", HitboxConfig.PVP_COMBO);
                toggle("Reach display", HitboxConfig.PVP_REACH); toggle("CPS", HitboxConfig.PVP_CPS);
                toggle("Target HUD", HitboxConfig.PVP_TARGET_HUD); skip();
                integer("HUD X", HitboxConfig.PVP_X); integer("HUD Y", HitboxConfig.PVP_Y);
                color("Text color", HitboxConfig.PVP_TEXT_COLOR);
        }
        addButton(new Button(width / 2 - 100, height - 26, 200, 20, new StringTextComponent("Done"), b -> onClose()));
    }

    // Two-column auto layout
    private int nx() { return col == 0 ? width / 2 - 155 : width / 2 + 5; }
    private int ny() { return 56 + row; }
    private void advance(int h) { if (col == 1) { row += h; col = 0; } else col = 1; }
    private void skip() { advance(24); }

    private void toggle(String name, ForgeConfigSpec.BooleanValue v) {
        addButton(new Button(nx(), ny(), 150, 20, label(name, v.get()), b -> { v.set(!v.get()); b.setMessage(label(name, v.get())); }));
        advance(24);
    }

    private static StringTextComponent label(String n, boolean on) {
        return new StringTextComponent(n + ": " + (on ? "\u00a7aON" : "\u00a7cOFF"));
    }

    private void number(String name, ForgeConfigSpec.DoubleValue v) {
        labels.add(new Object[]{name, nx(), ny()});
        TextFieldWidget f = field(nx(), ny() + 10, 150, String.valueOf(v.get()), s -> s.matches("-?[0-9.]*"));
        savers.add(() -> { try { v.set(Double.parseDouble(f.getValue())); } catch (Exception ignored) { } });
        advance(34);
    }

    private void integer(String name, ForgeConfigSpec.IntValue v) {
        labels.add(new Object[]{name, nx(), ny()});
        TextFieldWidget f = field(nx(), ny() + 10, 150, String.valueOf(v.get()), s -> s.matches("-?[0-9]*"));
        savers.add(() -> { try { v.set(Integer.parseInt(f.getValue())); } catch (Exception ignored) { } });
        advance(34);
    }

    private void color(String name, ForgeConfigSpec.ConfigValue<String> v) {
        labels.add(new Object[]{name, nx(), ny()});
        TextFieldWidget f = field(nx(), ny() + 10, 126, v.get(), s -> s.matches("#?[0-9a-fA-F]{0,8}"));
        swatches.add(new Object[]{f, nx() + 130, ny() + 10});
        savers.add(() -> { String t = f.getValue(); if (HitboxConfig.isValidColor(t)) v.set(t.startsWith("#") ? t : "#" + t); });
        advance(34);
    }

    private TextFieldWidget field(int x, int y, int w, String value, Predicate<String> filter) {
        TextFieldWidget f = new TextFieldWidget(font, x, y, w, 18, StringTextComponent.EMPTY);
        f.setMaxLength(12);
        f.setValue(value);
        f.setFilter(filter);
        children.add(f);
        fields.add(f);
        return f;
    }

    private void saveAll() { savers.forEach(Runnable::run); HitboxConfig.SPEC.save(); }

    @Override
    public void render(MatrixStack ms, int mx, int my, float pt) {
        renderBackground(ms);
        drawCenteredString(ms, font, title, width / 2, 12, 0xFFFFFF);
        for (Object[] l : labels) drawString(ms, font, (String) l[0], (int) l[1], (int) l[2], 0xAAAAAA);
        for (TextFieldWidget f : fields) f.render(ms, mx, my, pt);
        for (Object[] s : swatches) {
            TextFieldWidget f = (TextFieldWidget) s[0];
            int sx = (int) s[1], sy = (int) s[2];
            AbstractGui.fill(ms, sx, sy, sx + 20, sy + 18, 0xFF000000);
            AbstractGui.fill(ms, sx + 1, sy + 1, sx + 19, sy + 17,
                    HitboxConfig.isValidColor(f.getValue()) ? HitboxConfig.parseColor(f.getValue()) : 0xFF555555);
        }
        if (tab == 1 || tab == 2) drawCenteredString(ms, font, "Colors: #RRGGBB or #AARRGGBB (AA = opacity)", width / 2, height - 40, 0x888888);
        super.render(ms, mx, my, pt);
    }

    @Override
    public void tick() { for (TextFieldWidget f : fields) f.tick(); }

    @Override
    public void onClose() { saveAll(); minecraft.setScreen(parent); }
}
