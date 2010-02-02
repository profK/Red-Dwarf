/*
 * Copyright 2007-2008 Sun Microsystems, Inc.
 *
 * This file is part of Project Darkstar Server.
 *
 * Project Darkstar Server is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation and
 * distributed hereunder to you.
 *
 * Project Darkstar Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sun.sgs.test.app.util;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedObjectRemoval;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.PeriodicTaskHandle;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.app.TransactionNotActiveException;
import com.sun.sgs.app.util.ManagedSerializable;
import com.sun.sgs.internal.ManagerLocator;

import java.util.Map;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.math.BigInteger;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * This is a limited functionality {@link ManagerLocator} designed to 
 * mock the behavior for {@link com.sun.sgs.app.AppContext AppContext} for
 * use in testing the scalable data structures.  This implementation only
 * provides a replacement manager for the DataManager and TaskManager.
 */
public class MockManagerLocator implements ManagerLocator {
    
    /**
     * This is the master counter used to assign new ids to
     * every {@code ManagedReference} that is created.
     */
    private static BigInteger masterId = BigInteger.ZERO;
    
    private DataManager dataManager = new MockDataManager();
    private TaskManager taskManager = new MockTaskManager();
    
    public MockManagerLocator() {
        
    }

    @Override
    public ChannelManager getChannelManager() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    @Override
    public TaskManager getTaskManager() {
        return taskManager;
    }
    
    @Override
    public <T> T getManager(Class<T> type) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /**
     * This is a simple mockup of a {@link ManagedReference}. It stores the
     * id of the associated ManagedObject.  It always requests
     * an object by polling the backing {@link MockDataManager} with this id.
     */
    public static class MockManagedReference<T> implements ManagedReference<T>,
                                                           Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * The id of the associated {@code ManagedObject}
         */
        private final BigInteger id;
        
        /**
         * A boolean indicating whether or not this {@code ManagedReference}
         * is still in an active state.  If it is not active, attempts to 
         * retrieve the associated {@code ManagedObject} will throw an
         * exception.
         */
        private transient boolean active;
        
        public MockManagedReference(BigInteger id) {
            this.id = id;
            this.active = true;
        }
        
        @Override
        public T get() {
            return internalGet();
        }

        @Override
        public T getForUpdate() {
            return internalGet();
        }
        
        @SuppressWarnings("unchecked")
        private T internalGet() {
            if(!active)
                throw new TransactionNotActiveException(
                        "Transaction not active");
            
            DataManager dm = AppContext.getDataManager();
            if(!(dm instanceof MockDataManager))
                throw new IllegalStateException(
                        "MockManagedReference cannot be used without " +
                        " a backing MockDataManager");
            
            return (T) ((MockDataManager) dm).getObjectWithId(id);
        }

        @Override
        public BigInteger getId() {
            return id;
        }
        
