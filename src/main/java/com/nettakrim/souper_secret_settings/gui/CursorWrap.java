package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import com.mojang.blaze3d.platform.Window;
import com.nettakrim.souper_secret_settings.mixin.WindowAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

public interface CursorWrap {
    Vector2d offset = new Vector2d(0,0);

    // wrap cursor around edge of widget
    default double applyWrap(@NotNull MouseButtonEvent click, double deltaX, double deltaY) {
        AbstractWidget instance = (AbstractWidget)this;
        deltaX -= offset.x;
        deltaY -= offset.y;

        Window window = ClientData.minecraft.getWindow();
        int scale = window.getGuiScale();

        double x = 0;
        if (deltaX < 0 && click.x() < instance.getX() + 1) {
            x = instance.getWidth() - 2;
        }
        else if (deltaX > 0 && click.x() > instance.getRight() - 1) {
            x = 2 - instance.getWidth();
        }

        double y = 0;
        if (deltaY < 0 && click.y() < instance.getY() + 1) {
            y = instance.getHeight() - 2;
        }
        else if (deltaY > 0 && click.y() > instance.getBottom() - 1) {
            y = 2 - instance.getHeight();
        }

        //noinspection DataFlowIssue
        GLFW.glfwSetCursorPos(((WindowAccessor)(Object)window).getHandle(), (click.x() + x) * scale, (click.y() + y) * scale);
        offset.x = x;
        offset.y = y;

        // modify deltaX, since setting the cursor pos will add that offset to the next frames input
        return deltaX;
    }

    default void resetOffset() {
        offset.x = 0;
        offset.y = 0;
    }
}
