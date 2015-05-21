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
package to.sauerkraut.krautadmin.db;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;
import ru.vyarus.guice.persist.orient.db.PersistentContext;
import ru.vyarus.guice.persist.orient.db.data.DataInitializer;

import javax.inject.Singleton;

import to.sauerkraut.krautadmin.KrautAdminConfiguration;
import to.sauerkraut.krautadmin.db.model.FrontendDataMirror;
import to.sauerkraut.krautadmin.db.model.SampleEntity;
import to.sauerkraut.krautadmin.db.repository.FrontendDataMirrorRepository;
import to.sauerkraut.krautadmin.db.repository.SampleEntityRepository;

import java.util.*;
import java.util.regex.Pattern;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Singleton
@Transactional
public class ApplicationUpgradeManagerAndFixturesLoader implements DataInitializer {

    private static Pattern keyPattern = Pattern.compile("([^(]+)\\(([^)]+)\\)");
    // Allows people to clear the cache, so Fixture is not stateful
    private static Map<String, Object> idCache = new HashMap<>();
    
    @Log
    private static Logger logger;
    @Inject
    private SampleEntityRepository repository;
    @Inject
    private FrontendDataMirrorRepository frontendDataMirrorRepository;
    @Inject
    private PersistentContext<OObjectDatabaseTx> context;
    @Inject
    private KrautAdminConfiguration configuration;
    
    @Override
    public void initializeData() {
        if (repository.count() == 0) {
            logger.info("Bootstrapping sample data");

            for (int i = 0; i < 20; i++) {
                //repository.save(new SampleEntity("comment" + i));
                (new SampleEntity("comment" + i)).save();
            }
            
            final FrontendDataMirror frontendDataMirror = new FrontendDataMirror(
                    FrontendDataMirror.SecureProtocol.SSL, true);
            frontendDataMirror.setActive(true);
            frontendDataMirror.setFtpServer("ftp.test.org");
            frontendDataMirror.setFtpUsername("testuser");
            frontendDataMirror.setFtpPassword("testpass");
            
            frontendDataMirrorRepository.save(frontendDataMirror);
        }
        
        //TODO: at the end execute update scripts in right order and only if not executed yet
    }

    /*
    private void loadModels(final String yamlFileName) throws Exception {
        //try {
        final String defaultDataModelPackage = configuration.getDatabaseConfiguration().getDefaultDataModelPackage();
        Yaml yaml = new Yaml();
        Object o = yaml.load(IO.readContentAsString(
                this.getClass().getResourceAsStream("/databaseFixtures/" + yamlFileName)));
        if (o instanceof LinkedHashMap<?, ?>) {
            Annotation[] annotations = Fixtures.class.getAnnotations();
            @SuppressWarnings("unchecked") LinkedHashMap<Object, Map<?, ?>> objects =
                    (LinkedHashMap<Object, Map<?, ?>>) o;
            for (Object key : objects.keySet()) {
                Matcher matcher = keyPattern.matcher(key.toString().trim());
                if (matcher.matches()) {
                    // Type of the object. i.e. models.employee
                    String type = matcher.group(1);
                    // Id of the entity i.e. nicolas
                    String id = matcher.group(2);
                    if (!type.contains(".")) {
                        type = defaultDataModelPackage.concat(type);
                    }

                    // Was the entity already defined?
                    if (idCache.containsKey(type + "-" + id)) {
                        throw new RuntimeException(
                                "duplicate id '" + id + "' for type " + type);
                    }


                    // Those are the properties that were parsed from the YML file
                    final Map<?, ?> entityValues =  objects.get(key);

                    // Prefix is object, why is that?
                    final Map<String, String[]> fields = serialize(entityValues, "object");


                    @SuppressWarnings("unchecked")
                    Class<Model> cType = (Class<Model>) Class.forName(type);
                    final Map<String, String[]> resolvedFields = resolveDependencies(cType, fields);

                    RootParamNode rootParamNode = ParamNode.convert(resolvedFields);
                    // This is kind of hacky.
                    // This basically says that if we have an embedded class we should ignore it.
                    if (Model.class.isAssignableFrom(cType)) {
                        boolean saveReturnedKey = false;

                        Model model = (Model) Binder.bind(rootParamNode, "object", cType, cType, annotations);
                        for(Field f : model.getClass().getFields()) {
                            if (f.getType().isAssignableFrom(Map.class)) {
                                f.set(model, objects.get(key).get(f.getName()));
                            }
                            if (f.getType().equals(byte[].class)) {
                                f.set(model, objects.get(key).get(f.getName()));
                            }
                        }

                        try {
                            Method customSaveMethod = model.getClass().getMethod("save");
                            Object saveResult = customSaveMethod.invoke(model);

                            if (saveResult != null && saveResult instanceof Model) {
                                model = (Model) saveResult;
                                saveReturnedKey = true;
                            }

                        } catch (Exception e) {
                            model._save();
                        }

                        Class<?> tType = cType;
                        while (!tType.equals(Object.class)) {
                            idCache.put(tType.getName() + "-" + id, (saveReturnedKey ? model._key()
                                    : Model.Manager.factoryFor(cType).keyValue((Model)model)));
                            tType = tType.getSuperclass();
                        }
                    }
                    else {
                        idCache.put(cType.getName() + "-" + id,
                                Binder.bind(rootParamNode, "object", cType, cType, annotations));
                    }
                }
            }
        }
        //} catch (ClassNotFoundException e) {
        //    throw new RuntimeException("Class " + e.getMessage() + " was not found", e);
        //} catch (ScannerException e) {
        //    throw new YAMLException(e, yamlFile);
        //} catch (Throwable e) {
        //    throw new RuntimeException("Cannot load fixture " + name + ": " + e.getMessage(), e);
        //}
    }*/

