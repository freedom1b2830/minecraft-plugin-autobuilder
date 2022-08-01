package freedom1b2830.minecraft.plugin.autobuilder.config;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public class BuilderConfig {
    public final long timeCheck = 60000;
    public final CopyOnWriteArrayList<BuilderEntity> plugins = new CopyOnWriteArrayList<>(Collections.singletonList(new BuilderEntityExample()));
    public String reloadCMD = "reload confirm";
    public boolean debug = false;
}
