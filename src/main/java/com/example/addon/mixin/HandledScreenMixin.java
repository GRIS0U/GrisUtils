package com.example.addon.mixin;

import com.example.addon.modules.BreakBlockWhileUIOpened;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    @Shadow
    protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;

    protected HandledScreenMixin(Text title) { super(title); }

    Module module;

    @Inject(method = "init", at = @At("TAIL"))
    private void createButton(CallbackInfo info) {
        module = Modules.get().get("Break Block Button On Container UI");

        if(!module.isActive()) return;

        if (((HandledScreen<?>) (Object) this).getScreenHandler() instanceof PlayerScreenHandler) return;
        if (((HandledScreen<?>) (Object) this).getScreenHandler() instanceof CreativeInventoryScreen.CreativeScreenHandler) return;

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Break block"),
            btn -> breakBlock()
        ).dimensions(this.x + this.backgroundWidth + 10, this.y, 65, 20).build());
    }

    private void breakBlock() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.crosshairTarget instanceof BlockHitResult bhr && client.interactionManager != null) {
            BreakBlockWhileUIOpened.blockToBreak = bhr.getBlockPos();
        }
    }
}
