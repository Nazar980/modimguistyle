package edu.unl.csce466.mixins;

import edu.unl.csce466.cheat.HitboxManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    /**
     * 1.16.5 - official mappings:
     * Entity.getDimensions(Pose) -> EntitySize
     * 
     * Расширяем хитбокс для всех LivingEntity, кроме локального игрока.
     */
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true, remap = true)
    private void csce466_getDimensions(Pose pose, CallbackInfoReturnable<EntitySize> cir) {
        if (!HitboxManager.enabled) return;

        Entity self = (Entity)(Object)this;

        // Только LivingEntity (игроки/мобы), не трогаем предметы, стрелы и т.д.
        // Если хочешь и на всех - убери эту проверку
        if (!(self instanceof LivingEntity)) return;

        // Не расширяем своего игрока, иначе будешь застревать
        // if (self == net.minecraft.client.Minecraft.getInstance().player) return;

        EntitySize original = cir.getReturnValue();
        if (original == null) return;

        float w = HitboxManager.width;
        float h = HitboxManager.height;

        // Не трогаем если значения ванильные
        if (Math.abs(w - original.width) < 0.001f && Math.abs(h - original.height) < 0.001f) {
            return;
        }

        // EntitySize.scalable(width, height) - fixed = false
        // В 1.16.5 конструктор: EntitySize(float width, float height, boolean fixed)
        EntitySize expanded = EntitySize.scalable(w, h);
        cir.setReturnValue(expanded);
    }
}
