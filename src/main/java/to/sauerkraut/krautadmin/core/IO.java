package to.sauerkraut.krautadmin.core;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.appwork.exceptions.WTFException;
import to.sauerkraut.krautadmin.core.util.OrderSafeProperties;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * IO utils, taken from play!framework v1.x and modified by sauerkraut.to.
 */
public final class IO {
    public static final String DEFAULT_ENCODING = "UTF-8";

    private IO() {

    }

    /**
     * Read a properties file with the utf-8 encoding.
     * @param is Stream to properties file
     * @return The Properties object
     */
    public static Properties readUtf8Properties(final InputStream is) {
        final Properties properties = new OrderSafeProperties();
        try {
            properties.load(is);
            return properties;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(is);
        }
    }

    /**
     * Read the Stream content as a string (use utf-8).
     * @param is The stream to read
     * @return The String content
     */
    public static String readContentAsString(final InputStream is) {
        return readContentAsString(is, DEFAULT_ENCODING);
    }

    /**
     * Read the Stream content as a string.
     * @param is The stream to read
     * @return The String content
     */
    public static String readContentAsString(final InputStream is, final String encoding) {
        try {
            return IOUtils.toString(is, encoding);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Read file content to a String (always use utf-8).
     * @param file The file to read
     * @return The String content
     */
    public static String readContentAsString(final File file) {
        return readContentAsString(file, DEFAULT_ENCODING);
    }

    /**
     * Read file content to a String.
     * @param file The file to read
     * @return The String content
     */
    public static String readContentAsString(final File file, final String encoding) {
        try {
            return FileUtils.readFileToString(file, encoding);
        } catch (IOException e) {
            throw new WTFException(e);
        }
    }

    public static List<String> readLines(final InputStream is) {
        List<String> lines = null;
        try {
            lines = IOUtils.readLines(is);
        } catch (IOException ex) {
            throw new WTFException(ex);
        }
        return lines;
    }

    public static List<String> readLines(final File file, final String encoding) {
        try {
            return FileUtils.readLines(file, encoding);
        } catch (IOException ex) {
            throw new WTFException(ex);
        }
    }

    public static List<String> readLines(final File file) {
        return readLines(file, DEFAULT_ENCODING);
    }

    /**
     * Read binary content of a file (warning does not use on large file !).
     * @param file The file te read
     * @return The binary data
     */
    public static byte[] readContent(final File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new WTFException(e);
        }
    }

    /**
     * Read binary content of a stream (warning does not use on large file !).
     * @param is The stream to read
     * @return The binary data
     */
    public static byte[] readContent(final InputStream is) {
        try {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new WTFException(e);
        }
    }

    /**
     * Write String content to a stream (always use utf-8).
     * @param content The content to write
     * @param os The stream to write
     */
    public static void writeContent(final CharSequence content, final OutputStream os) {
        writeContent(content, os, DEFAULT_ENCODING);
    }

    /**
     * Write String content to a stream (always use utf-8).
     * @param content The content to write
     * @param os The stream to write
     */
    public static void writeContent(final CharSequence content, final OutputStream os, final String encoding) {
        try {
            IOUtils.write(content, os, encoding);
        } catch (IOException e) {
            throw new WTFException(e);
        } finally {
            closeQuietly(os);
        }
    }

    /**
     * Write String content to a file (always use utf-8).
     * @param content The content to write
     * @param file The file to write
     */
    public static void writeContent(final CharSequence content, final File file) {
        writeContent(content, file, DEFAULT_ENCODING);
    }

    /**
     * Write String content to a file (always use utf-8).
     * @param content The content to write
     * @param file The file to write
     */
    public static void writeContent(final CharSequence content, final File file, final String encoding) {
        try {
            FileUtils.write(file, content, encoding);
        } catch (IOException e) {
            throw new WTFException(e);
        }
    }

    /**
     * Write binay data to a file.
     * @param data The binary data to write
     * @param file The file to write
     */
    public static void write(final byte[] data, final File file) {
        try {
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException e) {
            throw new WTFException(e);
        }
    }

    /**
     * Copy an stream to another one.
     */
    public static void copy(final InputStream is, final OutputStream os) {
        try {
            IOUtils.copyLarge(is, os);
        } catch (IOException e) {
            throw new WTFException(e);
        } finally {
            closeQuietly(is);
        }
    }

    /**
     * Copy an stream to another one.
     */
    public static void write(final InputStream is, final OutputStream os) {
        try {
            IOUtils.copyLarge(is, os);
        } catch (IOException e) {
            throw new WTFException(e);
        } finally {
            closeQuietly(is);
            closeQuietly(os);
        }
    }

    /**
     * Copy an stream to another one.
     */
    public static void write(final InputStream is, final File f) {
        try {
            final OutputStream os = new FileOutputStream(f);
            try {
                IOUtils.copyLarge(is, os);
            } finally {
                closeQuietly(is);
                closeQuietly(os);
            }
        } catch (IOException e) {
            throw new WTFException(e);
        }
    }

    // If targetLocation does not exist, it will be created.
    public static void copyDirectory(final File source, final File target) {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdir();
            }
            for (String child: source.list()) {
                copyDirectory(new File(source, child), new File(target, child));
            }
        } else {
            try {
                write(new FileInputStream(source),  new FileOutputStream(target));
            } catch (IOException e) {
                throw new WTFException(e);
            }
        }
    }
}
