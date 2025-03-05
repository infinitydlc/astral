package im.expensive.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.expensive.Expensive;
import im.expensive.events.EventDisplay;
import im.expensive.functions.api.Function;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.ui.styles.Style;
import im.expensive.utils.client.KeyStorage;
import im.expensive.utils.drag.Dragging;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.Scissor;
import im.expensive.utils.render.font.Fonts;
import im.expensive.utils.text.GradientUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class KeyBindRenderer implements ElementRenderer {

    final Dragging dragging;

    float width;
    float height;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;

        String name ="KeyBinds";

        Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();
        ResourceLocation backgroundTexture = new ResourceLocation("astral/images/hud/K.png");

        
        drawStyledRect(posX, posY, width, height, 3);
        posY += 1f;

        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        Fonts.sfui.drawCenteredText(ms, name, posX  + 21, posY + padding + 0.5f, -1,fontSize);
        DisplayUtils.drawImage(backgroundTexture, posX +width -13, posY + 5, 9, 8, ColorUtils.rgb(93, 84, 165));

        posY += fontSize + padding * 2;

        float maxWidth = Fonts.sfMedium.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;

        
        posY += 3f;

        
        for (Function f : Expensive.getInstance().getFunctionRegistry().getFunctions()) {
            f.getAnimation().update();
            if (!(f.getAnimation().getValue() > 0) || f.getBind() == 0) continue;

            
            float bindRectX = posX + padding -2;
            float bindRectY = posY - 3;
            float bindRectWidth = width - padding * 1.4f;
            float bindRectHeight = fontSize + padding +1;

            
            DisplayUtils.drawRoundedRect(bindRectX, bindRectY, bindRectWidth, bindRectHeight, 0.5f, ColorUtils.rgba(21, 21, 31, 255));

            String nameText = f.getName();
            float nameWidth = Fonts.sfMedium.getWidth(nameText, fontSize);

            String bindText = "[" + KeyStorage.getKey(f.getBind()) + "]";
            float bindWidth = Fonts.sfMedium.getWidth(bindText, fontSize);

            float localWidth = nameWidth + bindWidth + padding * 3;

            
            Fonts.sfMedium.drawText(ms, nameText, posX + padding + 2 -2.5f, posY + 0.5f, ColorUtils.rgba(210, 210, 210, (int) (255 * f.getAnimation().getValue())), fontSize);
            Fonts.sfMedium.drawText(ms, bindText, posX + width - padding - bindWidth - 2, posY + 0.5f, ColorUtils.rgba(210, 210, 210, (int) (255 * f.getAnimation().getValue())), fontSize);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (float) ((fontSize + padding) * f.getAnimation().getValue());
            localHeight += (float) ((fontSize + padding) * f.getAnimation().getValue());
        }

        Scissor.unset();
        Scissor.pop();

        width = Math.max(maxWidth, 80);
        height = localHeight + 2.5f;
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private void drawStyledRect(float x, float y, float width, float height, float radius) {
        DisplayUtils.drawShadow(x, y, width, height, (int) (radius * 2f), ColorUtils.rgba(25, 25, 31, 120));
        DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height +1, radius, ColorUtils.rgba(32, 41, 83,255));
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(25, 25, 31,255));
    }
}