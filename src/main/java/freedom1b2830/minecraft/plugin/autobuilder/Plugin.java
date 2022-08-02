package freedom1b2830.minecraft.plugin.autobuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import freedom1b2830.minecraft.plugin.autobuilder.config.BuilderConfig;
import freedom1b2830.minecraft.plugin.autobuilder.config.BuilderEntity;
import freedom1b2830.minecraft.plugin.autobuilder.helpers.Githelper;
import freedom1b2830.minecraft.plugin.autobuilder.helpers.Shell;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

    public boolean update(BuilderEntity plugin) {
        try {
            boolean buildNeeded = Githelper.pull(plugin.gitUrl, plugin.repoDir);
            if (buildNeeded) {
                sendToOps(String.format("freedom1b2830.autobuilder: a git commit for the [%s] plugin has arrived.", plugin.gitUrl));
                boolean ret = Shell.exec(plugin.exeDir, plugin.exeScript);
                sendToOps(String.format("freedom1b2830.autobuilder: plugin [%s] is builded.", plugin.gitUrl));
                return ret;
            }
        } catch (IOException | GitAPIException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private static void sendToOps(String message) {
        if (getInstance().config.notifyOperators) {
            getInstance().getServer().getOnlinePlayers().parallelStream().filter(player -> {
                return player.isOp() || player.hasPermission("freedom1b2830.autobuilder.notify");
            }).forEachOrdered(player -> {
                player.sendMessage(message);
            });
        }
        if (getInstance().config.notifyConsole) {
            getInstance().getLogger().info(message.replaceAll("freedom1b2830.autobuilder:", ""));
        }
    }

    final Thread executor = new Thread(new Runnable() {//TODO  EXECUTOR SERVICE
        @Override
        public void run() {
            started = true;
            do {
                List<Boolean> resultList = config.plugins.stream().map(pluginForBuild -> update(pluginForBuild))
                        .filter(result -> {
                            if (result) {
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList());
                if (!resultList.isEmpty()) {
                    try {
                        reloadServer(config.reloadCMD);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(getInstance().config.timeCheck);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (started);
        }
    });

    private void reloadServer(String reloadCMD) throws ExecutionException, InterruptedException {
        sendToOps("freedom1b2830.autobuilder: >reload server< ");
    }

    public BuilderConfig config = new BuilderConfig();

    boolean started = false;

    @Override
    public void onEnable() {
        if (!getInstance().started) {
            getInstance().executor.start();
        }
        try {
            readConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final File pluginsDir = getPluginsDir();
    public static final File builderDir = getBuilderDir();

    public static File getBuilderDir() {
        return new File(getPluginsDir(), "freedom1b2830-plugin-autobuilder");
    }

    private static File getPluginsDir() {
        try {
            return findByFileName(new File("").toPath(), "plugins").get(0).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Path> findByFileName(Path path, String fileName)
            throws IOException {
        List<Path> result;
        try (Stream<Path> pathStream = Files.find(path, Integer.MAX_VALUE, (p, basicFileAttributes) -> p.getFileName().toString().equalsIgnoreCase(fileName))) {
            result = pathStream.collect(Collectors.toList());
        }
        return result;
    }

    public static final File configFile = new File(builderDir, "builder.config.yaml");

    public static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    private void readConfig() throws IOException {
        if (!builderDir.exists()) {
            Files.createDirectories(builderDir.toPath());
        }
        if (configFile.exists()) {
            config = mapper.readValue(configFile, BuilderConfig.class);
        } else {
            mapper.writeValue(configFile, config);
            getInstance().getLogger().info(String.format("edit builder config file: %s , and reload ", configFile));
        }
    }
}