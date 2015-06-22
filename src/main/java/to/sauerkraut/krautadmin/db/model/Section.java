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

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;
import ru.vyarus.guice.persist.orient.db.scheme.annotation.Persistent;
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.field.ci.CaseInsensitive;
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.field.index.Index;
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
public class Section extends Model {

    @NotBlank
    @Length(min = Constant.MIN_SIZE_NAME, max = Constant.MAX_SIZE_NAME)
    @Pattern(regexp = Constant.NAME_PATTERN_STRING)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @CaseInsensitive
    private String name;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC_WITH_IMAGES)
    @Pattern(regexp = Constant.TEXT_PATTERN_STRING)
    @Length(min = 0, max = Constant.MAX_SIZE_DESCRIPTION)
    @CaseInsensitive
    private String description;

    @NotNull
    private Set<Category> categories;

    @NotBlank
    @CaseInsensitive
    @Length(min = Constant.MIN_SIZE_SHORT_NAME, max = Constant.MAX_SIZE_SHORT_NAME)
    @Pattern(regexp = Constant.NAME_PATTERN_STRING)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Index(OClass.INDEX_TYPE.UNIQUE)
    private String shortName;

    public Section() {
        this.categories = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(final Set<Category> categories) {
        this.categories = categories;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(final String shortName) {
        this.shortName = shortName;
    }
}
