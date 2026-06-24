package edu.unl.csce466.mixins;

import edu.unl.csce466.cheat.HitboxManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    /**
     * 1.16.5 official:
     * Entity.getDimensions(Pose)
     */
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void csce466_getDimensions(Pose pose, CallbackInfoReturnable<EntitySize> cir) {
        if (!HitboxManager.enabled) return;
        Entity self = (Entity)(Object)this;
        if (!(self instanceof LivingEntity)) return;

        // Не трогаем локального игрока, иначе камера/коллизии ломаются
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && self.getId() == mc.player.getId()) return;

        EntitySize original = cir.getReturnValue();
        if (original == null) return;

        float w = HitboxManager.width;
        float h = HitboxManager.height;

        if (Math.abs(w - original.width) < 0.001f && Math.abs(h - original.height) < 0.001f) return;

        cir.setReturnValue(EntitySize.scalable(w, h));
    }

    /**
     * Самое главное для визуала и аима.
     * F3+B и pickEntity используют именно это.
     */
    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void csce466_getBoundingBox(CallbackInfoReturnable<AxisAlignedBB> cir) {
        if (!HitboxManager.enabled) return;
        Entity self = (Entity)(Object)this;
        if (!(self instanceof LivingEntity)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && self.getId() == mc.player.getId()) return;

        AxisAlignedBB original = cir.getReturnValue();
        if (original == null) return;

        // Текущие размеры из бокса
        double curW = original.getXsize();
        double curH = original.getYsize();

        float targetW = HitboxManager.width;
        float targetH = HitboxManager.height;

        // Если уже совпадает - ничего не делаем
        if (Math.abs(curW - targetW) < 0.001 && Math.abs(curH - targetH) < 0.001) return;

        // Центр бокса
        double cx = (original.minX + original.maxX) * 0.5;
        double cy = original.minY; // низ - ноги
        double cz = (original.minZ + original.maxZ) * 0.5;

        double hw = targetW * 0.5;
        AxisAlignedBB expanded = new AxisAlignedBB(
                cx - hw, cy, cz - hw,
                cx + hw, cy + targetH, cz + hw
        );

        cir.setReturnValue(expanded);
    }
}
