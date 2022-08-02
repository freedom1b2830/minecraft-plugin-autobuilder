package freedom1b2830.minecraft.plugin.autobuilder;

import freedom1b2830.minecraft.plugin.autobuilder.config.BuilderConfig;
import freedom1b2830.minecraft.plugin.autobuilder.config.BuilderEntity;
import freedom1b2830.minecraft.plugin.autobuilder.helpers.Githelper;
import freedom1b2830.minecraft.plugin.autobuilder.helpers.Shell;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static freedom1b2830.minecraft.plugin.autobuilder.Plugin.getInstance;

public class UpdateTask implements Runnable {


    @Override
    public void run() {
        BuilderConfig config = getInstance().config;
        List<Boolean> resultList = config.plugins.stream().map(this::update)
                .filter(result -> result).toList();
        if (!resultList.isEmpty()) {
            try {
                reloadServer();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void reloadServer() throws ExecutionException, InterruptedException {
        sendToOps("freedom1b2830.autobuilder: >reload server< ");
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

    public boolean update(BuilderEntity plugin) {
        try {
            boolean buildNeeded = Githelper.pull(plugin.gitUrl, plugin.repoDir);
            if (buildNeeded) {
                sendToOps(String.format("freedom1b2830.autobuilder: a git commit for the [%s] plugin has arrived.", plugin.gitUrl));
                boolean ret = Shell.exec(plugin.exeDir, plugin.exeScript);
                sendToOps(String.format("freedom1b2830.autobuilder: plugin [%s] is builded.[%s]", plugin.gitUrl, ret));
                return ret;
            }
        } catch (IOException | GitAPIException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
