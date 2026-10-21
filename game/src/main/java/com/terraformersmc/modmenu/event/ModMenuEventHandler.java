package com.terraformersmc.modmenu.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.List;

public class ModMenuEventHandler {
    private static KeyMapping MENU_KEY_MAPPING;

    public static void register() {
        MENU_KEY_MAPPING = new KeyMapping(
                "key.modmenu.open_menu",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(,
                KeyMapping.Category.MISC
        ));
        
        
    }

    public static void onClientEndTick(Minecraft client) {
        if (MENU_KEY_MAPPING == null) {
            return;
        }

        try {
            while (MENU_KEY_MAPPING.consumeClick()) {
                client.gui.setScreen(new ModsScreen(client.gui.screen()));
            }
        } catch (Throwable t) {
            ModMenu.LOGGER.error("Failed to open the Mod Menu from the key binding", t);
        }
    }
}
