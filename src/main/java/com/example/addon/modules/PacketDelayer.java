package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

import java.util.*;

public class PacketDelayer extends Module
{

    private boolean delayingPackets = false;

    List<Packet> delayedPackets = new ArrayList();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public PacketDelayer()
    {
        super(AddonTemplate.CATEGORY, "packet-delayer", "Delays selected packets.");
    }

    private final Setting<Set<Class<? extends Packet<?>>>> c2sPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("C2S-packets")
        .description("Client-to-server packets to delay.")
        .filter(aClass -> PacketUtils.getC2SPackets().contains(aClass))
        .build()
    );

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onSendPacket(PacketEvent.Send event)
    {
        if (delayingPackets) {
            if (c2sPackets.get().contains(event.packet.getClass())) {
                event.cancel();
                delayedPackets.add(event.packet);
            }
        }
    }

    @EventHandler
    private void onChatMessage(SendMessageEvent event) {
        if (!delayedPackets.isEmpty() && event.message.equals("send")) {
            event.cancel();

            if (MinecraftClient.getInstance().getNetworkHandler() == null) return;

            List<Packet> packetsToSend = new ArrayList<>(delayedPackets);

            delayedPackets.clear();
            delayingPackets = false;

            for (Packet<?> packet : packetsToSend) {
                MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
            }
        }
        if (event.message.equals("delay")) {
            delayingPackets = true;
            event.cancel();
        }
    }
}
