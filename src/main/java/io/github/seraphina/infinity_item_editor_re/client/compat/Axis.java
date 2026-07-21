package io.github.seraphina.infinity_item_editor_re.client.compat;

import com.mojang.math.Vector3f;

/**
 * 1.20-style axis names backed by the 1.18 Vector3f constants.
 */
public final class Axis {
    public static final Vector3f XP = Vector3f.XP;
    public static final Vector3f XN = Vector3f.XN;
    public static final Vector3f YP = Vector3f.YP;
    public static final Vector3f YN = Vector3f.YN;
    public static final Vector3f ZP = Vector3f.ZP;
    public static final Vector3f ZN = Vector3f.ZN;

    private Axis() {
    }
}
