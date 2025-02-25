package com.example.addon.modules;

import com.example.addon.GrisUtils;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;


public class UseItemSpam extends Module {

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private boolean spamming = false;

    public UseItemSpam() {
        super(GrisUtils.CATEGORY, "item-use-spam", "Spam interact item packets. Send \"itemUseSpam\" in the chat to start spamming, send \"itemUseSpam stop\" to stop");
    }

    private final Setting<Double> sendPacketIterations = sgGeneral.add(new DoubleSetting.Builder()
        .name("iterations")
        .description("the number of times the use item packets is sent (-1 for infinite).")
        .defaultValue(1000)
        .sliderRange(-1, 10000)
        .build()
    );

    private final Setting<Double> spamDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("delay (millisecond)")
        .description("the delay between packets sent in millisecond.")
        .defaultValue(5)
        .sliderRange(1, 300)
        .build()
    );

    private final Setting<Boolean> isCancelSpawnEntityPackets = sgGeneral.add(new BoolSetting.Builder()
        .name("Cancel spawn entity packets from the server.")
        .description("Recommended to reduce lag, but may cause desynchronization with the server (rejoin the server to sync again).")
        .build()
    );

    @EventHandler
    private void onChatMessage(SendMessageEvent event) {
        if (event.message.equals("itemUseSpam") || event.message.equals("itemUseSpam start")) {
            event.cancel();
            spamming = true;
            startSpamming();
            mc.inGameHud.getChatHud().addMessage(Text.of("Spamming use item packets..."));
        } else if (event.message.equals("itemUseSpam stop")) {
            event.cancel();
            spamming = false;
            mc.inGameHud.getChatHud().addMessage(Text.of("Use item spam stopped."));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onReceivePacket(PacketEvent.Receive event)
    {
        if (spamming && isCancelSpawnEntityPackets.get()) {
            if (event.packet.getClass().getSimpleName().equals("EntitySpawnS2CPacket")) {
                event.cancel();
            }
        }
    }

    private void startSpamming() {
        if (mc != null && mc.player != null) {
            new Thread(() -> {
                for (int i = sendPacketIterations.get().intValue(); i != 0; i--) {
                    if (mc == null || mc.player == null || !spamming) {spamming = false; break;}

                    mc.execute(() -> {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    });
                    try {Thread.sleep(spamDelay.get().intValue());}
                    catch (InterruptedException ignored) {}
                }
            }).start();
        }
    }
}
