package edu.unl.csce466.screens;

import com.mojang.blaze3d.vertex.PoseStack;

import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ImGuiScreen extends Screen{
	
	private static ImGuiScreen _INSTANCE = null;

	boolean _buttonClicked = false;
	
	public static ImGuiScreen getInstance() {
		if(_INSTANCE == null) { _INSTANCE = new ImGuiScreen(); }
		return _INSTANCE;
	}

	private ImGuiScreen() {
		super(Component.literal("ImGui"));
	}
	
	public void init() {
	}
	
	public void render(PoseStack proseStack, int x, int y, float partialTicks) {
		ImGuiRenderer.getInstance().draw(()->{
			ImGui.showDemoWindow();
		});
		
		ImGuiRenderer.getInstance().draw(() -> {
			ImGui.begin("Custom Window");
				ImGui.text("Example WIndow");
				if(ImGui.button("Click Me!")) {
					_buttonClicked = true;
				}
				
				if(_buttonClicked) {
					ImGui.text("Button has been clicked!");
				}
			ImGui.end();
		});
	}
}
