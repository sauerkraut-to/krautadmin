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
package to.sauerkraut.krautadmin.auth;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.secnod.dropwizard.shiro.ShiroBundle;
import org.secnod.dropwizard.shiro.ShiroConfiguration;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;
import to.sauerkraut.krautadmin.core.crypto.ThreefishCipherService;

import javax.inject.Inject;
import javax.servlet.Filter;
import java.util.Collection;
import java.util.Collections;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@SuppressWarnings("checkstyle:classdataabstractioncoupling")
public class SecureShiroBundle extends ShiroBundle<KrautAdminConfiguration> {
    @Inject
    private static ThreefishCipherService cipherService;

    protected final MemoryConstrainedCacheManager shiroCacheManager;

    public SecureShiroBundle() {
        super();
        this.shiroCacheManager = new MemoryConstrainedCacheManager();
    }

    @Override
    protected ShiroConfiguration narrow(final KrautAdminConfiguration configuration) {
        return configuration.getShiroConfiguration();
    }
    @Override
    protected Collection<org.apache.shiro.realm.Realm> createRealms(final KrautAdminConfiguration configuration) {
        final KrautAdminConfiguration.SecurityConfiguration securityConfiguration =
                configuration.getSecurityConfiguration();
        final HashedCredentialsMatcher hashedCredentialsMatcher =
                new HashedCredentialsMatcher(securityConfiguration.getPasswordHashFormat());
        hashedCredentialsMatcher.setHashIterations(securityConfiguration.getPasswordHashIterations());
        hashedCredentialsMatcher.setStoredCredentialsHexEncoded(false);
        final to.sauerkraut.krautadmin.auth.Realm r =
                new to.sauerkraut.krautadmin.auth.Realm(shiroCacheManager,
                        hashedCredentialsMatcher);
        r.setAuthorizationCachingEnabled(configuration.getSecurityConfiguration().isAuthorizationCachingEnabled());
        return Collections.singleton((org.apache.shiro.realm.Realm) r);
    }
    @Override
    protected Filter createFilter(final KrautAdminConfiguration configuration) {
        final ShiroConfiguration shiroConfig = narrow(configuration);
        final IniWebEnvironment shiroEnv = new IniWebEnvironment();
        shiroEnv.setConfigLocations(shiroConfig.iniConfigs());
        shiroEnv.init();

        return new SecureShiroFilter(configuration, shiroEnv);
    }

    /**
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    private class SecureShiroFilter extends AbstractShiroFilter {

        private final KrautAdminConfiguration configuration;
        private final IniWebEnvironment shiroEnv;

        public SecureShiroFilter(final KrautAdminConfiguration configuration, final IniWebEnvironment shiroEnv) {
            this.configuration = configuration;
            this.shiroEnv = shiroEnv;
        }

        @Override
        public void init() throws Exception {
            final Collection<org.apache.shiro.realm.Realm> realms = createRealms(configuration);
            if (realms.isEmpty()) {
                setSecurityManager(shiroEnv.getWebSecurityManager());
            } else {
                final DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
                final CookieRememberMeManager cookieRememberMeManager =
                        new ConfigurableCookieRememberMeManager(
                                configuration.getSecurityConfiguration()
                                        .getRememberMeCookieConfiguration());

                cookieRememberMeManager.setCipherService(cipherService);
                cookieRememberMeManager.setCipherKey(Base64.decode(configuration.getApplicationSecret()));

                securityManager.setRememberMeManager(cookieRememberMeManager);
                securityManager.setRealms(realms);
                securityManager.setCacheManager(shiroCacheManager);
                setSecurityManager(securityManager);
            }

            setFilterChainResolver(shiroEnv.getFilterChainResolver());
        }
    }
}
