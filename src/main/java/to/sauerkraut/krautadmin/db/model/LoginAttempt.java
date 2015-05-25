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
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.field.index.Index;

import javax.validation.constraints.NotNull;
import java.util.Date;
/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
public class LoginAttempt extends Model {

    @NotBlank
    @Index(OClass.INDEX_TYPE.UNIQUE)
    private String hashedIp;
    @NotNull
    private Date lastAttempt;
    private int failedAttempts;

    public LoginAttempt() {

    }

    public LoginAttempt(final String hashedIp, final Date lastAttempt) {
        this();
        this.setHashedIp(hashedIp);
        this.setLastAttempt(lastAttempt);
    }

    public String getHashedIp() {
        return hashedIp;
    }

    public void setHashedIp(final String hashedIp) {
        this.hashedIp = hashedIp;
    }

    public Date getLastAttempt() {
        return lastAttempt;
    }

    public void setLastAttempt(final Date lastAttempt) {
        this.lastAttempt = lastAttempt;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(final int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
}
