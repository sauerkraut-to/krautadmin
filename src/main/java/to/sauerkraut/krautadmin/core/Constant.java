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
package to.sauerkraut.krautadmin.core;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@SuppressWarnings("checkstyle:declarationorder")
public final class Constant {
    // regex prefix
    private static final String RP = "^";
    // regex suffix
    private static final String RS = "$";
    /*private static final String SUPPORTED_DOWNLOAD_PROTOCOLS_REGEX = "(ht|f)tps?";
    private static final String SUPPORTED_LINKING_PROTOCOLS_REGEX = "https?";
    private static final String CREDENTIALS_REGEX = "([a-zA-Z0-9\\.\\-_%]+(:[a-zA-Z0-9\\.\\-_%]+)?@)?";
    private static final String PORT_REGEX = "(:[0-9]+)?";
    private static final String PATH_SEPARATOR_REGEX = "/";
    private static final String PATH_WITH_QUERY_AND_ANCHOR_REGEX = "?([a-zA-Z0-9\\-\\._\\?,'/\\\\\\+&amp;%\\$#=~!])*";
    private static final String IPV6_REGEX = "(\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:"
            + "){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9]["
            + "0-9]|[1-9]?[0-9])){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4][0-9]|"
            + "1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3})|:))|(([0-9A-Fa-f]{1,4}"
            + ":){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])"
            + "(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,"
            + "4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4]"
            + "[0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-"
            + "Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|["
            + "1-9]?[0-9])){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:(("
            + "25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:"
            + "))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0"
            + "-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:)))(%.+)?\\s*)";
    private static final String IPV4_REGEX =
            "((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])";
    private static final String SUPPORTED_FQDN_REGEX = "(localhost|(([a-zA-Z0-9\\-\\._]*\\.)*[a-zA-Z0-9\\-]+\\.[a-"
            + "zA-Z]{2,63})|" + IPV4_REGEX + "|(\\[" + IPV6_REGEX + "\\]))";
    private static final String PROTOCOL_SEPARATOR_REGEX = "://";*/

    public static final int MIN_SIZE_CAPTION = 3;
    public static final int MIN_SIZE_NAME = 3;
    public static final int MIN_SIZE_USERNAME = 3;
    public static final int MIN_SIZE_SHORT_NAME = 3;
    public static final int MIN_SIZE_LONG_CAPTION = 3;
    public static final int MIN_SIZE_POSTING = 10;
    public static final int MIN_SIZE_DESCRIPTION = 10;
    public static final int MAX_SIZE_NAME = 60;
    public static final int MAX_SIZE_USERNAME = 35;
    public static final int MAX_SIZE_SHORT_NAME = 25;
    public static final int MAX_SIZE_CAPTION = 100;
    public static final int MAX_SIZE_LONG_CAPTION = 200;
    public static final int MAX_SIZE_POSTING = 2700;
    public static final int MAX_SIZE_DESCRIPTION = 1700;

    /*public static final String DOWNLOAD_PROTOCOL_PATTERN_STRING = RP + SUPPORTED_DOWNLOAD_PROTOCOLS_REGEX + RS;
    public static final String LINKING_PROTOCOL_PATTERN_STRING = RP + SUPPORTED_LINKING_PROTOCOLS_REGEX + RS;
    public static final String FQDN_PATTERN_STRING = RP + SUPPORTED_FQDN_REGEX + RS;
    public static final String DOWNLOAD_URL_PATTERN_STRING = RP + SUPPORTED_DOWNLOAD_PROTOCOLS_REGEX
            + PROTOCOL_SEPARATOR_REGEX + CREDENTIALS_REGEX + SUPPORTED_FQDN_REGEX + PORT_REGEX + PATH_SEPARATOR_REGEX
            + PATH_WITH_QUERY_AND_ANCHOR_REGEX + RS;
    public static final String LINKING_URL_PATTERN_STRING = RP + SUPPORTED_LINKING_PROTOCOLS_REGEX
            + PROTOCOL_SEPARATOR_REGEX + CREDENTIALS_REGEX + SUPPORTED_FQDN_REGEX + PORT_REGEX + PATH_SEPARATOR_REGEX
            + PATH_WITH_QUERY_AND_ANCHOR_REGEX + RS;*/
    public static final String NAME_PATTERN_STRING =
            RP + "([\\p{L}\\p{M}\\p{S}\\p{N}\\p{P}]+(\\p{Zs}+[\\p{L}\\p{M}\\p{S}\\p{N}\\p{P}])*)+" + RS;
    public static final String TEXT_PATTERN_STRING =
            RP + "([\\p{L}\\p{M}\\p{S}\\p{N}\\p{P}]+([\\p{Z}\\p{C}]+[\\p{L}\\p{M}\\p{S}\\p{N}\\p{P}])*)+" + RS;
    public static final String SUPPORTED_LINKING_PROTOCOLS_STRING = RP + "https?" + RS;

    private Constant() {

    }
}
