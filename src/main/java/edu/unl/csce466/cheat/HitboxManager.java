package edu.unl.csce466.cheat;

public class HitboxManager {
    public static boolean enabled = true;
    public static float width = 0.6f;
    public static float height = 1.8f;
    public static boolean throughWalls = true;
    public static float[] color = {1.0f, 0.28f, 0.28f, 1.0f};

    public static void reset() {
        enabled = true;
        width = 0.6f;
        height = 1.8f;
        throughWalls = true;
        color[0] = 1.0f; color[1] = 0.28f; color[2] = 0.28f; color[3] = 1.0f;
    }

    // Здесь можно добавить save/load в json
    public static void save() {
        // TODO
    }
}
