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
package to.sauerkraut.krautadmin.auth;

import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.crypto.hash.ConfigurableHashService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.SimpleHashRequest;
import org.apache.shiro.util.ByteSource;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Singleton
public class PasswordService {
    private final ConfigurableHashService configurableHashService;
    
    public PasswordService() {
        final DefaultHashService hashService = new DefaultHashService();
        hashService.setGeneratePublicSalt(true);
        this.configurableHashService = hashService;
    }

    public ConfigurableHashService getConfigurableHashService() {
        return configurableHashService;
    }
    
    public HashResult hashPassword(final String plainTextPassword) {
        final Hash computedHash = configurableHashService.computeHash(
                new SimpleHashRequest(null, ByteSource.Util.bytes(plainTextPassword), null, -1));
        return new HashResult(computedHash.toBase64(), Base64.encodeBase64String(computedHash.getSalt().getBytes()));
    }
    
    /**
    *
    * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
    */
    public static class HashResult {
        private final String hashedPasswordBase64;
        private final String passwordSaltBase64;
        
        public HashResult(final String hashedPasswordBase64, final String passwordSaltBase64) {
            this.hashedPasswordBase64 = hashedPasswordBase64;
            this.passwordSaltBase64 = passwordSaltBase64;
        }

        public String getHashedPasswordBase64() {
            return hashedPasswordBase64;
        }

        public String getPasswordSaltBase64() {
            return passwordSaltBase64;
        }
    }
}
