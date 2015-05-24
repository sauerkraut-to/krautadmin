package to.sauerkraut.krautadmin.core;

import org.apache.commons.io.IOUtils;
import to.sauerkraut.krautadmin.KrautAdminApplication;

import java.io.*;
import java.nio.channels.Channel;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Class taken from play!framework v1.x and modified by sauerkraut.to.
 */
public class VirtualFile {

    private final File realFile;

    VirtualFile(final File file) {
        this.realFile = file;
    }

    public String getName() {
        return realFile.getName();
    }

    public boolean isDirectory() {
        return realFile.isDirectory();
    }

    public String relativePath() {
        final List<String> path = new ArrayList<>();
        File f = realFile;
        while (true) {
            path.add(f.getName());
            f = f.getParentFile();
            if (f == null) {
                // ??
                break;
            }
            if (f.equals(KrautAdminApplication.getApplicationContainingFolder())) {
                break;
            }

        }
        Collections.reverse(path);
        final StringBuilder builder = new StringBuilder();
        for (String p : path) {
            builder.append("/" + p);
        }
        return builder.toString();
    }

    public List<VirtualFile> list() {
        final List<VirtualFile> res = new ArrayList<>();
        if (exists()) {
            final File[] children = realFile.listFiles();
            for (int i = 0; i < children.length; i++) {
                res.add(new VirtualFile(children[i]));
            }
        }
        return res;
    }

    public boolean exists() {
        try {
            if (realFile != null) {
                return realFile.exists();
            }
            return false;
        } catch (AccessControlException e) {
            return false;
        }
    }

    public InputStream inputstream() {
        try {
            return new FileInputStream(realFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OutputStream outputstream() {
        try {
            return new FileOutputStream(realFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long lastModified() {
        if (realFile != null) {
            return realFile.lastModified();
        }
        return 0L;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof VirtualFile) {
            final VirtualFile vf = (VirtualFile) other;
            if (realFile != null && vf.realFile != null) {
                return realFile.equals(vf.realFile);
            }
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        if (realFile != null) {
            return realFile.hashCode();
        }
        return super.hashCode();
    }

    public long length() {
        return realFile.length();
    }

    public VirtualFile child(final String name) {
        return new VirtualFile(new File(realFile, name));
    }

    public Channel channel() {
        try {
            final FileInputStream fis = new FileInputStream(realFile);
            try {
                return fis.getChannel();
            } finally {
                closeQuietly(fis);
            }
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static VirtualFile open(final String file) {
        return open(new File(file));
    }

    public static VirtualFile open(final File file) {
        return new VirtualFile(file);
    }

    public String contentAsString() {
        try {
            final InputStream is = inputstream();
            try {
                return IO.readContentAsString(is);
            } finally {
                closeQuietly(is);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public File getRealFile() {
        return realFile;
    }

    public void write(final CharSequence string) {
        try {
            IO.writeContent(string, outputstream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] content() {
        try {
            final InputStream is = inputstream();
            try {
                return IOUtils.toByteArray(is);
            } finally {
                closeQuietly(is);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public static VirtualFile search(final Collection<VirtualFile> roots, final String path) {
        for (VirtualFile file : roots) {
            if (file.child(path).exists()) {
                return file.child(path);
            }
        }
        return null;
    }

    public static VirtualFile fromApplicationRelativePath(final String relativePath) {
        return new VirtualFile(new File(KrautAdminApplication.getApplicationContainingFolder())).child(relativePath);
    }

    /**
     * Method to check if the name really match (very useful on system without case sensibility (like windows)).
     * @param fileName
     * @return true if match
     */
    public boolean matchName(final String fileName) {
        // we need to check the name case to be sure we is not conflict with a file with the same name
        String canonicalName;
        try {
            canonicalName = this.realFile.getCanonicalFile().getName();
        } catch (IOException e) {
            canonicalName = null;
        }
        // Name case match
        if (fileName != null && canonicalName != null && fileName.endsWith(canonicalName)) {
            return true;
        }
        return false;
    }
}
