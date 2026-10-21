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
}
