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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.ArrayList;
import jd.plugins.PluginForHost;
import org.apache.commons.io.FileUtils;
import org.appwork.exceptions.WTFException;
import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.jdownloader.plugins.controller.PluginClassLoader;
import org.jdownloader.plugins.controller.UpdateRequiredClassNotFoundException;
import org.jdownloader.plugins.controller.host.HostPluginController;
import org.jdownloader.plugins.controller.host.LazyHostPlugin;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;
import to.sauerkraut.krautadmin.KrautAdminApplication;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public final class Toolkit {
    @Log
    private static Logger logger;
    
    private Toolkit() {
        
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
    
    public static void setPrivateField(final Field field, final Object instance, final Object newValue) 
            throws Exception {
        field.setAccessible(true);
        field.set(instance, newValue);
    }
    
    public static String getJarContainingFolder() throws Exception {
        return getJarContainingFolder(KrautAdminApplication.class);
    }
    
    private static String getJarContainingFolder(final Class aclass) throws Exception {
        final CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();

        File jarFile;

        if (codeSource.getLocation() != null) {
            jarFile = new File(codeSource.getLocation().toURI());
        } else {
            final String path = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
            String jarFilePath = path.substring(path.indexOf(':') + 1, path.indexOf('!'));
            jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
            jarFile = new File(jarFilePath);
        }
        return jarFile.getParentFile().getAbsolutePath();
    }
    
    public static String parseDbPath(final String path) {
        final String trimmedPath = Strings.emptyToNull(path);
        
        try {
            return trimmedPath == null ? null
                : trimmedPath.replace("$TMP", System.getProperty("java.io.tmpdir"))
                    .replace("$JAR", KrautAdminApplication.getJarFolder());
            
        } catch (Exception e) {
            throw new IllegalStateException("Failed to determine application .jar location", e);
        }
    }
    
    public static void updateLinkCheckers() throws Exception {
        clearLinkCheckerCaches();
        HostPluginController.getInstance().init();
        try {
            Toolkit.setFinalPrivateField(ShutdownController.class.getDeclaredField("hooks"), 
                    ShutdownController.getInstance(), new ArrayList<ShutdownEvent>());
        } catch (Exception ex) {
            logger.error("could not disable unnecessary JD shutdown hooks", ex);
        }
    }
    
    protected static void clearLinkCheckerCaches() throws Exception {
        try {
            final String appPath = KrautAdminApplication.getJarFolder();
            FileUtils.deleteDirectory(new File(appPath.concat(File.separator.concat("cfg"))));
            FileUtils.deleteDirectory(new File(appPath.concat(File.separator.concat("tmp"))));
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
