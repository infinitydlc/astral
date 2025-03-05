package im.expensive.functions.impl.combat;

import com.google.common.eventbus.Subscribe;
import im.expensive.events.EventPacket;
import im.expensive.events.EventUpdate;
import im.expensive.functions.api.Category;
import im.expensive.functions.api.Function;
import im.expensive.functions.api.FunctionRegister;
import im.expensive.functions.settings.impl.ModeSetting;
import im.expensive.utils.TimerUtil;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

@FunctionRegister(name = "Velocity", type = Category.Combat)
public class Velocity extends Function {

    private final ModeSetting mode = new ModeSetting("Mode", "Cancel", "Cancel", "Grim", "Grim Updated", "Bravo");

    private int toSkip;
    private int await;
    private boolean damaged;
    private float lastHealth; // Track the player's last health value

    private final TimerUtil timer = new TimerUtil(); // Timer for "Bravo" mode
    private Vector3d lastPosition; // Track the player's last position

    public Velocity() {
        addSettings(mode);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null) return;

        if (e.isReceive()) {
            switch (mode.get()) {
                case "Cancel" -> {
                    if (e.getPacket() instanceof SEntityVelocityPacket p) {
                        if (p.getEntityID() != mc.player.getEntityId()) return;

                        // Cancel the packet only if the player took damage
                        if (damaged) {
                            e.cancel();
                            damaged = false; // Reset the flag after canceling
                        }
                    }
                }

                case "Grim" -> {
                    if (e.getPacket() instanceof SEntityVelocityPacket p) {
                        if (p.getEntityID() != mc.player.getEntityId() || toSkip < 0) return;

                        toSkip = 8;
                        e.cancel();
                    }

                    if (e.getPacket() instanceof SConfirmTransactionPacket) {
                        if (toSkip < 0) toSkip++;
                        else if (toSkip > 1) {
                            toSkip--;
                            e.cancel();
                        }
                    }

                    if (e.getPacket() instanceof SPlayerPositionLookPacket) toSkip = -8;
                }

                case "Grim Updated" -> {
                    if (e.getPacket() instanceof SEntityVelocityPacket p) {
                        if (p.getEntityID() != mc.player.getEntityId() || await > -5) return;

                        await = 2;
                        damaged = true;
                        e.cancel();
                    }
                }

                case "Bravo" -> {
                    // "Bravo" mode: Do nothing with packets
                }
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        // Check if the player's health has decreased
        float currentHealth = mc.player.getHealth();
        if (currentHealth < lastHealth) {
            damaged = true; // Player took damage
            if (mode.is("Bravo")) {
                lastPosition = mc.player.getPositionVec(); // Save the player's position
                timer.reset(); // Reset the timer
            }
        }
        lastHealth = currentHealth; // Update the last health value

        // Logic for "Bravo" mode
        if (mode.is("Bravo") && damaged) {
            if (timer.hasTimeElapsed(100)) { // Correct the position after 100 ms
                mc.player.setPosition(lastPosition.x, lastPosition.y, lastPosition.z);
                damaged = false; // Reset the flag
            }
        }

        // Logic for "Grim Updated" mode
        if (mode.is("Grim Updated")) {
            await--;

            if (damaged) {
                BlockPos blockPos = new BlockPos(mc.player.getPositionVec());
                mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
                mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
                damaged = false;
            }
        }
    }

    private void reset() {
        toSkip = 0;
        await = 0;
        damaged = false;
        lastHealth = mc.player != null ? mc.player.getHealth() : 0; // Reset health on disable
    }

    @Override
    public boolean onEnable() {
        super.onEnable();
        reset();
        return false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }
}