package com.example.addon.modules;

import com.example.addon.GrisUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BreakBlockWhileUIOpened extends Module {

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private boolean showSendPacketBeforeBreaking = false;

    private PacketDelayer packetDelayer = PacketDelayer.instance;

    public BreakBlockWhileUIOpened()
    {
        super(GrisUtils.CATEGORY, "Break Block Button On Container UI", "Add a button to break a block while an UI is opened.");
    }

    private final Setting<Boolean> breakAndSendPacket = sgGeneral.add(new BoolSetting.Builder()
        .name("break and send packets")
        .description("(This wont do anything if the delay packet module is disabled) If enabled, it will send packets just after breaking the target block.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> sendPacketBeforeBreaking = sgGeneral.add(new BoolSetting.Builder()
        .name("send packets and break")
        .description("Send packets before the server breaks the target block.")
        .defaultValue(false)
        .visible(() -> showSendPacketBeforeBreaking)
        .build()
    );

    public static BlockPos blockToBreak;

    @EventHandler
    private void onTick(TickEvent.Post event)
    {
        showSendPacketBeforeBreaking = breakAndSendPacket.get();
        if(!showSendPacketBeforeBreaking)
        {
            sendPacketBeforeBreaking.set(false);
        }

        if(blockToBreak != null)
        {
            BlockState blockState = mc.world.getBlockState(blockToBreak);

            if(!blockState.isAir() && blockState.getBlock() != Blocks.WATER && blockState.getBlock() != Blocks.LAVA
                && mc.player.getPos().distanceTo(Vec3d.ofCenter(blockToBreak)) < 4.5)
            {
                mc.interactionManager.updateBlockBreakingProgress(blockToBreak, Direction.DOWN);
            }
            else
            {
                blockToBreak = null;
                if(breakAndSendPacket.get() && !sendPacketBeforeBreaking.get())
                {
                    //send at the moment where the server said that the block is destroyed to the client
                    packetDelayer.SendDelayedPackets();
                }
            }
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerActionC2SPacket packet) {
            if (packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK && blockToBreak != null) {
                BlockPos pos = packet.getPos();
                if(pos.equals(blockToBreak) && sendPacketBeforeBreaking.get())
                {
                    packetDelayer.SendDelayedPackets();
                    blockToBreak = null;
                }
            }
        }
    }
}
