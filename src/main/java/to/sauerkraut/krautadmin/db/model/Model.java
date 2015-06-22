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

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.object.enhancement.OObjectEntitySerializer;
import ru.vyarus.guice.persist.orient.db.PersistentContext;
import ru.vyarus.guice.persist.orient.db.scheme.annotation.Persistent;
import ru.vyarus.guice.persist.orient.db.transaction.template.TxAction;

import javax.inject.Inject;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
public abstract class Model {

    public static final String TEMPORARY_ID = "#-1:-1";

    @Inject
    private static PersistentContext<OObjectDatabaseTx> context;

    @Inject
    private static ValidatorFactory validatorFactory;

    @Id
    private String id;

    @Version
    private Long version;

    public static PersistentContext<OObjectDatabaseTx> getContext() {
        return context;
    }

    public static ValidatorFactory getValidatorFactory() {
        return validatorFactory;
    }

    public String getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    @SuppressWarnings("unchecked")
    public <T extends Model> T save() {
        final T that = (T) this;
        return context.doInTransaction(new TxAction<T>() {
            @Override
            public T execute() throws Throwable {
                return context.getConnection().save(that);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Model> Set<ConstraintViolation<T>> validate() {
        return validatorFactory.getValidator().validate((T) this);
    }

    @SuppressWarnings("unchecked")
    public <T extends Model> T validateAndSave() {
        final T that = (T) this;

        final Validator validator = validatorFactory.getValidator();
        final Set<ConstraintViolation<T>> violations = validator.validate(that);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return save();
    }

    @SuppressWarnings("unchecked")
    public <T extends Model> Void delete() {
        final T that = (T) this;
        return context.doInTransaction(new TxAction<Void>() {
            @Override
            public Void execute() throws Throwable {
                context.getConnection().delete(that);

                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Model> T attach() {
        final T that = (T) this;
        return context.doInTransaction(new TxAction<T>() {
            @Override
            public T execute() throws Throwable {
                return OObjectEntitySerializer.attach(that, context.getConnection());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Model> T detach() {
        final T that = (T) this;
        return context.doInTransaction(new TxAction<T>() {
            @Override
            public T execute() throws Throwable {
                return context.getConnection().detachAll(that, true);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Model> boolean isManaged() {
        final T that = (T) this;
        return context.doInTransaction(new TxAction<Boolean>() {
            @Override
            public Boolean execute() throws Throwable {
                return context.getConnection().isManaged(that);
            }
        });
    }

    /**
     * Refresh the entity state.
     */
    @SuppressWarnings("unchecked")
    public <T extends Model> T refresh() {
        final T that = (T) this;
        return context.doInTransaction(new TxAction<T>() {
            @Override
            public T execute() throws Throwable {
                return context.getConnection().reload(that);
            }
        });
    }

    public static <T extends Model> T newInstance(final Class<T> type) {
        return context.doInTransaction(new TxAction<T>() {
            @Override
            public T execute() throws Throwable {
                return context.getConnection().newInstance(type);
            }
        });
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!this.getClass().isAssignableFrom(other.getClass())) {
            return false;
        }
        if (this.getId() == null || TEMPORARY_ID.equals(this.getId())) {
            return false;
        }
        return this.getId().equals(((Model) other).getId());
    }

    @Override
    public int hashCode() {
        if (this.getId() == null || TEMPORARY_ID.equals(this.getId())) {
            return 0;
        }
        return this.getId().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + this.getId() == null ? "unmanaged" : this.getId() + "]";
    }
}
