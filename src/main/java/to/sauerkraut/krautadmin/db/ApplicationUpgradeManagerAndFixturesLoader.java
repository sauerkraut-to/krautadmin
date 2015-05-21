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

    /*private void loadModels(final String yamlFileName) throws Exception {
        //try {

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
                        if (!type.startsWith("models.")) {
                            type = "models." + type;
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
                        Class<Model> cType = (Class<Model>)Play.classloader.loadClass(type);
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
                } else {
                    serialized.put(prefix + "." + key.toString(), new String[]{value.toString()});
                }
            }
        }

        return serialized;
    }*/
}
