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

import java.util.regex.Pattern;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@SuppressWarnings("checkstyle:declarationorder")
public final class Constant {
    // regex prefix
    private static final String RP = "^";
    // regex suffix
    private static final String RS = "$";
    private static final String SUPPORTED_DOWNLOAD_PROTOCOLS_REGEX = "(ht|f)tps?";
    private static final String SUPPORTED_LINKING_PROTOCOLS_REGEX = "https?";
    private static final String CREDENTIALS_REGEX = "([a-zA-Z0-9\\.\\-\\_\\%]+(\\:[a-zA-Z0-9\\.\\-\\_\\%]+)?\\@)?";
    private static final String PATH_AND_ANCHOR_REGEX =
            "(:[0-9]+)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~\\!])*";
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
            "((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])";
    private static final String SUPPORTED_FQDN_REGEX = "(localhost|(([a-zA-Z0-9\\-\\.\\_]*\\.)*[a-zA-Z0-9\\-]+\\.[a-"
            + "zA-Z]{2,63})|" + IPV4_REGEX + "|(\\[" + IPV6_REGEX + "\\]))";
    private static final String PROTOCOL_SEPERATOR_REGEX = "\\://";

    public static final int MIN_SIZE_CAPTION = 3;
    public static final int MIN_SIZE_NAME = 3;
    public static final int MIN_SIZE_LONG_CAPTION = 3;
    public static final int MIN_SIZE_POSTING = 10;
    public static final int MIN_SIZE_DESCRIPTION = 10;
    public static final int MIN_SIZE_SHORT_DESCRIPTION = 10;
    public static final int MAX_SIZE_NAME = 50;
    public static final int MAX_SIZE_CAPTION = 100;
    public static final int MAX_SIZE_LONG_CAPTION = 200;
    public static final int MAX_SIZE_POSTING = 2000;
    public static final int MAX_SIZE_DESCRIPTION = 1000;
    public static final int MAX_SIZE_SHORT_DESCRIPTION = 500;

    public static final String DOWNLOAD_PROTOCOL_PATTERN_STRING = RP + SUPPORTED_DOWNLOAD_PROTOCOLS_REGEX + RS;
    public static final String LINKING_PROTOCOL_PATTERN_STRING = RP + SUPPORTED_LINKING_PROTOCOLS_REGEX + RS;
    public static final String FQDN_PATTERN_STRING = RP + SUPPORTED_FQDN_REGEX + RS;
    public static final String DOWNLOAD_URL_PATTERN_STRING = RP + SUPPORTED_DOWNLOAD_PROTOCOLS_REGEX
            + PROTOCOL_SEPERATOR_REGEX + CREDENTIALS_REGEX + SUPPORTED_FQDN_REGEX + PATH_AND_ANCHOR_REGEX + RS;
    public static final String LINKING_URL_PATTERN_STRING = RP + SUPPORTED_LINKING_PROTOCOLS_REGEX
            + PROTOCOL_SEPERATOR_REGEX + CREDENTIALS_REGEX + SUPPORTED_FQDN_REGEX + PATH_AND_ANCHOR_REGEX + RS;
    public static final String USER_OR_GROUP_NAME_PATTERN_STRING =
            RP + "[a-z0-9_-]{" + MIN_SIZE_NAME + ',' + MAX_SIZE_NAME + '}' + RS;
    public static final String CAPTION_PATTERN_STRING =
            RP + ".{" + MIN_SIZE_CAPTION + ',' + MAX_SIZE_CAPTION + '}' + RS;

    public static final Pattern CAPTION_PATTERN = Pattern.compile(CAPTION_PATTERN_STRING);
    public static final Pattern USER_OR_GROUP_NAME_PATTERN = Pattern.compile(USER_OR_GROUP_NAME_PATTERN_STRING);
    public static final Pattern DOWNLOAD_URL_PATTERN = Pattern.compile(DOWNLOAD_URL_PATTERN_STRING);
    public static final Pattern LINKING_URL_PATTERN = Pattern.compile(LINKING_URL_PATTERN_STRING);
    public static final Pattern DOWNLOAD_PROTOCOL_PATTERN = Pattern.compile(DOWNLOAD_PROTOCOL_PATTERN_STRING);
    public static final Pattern LINKING_PROTOCOL_PATTERN = Pattern.compile(LINKING_PROTOCOL_PATTERN_STRING);
    public static final Pattern FQDN_PATTERN = Pattern.compile(FQDN_PATTERN_STRING);

    private Constant() {

    }
}
