package edu.unl.csce466.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import org.apache.logging.log4j.LogManager;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.flag.ImGuiConfigFlags;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {

    // initRenderer в 1.16.5 (official mappings) имеет тот же сигнатурный вид,
    // что и в 1.19.2, поэтому mixin работает без изменений по имени метода.
    @Inject(at = @At(value = "TAIL"), method = "initRenderer", remap = true)
    private static void initRenderer(CallbackInfo cbi) {
        // 1.16.5: assertOnRenderThread() переименован в assertOnGameThread()
        RenderSystem.assertOnGameThread();

        // 1.16.5: LogUtils нет (появился в 1.18+) -> используем log4j напрямую
        LogManager.getLogger().info("Initializing ImGui");

        ImGuiRenderer.getInstance().init(() -> {
            // Viewports отключены - убрано полностью, чтобы окна не вылазили и не крашились
            // ImGui.getIO().addConfigFlags(ImGuiConfigFlags.ViewportsEnable);  <- УДАЛЕНО

            // Docking оставляем (для вкладок внутри окна). Если не нужен - закомментируй
            ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable);
        });
    }

    @Inject(at = @At(value = "HEAD"), method = "flipFrame(J)V")
    private static void flipFrame(long p_69496_, CallbackInfo cbi) {
        RenderSystem.recordRenderCall(() -> {
            ImGuiRenderer.getInstance().render();
        });
    }
}
