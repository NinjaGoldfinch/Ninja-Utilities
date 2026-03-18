package com.ninjagoldfinch.nz.ninja_utils.mixin.client;

import com.ninjagoldfinch.nz.ninja_utils.features.stats.PingTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts PingResultS2CPacket to measure round-trip ping time.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class PingMeasurerMixin {
    @Inject(method = "onPingResult", at = @At("HEAD"))
    private void onPingResult(PingResultS2CPacket packet, CallbackInfo ci) {
        PingTracker.INSTANCE.onPingResult(packet.startTime());
    }
}
