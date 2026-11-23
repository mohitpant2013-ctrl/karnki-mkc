# Peak Client (Forge 1.16.5, client-side) - v2

## Hitboxes
White by default, RED when in your attack range, GOLD when getting hit.
New: crosshair-target color, per-type colors (player/hostile/passive), team colors, fade with distance.
Eye line, look line, line width, through walls, max distance.

## Elytra Pitch HUD
Shows `[ 40° ]` + elytra icon under crosshair while gliding. Turns green inside your "good pitch" range.
Shows speed in blocks/sec. Press G while flying to snap to your set angle (default 40°).

## PvP HUD
Combo counter (breaks when you get hit), reach of your last hit, CPS, and target HUD (name, health bar, armor, distance).

## Keys (change in Controls)
H toggle hitboxes · J settings · G elytra pitch snap

## Build
JDK 8 + Forge 1.16.5 MDK's gradlew/gradle folder copied in, then `gradlew build`. Jar in build/libs.
