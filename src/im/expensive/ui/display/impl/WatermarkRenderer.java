package im.expensive.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.expensive.Expensive;
import im.expensive.events.EventDisplay;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.ui.styles.Style;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.font.Fonts;
import im.expensive.utils.text.GradientUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WatermarkRenderer implements ElementRenderer {

    final ResourceLocation logo = new ResourceLocation("astral/images/hud/logo.png"); // Логотип
    final ResourceLocation user = new ResourceLocation("astral/images/hud/user.png"); // Иконка пользователя
    float animationOffset = 0; // Для анимации

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        float posX = 500; // Позиция X
        float posY = 200; // Позиция Y
        float padding = 5; // Отступы
        float fontSize = 8f; // Размер текста
        float iconSize = 12; // Размер иконки
        Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();
        String login = "baveko"; /* UserData.getUser()*/
        String uid = "99"; /* UserData.getUid()*/


        // Анимация плавного смещения
        animationOffset += 0.01f;
        if (animationOffset > 100) animationOffset = 0;
        //rect
        DisplayUtils.drawRoundedRect(posX +2,posY -1, 142 + Fonts.sfui.getWidth(login + uid + mc.debugFPS,7), 18, 6, ColorUtils.getColor(90)); // Обводка
        DisplayUtils.drawRoundedRect(posX + 3,posY, 140 + Fonts.sfui.getWidth(login + uid + mc.debugFPS,7), 16, 6, ColorUtils.rgb(0,0,0)); // Обводка
        //mini rect
        DisplayUtils.drawRoundedRect(posX + 60,posY + 4.5f, 1, 8, 0, ColorUtils.rgb(211,211,211)); // Обводка
        DisplayUtils.drawRoundedRect(posX + 92 + Fonts.sfui.getWidth(login,7),posY + 4.5f, 1.2f, 8, 0, ColorUtils.rgba(211,211,211, 160)); // Обводка
         DisplayUtils.drawRoundedRect(posX + 116 + Fonts.sfui.getWidth(login + uid,7),posY + 4.5f, 1.2f, 8, 0, ColorUtils.rgba(211,211,211, 160)); // Обводка


        Fonts.sfui.drawText(ms, "astral client      login: " + login , posX + 8, posY + padding ,
                ColorUtils.rgb(255, 255, 255), 7);
      Fonts.sfui.drawText(ms, "uid: " + uid, posX + Fonts.sfui.getWidth("astral client      login: " + login, 7) + 15, posY + padding ,
                ColorUtils.rgb(255, 255, 255), 7);
        Fonts.sfui.drawText(ms, "fps: " + mc.debugFPS, posX + Fonts.sfui.getWidth("astral client      login: " + login + "uid: " + uid , 7) + 21, posY + padding ,
                ColorUtils.rgb(255, 255, 255), 7);

    }

    private void drawStyledRect(float x, float y, float width, float height, float radius) {
        // Рисуем скруглённый прямоугольник с обводкой
        DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f, ColorUtils.getColor(0)); // Обводка
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(21, 21, 21, 255)); // Фон
    }
}