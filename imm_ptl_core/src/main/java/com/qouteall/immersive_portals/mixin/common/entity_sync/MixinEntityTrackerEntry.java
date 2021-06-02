package com.qouteall.immersive_portals.mixin.common.entity_sync;

import com.qouteall.immersive_portals.ducks.IEEntityTrackerEntry;
import com.qouteall.immersive_portals.network.CommonNetwork;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public abstract class MixinEntityTrackerEntry implements IEEntityTrackerEntry {
    @Shadow
    @Final
    private Entity entity;
    
    @Shadow
    public abstract void sendPackets(Consumer<Packet<?>> consumer_1);
    
    @Shadow
    protected abstract void storeEncodedCoordinates();
    
    // make sure that the packet is being redirected
    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void onTick(CallbackInfo ci) {
        CommonNetwork.validateForceRedirecting();
    }
    
    @Inject(
        method = "startTracking",
        at = @At("HEAD")
    )
    private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
        CommonNetwork.validateForceRedirecting();
    }
    
    @Inject(
        method = "stopTracking", at = @At("HEAD")
    )
    private void onStopTracking(ServerPlayerEntity player, CallbackInfo ci) {
        CommonNetwork.validateForceRedirecting();
    }
    
    @Redirect(
        method = "sendSyncPacket",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"
        )
    )
    private void onSendToWatcherAndSelf(
        ServerPlayNetworkHandler serverPlayNetworkHandler,
        Packet<?> packet_1
    ) {
        CommonNetwork.sendRedirectedPacket(serverPlayNetworkHandler, packet_1, entity.world.getRegistryKey());
    }
    
    @Override
    public void ip_updateTrackedEntityPosition() {
        storeEncodedCoordinates();
    }
}
