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
package to.sauerkraut.krautadmin.core.i18n;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public enum LanguageCode {
    // German
    de("Deutsch"),
    // English
    en("Englisch"),
    // multiple languages
    multi("Mehrere Sprachen"),
    // porn language
    porn("Porno-Sprache"),
    // Brazilian Portuguese
    pt_BR("Brasilianisches Portugiesisch"),
    // Bulgarian
    bg("Bulgarisch"),
    // Danish
    da("Dänisch"),
    // French
    fr("Französisch"),
    // Galician
    gl("Galicisch"),
    // Greek
    el("Griechisch"),
    // Hindi
    hi("Hindi"),
    // Indonesian
    id("Indonesisch"),
    // Italian
    it("Italienisch"),
    // Cambodian
    km("Kambodschanisch"),
    // Catalan
    ca("Katalanisch"),
    // Korean
    ko("Koreanisch"),
    // Croatian
    hr("Kroatisch"),
    // Latvian
    lv("Lettisch"),
    // Dutch
    nl("Niederländisch"),
    // Norwegian
    no("Norwegisch"),
    // Persian
    fa("Persisch"),
    // Polish
    pl("Polnisch"),
    // Portuguese
    pt("Portugiesisch"),
    // Romanian
    ro("Rumänisch"),
    // Russian
    ru("Russisch"),
    // Swedish
    sv("Schwedisch"),
    // Slovak
    sk("Slowakisch"),
    // Slovenian
    sl("Slowenisch"),
    // Spanish
    es("Spanisch"),
    // Traditional Chinese
    zh_TW("Tradidionelles Chinesisch"),
    // Czech
    cs("Tschechisch"),
    tr("Türkisch"),
    // Ukrainian
    uk("Ukrainisch"),
    // Hungarian
    hu("Ungarisch"),
    // Simplified Chinese
    zh("Vereinfachtes Chinesisch"),
    // Vietnamese
    vi("Vietnamesisch"),
    // other or unknown language
    unknown("Sonstige oder unbekannte Sprache");

    private final String displayName;

    LanguageCode(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
