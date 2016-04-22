/**
 * Copyright 2011, Deft Labs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deftlabs.cursor.mongo;

// Lib
// Java
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
// Mongo
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoCursorNotFoundException;

/**
 * The tailable cursor interface.
 */
public class TailableCursorImpl implements TailableCursor {

    /**
     * Returns the next object in the cursor. This call blocks until an object is available.
     * 
     * @throws InterruptedException
     */
    @Override
    public DBObject nextDoc() throws InterruptedException {
        if (_options.hasDocListener()) {
            throw new TailableCursorException("Can't use doc listener and nextDoc together");
        }

        DBObject doc = null;

        while (true) {
            doc = _docQueue.poll();
            if (doc != null)
                return doc;
            lock();
        }
    }

    private void lock() throws InterruptedException {
        boolean wasInterrupted = false;

        final Thread current = Thread.currentThread();

        _waiters.add(current);

        while (_waiters.peek() != current || !_locked.compareAndSet(false, true)) {
            LockSupport.park();

            // If interrupted, get out of here
            if (Thread.interrupted()) {
                wasInterrupted = true;
                break;
            }
        }

        _waiters.remove();

        if (wasInterrupted) {
            current.interrupt();
            throw new InterruptedException();
        }
    }

    private void unlock() {
        _locked.set(false);
        LockSupport.unpark(_waiters.peek());
    }

    /**
     * Called to start the tailable cursor.
     */
    @Override
    public synchronized void start() {
        if (_running.get())
            throw new TailableCursorException("Already running");
        _running.set(true);
        _cursorReader.start();
    }

    /**
     * Called to stop the tailable cursor. This causes an Interrupted exception to be thrown in the nextDoc method.
     */
    @Override
    public void stop() {
        if (!_running.get())
            throw new TailableCursorException("Not running");
        _running.set(false);
        if (_cursorReader != null)
            _cursorReader.close();
        _cursorReader.interrupt();
        for (final Thread t : _waiters)
            t.interrupt();
    }

    /**
     * A Thread that pulls the data off the cursor.
     */
    private class CursorReader extends Thread {

        private DBCursor cur;

        public CursorReader() {
            this.setDaemon(true);
            this.setName("TailableCursor - " + _options.getThreadName());
        }

        public void close() {
            if (cur != null) {
                cur.close();
            }
        }

        @Override
        public void run() {
            while (_running.get()) {
                try {
                    _db.requestStart();
                    cur = createCursor();
                    try {
                        while (_running.get() && cur.hasNext()) {
                            final DBObject doc = cur.next();

                            if (doc == null)
                                break;

                            if (_options.hasDocListener()) {
                                _options.getDocListener().nextDoc(doc);
                            }
                            else {
                                _docQueue.put(doc);
                                unlock();
                            }
                        }
                    } finally {
                        try {
                            if (cur != null)
                                cur.close();
                        } catch (final Throwable t) { /* nada */
                        }
                        try {
                            _db.requestDone();
                        } catch (final Throwable t) { /* nada */
                        }
                    }

                    if (_options.getNoDocSleepTime() > 0)
                        Thread.sleep(_options.getNoDocSleepTime());

                } catch (final MongoCursorNotFoundException cnf) {
                    if (_running.get())
                        if (handleException(cnf))
                            break;
                } catch (final InterruptedException ie) {
                    break;
                } catch (final Throwable t) {
                    if (handleException(t))
                        break;
                }
            }
        }

        private DBCursor createCursor() {
            final DBCollection col = _db.getCollection(_options.getCollectionName());

            return col.find(_options.getInitialQuery()).sort(new BasicDBObject("$natural", 1)).addOption(Bytes.QUERYOPTION_TAILABLE)
                    .addOption(Bytes.QUERYOPTION_AWAITDATA);
        }

        /**
         * Either log the exception or pass to the handler.
         * 
         * @param pT
         *            The throwable received.
         * @return True if the thread should exit. False otherwise :-)
         */
        private boolean handleException(final Throwable pT) {

            if (pT instanceof InterruptedException)
                return true;

            if (_options.hasErrorListener()) {
                try {
                    // Call the error listener.
                    _options.getErrorListener().onError(pT);
                } catch (final Throwable t) {
                    if (t instanceof InterruptedException)
                        return true;
                    _logger.log(Level.SEVERE, pT.getMessage(), pT);
                }
            }
            else {
                _logger.log(Level.SEVERE, pT.getMessage(), pT);
            }

            if (_options.getErrorSleepTime() <= 0)
                return false;

            try {
                Thread.sleep(_options.getErrorSleepTime());
            } catch (final InterruptedException ie) {
                return true;
            }

            return false;
        }
    }

    @Override
    public boolean isRunning() {
        return _running.get();
    }

    public DBCollection getCollection() {
        return _coll;
    }

    private DBCollection createCollection() {
        final BasicDBObject options = new BasicDBObject("capped", true);
        options.put("size", _options.getDefaultCappedCollectionSize());
        return _db.createCollection(_options.getCollectionName(), options);
    }

    /**
     * Construct a new object.
     * 
     * @param pOptions
     *            The cursor options.
     */
    public TailableCursorImpl(final DB db, final TailableCursorOptions pOptions) {

        _options = pOptions;
        _db = db;

        try {

            if (!_db.collectionExists(_options.getCollectionName())) {
                if (_options.getAssertIfNoCappedCollection()) {
                    throw new TailableCursorException("No capped collection found - db: " + _db.getName() + " - collection: "
                            + _options.getCollectionName() + " (" + TailableCursorException.NO_COLLECTION_FOUND + ")",
                            TailableCursorException.NO_COLLECTION_FOUND);
                }

                _coll = createCollection();
            }
            else {
                // make sure the colleciton is empty
                _db.getCollection(_options.getCollectionName()).drop();
                _coll = createCollection();

                // Verify the collection is capped.
                if (!_coll.isCapped()) {
                    throw new TailableCursorException("Not a capped collection - db: " + _db.getName() + " - collection: "
                            + _options.getCollectionName() + " (" + TailableCursorException.NON_CAPPED_COLLECTION + ")",
                            TailableCursorException.NON_CAPPED_COLLECTION);
                }
            }

            _cursorReader = new CursorReader();

        } catch (final Throwable t) {
            throw new TailableCursorException(t);
        }
    }

    private final DB _db;
    private final DBCollection _coll;
    private final AtomicBoolean _running = new AtomicBoolean(false);
    private final AtomicBoolean _locked = new AtomicBoolean(false);
    private final TailableCursorOptions _options;
    private final Queue<Thread> _waiters = new ConcurrentLinkedQueue<Thread>();

    private final Logger _logger = Logger.getLogger("com.deftlabs.cursor.mongo.TailableCursor");

    private final LinkedBlockingQueue<DBObject> _docQueue = new LinkedBlockingQueue<DBObject>(1);
    private final CursorReader _cursorReader;
}
