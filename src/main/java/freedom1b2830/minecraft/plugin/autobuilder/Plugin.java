package freedom1b2830.minecraft.plugin.autobuilder;

import freedom1b2830.minecraft.plugin.autobuilder.config.BuilderConfig;
import freedom1b2830.minecraft.plugin.autobuilder.config.BuilderEntity;
import org.bukkit.plugin.java.JavaPlugin;


public class Plugin extends JavaPlugin {

    private static Plugin instance;

    public Plugin() {
        if (instance != null) {
            throw new IllegalStateException("already inited");
        }
        instance = this;
    }

    public static Plugin getInstance() {
        return instance;
    }

    public void update(BuilderEntity plugin) {
    }

    Thread executor = new Thread(new Runnable() {
        @Override
        public void run() {
            started = true;
            do {
                config.plugins.stream().forEachOrdered(pluginForBuild -> update(pluginForBuild));
                try {
                    Thread.sleep(getInstance().config.timeCheck);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (started);
        }
    });

    BuilderConfig config = new BuilderConfig();

    boolean started = false;

    @Override
    public void onEnable() {
        if (!getInstance().started) {
            getInstance().executor.start();
        }
    }
}