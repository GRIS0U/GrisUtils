package com.example.addon.modules;

import com.example.addon.GrisUtils;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.*;

public class PacketDelayer extends Module
{
    private boolean delayingPackets = false;

    public static PacketDelayer instance;

    List<Packet> delayedPackets = new ArrayList();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public PacketDelayer()
    {
        super(GrisUtils.CATEGORY, "packet-delayer", "Delay selected packets. Type \"delay\" to start delaying and \"send\" to send delayed packets.");
        instance = this;
    }

    private final Setting<Set<Class<? extends Packet<?>>>> c2sPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("C2S-packets")
        .description("Client-to-server packets to delay.")
        .filter(aClass -> PacketUtils.getC2SPackets().contains(aClass))
        .build()
    );

    private final Setting<Boolean> sendAndDisconnect = sgGeneral.add(new BoolSetting.Builder()
        .name("send and disconnect")
        .description("Disconnect you just after sending packets.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> sendPacketsAndSendCommand = sgGeneral.add(new StringSetting.Builder()
        .name("send-packets-and-send-command")
        .description("Send a command you just after sending packets. Leave empty to not send any command.")
        .defaultValue("")
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
            SendDelayedPackets();
        }
        if (event.message.equals("delay")) {
            event.cancel();
            delayingPackets = true;
            mc.inGameHud.getChatHud().addMessage(Text.of("Delaying packets."));
        }

        if (event.message.equals("disc") || event.message.equals("disconnect"))
        {
            event.cancel();
            if(delayingPackets || !delayedPackets.isEmpty())
            {
                SendDelayedPackets();
                mc.inGameHud.getChatHud().addMessage(Text.of("Disconnecting and sending delayed packets."));
                assert mc.world != null;
                mc.world.disconnect();
            }
            else
            {
                mc.inGameHud.getChatHud().addMessage(Text.of("You not delayed any packets, you will not get disconnected"));
            }
        }
    }

    public void SendDelayedPackets()
    {
        delayingPackets = false;

        if(delayedPackets.isEmpty()) {
            mc.inGameHud.getChatHud().addMessage(Text.of("Sent 0 packets."));
            return;
        }

        if (mc.getNetworkHandler() == null) return;

        List<Packet> packetsToSend = new ArrayList<>(delayedPackets);
        delayedPackets.clear();

        for (Packet<?> packet : packetsToSend)
        {
            mc.getNetworkHandler().sendPacket(packet);
        }
        String cmd = sendPacketsAndSendCommand.get();
        if (!cmd.isEmpty()) {
            if (cmd.startsWith("/")) {
                cmd = cmd.substring(1);
            }

            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendChatCommand(cmd);
            }
        }
        if(sendAndDisconnect.get())
        {
            assert mc.world != null;
            mc.world.disconnect();
        }
        mc.inGameHud.getChatHud().addMessage(Text.of("Sent " + packetsToSend.size() + " packets."));
    }
}
