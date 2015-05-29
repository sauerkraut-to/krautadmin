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
package to.sauerkraut.krautadmin.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import javax.inject.Inject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;
import to.sauerkraut.krautadmin.client.dto.GenericResponse;
import to.sauerkraut.krautadmin.db.model.LoginAttempt;
import to.sauerkraut.krautadmin.db.repository.LoginAttemptRepository;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Path("/session")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {

    @Log
    private static Logger logger;

    @Inject
    private LoginAttemptRepository loginAttemptRepository;
    @Inject
    private KrautAdminConfiguration configuration;
    
    @POST
    @Path("/login")
    public GenericResponse<String> login(@FormParam("username") final String username,
                                         @FormParam("password") final String password,
                                         @Auth final Subject subject, @Context final HttpServletRequest request) {

        // do not set a rememberMe-cookie on non-encrypted connections
        final boolean rememberMe = !(!request.isSecure()
                && configuration.getSecurityConfiguration().getRememberMeCookieConfiguration().isSecure());
        final KrautAdminConfiguration.SecurityConfiguration securityConfiguration =
                configuration.getSecurityConfiguration();

        final String hashedIp = DigestUtils.md5Hex(request.getRemoteAddr());
        final int banDays = securityConfiguration.getBanDays();
        final int maximumFailedAttempts = securityConfiguration.getMaximumFailedAttempts();
        final LoginAttempt loginAttempt = loginAttemptRepository.findByHashedIp(hashedIp);

        if (loginAttempt != null && loginAttempt.getFailedAttempts() >= maximumFailedAttempts) {
            throw new AuthenticationException("Es sind nicht mehr als "
                    + maximumFailedAttempts
                    + " fehlerhafte Versuche pro IP und innerhalb einer Ban-Periode ("
                    + banDays + " Tag" + (banDays != 1 ? "e" : "") + ") "
                    + "erlaubt (IPs werden nicht gespeichert, sondern nur deren Hash-Werte "
                    + "und diese nur kurzfristig und nur, wenn die Login-Versuche erfolglos bleiben - "
                    + "bei erfolgreichem Login wird weder IP noch Hash gespeichert!). "
                    + "Der letze fehlerhafte Versuch fand statt am "
                    + (new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm")
                    .format(loginAttempt.getLastAttempt()) + " Uhr (Serverzeit)")
            );
        } else {
            return login(hashedIp, subject, username, password, rememberMe);
        }
    }

    private GenericResponse<String> login(final String hashedIp, final Subject subject, final String username,
                       final String password, final boolean rememberMe) {
        loginAttemptRepository.upsert(hashedIp, new Date());
        loginAttemptRepository.incrementFailedAttempt(hashedIp, 1);
        subject.login(new UsernamePasswordToken(username, password, rememberMe));
        // after successful login delete hashed ip from LoginAttempts log
        loginAttemptRepository.deleteByHashedIp(hashedIp);
        return new GenericResponse<>();
    }
    
    @POST
    @Path("/logout")
    public GenericResponse<String> logout(@Auth final Subject subject) {
        subject.logout();

        return new GenericResponse<>();
    }
}
