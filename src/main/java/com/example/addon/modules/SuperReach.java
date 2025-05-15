package com.example.addon.modules;

import com.example.addon.GrisUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.core.appender.rolling.action.IfAll;
import org.lwjgl.glfw.GLFW;
public class SuperReach extends Module {

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private boolean canClick = true;

    public SuperReach() {
        super(GrisUtils.CATEGORY, "super-reach", "Hit entities up to ? blocks.");
    }

    private final Setting<Double> maxReach = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-reach")
        .description("The maximum distance you can hit entities.")
        .defaultValue(40)
        .range(10, 100)
        .sliderRange(10, 100)
        .build());

    private final Setting<Double> step = sgGeneral.add(new DoubleSetting.Builder()
        .name("step")
        .description("Higher value = hit entities quicker.")
        .defaultValue(8)
        .range(1, 20)
        .sliderRange(1, 8)
        .build());

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay-(ms)")
        .description("Higher value = hit entities quicker.")
        .defaultValue(100)
        .range(0, 1000)
        .sliderRange(0, 100)
        .build());

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.currentScreen != null) return;

        Long window = mc.getWindow().getHandle();

        int mouseState = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT);

        if (!canClick && mouseState == GLFW.GLFW_RELEASE) {
            canClick = true;
            return;
        }

        Entity entity = raycastEntity();
        if (canClick && mouseState == GLFW.GLFW_PRESS && entity != null) {
            setPos(entity.getPos(), true, entity);
            canClick = false;
        }
    }

    public Entity raycastEntity() {
        Double maxReach_ = maxReach.get();
        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
        Vec3d lookVec = mc.player.getRotationVec(1.0f);
        Vec3d endPos = eyePos.add(lookVec.multiply(maxReach_));

        Box box = mc.player.getBoundingBox().stretch(lookVec.multiply(maxReach_)).expand(1.0);

        EntityHitResult hit = ProjectileUtil.getEntityCollision(
            mc.world,
            mc.player,
            eyePos,
            endPos,
            box,
            (entity) -> !entity.isSpectator() && entity.isAttackable()
        );

        return hit != null ? hit.getEntity() : null;
    }


        private void setPos (Vec3d targetPos, boolean threeBlocksDistance, Entity entityToHit){
            mc.player.setVelocity(0, 0, 0);
            Vec3d startPos = mc.player.getPos();

            Vec3d direction = targetPos.subtract(startPos).normalize();

            if(threeBlocksDistance)
                targetPos = targetPos.subtract(direction.multiply(3));

            double dx = targetPos.x - startPos.x;
            double dy = targetPos.y - startPos.y;
            double dz = targetPos.z - startPos.z;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double maxStep = step.get();
            int steps = (int) Math.ceil(distance / maxStep);
            int delay_ = delay.get();

            new Thread(() -> {
                try {
                    for (int i = 1; i <= steps; i++) {
                        double t = (double) i / steps;
                        double x = startPos.x + dx * t;
                        double y = startPos.y + dy * t;
                        double z = startPos.z + dz * t;

                        mc.player.setPosition(x, y, z);
                        Thread.sleep(delay_);
                    }

                    if(entityToHit != null)
                    {
                        mc.getNetworkHandler().sendPacket(
                            PlayerInteractEntityC2SPacket.attack(entityToHit, mc.player.isSneaking())
                        );
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    if(entityToHit != null)
                        setPos(startPos, false, null);
                }
            }).start();
        }
}
