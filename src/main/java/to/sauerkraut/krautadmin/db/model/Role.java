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
import javax.validation.constraints.NotNull;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.hibernate.validator.constraints.NotEmpty;
import ru.vyarus.guice.persist.orient.db.scheme.annotation.Persistent;
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.field.index.Index;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
public class Role extends Model {

    @NotEmpty
    @Index(OClass.INDEX_TYPE.UNIQUE)
    private String shortName;
    @NotEmpty
    private String name;
    @NotNull
    private Set<Permission> permissions;
    private String description;
    private int weight;
    
    public Role() {
        this.permissions = new HashSet<>();
    }
    
    public Role(final String shortName) {
        this();
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(final String shortName) {
        this.shortName = shortName;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(final int weight) {
        this.weight = weight;
    }

    public void setPermissions(final Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
