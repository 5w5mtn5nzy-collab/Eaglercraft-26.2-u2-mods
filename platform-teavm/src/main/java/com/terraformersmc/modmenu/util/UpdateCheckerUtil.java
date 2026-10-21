package com.terraformersmc.modmenu.util;

/**
 * Browser-target replacement: Mod Menu's update checker relies on desktop
 * executor and HTTP APIs that are not available in TeaVM's Java class library.
 */
public final class UpdateCheckerUtil {
    private UpdateCheckerUtil() {
    }

    public static void checkForUpdates() {
        // Online mod update checks are intentionally disabled on the web target.
    }

    public static void triggerV2RemovedToast() {
        // No update-checker toast is needed when update checks are disabled.
    }
}
