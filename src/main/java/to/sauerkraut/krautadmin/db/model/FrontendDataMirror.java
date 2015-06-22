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

import org.hibernate.validator.constraints.*;
import ru.vyarus.guice.persist.orient.db.scheme.annotation.Persistent;
import ru.vyarus.guice.persist.orient.db.scheme.initializer.ext.field.ci.CaseInsensitive;
import to.sauerkraut.krautadmin.core.Constant;

import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
public class FrontendDataMirror extends Model {

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Length(min = Constant.MIN_SIZE_NAME, max = Constant.MAX_SIZE_NAME)
    @Pattern(regexp = Constant.NAME_PATTERN_STRING)
    @CaseInsensitive
    @NotBlank
    private String name;

    @NotEmpty
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String ftpServer;

    @NotEmpty
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String ftpUsername;

    @NotEmpty
    private String ftpPassword;

    private int ftpPort;

    @NotEmpty
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String ftpBasePath;

    @NotEmpty
    @URL
    private String httpBaseUrl;

    private boolean visible;

    private boolean active;

    private Date syncedUntil;

    private Date lastFailedSync;

    private String lastErrorMessage;

    private boolean implicit;

    private SecureProtocol secureProtocol;
    
    public FrontendDataMirror() {
        this.syncedUntil = new Date(0);
        this.ftpPort = 21;
        this.visible = false;
        this.active = false;
        this.ftpBasePath = "/";
    }

    public FrontendDataMirror(final SecureProtocol secureProtocol) {
        this(secureProtocol, false);
    }

    public FrontendDataMirror(final SecureProtocol secureProtocol, final boolean implicit) {
        this();
        this.secureProtocol = secureProtocol;
        this.implicit = implicit;
    }

    public boolean getSecure() {
        return secureProtocol != null;
    }

    public boolean getImplicit() {
        return implicit;
    }

    public void setImplicit(final boolean implicit) {
        this.implicit = implicit;
    }

    public SecureProtocol getSecureProtocol() {
        return secureProtocol;
    }

    public void setSecureProtocol(final SecureProtocol secureProtocol) {
        this.secureProtocol = secureProtocol;
    }

    /**
     * 
     * @return true, if this FrontendDataMirror is publicly visible
     */
    public boolean getVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * 
     * @return true, if this FrontendDataMirror will be deployed to on ReferenceLink-updates
     */
    public boolean getActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public Date getLastFailedSync() {
        return lastFailedSync;
    }

    public void setLastFailedSync(final Date lastFailedSync) {
        this.lastFailedSync = lastFailedSync;
    }

    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(final String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public String getFtpUsername() {
        return ftpUsername;
    }

    public void setFtpUsername(final String ftpUsername) {
        this.ftpUsername = ftpUsername;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public void setFtpPassword(final String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    public String getFtpBasePath() {
        return ftpBasePath;
    }

    public void setFtpBasePath(final String ftpBasePath) {
        this.ftpBasePath = ftpBasePath;
    }

    public String getHttpBaseUrl() {
        return httpBaseUrl;
    }

    public void setHttpBaseUrl(final String httpBaseUrl) {
        this.httpBaseUrl = httpBaseUrl;
    }

    public Date getSyncedUntil() {
        return syncedUntil;
    }

    public void setSyncedUntil(final Date syncedUntil) {
        this.syncedUntil = syncedUntil;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(final int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(final String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    /**
     *
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    public enum SecureProtocol {
        SSL, TLS
    }
}
