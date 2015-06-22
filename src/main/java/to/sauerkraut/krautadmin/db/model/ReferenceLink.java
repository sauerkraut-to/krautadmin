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
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.field.index.lucene.LuceneIndex;
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.type.index.CompositeIndex;
import to.sauerkraut.krautadmin.core.Constant;
import to.sauerkraut.krautadmin.core.i18n.LanguageCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Set;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
@CompositeIndex(name = "uniqueReferenceLink",
        type = OClass.INDEX_TYPE.UNIQUE, fields = {"languageCode", "url", "anchor"}, ignoreNullValues = true)
public class ReferenceLink extends Model {

    // should be in the language the language-code indicates
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Pattern(regexp = Constant.NAME_PATTERN_STRING)
    @Length(min = Constant.MIN_SIZE_NAME, max = Constant.MAX_SIZE_NAME)
    @LuceneIndex
    @CaseInsensitive
    private String title;

    // should be in the language the language-code indicates
    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC_WITH_IMAGES)
    @Pattern(regexp = Constant.TEXT_PATTERN_STRING)
    @Length(min = Constant.MIN_SIZE_DESCRIPTION, max = Constant.MAX_SIZE_DESCRIPTION)
    @CaseInsensitive
    // do not index this field yet - searching in title and producer should be enough
    //@LuceneIndex
    private String description;

    @NotNull
    @CaseInsensitive
    private LanguageCode languageCode;

    @NotNull
    private Integer initialReleaseYear;

    private boolean consecutivelyReleased;

    // e.g. band name, author name, developer corporation name, artist name, journal name
    @Pattern(regexp = Constant.NAME_PATTERN_STRING)
    @Length(min = 0, max = Constant.MAX_SIZE_NAME)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @LuceneIndex
    @CaseInsensitive
    private String producer;

    private boolean prependWWW;

    // without www (if available) and without protocol
    @NotBlank
    @CaseInsensitive
    private String url;

    @CaseInsensitive
    private String anchor;

    @NotNull
    private Set<Section> sections;

    @NotNull
    private Set<Category> categories;

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

    public Set<Section> getSections() {
        return sections;
    }

    public void setSections(final Set<Section> sections) {
        this.sections = sections;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(final Set<Category> categories) {
        this.categories = categories;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(final String producer) {
        this.producer = producer;
    }
}
