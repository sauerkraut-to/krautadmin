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

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Path("/session")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {
    
    @POST
    @Path("/login")
    public void login(@FormParam("username") final String username, @FormParam("password") final String password, 
            @Auth final Subject subject) {
        subject.login(new UsernamePasswordToken(username, password));
        //return username;
    }
    
    @POST
    @Path("/logout")
    public void logout(@Auth final Subject subject) {
        subject.logout();
    }
}