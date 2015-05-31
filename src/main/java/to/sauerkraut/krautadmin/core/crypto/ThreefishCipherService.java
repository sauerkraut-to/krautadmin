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
package to.sauerkraut.krautadmin.core.crypto;

import org.apache.shiro.crypto.*;
import org.apache.shiro.util.StringUtils;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class ThreefishCipherService extends DefaultBlockCipherService {

    public static final int RECOMMENDED_KEY_SIZE = 512;
    public static final OperationMode RECOMMENDED_OPERATION_MODE = OperationMode.CBC;
    public static final PaddingScheme RECOMMENDED_PADDING_SCHEME = PaddingScheme.PKCS5;
    private static final String ALGORITHM_NAME = "Threefish";
    private static final String TRANSFORMATION_STRING_DELIMITER = "/";

    public ThreefishCipherService(final int keySize) {
        super(ALGORITHM_NAME + "-" + keySize);
        setKeySize(keySize);
        setBlockSize(keySize);
        setInitializationVectorSize(keySize);
    }

    @Override
    protected String getTransformationString(final boolean streaming) {
        if (streaming) {
            throw new UnsupportedOperationException("streaming-mode for Threefish cipher is currently not supported");
        }

        final StringBuilder sb = new StringBuilder(getAlgorithmName());
        if (StringUtils.hasText(getModeName())) {
            sb.append(TRANSFORMATION_STRING_DELIMITER).append(getModeName());
        }
        if (StringUtils.hasText(getPaddingSchemeName())) {
            sb.append(TRANSFORMATION_STRING_DELIMITER).append(getPaddingSchemeName());
        }
        return sb.toString();
    }
}
