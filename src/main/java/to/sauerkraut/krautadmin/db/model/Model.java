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

import com.google.inject.Inject;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.object.enhancement.OObjectEntitySerializer;
import io.dropwizard.Application;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.guice.persist.orient.db.PersistentContext;
import ru.vyarus.guice.persist.orient.db.scheme.annotation.Persistent;
import ru.vyarus.guice.persist.orient.db.transaction.template.TxAction;

import javax.persistence.Id;
import javax.persistence.Version;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 * @param <T> the implementing data model class
 */
@Persistent
public abstract class Model<T> {

    private static final Application APP = null;

    @Inject
    private transient PersistentContext<OObjectDatabaseTx> context;

    @Id
    private String id;
    @Version
    private Long version;

    public String getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public T save() {
        injectDependenciesIfNecessary();
        final T that = (T) this;
        return context.doInTransaction(new TxAction<T>() {
            @Override
            public T execute() throws Throwable {
                return context.getConnection().save(that);
            }
        });
    }

    public Void delete() {
        injectDependenciesIfNecessary();
        final T that = (T) this;
        return context.doInTransaction(new TxAction<Void>() {
            @Override
            public Void execute() throws Throwable {
                context.getConnection().delete(that);

                return null;
            }
        });
    }

    public T attach() {
        injectDependenciesIfNecessary();
        final T that = (T) this;
        return context.doInTransaction(new TxAction<T>() {
            @Override
            public T execute() throws Throwable {
                return OObjectEntitySerializer.attach(that, context.getConnection());
            }
        });
    }

    public T detach() {
        injectDependenciesIfNecessary();
        final T that = (T) this;
        return context.doInTransaction(new TxAction<T>() {
            @Override
            public T execute() throws Throwable {
                return context.getConnection().detachAll(that, true);
            }
        });
    }

    private synchronized void injectDependenciesIfNecessary() {
        if (context == null) {
            InjectorLookup.getInjector(APP).get().injectMembers(this);
        }
    }
}
