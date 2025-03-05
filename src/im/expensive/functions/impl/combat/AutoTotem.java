package im.expensive.functions.impl.combat;

import com.google.common.eventbus.Subscribe;
import im.expensive.events.EventPacket;
import im.expensive.events.EventSpawnEntity;
import im.expensive.events.EventUpdate;
import im.expensive.functions.api.Category;
import im.expensive.functions.api.Function;
import im.expensive.functions.api.FunctionRegister;
import im.expensive.functions.settings.impl.BooleanSetting;
import im.expensive.functions.settings.impl.ModeListSetting;
import im.expensive.functions.settings.impl.ModeSetting;
import im.expensive.functions.settings.impl.SliderSetting;
import im.expensive.utils.math.StopWatch;
import im.expensive.utils.player.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@FunctionRegister(name = "AutoTotem", type = Category.Combat)
public class AutoTotem extends Function {
    private final SliderSetting health = new SliderSetting("Здоровье", 3.5f, 1.0f, 20.0f, 0.5f);
    private final BooleanSetting swapBack = new BooleanSetting("Возвращать предмет", true);
    private final BooleanSetting noBallSwitch = new BooleanSetting("Не брать если шар в руке", false);
    private final ModeListSetting mode = new ModeListSetting("Срабатывать", new BooleanSetting("Золотые сердца", true),
            new BooleanSetting("Кристаллы", true),
            new BooleanSetting("Обсидиан", false),
            new BooleanSetting("Якорь", false),
            new BooleanSetting("Падение", true),
            new BooleanSetting("Кристалл в руке", true),
            new BooleanSetting("Здоровье на элитре", true));
    private final SliderSetting radiusExplosion = new SliderSetting("Дистанция до кристала", 6.0f, 1.0f, 8.0f, 1.0f).setVisible(() -> mode.getValueByName("Кристаллы").get());
    private final SliderSetting radiusObs = new SliderSetting("Дистанция до обсидиана", 6.0f, 1.0f, 8.0f, 1.0f).setVisible(() -> mode.getValueByName("Обсидиан").get());
    private final SliderSetting radiusAnch = new SliderSetting("Дистанция до якоря", 6.0f, 1.0f, 8.0f, 1.0f).setVisible(() -> mode.getValueByName("Якорь").get());
    private final SliderSetting HPElytra = new SliderSetting("Брать по здоровью на элитре", 6.0f, 1.0f, 20.0f, 0.5f).setVisible(() -> mode.getValueByName("Здоровье на элитре").get());
    private final SliderSetting DistanceFall = new SliderSetting("Дистанция падения", 20.0f, 3.0f, 50.0f, 0.5f).setVisible(() -> mode.getValueByName("Падение").get());

    int oldItem = -1;
    public boolean isActive;
    StopWatch stopWatch = new StopWatch();

    private Item backItem = Items.AIR;
    private ItemStack backItemStack;

    public AutoTotem() {
        addSettings(health, swapBack, noBallSwitch, mode, radiusExplosion, radiusObs, radiusAnch, HPElytra, DistanceFall);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (shouldToSwapTotem()) {
            int slot = getSlotInInventory(Items.TOTEM_OF_UNDYING);
            boolean handNotNull = !(mc.player.getHeldItemOffhand().getItem() instanceof AirItem);

            if (slot != -1 && !isTotemInHands()) {
                InventoryUtil.moveItem(slot, 45, handNotNull);
                if (handNotNull && oldItem == -1) {
                    oldItem = slot;
                }
            }
        } else if (oldItem != -1 && swapBack.get()) {
            InventoryUtil.moveItem(oldItem, 45, !(mc.player.getHeldItemOffhand().getItem() instanceof AirItem));
            oldItem = -1;
        }
    }

    private boolean shouldToSwapTotem() {
        float absorption = mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0f;
        float currentHealth = mc.player.getHealth() + (mode.getValueByName("Золотые сердца").get() ? absorption : 0.0f);

        if (currentHealth <= health.get()) {
            return true;
        }

        if (!isBall()) {
            if (checkCrystal()) return true;
            if (checkObsidian()) return true;
            if (checkAnchor()) return true;
            if (checkPlayerCrystal()) return true;
        }

        if (checkHPElytra()) return true;
        return checkFall();
    }

