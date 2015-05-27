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

import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.servlet.Cookie;
import static to.sauerkraut.krautadmin.KrautAdminConfiguration.SecurityConfiguration.RememberMeCookieConfiguration;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class ConfigurableCookieRememberMeManager extends CookieRememberMeManager {

    public ConfigurableCookieRememberMeManager() {
        this(new RememberMeCookieConfiguration());
    }

    public ConfigurableCookieRememberMeManager(final RememberMeCookieConfiguration rememberMeCookieConfiguration) {
        super();
        configure(rememberMeCookieConfiguration);
    }

    public void configure(final RememberMeCookieConfiguration rememberMeCookieConfiguration) {
        final Cookie defaultCookie = getCookie();
        defaultCookie.setName(rememberMeCookieConfiguration.getName());
        defaultCookie.setMaxAge(rememberMeCookieConfiguration.getMaxAgeSeconds());
        defaultCookie.setHttpOnly(rememberMeCookieConfiguration.isHttpOnly());
        defaultCookie.setSecure(rememberMeCookieConfiguration.isSecure());
        defaultCookie.setDomain(rememberMeCookieConfiguration.getDomain());
        defaultCookie.setPath(rememberMeCookieConfiguration.getPath());
        defaultCookie.setVersion(rememberMeCookieConfiguration.getVersion());
    }
}
