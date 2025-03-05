package im.expensive.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.events.EventDisplay;
import im.expensive.utils.drag.Dragging;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ArmorRenderer implements ElementRenderer {

    final ResourceLocation logo = new ResourceLocation("astral/images/hud/setting.png");

    final Dragging dragging;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        Minecraft mc = Minecraft.getInstance();

        float width = 65; 
        float height = 32;  

        float iconSizeX = 8;
        float iconSizeY = 8;

        float x = dragging.getX();
        float y = dragging.getY();

        float padding = 5;

        drawStyledRect(x, y, width, height + 1, 3);

        float imagePosX = x + width - iconSizeX - padding;
        DisplayUtils.drawImage(logo, imagePosX, y + 3.7f, iconSizeX, iconSizeY, ColorUtils.rgb(129, 135, 255));

        Fonts.sfui.drawText(ms, "Armor", x + 5, y + 5, -1, 6.5f);

        int posX = (int) x -9;
        int posY = (int) y + 12; 

        
        for (ItemStack itemStack : mc.player.getArmorInventoryList()) {
            if (!itemStack.isEmpty()) {
                
                mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX + 12, posY);

                
                float damagePercentage = (itemStack.getDamage() * 100.0f) / itemStack.getMaxDamage();
                int red = (int) (255 * (damagePercentage / 100));
                int green = 255 - red;

                
                int barWidth = Math.round((8 * (100 - damagePercentage)) / 100); 
                DisplayUtils.drawRoundedRect(posX + 16, posY + 16, 8, 2, 1, ColorUtils.rgb(15, 15, 15)); 
                DisplayUtils.drawRoundedRect(posX + 16, posY + 16, barWidth, 2, 1, ColorUtils.rgb(red, green, 0)); 
                DisplayUtils.drawShadow(posX + 16, posY + 16, barWidth, 2, 4, ColorUtils.rgb(red, green, 0)); 
            } else {
                
                DisplayUtils.drawRoundedRect(posX + 16, posY + 16, 8, 2, 1, ColorUtils.rgb(135, 136, 148));
            }

            posX += 15;
        }
        dragging.setWidth(width);
        dragging.setHeight(height);
    }
    private void drawStyledRect(float x, float y, float width, float height, float radius) {
        DisplayUtils.drawShadow(x, y, width, height, (int) (radius * 2f), ColorUtils.rgba(25, 25, 31, 120));
        DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height +1, radius, ColorUtils.rgba(32, 41, 83,255));
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(25, 25, 31,255));
    }
}