    private boolean checkCrystal() {
        if (!mode.getValueByName("Кристаллы").get()) return false;

        for (Entity entity : mc.world.getAllEntities()) {
            if ((entity instanceof EnderCrystalEntity || entity instanceof TNTEntity || entity instanceof TNTMinecartEntity) &&
                    mc.player.getDistance(entity) <= radiusExplosion.get()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkObsidian() {
        if (!mode.getValueByName("Обсидиан").get()) return false;
        return getBlock(radiusObs.get(), Blocks.OBSIDIAN) != null;
    }

    private boolean checkAnchor() {
        if (!mode.getValueByName("Якорь").get()) return false;
        return getBlock(radiusAnch.get(), Blocks.RESPAWN_ANCHOR) != null;
    }

    private boolean checkPlayerCrystal() {
        if (!mode.getValueByName("Кристалл в руке").get()) return false;

        for (LivingEntity entity : mc.world.getPlayers()) {
            if (entity != mc.player && (entity.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ||
                    entity.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) &&
                    mc.player.getDistance(entity) <= 6.0f) {
                return true;
            }
        }
        return false;
    }

    private boolean checkHPElytra() {
        if (!mode.getValueByName("Здоровье на элитре").get()) return false;
        return mc.player.inventory.armorInventory.get(2).getItem() == Items.ELYTRA &&
                mc.player.getHealth() <= HPElytra.get();
    }

    private boolean checkFall() {
        if (!mode.getValueByName("Падение").get()) return false;
        return mc.player.fallDistance > DistanceFall.get();
    }

    private boolean isBall() {
        if (mode.getValueByName("Падение").get() && mc.player.fallDistance > 5.0f) return false;
        return noBallSwitch.get() && mc.player.getHeldItemOffhand().getItem() == Items.PLAYER_HEAD;
    }

    private boolean isTotemInHands() {
        return mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING ||
                mc.player.getHeldItemMainhand().getItem() == Items.TOTEM_OF_UNDYING;
    }

    private BlockPos getBlock(float distance, Block block) {
        return getSphere(getPlayerPosLocal(), distance, 6, false, true, 0).stream()
                .filter(pos -> mc.world.getBlockState(pos).getBlock() == block)
                .min(Comparator.comparing(pos -> getDistanceOfEntityToBlock(mc.player, pos)))
                .orElse(null);
    }

    private List<BlockPos> getSphere(BlockPos center, float radius, int height, boolean hollow, boolean fromBottom, int yOffset) {
        List<BlockPos> positions = new ArrayList<>();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        for (int x = centerX - (int) radius; x <= centerX + radius; x++) {
            for (int z = centerZ - (int) radius; z <= centerZ + radius; z++) {
                int yStart = fromBottom ? centerY - (int) radius : centerY;
                int yEnd = fromBottom ? centerY + (int) radius : centerY + height;

                for (int y = yStart; y < yEnd; y++) {
                    if (isPositionWithinSphere(centerX, centerY, centerZ, x, y, z, radius, hollow)) {
                        positions.add(new BlockPos(x, y + yOffset, z));
                    }
                }
            }
        }
        return positions;
    }

    private BlockPos getPlayerPosLocal() {
        return new BlockPos(Math.floor(mc.player.getPosX()), Math.floor(mc.player.getPosY()), Math.floor(mc.player.getPosZ()));
    }

    private double getDistanceOfEntityToBlock(Entity entity, BlockPos blockPos) {
        return MathHelper.sqrt(entity.getDistanceSq(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    private static boolean isPositionWithinSphere(int centerX, int centerY, int centerZ, int x, int y, int z, float radius, boolean hollow) {
        double distanceSq = Math.pow(centerX - x, 2) + Math.pow(centerZ - z, 2) + Math.pow(centerY - y, 2);
        return distanceSq < Math.pow(radius, 2) && (!hollow || distanceSq >= Math.pow(radius - 1.0f, 2));
    }

    private int getSlotInInventory(Item item) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == item) {
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        oldItem = -1;
        super.onDisable();
    }
}