        @Override
        public boolean equals(Object object) {
            if(object instanceof MockManagedReference) {
                return ((MockManagedReference)object).id.equals(id);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            long oid = id.longValue();
            return (int) (oid ^ (oid >>> 32)) + 6883;
        }
        
        /**
         * Deactivates this {@code ManagedReference}.  Attempts to retrieve
         * the associated {@code ManagedObject} while in a deactivated state
         * will throw an exception.
         */
        void deactivate() {
            this.active = false;
        }
        
        /**
         * Reads in a serialized object.  This will initialize the state of
         * the object to its state from the input stream and will also set
         * the value of its transient active field to {@code true}.
         */
        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            active = true;
        }
    }
    
    /**
     * Mock's darkstar's DataManager interface so that it behaves as 
     * expected without the backing datastore. <p>
     * 
     * When writing tests using this Manager, a transaction boundary can
     * be simulated by calling the {@link MockDataManager#serializeDataStore}
     * method.  This will serialize each object in the store, and then
     * deserialize them, getting a fresh, separate copy of each object.
     */
    public class MockDataManager implements DataManager {

        /**
         * This is the main representation of the Data Store as a map
         * of ids to {@code ManagedObject}s.
         */
        private Map<BigInteger, ManagedObject> store =
                new HashMap<BigInteger, ManagedObject>();
        
        /**
         * This maps maintains the name bindings from names to object ids.
         */
        private Map<String, BigInteger> bindings = 
                new HashMap<String, BigInteger>();
        
        /**
         * Maps each ManagedObject to its associated id.
         */
        private Map<ManagedObject, BigInteger> idMap = 
                new IdentityHashMap<ManagedObject, BigInteger>();
        
        /**
         * List of references created during this transaction
         */
        private List<MockManagedReference> referenceList =
                new ArrayList<MockManagedReference>();
        
        /**
         * Retrieves the object with the specified id from the data store
         * 
         * @param id the id of the object
         * @return the {@code ManagedObject} that is associated with this id
         * @throws ObjectNotFoundException if no object with the given id
         *         exists in the data store
         */
        ManagedObject getObjectWithId(BigInteger id) {
            if(!store.containsKey(id)) {
                throw new ObjectNotFoundException(
                        "No object found in the data store with id : " + id);
            }
            
            return store.get(id);
        }
        
        /**
         * Serialize and de-serialize each of the objects in the data store
         * to simulate the start of a new transaction.  <p>
         * 
         * This method will
         * also have the added side effect of deactivating any
         * {@code ManagedReference} objects that have been created through
         * this data store since the last call to this method.  <p>
         * 
         * A deactivated
         * {@code ManagedReference} will always throw a
         * {@code TransactionNotActiveException} if an attempt is made to
         * get its associated {@code ManagedObject}.
         */
        void serializeDataStore() throws Exception {
            
            //deactive all current references
            for(MockManagedReference r : referenceList) {
                r.deactivate();
            }
            referenceList.clear();
            
            //serialize each member of the data store and then read it back
            //and store in the data store
            for(Iterator<ManagedObject> im = idMap.keySet().iterator();
                    im.hasNext(); ) {
                ManagedObject m = im.next();
                BigInteger id = idMap.get(m);
            
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(m);

                byte[] serializedForm = baos.toByteArray();

                ByteArrayInputStream bais =
                        new ByteArrayInputStream(serializedForm);
                ObjectInputStream ois = new ObjectInputStream(bais);

                m = (ManagedObject) ois.readObject();
                store.put(id, m);
            }
            
            //record each object id in the id map 
            idMap.clear();
            for(Iterator<BigInteger> ib = store.keySet().iterator();
                    ib.hasNext(); ) {
                BigInteger id = ib.next();
                ManagedObject object = store.get(id);
                idMap.put(object, id);
            }
        }
        
        public int size() {
            return store.size();
        }
        
        @Override
        public <T> ManagedReference<T> createReference(T object) {
            checkArgument(object);
            ManagedObject o = (ManagedObject) object;
            BigInteger id = addToDataStore(o);
            MockManagedReference<T> m = new MockManagedReference<T>(id);
            referenceList.add(m);
            return m;
        }

        @Override
        public ManagedObject getBinding(String name) {
            BigInteger id = bindings.get(name);
            if (id == null) {
                throw new NameNotBoundException(
                        "No binding for " + name + " in the data store");
            }
            
            return getObjectWithId(id);
        }

        @Override
        public void markForUpdate(Object object) {
            checkArgument(object);
        }

        @Override
        public String nextBoundName(String name) {
            List<String> names = new ArrayList<String>(bindings.keySet());
            Collections.sort(names);

            for (Iterator<String> in = names.iterator(); in.hasNext();) {
                if (in.next().equals(name) && in.hasNext()) {
                    return in.next();
                }
            }
            return null;
        }

        @Override
        public void removeBinding(String name) {
            if (!bindings.containsKey(name)) {
                throw new NameNotBoundException(
                        "No binding for " + name + " in the data store");
            }
            bindings.remove(name);
        }

        @Override
        public void removeObject(Object object) {
            checkArgument(object);

            if (!idMap.containsKey(object)) {
                throw new ObjectNotFoundException(
                        "Object not in the data store: " + object);
            }

            if (object instanceof ManagedObjectRemoval) {
                ((ManagedObjectRemoval) object).removingObject();
            }
            BigInteger id = idMap.remove(object);
            store.remove(id);
        }

        @Override
        public void setBinding(String name, Object object) {
            checkArgument(object);
            BigInteger id = addToDataStore((ManagedObject) object);
            bindings.put(name, id);
        }

        /**
         * Verify that the object implements both ManagedObject
         * and Serializable
         * @param object
         */
        private void checkArgument(Object object) {
            if (object == null) {
                throw new NullPointerException("The object must not be null");
            }
            if (!(object instanceof ManagedObject)) {
                throw new IllegalArgumentException(
                        "Object does not implement ManagedObject: " +
                        object);
            }
            if (!(object instanceof Serializable)) {
                throw new IllegalArgumentException(
                        "Object does not implement Serializable: " +
                        object);
            }
        }

        /**
         * Adds the object to the backing data store map.  If the object
         * is already in the map, no changes are made and its id is simply
         * returned.  Otherwise, a new id is assigned to this object,
         * the object is stored, and the id is returned.
         * 
         * @param object the object to put into the data store
         * @return the id of the object in the data store
         */
        private BigInteger addToDataStore(ManagedObject object) {
            BigInteger id = idMap.get(object);
            if (id == null) {
                id = masterId;
                masterId = masterId.add(BigInteger.ONE);
                store.put(id, object);
                idMap.put(object, id);
            }
            return id;
        }

    }
    
    /**
     * This is a limited functionality implementation of a {@link TaskManager}.
     * Its sole purpose is to record the Tasks that are scheduled.  These
     * can then be retrieved and executed by the test if necessary.
     */
    public class MockTaskManager implements TaskManager {
        
        private Queue<Task> tasks = new ConcurrentLinkedQueue<Task>();
        
        public MockTaskManager() {
            
        }

        @Override
        public PeriodicTaskHandle schedulePeriodicTask(Task task, 
                                                       long delay, 
                                                       long period) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void scheduleTask(Task task) {
            if (!(task instanceof Serializable)) {
                throw new IllegalArgumentException(
                        "Task does not implement Serializable: " +
                        task);
            }
            
            if (task instanceof ManagedObject) {
                dataManager.createReference(task);
            }
            else {
                dataManager.createReference(
                        new ManagedSerializable<Task>(task));
            }
            tasks.offer(task);
        }

        @Override
        public void scheduleTask(Task task, long delay) {
            throw new UnsupportedOperationException("Not supported.");
        }

        /**
         * Retrieves the list of tasks that have been scheduled.
         * @return the list of tasks that have been scheduled
         */
        public Queue<Task> getScheduledTasks() {
            return tasks;
        }
        
    }

}
