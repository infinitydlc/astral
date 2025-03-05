package im.expensive.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.expensive.Expensive;
import im.expensive.events.EventDisplay;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.ui.styles.Style;
import im.expensive.utils.drag.Dragging;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.Scissor;
import im.expensive.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.ResourceLocation;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PotionRenderer implements ElementRenderer {

    final Dragging dragging;

    float width;
    float height;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 7;
        float padding = 3;

        String name = I18n.format("Potions");

        Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();

        drawStyledRect(posX, posY -1.5f, width, height +3, 3);
        posY += 1f;

        ResourceLocation backgroundTexture = new ResourceLocation("astral/images/hud/P.png");
        DisplayUtils.drawImage(backgroundTexture, posX +width -13, posY + 3, 9, 8, ColorUtils.rgb(93, 84, 165));

        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        Fonts.sfMedium.drawCenteredText(ms, name, posX + width / 2 -20, posY + padding +0.5f, ColorUtils.rgb(255, 255, 255), 7);

        posY += fontSize + padding * 2;

        float maxWidth = Fonts.sfMedium.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;

        posY += 1f;

        for (EffectInstance effectInstance : mc.player.getActivePotionEffects()) {
            int amplifier = effectInstance.getAmplifier();

            String amplifierStr = "";
            if (amplifier >= 1 && amplifier <= 9) {
                amplifierStr = " " + I18n.format("enchantment.level." + (amplifier + 1));
            }

            String effectName = I18n.format(effectInstance.getEffectName()) + amplifierStr;
            float nameWidth = Fonts.sfMedium.getWidth(effectName, fontSize);

            String durationText = EffectUtils.getPotionDurationString(effectInstance, 1);
            float durationWidth = Fonts.sfMedium.getWidth(durationText, fontSize);


            float effectRectX = posX + padding;
            float effectRectY = posY -0.5f;
            float effectRectWidth = 74;
            float effectRectHeight = fontSize + padding +1;

            DisplayUtils.drawRoundedRect(effectRectX, effectRectY, effectRectWidth, effectRectHeight, 0.5f, ColorUtils.rgba(21, 21, 31, 255));

            TextureAtlasSprite potionSprite = mc.getPotionSpriteUploader().getSprite(effectInstance.getPotion());
            mc.getTextureManager().bindTexture(potionSprite.getAtlasTexture().getTextureLocation());
            DisplayEffectsScreen.blit(ms, (int) (posX + padding + 1), (int) posY + 1, 7, 8, 8, potionSprite);

            float localWidth = nameWidth + durationWidth + padding * 3;
            Fonts.sfbold.drawText(ms, effectName, posX + padding  + 10, posY + 2.5f, ColorUtils.rgb(255, 255, 255), 6);
            Fonts.sfbold.drawText(ms, durationText, posX + padding  +width  -21, posY + 2.55f, ColorUtils.rgb(255, 255, 255), 6);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += fontSize + padding;
            localHeight += fontSize + padding +0.3f;
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