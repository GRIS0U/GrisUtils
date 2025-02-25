package com.example.addon.modules;

import com.example.addon.GrisUtils;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

import java.util.*;

public class PacketDelayer extends Module
{
    private boolean delayingPackets = false;

    List<Packet> delayedPackets = new ArrayList();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public PacketDelayer()
    {
        super(GrisUtils.CATEGORY, "packet-delayer", "Delay selected packets. Type \"delay\" to start delaying and \"send\" to send delayed packets.");
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
        if (event.message.equals("send")) {
            event.cancel();
            delayingPackets = false;

            if(delayedPackets.isEmpty()) {
                mc.inGameHud.getChatHud().addMessage(Text.of("Sent 0 packets."));
                return;
            }

            if (mc.getNetworkHandler() == null) return;

            List<Packet> packetsToSend = new ArrayList<>(delayedPackets);
            delayedPackets.clear();

            for (Packet<?> packet : packetsToSend) {
                mc.getNetworkHandler().sendPacket(packet);
            }

            mc.inGameHud.getChatHud().addMessage(Text.of("Sent " + packetsToSend.size() + " packets."));
        }
        if (event.message.equals("delay")) {
            event.cancel();
            delayingPackets = true;
            mc.inGameHud.getChatHud().addMessage(Text.of("Delaying packets."));
        }
    }
}
