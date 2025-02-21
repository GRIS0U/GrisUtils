package com.example.addon.modules;

import com.example.addon.GrisUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Escalator extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private final Setting<Double> reach = sgGeneral.add(new DoubleSetting.Builder()
        .name("reach")
        .description("The maximum distance between the player and the ground/ceiling.")
        .defaultValue(5)
        .build()
    );

    private final Setting<Double> maxTpDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-tp-distance")
        .description("The maximum distance you can teleport through blocks.")
        .defaultValue(5)
        .sliderMax(20)
        .build()
    );

    public Escalator() {
        super(GrisUtils.CATEGORY, "escalator", "Teleports you up or down through blocks");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player.isUsingItem()) return;

        if (mc.options.useKey.isPressed()) {
            HitResult hitResult = mc.player.raycast(reach.get(), 1f / 20f, false);

            if (hitResult.getType() == HitResult.Type.ENTITY && mc.player.interact(((EntityHitResult) hitResult).getEntity(), Hand.MAIN_HAND) != ActionResult.PASS) return;

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                Direction side = ((BlockHitResult) hitResult).getSide();
                BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();

                int iteration = 0;

                while (!mc.world.getBlockState(pos).isOf(Blocks.AIR) &&
                       !mc.world.getBlockState(pos).isOf(Blocks.WATER) &&
                       iteration < maxTpDistance.get()) {

                        pos = pos.offset(side.getOpposite());
                        iteration++;
                }

                pos = pos.offset(side.getOpposite());

                if (mc.world.getBlockState(pos).onUse(mc.world, mc.player, (BlockHitResult) hitResult) != ActionResult.PASS) return;

                mc.player.setPosition(pos.getX() + 0.5 + side.getOffsetX(), pos.getY(), pos.getZ() + 0.5 + side.getOffsetZ());
            }
        }
    }
}
