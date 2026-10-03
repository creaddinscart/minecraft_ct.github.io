package com.ct.module.launcher;

import com.ct.module.installer.GameInstaller;
import java.io.IOException;
import java.util.List;

public final class ProfileRouter {
    private ProfileRouter() {
    }

    public static String resolveLaunchProfileId(GameInstaller installer, String selectedProfile,
            List<String> loaderProfiles) throws IOException {
        if (loaderProfiles.contains(selectedProfile) || !installer.hasFabricMods()) {
            return selectedProfile;
        }
        List<String> matchingProfiles = installer.installedFabricProfiles(selectedProfile);
        if (matchingProfiles.isEmpty()) {
            throw new IOException("Fabric mod JARs were found in the mods folder, but no Fabric Loader "
                    + "profile is installed for Minecraft " + selectedProfile
                    + ". Install Fabric Loader into this game directory and click Refresh versions. "
                    + "Select Minecraft " + selectedProfile + " to use the sole matching profile, or "
                    + "choose a Fabric profile explicitly if multiple profiles match.");
        }
        if (matchingProfiles.size() > 1) {
            throw new IOException("Multiple Fabric Loader profiles are installed for Minecraft "
                    + selectedProfile + ". Select a Fabric profile explicitly.");
        }
        return matchingProfiles.get(0);
    }
}
