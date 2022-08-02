package freedom1b2830.minecraft.plugin.autobuilder.helpers;

import freedom1b2830.minecraft.plugin.autobuilder.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Shell {
    public static boolean exec(File exeDir, File exeScript) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(getShell().getAbsolutePath(), exeScript.getAbsolutePath());
        processBuilder.directory(exeDir);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        if (Plugin.getInstance().config.debug) {
            while (true) {
                String out = process.inputReader(StandardCharsets.UTF_8).readLine();
                if (out == null) {
                    break;
                }
                Plugin.getInstance().getLogger().info(out);
            }
        }
        int exit = process.waitFor();
        return exit == 0;
    }

    public enum Shells {
        BASH("bash"), SH("sh");
        private String name;

        Shells(String binName) {
            name = binName;
        }

        public File search() {
            String env = System.getenv("PATH");
            String[] envParts = env.split(":");
            for (String path : envParts) {
                File bin = new File(path, name);
                if (bin.exists()) {
                    return bin;
                }
            }
            return null;
        }
    }

    private static File getShell() throws FileNotFoundException {
        List<File> list = new ArrayList<>();
        for (Shells shell : Arrays.asList(Shells.values())) {
            File file = shell.search();
            if (file != null) {
                list.add(file);
            }
        }
        if (list.isEmpty()) {
            throw new FileNotFoundException("cont get ANY Shell");
        }
        return list.get(0);
    }
}
