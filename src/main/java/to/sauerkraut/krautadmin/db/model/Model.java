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
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Persistent
public abstract class Model {

    @Inject
    private static Application application;

    @Inject
    private transient ValidatorFactory validatorFactory;

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

    @SuppressWarnings("unchecked")
    public <T extends Model> T save() {
        injectDependenciesIfNecessary();
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

    @SuppressWarnings("unchecked")
    public <T extends Model> T attach() {
        injectDependenciesIfNecessary();
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
        injectDependenciesIfNecessary();
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
        injectDependenciesIfNecessary();
        final T that = (T) this;
        return context.doWithoutTransaction(new TxAction<Boolean>() {
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
        injectDependenciesIfNecessary();
        final T that = (T) this;
        return context.doWithoutTransaction(new TxAction<T>() {
            @Override
            public T execute() throws Throwable {
                return context.getConnection().reload(that);
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
        if (this.getId() == null) {
            return false;
        }
        return this.getId().equals(((Model) other).getId());
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return 0;
        }
        return this.getId().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + this.getId() == null ? "unmanaged" : this.getId() + "]";
    }

    private synchronized void injectDependenciesIfNecessary() {
        if (context == null) {
            InjectorLookup.getInjector(application).get().injectMembers(this);
        }
    }

    /**
     * Originating from play!framework v1.x, modified by sauerkraut.to.
     */
    public static class Property {
        private String name;
        private Class<?> type;
        private Field field;
        private boolean isSearchable;
        private boolean isMultiple;
        private boolean isRelation;
        private boolean isGenerated;
        private Class<?> relationType;
        private Choices choices;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(final Class<?> type) {
            this.type = type;
        }

        public Field getField() {
            return field;
        }

        public void setField(final Field field) {
            this.field = field;
        }

        public boolean isSearchable() {
            return isSearchable;
        }

        public void setIsSearchable(final boolean isSearchable) {
            this.isSearchable = isSearchable;
        }

        public boolean isMultiple() {
            return isMultiple;
        }

        public void setIsMultiple(final boolean isMultiple) {
            this.isMultiple = isMultiple;
        }

        public boolean isRelation() {
            return isRelation;
        }

        public void setIsRelation(final boolean isRelation) {
            this.isRelation = isRelation;
        }

        public boolean isGenerated() {
            return isGenerated;
        }

        public void setIsGenerated(final boolean isGenerated) {
            this.isGenerated = isGenerated;
        }

        public Class<?> getRelationType() {
            return relationType;
        }

        public void setRelationType(final Class<?> relationType) {
            this.relationType = relationType;
        }

        public Choices getChoices() {
            return choices;
        }

        public void setChoices(final Choices choices) {
            this.choices = choices;
        }
    }

    /**
     * Originating from play!framework v1.x, modified by sauerkraut.to.
     */
    public interface Factory {
        String keyName();
        Class<?> keyType();
        Object keyValue(Model m);
        Model findById(Object id);
        List<Model> fetch(int offset, int length, String orderBy, String orderDirection,
                                 List<String> properties, String keywords, String where);
        Long count(List<String> properties, String keywords, String where);
        void deleteAll();
        List<Model.Property> listProperties();
    }

    /**
     * Originating from play!framework v1.x, modified by sauerkraut.to.
     */
    public interface Choices {
        List<Object> list();
    }
}
