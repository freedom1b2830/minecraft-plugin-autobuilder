package freedom1b2830.minecraft.plugin.autobuilder.helpers;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.SubmoduleConfig;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public final class Githelper {
    private Githelper() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Githelper.class);

    public static boolean pull(URL gitUrl, File repoDir) throws IOException, GitAPIException {
        if (repoDir.exists()) {
            try (Git git = Git.open(repoDir)) {
                PullResult pullResult = git.pull().setRecurseSubmodules(SubmoduleConfig.FetchRecurseSubmodulesMode.YES).call();
                if (pullResult.isSuccessful()) {
                    FetchResult result = pullResult.getFetchResult();
                    int index = 0;
                    for (TrackingRefUpdate trackingRefUpdate : result.getTrackingRefUpdates()) {
                        LOGGER.info("ID:{} msg:{}", index,
                                ToStringBuilder.
                                        reflectionToString(trackingRefUpdate, ToStringStyle.MULTI_LINE_STYLE));
                        index++;
                    }
                    return !result.getTrackingRefUpdates().isEmpty();
                }
            }
        } else {
            CloneCommand a = Git.cloneRepository().setURI(gitUrl.toString()).setDirectory(repoDir)
                    .setCloneSubmodules(true)
                    .setCloneAllBranches(true);
            Git ret2 = a.call();
            ret2.close();
            return true;
        }
        return false;
    }
}
