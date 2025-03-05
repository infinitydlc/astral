package im.expensive.ui.display.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import im.expensive.Expensive;
import im.expensive.events.EventDisplay;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.ui.styles.Style;
import im.expensive.utils.animations.Animation;
import im.expensive.utils.animations.Direction;
import im.expensive.utils.animations.impl.EaseBackIn;
import im.expensive.utils.client.ClientUtil;
import im.expensive.utils.drag.Dragging;
import im.expensive.utils.math.MathUtil;
import im.expensive.utils.math.StopWatch;
import im.expensive.utils.math.Vector4i;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.Scissor;
import im.expensive.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TargetInfoRenderer implements ElementRenderer {
    final StopWatch stopWatch = new StopWatch();
    final Dragging drag;
    LivingEntity entity = null;
    boolean allow;
    final Animation animation = new EaseBackIn(400, 1, 1);
    float healthAnimation = 0.0f;
    float absorptionAnimation = 0.0f;

    @Override
    public void render(EventDisplay eventDisplay) {
        entity = getTarget(entity);

        float rounding = 6;
        boolean out = !allow || stopWatch.isReached(1000);
        animation.setDuration(out ? 400 : 300);
        animation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);

        if (animation.getOutput() == 0.0f) {
            entity = null;
        }

        if (entity != null) {
            String name = entity.getName().getString();

            float posX = drag.getX();
            float posY = drag.getY();

            float headSize = 28;
            float spacing = 5;

            float width = 172 / 1.5f;
            float height = 59 / 1.5f;
            drag.setWidth(width);
            drag.setHeight(height);
            float shrinking = 1.5f;
            Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));


            float hp = entity.getHealth();
            float maxHp = entity.getMaxHealth();
            String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();

            if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime")
                    && (header.contains("анархия") || header.contains("гриферский")) && entity instanceof PlayerEntity) {
                hp = score.getScorePoints();
                maxHp = 20;
            }
            healthAnimation = MathUtil.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);
            absorptionAnimation = MathUtil.fast(absorptionAnimation, MathHelper.clamp(entity.getAbsorptionAmount() / maxHp, 0, 1), 10);


            if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime")
                    && (header.contains("анархия") || header.contains("гриферский")) && entity instanceof PlayerEntity) {
                hp = score.getScorePoints();
                maxHp = 20;
            }


            float animationValue = (float) animation.getOutput();

            float halfAnimationValueRest = (1 - animationValue) / 2f;

            float testX = posX + (width * halfAnimationValueRest);
            float testY = posY + (height * halfAnimationValueRest);
            float testW = width * animationValue;
            float testH = height * animationValue;
            int windowWidth = ClientUtil.calc(mc.getMainWindow().getScaledWidth());

            GlStateManager.pushMatrix();
            Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();

            sizeAnimation(posX + (width / 2), posY + (height / 2), animation.getOutput());
            drawStyledRect(posX, posY, width, 15, rounding, 255);
            Scissor.push();
            Scissor.setFromComponentCoordinates(testX, testY, testW - 6, testH);
            //ник
            Fonts.sfbold.drawText(eventDisplay.getMatrixStack(), entity.getName().getString(), posX + headSize + spacing + spacing  -8, posY + spacing + 1 , ColorUtils.rgb(231,231,231), 7);

            Scissor.unset();
            Scissor.pop();

            Vector4i vector4i = new Vector4i(style.getFirstColor().getRGB(), style.getFirstColor().getRGB(), style.getSecondColor().getRGB(), style.getSecondColor().getRGB());
            posX -= 4.5f;

            DisplayUtils.drawRoundedRect(posX + headSize + spacing + spacing -2 -5.5f +0.5f, posY + height - spacing * 2 - 3 -3, (width - 38) +11, 10, 2, ColorUtils.rgb(32, 32, 32));
            DisplayUtils.drawRoundedRect(posX + headSize + spacing + spacing -5 + 0.5f, posY + height - spacing * 2 - 3, (width - 42) * healthAnimation + 10, 4,2, ColorUtils.rgb(92, 83, 166));

            posX +=4.5f;
            Fonts.sfbold.drawText(eventDisplay.getMatrixStack(), String.valueOf(((int) hp + (int) mc.player.getAbsorptionAmount())), posX +96, posY + spacing + 1, ColorUtils.rgb(88, 84, 172), 7);
            drawArmor(eventDisplay, posX + headSize + spacing + spacing -8, posY + height - spacing * 2 - 3 -3);
            GlStateManager.popMatrix();

        }
    }


    private LivingEntity getTarget(LivingEntity nullTarget) {
        LivingEntity auraTarget = Expensive.getInstance().getFunctionRegistry().getKillAura().getTarget();
        LivingEntity target = nullTarget;
        if (auraTarget != null) {
            stopWatch.reset();
            allow = true;
            target = auraTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            stopWatch.reset();
            allow = true;
            target = mc.player;
        } else {
            allow = false;
        }
        return target;
    }

    public void drawTargetHead(LivingEntity entity, float x, float y, float width, float height) {
        if (entity != null) {
            EntityRenderer<? super LivingEntity> rendererManager = mc.getRenderManager().getRenderer(entity);
            drawFace(rendererManager.getEntityTexture(entity), x, y, 8F, 8F, 8F, 8F, width, height, 64F, 64F, entity);
        }
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }

    public void drawFace(ResourceLocation res, float d,
                         float y,
                         float u,
                         float v,
                         float uWidth,
                         float vHeight,
                         float width,
                         float height,
                         float tileWidth,
                         float tileHeight,
                         LivingEntity target) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        mc.getTextureManager().bindTexture(res);
        float hurtPercent = (target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
        GL11.glColor4f(1, 1 - hurtPercent, 1 - hurtPercent, 1);
        AbstractGui.drawScaledCustomSizeModalRect(d, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }
    private void drawArmor(EventDisplay eventDisplay, float x, float y) {
        float spacing = 10f;
        float scale = 0.6f;
        float offsetX = 0;

        ItemStack helmet = entity.getItemStackFromSlot(EquipmentSlotType.HEAD);
        ItemStack chestplate = entity.getItemStackFromSlot(EquipmentSlotType.CHEST);
        ItemStack leggings = entity.getItemStackFromSlot(EquipmentSlotType.LEGS);
        ItemStack boots = entity.getItemStackFromSlot(EquipmentSlotType.FEET);
        ItemStack heldItem = entity.getHeldItemOffhand();
        ItemStack Item = entity.getActiveItemStack();

        boolean allEmpty = (helmet == null || helmet.isEmpty()) &&
                (chestplate == null || chestplate.isEmpty()) &&
                (leggings == null || leggings.isEmpty()) &&
                (boots == null || boots.isEmpty()) &&
                (Item == null || Item.isEmpty()) &&
                (heldItem == null || heldItem.isEmpty());

        if (allEmpty) {
            float textX = x - 0;
            float textY = y - 30;
        } else {
            if (helmet != null && !helmet.isEmpty()) {
                renderItemStack(eventDisplay, helmet, x + offsetX, y - 33, scale);
                offsetX += spacing;
            }
            if (chestplate != null && !chestplate.isEmpty()) {
                renderItemStack(eventDisplay, chestplate, x + offsetX, y - 33, scale);
                offsetX += spacing;
            }
            if (leggings != null && !leggings.isEmpty()) {
                renderItemStack(eventDisplay, leggings, x + offsetX, y - 33, scale);
                offsetX += spacing;
            }
            if (boots != null && !boots.isEmpty()) {
                renderItemStack(eventDisplay, boots, x + offsetX, y - 33, scale);
                offsetX += spacing;
            }
            if (heldItem != null && !heldItem.isEmpty()) {
                renderItemStack(eventDisplay, heldItem, x + offsetX, y - 31f, scale * 0.63f);
            }
            if (Item != null && !Item.isEmpty()) {
                renderItemStack(eventDisplay, Item, x + offsetX, y - 31f, scale * 0.63f);
            }
        }
    }

    private void renderItemStack(EventDisplay eventDisplay, ItemStack stack, float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.enableDepthTest();
        GlStateManager.scalef(scale, scale, scale);
        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, (int) (x / scale), (int) (y / scale));
        mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, stack, (int) (x / scale), (int) (y / scale),
                stack.getCount() > 1 ? String.valueOf(stack.getCount()) : "");
        GlStateManager.disableDepthTest();
        GlStateManager.popMatrix();
    }


    private void drawStyledRect(float x,
                                float y,
                                float width,
                                float height,
                                float radius, int alpha) {
        Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();
        DisplayUtils.drawRoundedRect(x + 25, y, width -25, height + 4, 3.5f, ColorUtils.rgba(21, 21, 21, 255));

    }
}

// Благодарю за покупку.