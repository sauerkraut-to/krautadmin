package to.sauerkraut.krautadmin.resources.assets;

import com.google.common.cache.CacheBuilderSpec;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An assets bundle (like {@link io.dropwizard.assets.AssetsBundle}) that utilizes configuration to provide the
 * ability to override how assets are loaded and cached.  Specifying an override is useful during the development phase
 * to allow assets to be loaded directly out of source directories instead of the classpath and to force them to not be
 * cached by the browser or the server.  This allows developers to edit an asset, save and then immediately refresh the
 * web browser and see the updated assets.  No compilation or copy steps are necessary.
 * @param <T> configuration type
 */
public class ConfiguredAssetsBundle<T extends Configuration & HasAssetsConfiguration> implements ConfiguredBundle<T> {
    public static final CacheBuilderSpec DEFAULT_CACHE_SPEC = CacheBuilderSpec.parse("maximumSize=100");
    private static final String DEFAULT_ROOT = "/";
    private static final String DEFAULT_PATH = "/assets";
    private static final String DEFAULT_INDEX_FILE = "index.html";
    private static final String DEFAULT_SERVLET_MAPPING_NAME = "assets";
    
    private final String resourcePath;
    private final CacheBuilderSpec cacheBuilderSpec;
    private final String uriPath;
    private final String indexFile;
    private final String assetsName;

    /**
     * Creates a new {@link ConfiguredAssetsBundle} which serves up static assets from
     * {@code src/main/resources/assets/*} as {@code /assets/*}.
     *
     * @see ConfiguredAssetsBundle#ConfiguredAssetsBundle(String, CacheBuilderSpec)
     */
    public ConfiguredAssetsBundle() {
        this(DEFAULT_PATH, DEFAULT_CACHE_SPEC);
    }

    /**
     * Creates a new {@link ConfiguredAssetsBundle} which will configure the service to serve the static files
     * located in {@code src/main/resources/${path}} as {@code /${path}}. For example, given a
     * {@code path} of {@code "/assets"}, {@code src/main/resources/assets/example.js} would be
     * served up from {@code /assets/example.js}.
     *
     * @param path the classpath and URI root of the static asset files
     * @see ConfiguredAssetsBundle#ConfiguredAssetsBundle(String, CacheBuilderSpec)
     */
    public ConfiguredAssetsBundle(final String path) {
        this(path, DEFAULT_CACHE_SPEC, path, DEFAULT_INDEX_FILE, DEFAULT_SERVLET_MAPPING_NAME);
    }

    /**
     * Creates a new {@link ConfiguredAssetsBundle} which will configure the service to serve the static files
     * located in {@code src/main/resources/${resourcePath}} as {@code /${uriPath}}. For example, given a
     * {@code resourcePath} of {@code "/assets"} and a uriPath of {@code "/js"},
     * {@code src/main/resources/assets/example.js} would be served up from {@code /js/example.js}.
     *
     * @param resourcePath the resource path (in the classpath) of the static asset files
     * @param uriPath      the uri path for the static asset files
     * @see ConfiguredAssetsBundle#ConfiguredAssetsBundle(String, CacheBuilderSpec)
     */
    public ConfiguredAssetsBundle(final String resourcePath, final String uriPath) {
        this(resourcePath, DEFAULT_CACHE_SPEC, uriPath, DEFAULT_INDEX_FILE, DEFAULT_SERVLET_MAPPING_NAME);
    }

    /**
     * Creates a new AssetsBundle which will configure the service to serve the static files
     * located in {@code src/main/resources/${resourcePath}} as {@code /${uriPath}}. If no file name is
     * in ${uriPath}, ${indexFile} is appended before serving. For example, given a
     * {@code resourcePath} of {@code "/assets"} and a uriPath of {@code "/js"},
     * {@code src/main/resources/assets/example.js} would be served up from {@code /js/example.js}.
     *
     * @param resourcePath        the resource path (in the classpath) of the static asset files
     * @param uriPath             the uri path for the static asset files
     * @param indexFile           the name of the index file to use
     */
    public ConfiguredAssetsBundle(final String resourcePath, final String uriPath, final String indexFile) {
        this(resourcePath, DEFAULT_CACHE_SPEC, uriPath, indexFile, DEFAULT_SERVLET_MAPPING_NAME);
    }