    /**
     *
     * TODO: reuse beanutils or MapUtils?
     *
     * @param entityProperties
     * @param prefix
     * @return an hash with the resolved entity name and the corresponding value
     */
    /*static Map<String, String[]> serialize(Map<?, ?> entityProperties, String prefix) {

        final Map<String, String[]> serialized = new HashMap<String, String[]>();

        if (entityProperties != null) {
            for (Object key : entityProperties.keySet()) {

                Object value = entityProperties.get(key);
                if (value == null) {
                    continue;
                }
                if (value instanceof Map<?, ?>) {
                    serialized.putAll(serialize((Map<?, ?>) value, prefix + "[" + key.toString() +"]"));
                } else if (value instanceof Date) {
                    serialized.put(prefix + "." + key.toString(), new String[]{
                        new SimpleDateFormat(DateBinder.ISO8601).format(((Date) value))});
                } else if (Collection.class.isAssignableFrom(value.getClass())) {
                    Collection<?> l = (Collection<?>) value;
                    String[] r = new String[l.size()];
                    int i = 0;
                    for (Object el : l) {
                        r[i++] = el.toString();
                    }
                    serialized.put(prefix + "." + key.toString(), r);
                } else if (value instanceof String && value.toString().matches("<<<\\s*\\{[^}]+}\\s*")) {
                    Matcher m = Pattern.compile("<<<\\s*\\{([^}]+)}\\s*").matcher(value.toString());
                    m.find();
                    String file = m.group(1);
                    VirtualFile f = Toolkit.getVirtualFile(file);
                    if (f != null && f.exists() && !f.isDirectory()) {
                        serialized.put(prefix + "." + key.toString(), new String[]{f.contentAsString()});
                    }
                } else {
                    serialized.put(prefix + "." + key.toString(), new String[]{value.toString()});
                }
            }
        }

        return serialized;
    }

    @SuppressWarnings("unchecked")*/
    /**
     *  Resolve dependencies between objects using their keys. For each referenced objects, it sets the foreign key
     */
    /*static Map<String, String[]> resolveDependencies(Class<Model> type, Map<String, String[]> yml) {

        // Contains all the fields (object properties) we should look up
        final Set<Field> fields = new HashSet<Field>();
        final Map<String, String[]> resolvedYml = new HashMap<String, String[]>();
        resolvedYml.putAll(yml);

        // Look up the super classes
        Class<?> clazz = type;
        while (!clazz.equals(Object.class)) {
            Collections.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }


        // Iterate through the Entity property list
        // @Embedded are not managed by the JPA plugin
        // This is not the nicest way of doing things.
        //modelFields =  Model.Manager.factoryFor(type).listProperties();
        final List<Model.Property> modelFields =  new JPAPlugin.JPAModelLoader(type).listProperties();

        for (Model.Property field : modelFields) {
            // If we have a relation, get the matching object
            if (field.isRelation) {
                // These are the Ids that were set in the yml file (i.e person(nicolas)-> nicolas is the id)
                final String[] ids = resolvedYml.get("object." + field.name);
                if (ids != null) {
                    final String[] resolvedIds = new String[ids.length];
                    for (int i = 0; i < ids.length; i++) {
                        final String id = field.relationType.getName() + "-" + ids[i];
                        if (!idCache.containsKey(id)) {
                            throw new RuntimeException("No previous reference found for object of type "
                                + field.name + " with key " + ids[i]);
                        }
                        // We now get the primary key
                        resolvedIds[i] = idCache.get(id).toString();
                    }
                    // Set the primary keys instead of the object itself.
                    // Model.Manager.factoryFor((Class<? extends Model>)field.relationType).keyName()
                    // returns the primary key label.
                    if (Model.class.isAssignableFrom(field.relationType )) {
                        resolvedYml.put("object." + field.name + "."
                        + Model.Manager.factoryFor((Class<? extends Model>)field.relationType).keyName(), resolvedIds);
                    } else {
                        // Might be an embedded object
                        final String id = field.relationType.getName() + "-" + ids[0];
                        Object o = idCache.get(id);
                        // This can be a composite key
                        if (o.getClass().isArray()) {
                            for (Object a : (Object[])o) {
                                for (Field f : field.relationType.getDeclaredFields()) {
                                    try {
                                        resolvedYml.put("object." + field.name + "." + f.getName(),
                                            new String[] {f.get(a).toString()});
                                    } catch(Exception e) {
                                        // Ignores
                                    }
                                }
                            }
                        } else {
                            for (Field f : field.relationType.getDeclaredFields()) {
                                try {
                                    resolvedYml.put("object." + field.name + "." + f.getName(),
                                        new String[] {f.get(o).toString()});
                                } catch(Exception e) {
                                    // Ignores
                                }
                            }
                        }
                    }
                }

                resolvedYml.remove("object." + field.name);
            }
        }
        // Returns the map containing the ids to load for this object's relation.
        return resolvedYml;
    }

    public static class JPAModelLoader implements Model.Factory {

        private Class<? extends Model> clazz;
        private Map<String, Model.Property> properties;


        public JPAModelLoader(Class<? extends Model> clazz) {
            this.clazz = clazz;
        }

        public Model findById(Object id) {
            try {
                if (id == null) {
                    return null;
                }
                return JPA.em().find(clazz, id);
            } catch (Exception e) {
                // Key is invalid, thus nothing was found
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        public List<Model> fetch(int offset, int size, String orderBy, String order,
                List<String> searchFields, String keywords, String where) {
            String q = "from " + clazz.getName();
            if (keywords != null && !keywords.equals("")) {
                String searchQuery = getSearchQuery(searchFields);
                if (!searchQuery.equals("")) {
                    q += " where (" + searchQuery + ")";
                }
                q += (where != null ? " and " + where : "");
            } else {
                q += (where != null ? " where " + where : "");
            }
            if (orderBy == null && order == null) {
                orderBy = "id";
                order = "ASC";
            }
            if (orderBy == null && order != null) {
                orderBy = "id";
            }
            if (order == null || (!order.equals("ASC") && !order.equals("DESC"))) {
                order = "ASC";
            }
            q += " order by " + orderBy + " " + order;
            Query query = JPA.em().createQuery(q);
            if (keywords != null && !keywords.equals("") && q.indexOf("?1") != -1) {
                query.setParameter(1, "%" + keywords.toLowerCase() + "%");
            }
            query.setFirstResult(offset);
            query.setMaxResults(size);
            return query.getResultList();
        }

        public Long count(List<String> searchFields, String keywords, String where) {
            String q = "select count(*) from " + clazz.getName() + " e";
            if (keywords != null && !keywords.equals("")) {
                String searchQuery = getSearchQuery(searchFields);
                if (!searchQuery.equals("")) {
                    q += " where (" + searchQuery + ")";
                }
                q += (where != null ? " and " + where : "");
            } else {
                q += (where != null ? " where " + where : "");
            }
            Query query = JPA.em().createQuery(q);
            if (keywords != null && !keywords.equals("") && q.indexOf("?1") != -1) {
                query.setParameter(1, "%" + keywords.toLowerCase() + "%");
            }
            return Long.decode(query.getSingleResult().toString());
        }

        public void deleteAll() {
            JPA.em().createQuery("delete from " + clazz.getName()).executeUpdate();
        }

        public List<Model.Property> listProperties() {
            List<Model.Property> properties = new ArrayList<Model.Property>();
            Set<Field> fields = new LinkedHashSet<Field>();
            Class<?> tclazz = clazz;
            while (!tclazz.equals(Object.class)) {
                Collections.addAll(fields, tclazz.getDeclaredFields());
                tclazz = tclazz.getSuperclass();
            }
            for (Field f : fields) {
                int mod = f.getModifiers();
                if (Modifier.isTransient(mod) || Modifier.isStatic(mod)) {
                    continue;
                }
                if (f.isAnnotationPresent(Transient.class)) {
                    continue;
                }
                if (f.isAnnotationPresent(NoBinding.class)) {
                    NoBinding a = f.getAnnotation(NoBinding.class);
                    List<String> values = Arrays.asList(a.value());
                    if (values.contains("*")) {
                        continue;
                    }
                }
                Model.Property mp = buildProperty(f);
                if (mp != null) {
                    properties.add(mp);
                }
            }
            return properties;
        }

        public String keyName() {
            return keyField().getName();
        }

        public Class<?> keyType() {
            return keyField().getType();
        }

        public Class<?>[] keyTypes() {
            Field[] fields = keyFields();
            Class<?>[] types = new Class<?>[fields.length];
            int i = 0;
            for (Field field : fields) {
                types[i++] = field.getType();
            }
            return types;
        }

        public String[] keyNames() {
            Field[] fields = keyFields();
            String[] names = new String[fields.length];
            int i = 0;
            for (Field field : fields) {
                names[i++] = field.getName();
            }
            return names;
        }

        private Class<?> getCompositeKeyClass() {
            Class<?> tclazz = clazz;
            while (!tclazz.equals(Object.class)) {
                // Only consider mapped types
                if (tclazz.isAnnotationPresent(Entity.class)
                        || tclazz.isAnnotationPresent(MappedSuperclass.class)) {
                    IdClass idClass = tclazz.getAnnotation(IdClass.class);
                    if (idClass != null)
                        return idClass.value();
                }
                tclazz = tclazz.getSuperclass();
            }
            throw new UnexpectedException("Invalid mapping for class " + clazz
                + ": multiple IDs with no @IdClass annotation");
        }


        private void initProperties() {
            synchronized(this){
                if(properties != null)
                    return;
                properties = new HashMap<String,Model.Property>();
                Set<Field> fields = getModelFields(clazz);
                for (Field f : fields) {
                    int mod = f.getModifiers();
                    if (Modifier.isTransient(mod) || Modifier.isStatic(mod)) {
                        continue;
                    }
                    if (f.isAnnotationPresent(Transient.class)) {
                        continue;
                    }
                    Model.Property mp = buildProperty(f);
                    if (mp != null) {
                        properties.put(mp.name, mp);
                    }
                }
            }
        }

        private Object makeCompositeKey(Model model) throws Exception {
            initProperties();
            Class<?> idClass = getCompositeKeyClass();
            Object id = idClass.newInstance();
            PropertyDescriptor[] idProperties = PropertyUtils.getPropertyDescriptors(idClass);
            if(idProperties == null || idProperties.length == 0)
                throw new UnexpectedException("Composite id has no properties: "+idClass.getName());
            for (PropertyDescriptor idProperty : idProperties) {
                // do we have a field for this?
                String idPropertyName = idProperty.getName();
                // skip the "class" property...
                if(idPropertyName.equals("class"))
                    continue;
                Model.Property modelProperty = this.properties.get(idPropertyName);
                if(modelProperty == null)
                    throw new UnexpectedException("Composite id property missing: "+clazz.getName()+"."+idPropertyName
                            +" (defined in IdClass "+idClass.getName()+")");
                // sanity check
                Object value = modelProperty.field.get(model);

                if(modelProperty.isMultiple)
                    throw new UnexpectedException("Composite id property cannot be multiple: "+clazz.getName()
                        +"."+idPropertyName);
                // now is this property a relation? if yes then we must use its ID in the key (as per specs)
                if(modelProperty.isRelation){
                    // get its id
                    if(!Model.class.isAssignableFrom(modelProperty.type))
                        throw new UnexpectedException("Composite id property entity has to be a subclass of Model: "
                                +clazz.getName()+"."+idPropertyName);
                    // we already checked that cast above
                    @SuppressWarnings("unchecked")
                    Model.Factory factory = Model.Manager.factoryFor((Class<? extends Model>) modelProperty.type);
                    if(factory == null)
                        throw new UnexpectedException("Failed to find factory for Composite id property entity: "
                                +clazz.getName()+"."+idPropertyName);
                    // we already checked that cast above
                    if(value != null)
                        value = factory.keyValue((Model) value);
                }
                // now affect the composite id with this id
                PropertyUtils.setSimpleProperty(id, idPropertyName, value);
            }
            return id;
        }



        public Object keyValue(Model m) {
            try {
                if (m == null) {
                    return null;
                }

                // Do we have a @IdClass or @Embeddable?
                if (m.getClass().isAnnotationPresent(IdClass.class)) {
                    return makeCompositeKey(m);
                }

                // Is it a composite key? If yes we need to return the matching PK
                final Field[] fields = keyFields();
                final Object[] values = new Object[fields.length];
                int i = 0;
                for (Field f : fields) {
                    final Object o = f.get(m);
                    if (o != null) {
                        values[i++] = o;
                    }
                }

                // If we have only one id return it
                if (values.length == 1) {
                    return values[0];
                }

                return values;
            } catch (Exception ex) {
                throw new UnexpectedException(ex);
            }
        }

        public static Set<Field> getModelFields(Class<?> clazz){
            Set<Field> fields = new LinkedHashSet<Field>();
            Class<?> tclazz = clazz;
            while (!tclazz.equals(Object.class)) {
                // Only add fields for mapped types
                if(tclazz.isAnnotationPresent(Entity.class)
                        || tclazz.isAnnotationPresent(MappedSuperclass.class))
                    Collections.addAll(fields, tclazz.getDeclaredFields());
                tclazz = tclazz.getSuperclass();
            }
            return fields;
        }

        //
        Field keyField() {
            Class c = clazz;
            try {
                while (!c.equals(Object.class)) {
                    for (Field field : c.getDeclaredFields()) {
                        if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                            field.setAccessible(true);
                            return field;
                        }
                    }
                    c = c.getSuperclass();
                }
            } catch (Exception e) {
                throw new UnexpectedException("Error while determining the object @Id for an object of type " + clazz);
            }
            throw new UnexpectedException("Cannot get the object @Id for an object of type " + clazz);
        }

        Field[] keyFields() {
            Class c = clazz;
            try {
                List<Field> fields = new ArrayList<Field>();
                while (!c.equals(Object.class)) {
                    for (Field field : c.getDeclaredFields()) {
                        if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                            field.setAccessible(true);
                            fields.add(field);
                        }
                    }
                    c = c.getSuperclass();
                }
                final Field[] f = fields.toArray(new Field[fields.size()]);
                if (f.length == 0) {
                    throw new UnexpectedException("Cannot get the object @Id for an object of type " + clazz);
                }
                return f;
            } catch (Exception e) {
                throw new UnexpectedException("Error while determining the object @Id for an object of type " + clazz);
            }
        }

        String getSearchQuery(List<String> searchFields) {
            String q = "";
            for (Model.Property property : listProperties()) {
                if (property.isSearchable && (searchFields == null || searchFields.isEmpty() ? true
                        : searchFields.contains(property.name))) {
                    if (!q.equals("")) {
                        q += " or ";
                    }
                    q += "lower(" + property.name + ") like ?1";
                }
            }
            return q;
        }

        Model.Property buildProperty(final Field field) {
            Model.Property modelProperty = new Model.Property();
            modelProperty.type = field.getType();
            modelProperty.field = field;
            if (Model.class.isAssignableFrom(field.getType())) {
                if (field.isAnnotationPresent(OneToOne.class)) {
                    if (field.getAnnotation(OneToOne.class).mappedBy().equals("")) {
                        modelProperty.isRelation = true;
                        modelProperty.relationType = field.getType();
                        modelProperty.choices = new Model.Choices() {

                            @SuppressWarnings("unchecked")
                            public List<Object> list() {
                                return JPA.em().createQuery("from " + field.getType().getName()).getResultList();
                            }
                        };
                    }
                }
                if (field.isAnnotationPresent(ManyToOne.class)) {
                    modelProperty.isRelation = true;
                    modelProperty.relationType = field.getType();
                    modelProperty.choices = new Model.Choices() {

                        @SuppressWarnings("unchecked")
                        public List<Object> list() {
                            return JPA.em().createQuery("from " + field.getType().getName()).getResultList();
                        }
                    };
                }
            }
            if (Collection.class.isAssignableFrom(field.getType())) {
                final Class<?> fieldType =
                    (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (field.isAnnotationPresent(OneToMany.class)) {
                    if (field.getAnnotation(OneToMany.class).mappedBy().equals("")) {
                        modelProperty.isRelation = true;
                        modelProperty.isMultiple = true;
                        modelProperty.relationType = fieldType;
                        modelProperty.choices = new Model.Choices() {

                            @SuppressWarnings("unchecked")
                            public List<Object> list() {
                                return JPA.em().createQuery("from " + fieldType.getName()).getResultList();
                            }
                        };
                    }
                }
                if (field.isAnnotationPresent(ManyToMany.class)) {
                    if (field.getAnnotation(ManyToMany.class).mappedBy().equals("")) {
                        modelProperty.isRelation = true;
                        modelProperty.isMultiple = true;
                        modelProperty.relationType = fieldType;
                        modelProperty.choices = new Model.Choices() {

                            @SuppressWarnings("unchecked")
                            public List<Object> list() {
                                return JPA.em().createQuery("from " + fieldType.getName()).getResultList();
                            }
                        };
                    }
                }
            }
            if (field.getType().isEnum()) {
                modelProperty.choices = new Model.Choices() {

                    @SuppressWarnings("unchecked")
                    public List<Object> list() {
                        return (List<Object>) Arrays.asList(field.getType().getEnumConstants());
                    }
                };
            }
            modelProperty.name = field.getName();
            if (field.getType().equals(String.class)) {
                modelProperty.isSearchable = true;
            }
            if (field.isAnnotationPresent(GeneratedValue.class)) {
                modelProperty.isGenerated = true;
            }
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                // Look if the target is an embeddable class
                if (field.getType().isAnnotationPresent(Embeddable.class)
                        || field.getType().isAnnotationPresent(IdClass.class) ) {
                    modelProperty.isRelation = true;
                    modelProperty.relationType =  field.getType();
                }
            }
            return modelProperty;
        }
    }*/
}
