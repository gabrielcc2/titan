package com.thinkaurelius.titan.diskstorage.cassandra;

import com.thinkaurelius.titan.diskstorage.common.RemoteStoreManager;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.ConsistencyLevel;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeyColumnValueStoreManager;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StoreFeatures;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StoreTransaction;
import org.apache.commons.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public abstract class AbstractCassandraStoreManager extends RemoteStoreManager implements KeyColumnValueStoreManager {

    //################### CASSANDRA SPECIFIC CONFIGURATION OPTIONS ######################
    public static final String READ_CONSISTENCY_LEVEL_KEY = "read-consistency-level";
    public static final String READ_CONSISTENCY_LEVEL_DEFAULT = "QUORUM";

    public static final String WRITE_CONSISTENCY_LEVEL_KEY = "write-consistency-level";
    /*
     * Any operation attempted with ConsistencyLevel.TWO
     * against a single-node Cassandra cluster (like the one
     * we use in a lot of our test cases) will fail with
     * an UnavailableException.  In other words, if you
     * set TWO here, Cassandra will require TWO nodes, even
     * if only one node has ever been a member of the
     * cluster in question.
     */
    public static final String WRITE_CONSISTENCY_LEVEL_DEFAULT = "QUORUM";
    /**
     * Default name for the Cassandra keyspace
     * <p>
     * Value = {@value}
     */
    public static final String KEYSPACE_DEFAULT = "titan";
    public static final String KEYSPACE_KEY = "keyspace";

    /**
     * Default port at which to attempt Cassandra Thrift connection.
     * <p>
     * Value = {@value}
     */
    public static final int PORT_DEFAULT = 9160;


    public static final String REPLICATION_FACTOR_KEY = "replication-factor";
    public static final int REPLICATION_FACTOR_DEFAULT  = 1;


    protected final String keySpaceName;
    protected final int replicationFactor;
    
    private final CassandraTransaction.Consistency readConsistencyLevel;
    private final CassandraTransaction.Consistency writeConsistencyLevel;
    
    protected StoreFeatures features;

    public AbstractCassandraStoreManager(Configuration storageConfig) {
        super(storageConfig, PORT_DEFAULT);

        features = new StoreFeatures();
        features.supportsScan=false; features.supportsBatchMutation=true; features.isTransactional=false;
        features.supportsConsistentKeyOperations=true; features.supportsLocking=false; features.isKeyOrdered=false;
        features.isDistributed=true; features.hasLocalKeyPartition=false;
        
        this.keySpaceName = storageConfig.getString(KEYSPACE_KEY, KEYSPACE_DEFAULT);

        this.replicationFactor = storageConfig.getInt(REPLICATION_FACTOR_KEY, REPLICATION_FACTOR_DEFAULT);

        this.readConsistencyLevel = CassandraTransaction.Consistency.parse(storageConfig.getString(
                READ_CONSISTENCY_LEVEL_KEY, READ_CONSISTENCY_LEVEL_DEFAULT));

        this.writeConsistencyLevel = CassandraTransaction.Consistency.parse(storageConfig.getString(
                WRITE_CONSISTENCY_LEVEL_KEY, WRITE_CONSISTENCY_LEVEL_DEFAULT));
    }
    

    @Override
    public StoreTransaction beginTransaction(ConsistencyLevel level) {
        return new CassandraTransaction(level,readConsistencyLevel,writeConsistencyLevel);
    }

    @Override
    public String toString() {
        return "["+keySpaceName+"@"+super.toString()+"]";
    }
    
    @Override
    public StoreFeatures getFeatures() {
        return features;
    }


}