    /**
     * Creates a new AssetsBundle which will configure the service to serve the static files
     * located in {@code src/main/resources/${resourcePath}} as {@code /${uriPath}}. If no file name is
     * in ${uriPath}, ${indexFile} is appended before serving. For example, given a
     * {@code resourcePath} of {@code "/assets"} and a uriPath of {@code "/js"},
     * {@code src/main/resources/assets/example.js} would be served up from {@code /js/example.js}.
     *
     * @param resourcePath        the resource path (in the classpath) of the static asset files
     * @param uriPath             the uri path for the static asset files
     * @param indexFile           the name of the index file to use
     * @param assetsName          the name of servlet mapping used for this assets bundle
     */
    public ConfiguredAssetsBundle(final String resourcePath, final String uriPath, final String indexFile,
                                  final String assetsName) {
        this(resourcePath, DEFAULT_CACHE_SPEC, uriPath, indexFile, assetsName);
    }
    
    /**
     * Creates a new {@link ConfiguredAssetsBundle} which will configure the service to serve the static files
     * located in {@code src/main/resources/${path}} as {@code /${path}}. For example, given a
     * {@code path} of {@code "/assets"}, {@code src/main/resources/assets/example.js} would be
     * served up from {@code /assets/example.js}.
     *
     * @param resourcePath     the resource path (in the classpath) of the static asset files
     * @param cacheBuilderSpec the spec for the cache builder
     */
    public ConfiguredAssetsBundle(final String resourcePath, final CacheBuilderSpec cacheBuilderSpec) {
        this(resourcePath, cacheBuilderSpec, resourcePath, DEFAULT_INDEX_FILE, DEFAULT_SERVLET_MAPPING_NAME);
    }

    /**
     * Creates a new {@link ConfiguredAssetsBundle} which will configure the service to serve the static files
     * located in {@code src/main/resources/${resourcePath}} as {@code /${uriPath}}. For example, given a
     * {@code resourcePath} of {@code "/assets"} and a uriPath of {@code "/js"},
     * {@code src/main/resources/assets/example.js} would be served up from {@code /js/example.js}.
     *
     * @param resourcePath     the resource path (in the classpath) of the static asset files
     * @param cacheBuilderSpec the spec for the cache builder
     * @param uriPath          the uri path for the static asset files
     * @param indexFile        the name of the index file to use
     * @param assetsName       the name of servlet mapping used for this assets bundle
     */
    public ConfiguredAssetsBundle(final String resourcePath, final CacheBuilderSpec cacheBuilderSpec,
                                  final String uriPath, final String indexFile, final String assetsName) {
        checkArgument(resourcePath.startsWith(DEFAULT_ROOT), "%s is not an absolute path", resourcePath);
        checkArgument(!DEFAULT_ROOT.equals(resourcePath), "%s is the classpath root", resourcePath);
        this.resourcePath = resourcePath.endsWith(DEFAULT_ROOT) ? resourcePath : (resourcePath + '/');
        this.cacheBuilderSpec = cacheBuilderSpec;
        this.uriPath = uriPath.endsWith(DEFAULT_ROOT) ? uriPath : (uriPath + '/');
        this.indexFile = indexFile;
        this.assetsName = assetsName;
    }

    @Override
    public void run(final T bundleConfig, final Environment env) throws Exception {
        final KrautAdminConfiguration.AssetsConfiguration config = bundleConfig.getAssetsConfiguration();

        // Let the cache spec from the configuration override the one specified in the code
        final CacheBuilderSpec spec = (config.getCacheSpec() != null)
                ? CacheBuilderSpec.parse(config.getCacheSpec())
                : cacheBuilderSpec;

        final Iterable<Map.Entry<String, String>> overrides = config.getOverrides();
        final Iterable<Map.Entry<String, String>> mimeTypes = config.getMimeTypes();

        final AssetServlet servlet = new AssetServlet(resourcePath, spec, uriPath, indexFile, overrides, mimeTypes);
        env.servlets().addServlet(assetsName, servlet).addMapping(uriPath + "*");
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {

    }
}
