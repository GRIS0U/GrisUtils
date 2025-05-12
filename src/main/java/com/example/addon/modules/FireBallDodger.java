package com.example.addon.modules;

import com.example.addon.GrisUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;


public class FireBallDodger extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public FireBallDodger() {
        super(GrisUtils.CATEGORY, "fire-ball-dodger", "Try to dodge fire balls");
    }

    private final Setting<Double> detectionRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("detectionRadius")
        .description("The radius around the player in which fireballs are detected.")
        .defaultValue(25)
        .sliderRange(10, 100)
        .build()
    );
    private final Setting<Double>dodgeOffset = sgGeneral.add(new DoubleSetting.Builder()
        .name("dodgeOffset")
        .description("The teleportation offset to dodge each fireballs.")
        .defaultValue(1.5)
        .sliderRange(0.5, 10)
        .build()
    );

    private final Setting<Double>tickSimulation = sgGeneral.add(new DoubleSetting.Builder()
        .name("tickSimulation")
        .description("How many tick the fireball trajectory will be simulated.")
        .defaultValue(15)
        .sliderRange(1, 30)
        .build()
    );

    private final Setting<Double>playerBoxAproximation = sgGeneral.add(new DoubleSetting.Builder()
        .name("playerBoxAproximation")
        .description("How many the real player hit box is extended.")
        .defaultValue(0.3)
        .sliderRange(0.05, 1)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event)
    {
        DetectFireBalls();
    }
    private void DetectFireBalls() {
        List<SmallFireballEntity> fireballs = mc.world.getEntitiesByClass(
            SmallFireballEntity.class,
            mc.player.getBoundingBox().expand(detectionRadius.get()),
            fireball -> fireball != null && fireball.isAlive()
        );

        for (SmallFireballEntity fireball : fireballs) {
            Vec3d playerPos = mc.player.getPos();

            if (willHitPlayer(fireball, mc.player)) {
                Vec3d fireBallDir = fireball.getVelocity().normalize();

                Vec3d right = new Vec3d(-fireBallDir.z, 0, fireBallDir.x).normalize(); // droite (perpendiculaire)
                Vec3d left = right.multiply(-1);                      // gauche
                Vec3d up = new Vec3d(0, 2, 0);                         // au-dessus
                Vec3d down = new Vec3d(0, -2, 0);                      // en dessous

                Vec3d[] directions = new Vec3d[] { right, left, up, down };

                for (Vec3d dir: directions) {
                    Vec3d newDir = dir.multiply(dodgeOffset.get());
                    if(isSafe(playerPos.add(newDir))) {
                        mc.player.move(MovementType.SELF, newDir);
                        break;
                    }
                }
            }
        }
    }

    boolean isSafe(Vec3d pos) {
        Box box = mc.player.getBoundingBox().offset(pos.subtract(mc.player.getPos()));
        return mc.world.isSpaceEmpty(mc.player, box);
    }

    public boolean willHitPlayer(SmallFireballEntity fireball, PlayerEntity player) {
        Vec3d fireballPos = fireball.getPos();
        Vec3d velocity = fireball.getVelocity();
        Box playerBox = player.getBoundingBox().expand(playerBoxAproximation.get());

        double fireballSpeed = velocity.length();

        int ticksToSimulate = 20;
        double r = 0.2;

        for (int i = 0; i < ticksToSimulate; i++) {
            fireballPos = fireballPos.add(velocity.normalize().multiply(fireballSpeed));

            Box fireballBox = new Box(
                fireballPos.x - r, fireballPos.y - r, fireballPos.z - r,
                fireballPos.x + r, fireballPos.y + r, fireballPos.z + r
            );

            if (fireballBox.intersects(playerBox)) {
                return true;
            }
        }
        return false;
    }
}
