/*
 * Copyright (C) 2015 sauerkraut.to <gutsverwalter@sauerkraut.to>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package to.sauerkraut.krautadmin.core;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import jd.plugins.PluginForHost;
import org.apache.commons.io.FileUtils;
import org.appwork.exceptions.WTFException;
import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.RebaseResult.Status;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.merge.MergeStrategy;
import org.jdownloader.plugins.controller.PluginClassLoader;
import org.jdownloader.plugins.controller.UpdateRequiredClassNotFoundException;
import org.jdownloader.plugins.controller.host.HostPluginController;
import org.jdownloader.plugins.controller.host.LazyHostPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.sauerkraut.jgitclone.api.commands.GitClone;
import to.sauerkraut.jgitclone.api.commands.GitCloneOptions;
import to.sauerkraut.jgitclone.utilities.UrlUtilities;
import to.sauerkraut.krautadmin.KrautAdminApplication;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@SuppressWarnings("checkstyle:classfanoutcomplexity")
public final class Toolkit {
    public static final String LINK_CHECKER_UPDATES_GIT_URL = "https://github.com/sauerkraut-to/jdupdates.git";
    public static final String LINK_CHECKER_DOWNLOAD_FOLDER_NAME = "jd";
    
    private static final Logger LOG = LoggerFactory.getLogger(Toolkit.class);
    
    private Toolkit() {
        
    }

    public static void setAssertionsEnabled(final boolean enabled) {
        setAssertionsEnabled(enabled, ClassLoader.getSystemClassLoader());
    }

    public static <T> String join(final Iterable<T> values, final String separator) {
        if (values == null) {
            return "";
        }
        final Iterator<T> iter = values.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        final StringBuffer toReturn = new StringBuffer(String.valueOf(iter.next()));
        while (iter.hasNext()) {
            toReturn.append(separator + String.valueOf(iter.next()));
        }
        return toReturn.toString();
    }

    public static String join(final String[] values, final String separator) {
        return (values == null) ? "" : join(Arrays.asList(values), separator);
    }

    public static void kill(final String pid) throws Exception {
        final String os = System.getProperty("os.name");
        final String command = (os.startsWith("Windows"))
                ? "taskkill /F /PID " + pid
                : "kill " + pid;
        Runtime.getRuntime().exec(command).waitFor();
    }

    public static void setAssertionsEnabled(final boolean enabled, final ClassLoader classLoader) {
        classLoader.setDefaultAssertionStatus(enabled);
    }
    
    public static void setFinalStaticField(final Field field, final Object newValue) throws Exception {
        setFinalPrivateField(field, null, newValue);
    }
    
    public static void setFinalPrivateField(final Field field, final Object instance, final Object newValue) 
            throws Exception {
        field.setAccessible(true);

        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(instance, newValue);
    }

    public static synchronized void restartApplication() throws Exception {
        //TODO: implement
    }
    
    public static void setPrivateField(final Field field, final Object instance, final Object newValue) 
            throws Exception {
        field.setAccessible(true);
        field.set(instance, newValue);
    }

    public static void setPrivateStaticField(final Field field, final Object newValue)
            throws Exception {
        setPrivateField(field, null, newValue);
    }

    public static void modifyByteCode(final CtClass ctClass, final String methodName,
                                      final CtClass[] methodParams, final String methodBody, final boolean loadAfter,
                                      final boolean writeClass)
            throws NotFoundException, CannotCompileException, IOException {
        // get the method from the Class byte code
        final CtMethod method = ctClass.getDeclaredMethod(methodName, methodParams);

        // set new method body
        method.setBody(methodBody);

        if (writeClass) {
            // class-file replacement
            ctClass.writeFile();
        }

        if (loadAfter) {
            ctClass.toClass();
        }
    }

    public static void modifyByteCode(final CtClass ctClass, final String methodName,
                                      final CtClass[] methodParams, final String methodBody, final boolean loadAfter)
            throws NotFoundException, CannotCompileException, IOException {
        modifyByteCode(ctClass, methodName, methodParams, methodBody, loadAfter, false);
    }
    
    public static String getApplicationContainingFolder() throws Exception {
        return getApplicationContainingFolder(KrautAdminApplication.class);
    }
    
    private static String getApplicationContainingFolder(final Class aClass) throws Exception {
        final File jarFile = getPossibleApplicationJarFile(aClass);

        return jarFile.getParentFile().getAbsolutePath();
    }

    /**
     *
     * @return null, if the application is not delivered as a fat .jar
     */
    public static String getApplicationJarName() throws IOException {
        final File possibleJarFile = getPossibleApplicationJarFile(KrautAdminApplication.class);

        if (possibleJarFile != null && possibleJarFile.getName().toUpperCase().endsWith(".JAR")) {
            return possibleJarFile.getName();
        } else {
            return null;
        }
    }

    public static VirtualFile getVirtualFile(final String applicationRelativePath) {
        return VirtualFile.fromApplicationRelativePath(applicationRelativePath);
    }

    private static File getPossibleApplicationJarFile(final Class aClass) throws IOException {
        final CodeSource codeSource = aClass.getProtectionDomain().getCodeSource();

        File jarFile;

        if (codeSource.getLocation() != null) {
            try {
                jarFile = new File(codeSource.getLocation().toURI());
            } catch (URISyntaxException e) {
                return null;
            }
        } else {
            final String path = aClass.getResource(aClass.getSimpleName() + ".class").getPath();
            String jarFilePath = path.substring(path.indexOf(':') + 1, path.indexOf('!'));
            jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
            jarFile = new File(jarFilePath);
        }

        return jarFile;
    }
    
    public static String parseDbPath(final String path) {
        final String trimmedPath = Strings.emptyToNull(path);

        return trimmedPath == null ? null : trimmedPath.replace("$TMP", System.getProperty("java.io.tmpdir"))
                .replace("$APP", KrautAdminApplication.getApplicationContainingFolder());
    }

    /**
     * Delete all downloaded link checker classes.
     * It will force the next iteration of the link checker update cronjob to fully re-download the latest set
     * of link checker classes.
     * @throws IOException if the directory containing the link checker classes couldn't be deleted
     */
    public static synchronized void clearLinkCheckers() throws IOException {
        FileUtils.deleteDirectory(new File(KrautAdminApplication.getApplicationContainingFolder()
                .concat(File.separator.concat(LINK_CHECKER_DOWNLOAD_FOLDER_NAME))));
    }

    /**
     * Validates a Zip archive.
     * If the archive is invalid, cannot be opened or integrity errors are detected,
     * Exceptions will be thrown.
     * @param zipFile
     * @throws Exception
     */
    public static void validateZipFile(final File zipFile) throws IOException {
        final ZipFile applicationUpdateJar = new ZipFile(zipFile);
        final Enumeration<? extends ZipEntry> applicationUpdateJarEntries =
                applicationUpdateJar.entries();
        // iterating through the .jar will detect integrity errors of the zip archive
        while (applicationUpdateJarEntries.hasMoreElements()) {
            applicationUpdateJarEntries.nextElement().getName();
        }
    }
    
    public static synchronized void updateLinkCheckers() throws Exception {
        final File pluginsParentDirectory = new File(
                KrautAdminApplication.getApplicationContainingFolder().concat(File.separator)
                        .concat(LINK_CHECKER_DOWNLOAD_FOLDER_NAME));
        boolean needsClassesReload = false;
        final URI linkCheckerUpdatesGitUri = new URI(LINK_CHECKER_UPDATES_GIT_URL);
        
        // try incremental update at first - if it fails, try a whole new repo clone
        try {
            needsClassesReload = updateFromGit(pluginsParentDirectory, linkCheckerUpdatesGitUri);
        } catch (Exception e) {
            if (e instanceof WTFException) {
                LOG.error(e.getMessage(), e);
            } else {
                LOG.info(e.getMessage());
            }
            cloneFromGit(pluginsParentDirectory, linkCheckerUpdatesGitUri, true);
            needsClassesReload = true;
        }
        
        if (needsClassesReload) {
            reloadLinkCheckerClasses();
        }
    }
    
    public static void cloneFromGit(final File targetDirectory, final URI repositoryUri, final boolean shallow) 
            throws Exception {
        try {
            FileUtils.deleteDirectory(targetDirectory);
            FileUtils.forceMkdir(targetDirectory);
        } catch (IOException ioex) {
            LOG.info("Could not delete and/or recreate plugin git repo directory, maybe did not exist");
        }
        
        LOG.info("starting full plugin-update from git (shallow: " + String.valueOf(shallow) + ") ...");

        if (shallow) {
            final GitClone gitClone = new GitClone();
            final GitCloneOptions gitCloneOptions = new GitCloneOptions();
            gitCloneOptions.setDepth(2);

            gitClone.clone(targetDirectory, gitCloneOptions, UrlUtilities.url2JavaGitUrl(repositoryUri.toURL()), 
                    targetDirectory);
        } else {
            Git.cloneRepository().setURI(repositoryUri.toString()).setDirectory(targetDirectory).call();
        }
        LOG.info("full plugin-update successful");
    }
    
    public static boolean updateFromGit(final File repositoryDirectory, final URI repositoryUri) 
            throws Exception {
        final String unexpectedExceptionText = "incremental plugin-update from git failed";
        final String upstream = "refs/remotes/origin/master";
        boolean hasUpdated = false;
        Git gitRepo = null;
        
        try {
            gitRepo = Git.open(repositoryDirectory);
            // first reset local changes
            gitRepo.reset().setMode(ResetCommand.ResetType.HARD).call(); 
            LOG.info("starting incremental plugin-update from git...");
            gitRepo.fetch().setRemote(Constants.DEFAULT_REMOTE_NAME).call();
            final RebaseResult rebaseResult = gitRepo.rebase().setStrategy(MergeStrategy.THEIRS).
                    setUpstream(upstream).
                    setUpstreamName(upstream).call();
            final Status rebaseStatus = rebaseResult.getStatus();
            if (rebaseStatus.isSuccessful()) {
                if (!(Status.UP_TO_DATE.equals(rebaseStatus))) {
                    hasUpdated = true;
                }
            } else {
                throw new WTFException(unexpectedExceptionText);
            }

            if (hasUpdated) {
                LOG.info("incremental plugin-update from git successful");
            } else {
                LOG.info("plugin-files are up-to-date");
            }
        } finally {
            try {
                if (gitRepo != null) {
                    gitRepo.close();
                }
            } catch (Exception closex) {
                LOG.debug("closing git repo failed");
            }
        }
        
        return hasUpdated;
    }
    
    protected static synchronized void reloadLinkCheckerClasses() throws Exception {
        clearLinkCheckerCaches();
        HostPluginController.getInstance().init();
        try {
            Toolkit.setFinalPrivateField(ShutdownController.class.getDeclaredField("hooks"), 
                    ShutdownController.getInstance(), new ArrayList<ShutdownEvent>());
        } catch (Exception ex) {
            LOG.error("could not disable unnecessary JD shutdown hooks", ex);
        }
    }
    
    protected static synchronized void clearLinkCheckerCaches() throws Exception {
        try {
            final String appPath = KrautAdminApplication.getApplicationContainingFolder();
            
            try {
                FileUtils.deleteDirectory(new File(appPath.concat(File.separator.concat("cfg"))));
            } catch (IOException ioex) {
                LOG.info("Could not delete 'cfg' directory");
            }
            try {
                FileUtils.deleteDirectory(new File(appPath.concat(File.separator.concat("tmp"))));
            } catch (IOException ioex) {
                LOG.info("Could not delete 'tmp' directory");
            }
            
            Toolkit.setPrivateField(HostPluginController.class.getDeclaredField("lastKnownPlugins"), 
                    HostPluginController.getInstance(), new ArrayList<LazyHostPlugin>());
        } catch (Exception ex) {
            throw new WTFException("error clearing link checker caches", ex);
        }
        //ExtensionController.getInstance().invalidateCache();
        //CrawlerPluginController.invalidateCache();
        HostPluginController.getInstance().invalidateCache();
    }
    
    public static PluginForHost getLinkCheckerForHost(final String host) 
            throws UpdateRequiredClassNotFoundException {
        final LazyHostPlugin lplugin = HostPluginController.getInstance().get(host);
        if (lplugin != null) {
            return lplugin.newInstance(PluginClassLoader.getThreadPluginClassLoaderChild());
        }
        return null;
    }
}
