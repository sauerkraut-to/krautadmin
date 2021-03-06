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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;
import ru.vyarus.guice.persist.orient.db.scheme.annotation.Persistent;
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.field.ci.CaseInsensitive;
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.field.index.Index;
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.field.index.lucene.LuceneIndex;
import to.sauerkraut.krautadmin.core.Constant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
public class User extends Model {

    @CaseInsensitive
    @Index(OClass.INDEX_TYPE.UNIQUE)
    @LuceneIndex(name = "user.username.lucene")
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Length(min = Constant.MIN_SIZE_USERNAME, max = Constant.MAX_SIZE_USERNAME)
    @Pattern(regexp = Constant.NAME_PATTERN_STRING)
    private String username;

    @NotBlank
    @JsonIgnore
    private String passwordHash;

    @NotNull
    private Set<Role> roles;

    private boolean active;

    @NotBlank
    @JsonIgnore
    private String passwordSalt;

    private boolean passwordChangeAdvised;
    
    public User() {
        this.roles = new HashSet<>();
        this.active = true;
    }
    
    public User(final String username, final String passwordHash) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(final String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public boolean getPasswordChangeAdvised() {
        return passwordChangeAdvised;
    }

    public void setPasswordChangeAdvised(final boolean passwordChangeAdvised) {
        this.passwordChangeAdvised = passwordChangeAdvised;
    }

    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }
}
