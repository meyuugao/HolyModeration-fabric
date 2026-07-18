package me.yuugao.holymoderation.client.gui.drawable;

import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

public interface MouseScrollable {
    void onMouseScroll(double dx, double dy, int x, int y);
}