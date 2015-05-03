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

import ru.vyarus.guice.persist.orient.db.scheme.annotation.Persistent;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
public class SecureFrontendDataMirror extends FrontendDataMirror {
    private boolean isImplicit;
    private SecureFrontendDataMirrorProtocol protocol;
    
    public SecureFrontendDataMirror() {
        super();
        this.protocol = SecureFrontendDataMirrorProtocol.TLS;
        this.isImplicit = false;
    }
    
    public SecureFrontendDataMirror(final SecureFrontendDataMirrorProtocol protocol, final boolean isImplicit) {
        super();
        this.protocol = protocol;
        this.isImplicit = isImplicit;
    }

    public boolean isIsImplicit() {
        return isImplicit;
    }

    public void setIsImplicit(final boolean isImplicit) {
        this.isImplicit = isImplicit;
    }

    public SecureFrontendDataMirrorProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(final SecureFrontendDataMirrorProtocol protocol) {
        this.protocol = protocol;
    }
    
    /**
    *
    * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
    */
    public enum SecureFrontendDataMirrorProtocol {
        SSL, TLS
    }
}
