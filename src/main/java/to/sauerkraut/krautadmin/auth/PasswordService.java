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
        return new HashResult(computedHash.toBase64(), computedHash.getSalt().getBytes());
    }
    
    /**
    *
    * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
    */
    public static class HashResult {
        private final String hashedPasswordBase64;
        private final byte[] passwordSalt;
        
        public HashResult(final String hashedPasswordBase64, final byte[] passwordSalt) {
            this.hashedPasswordBase64 = hashedPasswordBase64;
            this.passwordSalt = passwordSalt;
        }

        public String getHashedPasswordBase64() {
            return hashedPasswordBase64;
        }

        public byte[] getPasswordSalt() {
            return passwordSalt;
        }
    }
}
