package com.ninjagoldfinch.nz.ninja_utils.mixin.client;

import com.ninjagoldfinch.nz.ninja_utils.features.stats.TPSTracker;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts all incoming packets for TPS tracking.
 * Sets a flag that TPSTracker checks each client tick to count server activity.
 */
@Mixin(ClientConnection.class)
public class WorldTimeUpdateMixin {
    @Inject(
        method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
        at = @At("HEAD")
    )
    private void onPacketReceived(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        TPSTracker.INSTANCE.onPacketReceived();
    }
}
