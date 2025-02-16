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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WatermarkRenderer implements ElementRenderer {

    final ResourceLocation logo = new ResourceLocation("astral/images/hud/logo.png");
    private final ResourceLocation user = new ResourceLocation("astral/images/hud/user.png");

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        float posX = 500;
        float posY = 124;
        float padding = 5;
        float fontSize = 6.5f;
        float iconSize = 10;
        Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();

        DisplayUtils.drawShadow(posX, posY, iconSize + padding * 2, iconSize + padding * 2, 10, style.getFirstColor().getRGB(), style.getSecondColor().getRGB());
        drawStyledRect(posX, posY, iconSize + padding * 2, iconSize + padding * 2, 4);
        DisplayUtils.drawImage(logo, posX + padding, posY + padding, iconSize, iconSize, ColorUtils.rgb(255, 255, 255));

        ITextComponent text = GradientUtil.gradient("Astral client");

        float textWidth = Fonts.sfui.getWidth(text, fontSize);

        float localPosX = posX + iconSize + padding * 3;

        DisplayUtils.drawShadow(localPosX, posY, iconSize + padding * 2.5f + textWidth, iconSize + padding * 2, 10, style.getFirstColor().getRGB(), style.getSecondColor().getRGB());
        drawStyledRect(localPosX, posY, iconSize + padding * 2.5f + textWidth, iconSize + padding * 2, 4);
        DisplayUtils.drawImage(user, localPosX + padding, posY + padding, iconSize, iconSize, ColorUtils.rgb(255, 255, 255));

        Fonts.sfui.drawText(ms, text, localPosX + iconSize + padding * 1.5f - 1, posY + iconSize / 2 + 1.5f, fontSize, 255);
    }

    private void drawStyledRect(float x,
                                float y,
                                float width,
                                float height,
                                float radius) {

        DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f, ColorUtils.getColor(0)); // outline
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(21, 21, 21, 255));
    }
}
