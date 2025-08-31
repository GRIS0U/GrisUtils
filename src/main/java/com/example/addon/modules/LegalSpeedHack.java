package com.example.addon.modules;

import com.example.addon.GrisUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;

public class LegalSpeedHack extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private boolean forcedLeft = false;

    private FreeLook freeLook = Modules.get().get(FreeLook.class);
    Float previousFreeLookCamYaw;
    private boolean yawTweaked = false;
    private boolean airYawTweaked = false;

    public static boolean freezeCamera = false;

    public LegalSpeedHack() {
        super(GrisUtils.CATEGORY, "legal-speed-hack", "Boost ur speed legally");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        // États des touches
        boolean forward = mc.options.forwardKey.isPressed();
        boolean leftPhysical = isKeyDown(mc.options.leftKey);
        boolean rightPhysical = isKeyDown(mc.options.rightKey);
        boolean backPhysical = isKeyDown(mc.options.backKey);

        Entity cameraEntity = mc.getCameraEntity();

        if (forward && !leftPhysical && !rightPhysical && !backPhysical) {
            mc.options.leftKey.setPressed(true);
            forcedLeft = true;
            if(!yawTweaked)
            {
                freezeCamera = true;
                cameraEntity.setYaw(cameraEntity.getYaw() + 45f);

                boolean previousTogglePerspective = freeLook.togglePerspective.get();
                Double previousSens = freeLook.sensitivity.get();
                freeLook.togglePerspective.set(false);

                freeLook.sensitivity.set(0.0);
                if(!freeLook.isActive()) freeLook.toggle();
                freeLook.cameraYaw = freeLook.cameraYaw - 45;
                yawTweaked = true;

                freeLook.sensitivity.set(previousSens);
                freeLook.togglePerspective.set(previousTogglePerspective);
                freezeCamera = false;
            }
            else
            {
                if (!airYawTweaked && mc.options.jumpKey.isPressed() && !mc.player.isOnGround()) {
                    cameraEntity.setYaw(cameraEntity.getYaw() - 12.35f);
                    airYawTweaked = true;
                }
                else if (airYawTweaked && !mc.options.jumpKey.isPressed())
                {
                    cameraEntity.setYaw(cameraEntity.getYaw() + 12.35f);
                    airYawTweaked = false;
                }
                if(previousFreeLookCamYaw != null)
                    cameraEntity.setYaw(cameraEntity.getYaw() - (previousFreeLookCamYaw - freeLook.cameraYaw));
                cameraEntity.setPitch(freeLook.cameraPitch);
                previousFreeLookCamYaw = freeLook.cameraYaw;
            }
        }
        else {
            // Relâche gauche seulement si on l'avait forcée
            if (forcedLeft && !leftPhysical) mc.options.leftKey.setPressed(false);
            forcedLeft = false;
            if(!yawTweaked) return;
            cameraEntity.setYaw(freeLook.cameraYaw);
            cameraEntity.setPitch(freeLook.cameraPitch);
            if(freeLook.isActive()) freeLook.toggle();
            yawTweaked = false;
            previousFreeLookCamYaw = null;
        }
    }

    private boolean isKeyDown(KeyBinding kb) {
        long handle = mc.getWindow().getHandle();
        return InputUtil.isKeyPressed(handle, kb.getDefaultKey().getCode());
    }
}
