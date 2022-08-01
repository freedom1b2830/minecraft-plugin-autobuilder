package freedom1b2830.minecraft.plugin.autobuilder.config;

import freedom1b2830.minecraft.plugin.autobuilder.Plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class BuilderEntityExample extends BuilderEntity {
    public BuilderEntityExample() {
        try {
            gitUrl = new URL("https://github.com/freedom1b2830/minecraft-plugin-autobuilder");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        exeScript = new File(new File(Plugin.pluginsDir, "freedom1b2830-plugin-autobuilder-SOURCES"), "update.sh");
        File exeScriptTmp = new File(exeScript.getAbsolutePath());//FIX NPE for "exeScript.getAbsolutePath()"
        exeDir = new File(exeScriptTmp.getParentFile().getAbsolutePath());
        repoDir = exeDir;
    }
}
