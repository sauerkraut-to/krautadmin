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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import to.sauerkraut.krautadmin.db.repository.UserRepository;
import to.sauerkraut.krautadmin.jersey.GenericExceptionMapper;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class PermissionsFilter extends PermissionsAuthorizationFilter {

    @Inject
    private static UserRepository userRepository;

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws IOException {

        final Subject subject = getSubject(request, response);
        // If the subject isn't identified, redirect to login URL
        if (subject.getPrincipal() == null) {
            saveRequestAndRedirectToLogin(request, response);
        } else if (null == userRepository.findActiveByUsername(String.valueOf((String) subject.getPrincipal()))) {
            subject.logout();
            saveRequestAndRedirectToLogin(request, response);
        } else {
            final HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("utf-8");
            httpResponse.setStatus(403);
            final PrintWriter out = response.getWriter();
            out.print(GenericExceptionMapper.defaultJSON(
                    new AuthorizationException("Auf die gewünschte Ressource kann mangels Berechtigung "
                            + "leider nicht zugegriffen werden")));
            out.flush();
        }
        return false;
    }
}
