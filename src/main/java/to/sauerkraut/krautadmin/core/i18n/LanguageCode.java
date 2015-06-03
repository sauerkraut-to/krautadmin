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
    // Bulgarian
    bg("Bulgarisch"),
    // Catalan
    ca("Katalanisch"),
    // Czech
    cs("Tschechisch"),
    // Danish
    da("Dänisch"),
    // German
    de("Deutsch"),
    // Brazilian Portuguese
    pt_BR("Brasilianisches Portugiesisch"),
    // Greek
    el("Griechisch"),
    // English
    en("Englisch"),
    // Spanish
    es("Spanisch"),
    // Persian
    fa("Persisch"),
    // French
    fr("Französisch"),
    // Galician
    gl("Galicisch"),
    // Hindi
    hi("Hindi"),
    // Croatian
    hr("Kroatisch"),
    // Hungarian
    hu("Ungarisch"),
    // Indonesian
    id("Indonesisch"),
    // Italian
    it("Italienisch"),
    // Cambodian
    km("Kambodschanisch"),
    // Korean
    ko("Koreanisch"),
    // Traditional Chinese
    zh_TW("Tradidionelles Chinesisch"),
    // Latvian
    lv("Lettisch"),
    // Dutch
    nl("Niederländisch"),
    // Norwegian
    no("Norwegisch"),
    // Polish
    pl("Polnisch"),
    // Portuguese
    pt("Portugiesisch"),
    // Romanian
    ro("Rumänisch"),
    // Russian
    ru("Russisch"),
    // Slovak
    sk("Slowakisch"),
    // Slovenian
    sl("Slowenisch"),
    // Swedish
    sv("Schwedisch"),
    // Turkish
    tr("Türkisch"),
    // Ukrainian
    uk("Ukrainisch"),
    // Vietnamese
    vi("Vietnamesisch"),
    // Simplified Chinese
    zh("Vereinfachtes Chinesisch"),
    // other or unknown language
    unknown("Sonstige oder unbekannte Sprache"),
    // multiple languages
    multi("Mehrere Sprachen"),
    // porn language
    porn("Porno-Sprache");

    private final String displayName;

    LanguageCode(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
