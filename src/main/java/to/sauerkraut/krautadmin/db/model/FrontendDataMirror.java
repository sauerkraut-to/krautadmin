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

import java.util.Date;
import javax.persistence.Id;
import javax.persistence.Version;
import org.hibernate.validator.constraints.NotEmpty;
import ru.vyarus.guice.persist.orient.db.scheme.annotation.Persistent;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
public class FrontendDataMirror {
    @Id
    private String id;
    @Version
    private Long version;
    private String name;
    @NotEmpty
    private String ftpServer;
    @NotEmpty
    private String ftpUsername;
    @NotEmpty
    private String ftpPassword;
    private int ftpPort;
    @NotEmpty
    private String ftpBasePath;
    @NotEmpty
    private String httpBaseUrl;
    private boolean visible; 
    private boolean active; 
    private Date syncedUntil;
    private Date lastFailedSync;
    
    public FrontendDataMirror() {
        this.syncedUntil = new Date(Long.MIN_VALUE);
        this.ftpPort = 21;
        this.visible = false;
        this.active = false;
        this.ftpBasePath = "/";
    }

    /**
     * 
     * @return true, if this FrontendDataMirror is publicly visible
     */
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * 
     * @return true, if this FrontendDataMirror will be deployed to on ReferenceLink-updates
     */
    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public Long getVersion() {
        return version;
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
}