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
package to.sauerkraut.krautadmin.db.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import ru.vyarus.guice.persist.orient.db.scheme.annotation.Persistent;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
public class User {
    @Id
    private String id;
    @Version
    private Long version;
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @NotNull
    @Valid
    private final Set<Role> roles;
    private boolean active;
    private byte[] passwordSalt;
    
    public User() {
        this.roles = new HashSet<>();
        this.active = true;
    }
    
    public User(final String username, final String password) {
        this();
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(final byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
    }
}
