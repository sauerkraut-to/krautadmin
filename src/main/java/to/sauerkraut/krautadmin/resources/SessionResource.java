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
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.glassfish.jersey.server.ManagedAsync;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;
import to.sauerkraut.krautadmin.client.dto.GenericResponse;
import to.sauerkraut.krautadmin.db.model.LoginAttempt;
import to.sauerkraut.krautadmin.db.repository.LoginAttemptRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Path("/session")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {

    private static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool();
    private static final int SLEEP_TIME_IN_MILLISECONDS = 5000;

    @Log
    private static Logger logger;

    @Inject
    private LoginAttemptRepository loginAttemptRepository;
    
    @POST
    @Path("/login")
    @ManagedAsync
    @SuppressWarnings("checkstyle:anoninnerlength")
    public void login(@FormParam("username") final String username, @FormParam("password") final String password,
            @FormParam("rememberMe") final boolean rememberMe, @Auth final Subject subject,
                      @Suspended final AsyncResponse asyncResponse, @Context final HttpServletRequest request) {

        TASK_EXECUTOR.submit(new Runnable() {

            @Override
            public void run() {
                String hashedIp = null;
                try {
                    hashedIp = DigestUtils.md5Hex(request.getRemoteAddr());
                    final int banDays = 1;
                    final int maximumFailedAttempts = 3;
                    final LoginAttempt loginAttemptTooSoon = loginAttemptRepository
                            .findByHashedIpAndNewerThan(hashedIp,
                                    System.currentTimeMillis() - SLEEP_TIME_IN_MILLISECONDS);
                    final LoginAttempt loginAttemptLimitExceeded = loginAttemptRepository
                            .findByHashedIpAndLimitExceeded(hashedIp, maximumFailedAttempts);

                    if (null != loginAttemptTooSoon) {
                        asyncResponse.resume(new AuthenticationException("Gleichzeitige Login-Versuche "
                                + "von derselben IP sind nicht erlaubt (IPs werden nicht gespeichert, sondern nur "
                                + "deren Hash-Werte und diese nur kurzfristig und nur, "
                                + "wenn die Login-Versuche erfolglos bleiben)"));
                    } else if (null != loginAttemptLimitExceeded) {
                        asyncResponse.resume(new AuthenticationException("Es sind nicht mehr als "
                                + maximumFailedAttempts
                                + " fehlerhafte Versuche pro IP und innerhalb einer Ban-Periode ("
                                + banDays + " Tag" + (banDays != 1 ? "e" : "") + ") "
                                + "erlaubt (IPs werden nicht gespeichert, sondern nur deren Hash-Werte "
                                + "und diese nur kurzfristig und nur, wenn die Login-Versuche erfolglos bleiben). "
                                + "Der letze fehlerhafte Versuch fand statt am "
                                + (new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm")
                                .format(loginAttemptLimitExceeded.getLastAttempt()) + " Uhr (Serverzeit)")
                        ));
                    } else {
                        loginAttemptRepository.upsert(hashedIp, new Date());
                        Thread.sleep(SLEEP_TIME_IN_MILLISECONDS);
                        subject.login(new UsernamePasswordToken(username, password, rememberMe));
                        // after successful login delete hashed ip from LoginAttempts log
                        loginAttemptRepository.deleteByHashedIp(hashedIp);
                        asyncResponse.resume(new GenericResponse<>());
                    }
                } catch (final InterruptedException ex) {
                    asyncResponse.cancel();
                } catch (final ShiroException e) {
                    if (hashedIp != null) {
                        try {
                            loginAttemptRepository.increaseFailedAttemptByOneForHashedIp(hashedIp);
                        } catch (Exception shouldNotHappen) {
                            logger.error(shouldNotHappen.getMessage(), shouldNotHappen);
                        }
                    }
                    asyncResponse.resume(e);
                } catch (final Exception e) {
                    asyncResponse.resume(e);
                }
            }
        });
    }
    
    @POST
    @Path("/logout")
    public GenericResponse<String> logout(@Auth final Subject subject) {
        subject.logout();

        return new GenericResponse<>();
    }
}
