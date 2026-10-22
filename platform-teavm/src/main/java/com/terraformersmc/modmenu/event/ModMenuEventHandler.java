package com.terraformersmc.modmenu.event;

/**
 * Browser-target replacement for Mod Menu's Fabric event registration.
 * The web client has no Fabric event bus or keyboard-mapping registry.
 */
public final class ModMenuEventHandler {
    private ModMenuEventHandler() {
    }

    public static void register() {
        // Fabric key mappings and screen/tick events are not available in the browser.
    }

    /**
     * Compatibility hook used by the shared Eaglercraft client startup path.
     * Browser builds do not have Fabric client tick events to dispatch here.
     */
    public static void onClientEndTick(net.minecraft.client.Minecraft minecraft) {
        // Intentionally empty for the browser target.
    }
}
