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
import org.hibernate.validator.constraints.NotBlank;
import ru.vyarus.guice.persist.orient.db.scheme.annotation.Persistent;
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.type.index.CompositeIndex;
import to.sauerkraut.krautadmin.core.i18n.LanguageCode;

import javax.validation.constraints.NotNull;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
@CompositeIndex(name = "uniqueReferenceLink",
        type = OClass.INDEX_TYPE.UNIQUE, fields = {"languageCode", "url", "anchor"})
public class ReferenceLink extends Model {
    // should be in the language the language-code indicates
    @NotBlank
    private String title;
    // should be in the language the language-code indicates
    @NotBlank
    private String description;
    @NotNull
    private LanguageCode languageCode;
    @NotNull
    private Integer initialReleaseYear;
    private boolean consecutivelyReleased;
    private boolean prependWWW;
    // without www (if available) and without protocol
    @NotBlank
    private String url;
    //TODO: remove @NotNull annotation as soon as the library supports defining indexes that will not ignore null values
    @NotNull
    private String anchor;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public LanguageCode getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(final LanguageCode languageCode) {
        this.languageCode = languageCode;
    }

    public Integer getInitialReleaseYear() {
        return initialReleaseYear;
    }

    public void setInitialReleaseYear(final Integer initialReleaseYear) {
        this.initialReleaseYear = initialReleaseYear;
    }

    public boolean getConsecutivelyReleased() {
        return consecutivelyReleased;
    }

    public void setConsecutivelyReleased(final boolean consecutivelyReleased) {
        this.consecutivelyReleased = consecutivelyReleased;
    }

    public boolean getPrependWWW() {
        return prependWWW;
    }

    public void setPrependWWW(final boolean prependWWW) {
        this.prependWWW = prependWWW;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(final String anchor) {
        this.anchor = anchor;
    }
}
