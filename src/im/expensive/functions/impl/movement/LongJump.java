package im.expensive.functions.impl.movement;

import com.google.common.eventbus.Subscribe;
import im.expensive.events.EventUpdate;
import im.expensive.functions.api.Category;
import im.expensive.functions.api.Function;
import im.expensive.functions.api.FunctionRegister;
import im.expensive.functions.settings.impl.SliderSetting;
import net.minecraft.util.math.vector.Vector3d;

@FunctionRegister(name = "Step", type = Category.Movement)
public class LongJump extends Function {

    private final SliderSetting stepHeight = new SliderSetting("Step Height", 1.0f, 1.0f, 2.5f, 0.1f);

    public LongJump() {
        addSettings(stepHeight);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        // Устанавливаем высоту шага
        mc.player.stepHeight = (float) stepHeight.get();

        // Если игрок на земле и пытается подняться, отправляем пакет для корректного подъема
        if (mc.player.collidedVertically && mc.player.motion.y > 0) {
            Vector3d position = mc.player.getPositionVec();
            mc.player.setPosition(position.x, position.y + 0.5, position.z);
        }
    }

    @Override
    public void onDisable() {
        // Восстанавливаем стандартную высоту шага при отключении функции
        if (mc.player != null) {
            mc.player.stepHeight = 0.6f; // Стандартная высота шага в Minecraft
        }
        super.onDisable();
    }
}