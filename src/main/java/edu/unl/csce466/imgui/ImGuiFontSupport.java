package edu.unl.csce466.imgui;

import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.ImGuiIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImGuiFontSupport {
    private static final String FONT_RESOURCE = "assets/csce466/fonts/Roboto-Regular.ttf";
    private static final float FONT_SIZE = 16.0f;

    private ImGuiFontSupport() {
    }

    public static void installFonts() {
        ImGuiIO io = ImGui.getIO();

        byte[] fontBytes = readResource(FONT_RESOURCE);

        ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder();
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesDefault());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesCyrillic());

        ImFontConfig config = new ImFontConfig();
        config.setMergeMode(true);

        short[] ranges = rangesBuilder.buildRanges();
        io.getFonts().addFontDefault();
        io.getFonts().addFontFromMemoryTTF(fontBytes, FONT_SIZE, config, ranges);
        io.getFonts().build();

        config.destroy();
    }

    private static byte[] readResource(String path) {
        InputStream inputStream = ImGuiFontSupport.class.getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalStateException("Missing font resource: " + path);
        }

        try (InputStream in = inputStream; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[16_384];
            int read;
            while ((read = in.read(data)) != -1) {
                buffer.write(data, 0, read);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font resource: " + path, e);
        }
    }
}
