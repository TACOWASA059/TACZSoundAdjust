/*
 * このファイルは [TACZ] (GPL 3.0) を基に改変されています。
 *
 * 改変者: tacowasa_059
 * 改変日: 2025-02-24
 *
 * 本ファイルは GNU General Public License v3.0 (GPL 3.0) に従って配布されます。
 * ライセンスの詳細については `LICENSE` ファイルを参照してください。
 */

package com.github.tacowasa059.taczsoundadjust.mixin;

import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.sound.GunSoundInstance;
import sun.misc.Unsafe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(GunSoundInstance.class)
public class GunSoundInstanceMixin {

    @Unique
    private static final Unsafe taczSoundAdjust$UNSAFE = taczSoundAdjust$getUnsafe();

    @Unique
    private static Unsafe taczSoundAdjust$getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Unsafe cannot be accessed!", e);
        }
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void onInit(SoundEvent soundEvent, SoundSource source, float volume, float pitch, Entity entity, int soundDistance, ResourceLocation registryName, boolean mono, CallbackInfo ci){
        if(registryName == null) return;

        String nameSpace = registryName.getNamespace();
        String path = registryName.getPath();
        if(ClientAssetsManager.INSTANCE.getSoundBuffers(registryName) != null) return;

        ResourceLocation newName = taczSoundAdjust$processPath(registryName, nameSpace, path);
        if(newName==null)return;
        if(newName.getPath().equalsIgnoreCase(registryName.getPath()))return;

        GunSoundInstance instance = (GunSoundInstance) (Object) this;

        try {
            Field field = GunSoundInstance.class.getDeclaredField("registryName");
            field.setAccessible(true);
            taczSoundAdjust$UNSAFE.putObject(instance, taczSoundAdjust$UNSAFE.objectFieldOffset(field), newName);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Unique
    private ResourceLocation taczSoundAdjust$processPath(ResourceLocation location, String namespace, String path) {
        if (ClientAssetsManager.INSTANCE.getSoundBuffers(location) != null) {
            return location;
        }

        if (path.endsWith("silence_3p")) {
            return taczSoundAdjust$tryAlternative(namespace, path.replace("silence_3p", "silence"), path.replace("silence_3p", "shoot_3p"),path.replace("silence_3p", "shoot"));
        } else if (path.endsWith("shoot_3p")) {
            return taczSoundAdjust$tryAlternative(namespace, path.replace("shoot_3p", "shoot"));
        } else if (path.endsWith("shoot")) {
            return taczSoundAdjust$tryAlternative(namespace, path.replace("shoot", "shoot_3p"));
        } else if (path.endsWith("silence")){
            return taczSoundAdjust$tryAlternative(namespace, path.replace("silence", "silence_3p"), path.replace("silence", "shoot"),path.replace("silence", "shoot_3p"));
        }

        return location;
    }

    @Unique
    private ResourceLocation taczSoundAdjust$tryAlternative(String namespace, String... alternatives) {
        for (String alt : alternatives) {
            ResourceLocation sound = new ResourceLocation(namespace, alt);
            if (ClientAssetsManager.INSTANCE.getSoundBuffers(sound) != null) {
                return sound;
            }
        }
        return null;
    }
}
