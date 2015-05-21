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

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import to.sauerkraut.krautadmin.db.model.Permission;
import to.sauerkraut.krautadmin.db.model.Role;
import to.sauerkraut.krautadmin.db.model.User;
import to.sauerkraut.krautadmin.db.repository.UserRepository;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class Realm extends AuthorizingRealm {
    public static final String ERROR_USERNAME_NOT_PROVIDED = "Es wurde kein Benutzername angegeben";
    public static final String ERROR_USER_NOT_EXISTS = "Dieser Benutzer existiert nicht";

    @Inject
    private UserRepository userRepository;
    
    public Realm() {
        super(null, null);
    }

    public Realm(final CacheManager cacheManager) {
        super(cacheManager, null);
    }

    public Realm(final CredentialsMatcher matcher) {
        super(null, matcher);
    }
    
    public Realm(final CacheManager cacheManager, final CredentialsMatcher matcher) {
        super(cacheManager, matcher);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            final AuthenticationToken token)
            throws AuthenticationException {
        final UsernamePasswordToken credentials = (UsernamePasswordToken) token;
        final String username = credentials.getUsername();
        if (username == null) {
            throw new UnknownAccountException(ERROR_USERNAME_NOT_PROVIDED);
        }
        final User user = userRepository.findByUsernameAndActive(username, true);
        if (user == null) {
            throw new UnknownAccountException(ERROR_USER_NOT_EXISTS);
        }
        return new SimpleAuthenticationInfo(username, user.getPassword().toCharArray(),
                ByteSource.Util.bytes(user.getPasswordSalt()), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            final PrincipalCollection principals) {
        // retrieve role names and permission names
        final String username = (String) principals.getPrimaryPrincipal();
        final User user = userRepository.findByUsernameAndActive(username, true);
        if (user == null) {
            throw new UnknownAccountException(ERROR_USER_NOT_EXISTS);
        }
        final int totalRoles = user.getRoles().size();
        final Set<String> roleNames = new LinkedHashSet<>(totalRoles);
        final Set<String> permissionNames = new LinkedHashSet<>();
        if (totalRoles > 0) {
            for (Role role : user.getRoles()) {
                roleNames.add(role.getName());
                for (Permission permission : role.getPermissions()) {
                    permissionNames.add(permission.getName());
                }
            }
        }
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
        info.setStringPermissions(permissionNames);
        return info;
    }
}
