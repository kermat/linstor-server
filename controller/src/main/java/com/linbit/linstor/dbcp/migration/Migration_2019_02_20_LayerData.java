package com.linbit.linstor.dbcp.migration;

import com.linbit.ImplementationError;
import com.linbit.linstor.dbdrivers.GenericDbDriver;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"checkstyle:typename", "checkstyle:magicnumber"})
@Migration(
    version = "2019.02.20.09.26",
    description = "Add tables for layer data and move old layer-specifc data to new tables (drbd-port, -minor,...)"
)
public class Migration_2019_02_20_LayerData extends LinstorMigration
{
    private static final int DEFAULT_DRBD_PEER_SLOTS = 7;
    private static final int DEFAULT_DRBD_AL_STRIPES = 1;
    private static final long DEFAULT_DRBD_AL_STRIPE_SIZE = 32;

    private static final int FLAG_VLM_DFN_ENCRYPTED = 2;

    private static final String TBL_LAYER_RESOURCE_IDS = "LAYER_RESOURCE_IDS";

    private static final String LAYER_KIND_DRBD = "DRBD";
    private static final String LAYER_KIND_CRYPT_SETUP = "CRYPT_SETUP";
    private static final String LAYER_KIND_STORAGE = "STORAGE";

    private static final String PROVIDER_KIND_DRBD_DISKLESS = "DRBD_DISKLESS";
    private static final String PROVIDER_KIND_LVM = "LVM";
    private static final String PROVIDER_KIND_LVM_THIN = "LVM_THIN";
    private static final String PROVIDER_KIND_ZFS = "ZFS";
    private static final String PROVIDER_KIND_ZFS_THIN = "ZFS_THIN";
    private static final String PROVIDER_KIND_SF_TARGET = "SWORDFISH_TARGET";
    private static final String PROVIDER_KIND_SF_INITIATOR = "SWORDFISH_INITIATOR";

    private static final String SELECT_VOLUMES =
        "SELECT " +
            "V.NODE_NAME, V.RESOURCE_NAME, V.VLM_NR, V.VLM_FLAGS, V.STOR_POOL_NAME, " +
            "VD.VLM_MINOR_NR, VD.VLM_FLAGS, " +
            "R.NODE_ID, R.RESOURCE_FLAGS, " +
            "RD.TCP_PORT, RD.SECRET, RD.TRANSPORT_TYPE, " +
            "SP.DRIVER_NAME " +
        "FROM " +
            "VOLUMES AS V, VOLUME_DEFINITIONS AS VD, " +
            "RESOURCES AS R, RESOURCE_DEFINITIONS AS RD, " +
            "NODE_STOR_POOL AS SP " +
        "WHERE " +
            "V.RESOURCE_NAME = VD.RESOURCE_NAME AND " +
            "V.VLM_NR = VD.VLM_NR AND " +
            "V.NODE_NAME = R.NODE_NAME AND " +
            "V.RESOURCE_NAME = R.RESOURCE_NAME AND " +
            "V.RESOURCE_NAME = RD.RESOURCE_NAME AND " +
            "V.NODE_NAME = SP.NODE_NAME AND " +
            "V.STOR_POOL_NAME = SP.POOL_NAME";
    private static final String SELECT_SINGLE_PROP =
        "SELECT PROP_VALUE " +
        "FROM PROPS_CONTAINERS " +
        "WHERE PROPS_INSTANCE = ? AND " +
               "PROP_KEY = ?";


    private static final String INSERT_LAYER_RSC_ID =
        "INSERT INTO LAYER_RESOURCE_IDS " +
        "(LAYER_RESOURCE_ID, NODE_NAME, RESOURCE_NAME," +
        " LAYER_RESOURCE_KIND, LAYER_RESOURCE_PARENT_ID, LAYER_RESOURCE_SUFFIX) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String INSERT_DRBD_RSC_DFN =
        "INSERT INTO LAYER_DRBD_RESOURCE_DEFINITIONS " +
        "(RESOURCE_NAME, RESOURCE_NAME_SUFFIX, PEER_SLOTS, AL_STRIPES, " +
        " AL_STRIPE_SIZE, TCP_PORT, TRANSPORT_TYPE, SECRET) " +
        "VALUES (?, ?, ?, ?, ? ,? ,? ,?)";
    private static final String INSERT_DRBD_VLM_DFN =
        "INSERT INTO LAYER_DRBD_VOLUME_DEFINITIONS " +
        "(RESOURCE_NAME, RESOURCE_NAME_SUFFIX, VLM_NR, VLM_MINOR_NR) " +
        "VALUES (?, ?, ?, ?)";
    private static final String INSERT_DRBD_RSC =
        "INSERT INTO LAYER_DRBD_RESOURCES " +
        "(LAYER_RESOURCE_ID, PEER_SLOTS, AL_STRIPES, AL_STRIPE_SIZE, FLAGS, NODE_ID) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String INSERT_CRYPT_VLM =
        "INSERT INTO LAYER_CRYPT_SETUP_VOlUMES " +
        "(LAYER_RESOURCE_ID, VLM_NR, ENCRYPTED_PASSWORD) " +
        "VALUES (?, ?, ?)";
    private static final String INSERT_SF_VLM_DFN =
        "INSERT INTO LAYER_SWORDFISH_VOLUME_DEFINITIONS " +
        "(RESOURCE_NAME, RESOURCE_NAME_SUFFIX, VLM_NR, VLM_ODATA) " +
        "VALUES (?, ?, ?, ?)";
    private static final String INSERT_STOR_VLM =
        "INSERT INTO LAYER_STORAGE_VOLUMES " +
        "(LAYER_RESOURCE_ID, VLM_NR, PROVIDER_KIND) " +
        "VALUES (?, ?, ?)";

    private int nextRscLayerId = 0;
    private final Map<Pair<String, String>, Integer> layerRscIds = new HashMap<>();
    private final Set<Pair<String, Integer>> drbdVlmDfns = new HashSet<>();
    private final Set<String> drbdRscDfns = new HashSet<>();
    private final Set<Integer> drbdRsc = new HashSet<>();
    private final Set<Pair<String, Integer>> sfVlmDfns = new HashSet<>();

    @Override
    public void migrate(Connection connection)
        throws Exception
    {
        if (!MigrationUtils.tableExists(connection, TBL_LAYER_RESOURCE_IDS))
        {
            GenericDbDriver.runSql(
                connection,
                MigrationUtils.loadResource(
                    "2019_02_20_add-layer-data.sql"
                )
            );

            // database tables are now created. now we have to copy the data

            // lookup tables / sets of existing database entries


            try (PreparedStatement prepareStatement = connection.prepareStatement(SELECT_VOLUMES))
            {
                try (ResultSet resultSet = prepareStatement.executeQuery())
                {
                    while (resultSet.next())
                    {
                        String nodeName = resultSet.getString(1);
                        String rscName = resultSet.getString(2);
                        int vlmNr = resultSet.getInt(3);
                        long vlmFlags = resultSet.getLong(4);
                        String storPoolName = resultSet.getString(5);
                        int vlmDfnMinor = resultSet.getInt(6);
                        long vlmDfnFlags = resultSet.getLong(7);
                        int rNodeId = resultSet.getInt(8);
                        long rFlags = resultSet.getLong(9);
                        int rdTcpPort = resultSet.getInt(10);
                        String rdSecret = resultSet.getString(11);
                        String rdTransportType = resultSet.getString(12);
                        String spDriverName = resultSet.getString(13);

                        System.out.println(
                            nodeName + ", " +
                            rscName + ", " +
                            vlmNr + ", " +
                            vlmFlags + ", " +
                            storPoolName + ", " +
                            vlmDfnMinor + ", " +
                            vlmDfnFlags + ", " +
                            rNodeId + ", " +
                            rFlags + ", " +
                            rdTcpPort + ", " +
                            rdSecret + ", " +
                            rdTransportType + ", " +
                            spDriverName
                        );

                        // first, check if this is a drbd or a swordfish volume

                        if (spDriverName.equalsIgnoreCase("SwordfishInitiatorDriver") ||
                            spDriverName.equalsIgnoreCase("SwordfishTargetDriver"))
                        {
                            // only entries for sfVlmDfn table

                            // make sure to not create the same sfVlmDfn multiple times
                            if (sfVlmDfns.add(new Pair<>(rscName, vlmNr)))
                            {
                                String odata = (String) querySingleResult(
                                    connection,
                                    SELECT_SINGLE_PROP,
                                    "/VOLUMEDEFINITIONS/" + rscName.toUpperCase() + "/" + vlmNr,
                                    "StorDriver/@odata"
                                );

                                executeUpdate(
                                    connection,
                                    INSERT_SF_VLM_DFN,
                                    rscName,
                                    "",
                                    vlmNr,
                                    odata != null ? odata : new SqlNullType(Types.VARCHAR)
                                );
                            }

                            int rscLayerId = getOrCreateLayerRscIdEntry(
                                connection,
                                nodeName,
                                rscName,
                                LAYER_KIND_STORAGE,
                                null
                            );

                            executeUpdate(
                                connection,
                                INSERT_STOR_VLM,
                                rscLayerId,
                                vlmNr,
                                spDriverName.equalsIgnoreCase("SwordfishInitiatorDriver") ?
                                    PROVIDER_KIND_SF_INITIATOR :
                                    PROVIDER_KIND_SF_TARGET
                            );
                        }
                        else
                        {
                            // drbd tables
                            if (drbdRscDfns.add(rscName))
                            {
                                String peerSlotsStr = (String) querySingleResult(
                                    connection,
                                    SELECT_SINGLE_PROP,
                                    "/RESOURCEDEFINITIONS/" + rscName.toUpperCase(),
                                    "PeerSlotsNewResource"
                                );
                                executeUpdate(
                                    connection,
                                    INSERT_DRBD_RSC_DFN,
                                    rscName,
                                    "",
                                    peerSlotsStr == null ? DEFAULT_DRBD_PEER_SLOTS : Integer.parseInt(peerSlotsStr),
                                    DEFAULT_DRBD_AL_STRIPES,
                                    DEFAULT_DRBD_AL_STRIPE_SIZE,
                                    rdTcpPort,
                                    rdTransportType,
                                    rdSecret
                                );
                            }
                            if (drbdVlmDfns.add(new Pair<>(rscName, vlmNr)))
                            {
                                executeUpdate(
                                    connection,
                                    INSERT_DRBD_VLM_DFN,
                                    rscName,
                                    "",
                                    vlmNr,
                                    vlmDfnMinor
                                );
                            }

                            int drbdRscLayerId = getOrCreateLayerRscIdEntry(
                                connection,
                                nodeName,
                                rscName,
                                LAYER_KIND_DRBD,
                                null
                            );
                            if (drbdRsc.add(drbdRscLayerId))
                            {
                                String peerSlotsStr = (String) querySingleResult(
                                    connection,
                                    SELECT_SINGLE_PROP,
                                    "/RESOURCES/" + nodeName.toUpperCase() + "/" + rscName.toUpperCase(),
                                    "PeerSlots"
                                );
                                executeUpdate(
                                    connection,
                                    INSERT_DRBD_RSC,
                                    drbdRscLayerId,
                                    peerSlotsStr == null ? DEFAULT_DRBD_PEER_SLOTS : Integer.parseInt(peerSlotsStr),
                                    DEFAULT_DRBD_AL_STRIPES,
                                    DEFAULT_DRBD_AL_STRIPE_SIZE,
                                    rFlags,
                                    rNodeId
                                );
                            }
                            // no dedicated table for drbd volumes

                            int parentRscLayerId = drbdRscLayerId;

                            // crypt setup tables
                            if ((vlmDfnFlags & FLAG_VLM_DFN_ENCRYPTED) == FLAG_VLM_DFN_ENCRYPTED)
                            {
                                int cryptRscLayerId = getOrCreateLayerRscIdEntry(
                                    connection,
                                    nodeName,
                                    rscName,
                                    LAYER_KIND_CRYPT_SETUP,
                                    parentRscLayerId
                                );

                                String encryptedPw = (String) querySingleResult(
                                    connection,
                                    SELECT_SINGLE_PROP,
                                    "/VOLUMEDEFINITIONS/" + rscName.toUpperCase() + "/" + vlmNr,
                                    "CryptPasswd"
                                );
                                executeUpdate(
                                    connection,
                                    INSERT_CRYPT_VLM,
                                    cryptRscLayerId,
                                    vlmNr,
                                    encryptedPw
                                );
                                parentRscLayerId = cryptRscLayerId;
                            }

                            int storRscLayerId = getOrCreateLayerRscIdEntry(
                                connection,
                                nodeName,
                                rscName,
                                LAYER_KIND_STORAGE,
                                parentRscLayerId
                            );

                            String providerKind;
                            switch (spDriverName)
                            {
                                case "DisklessDriver":
                                    providerKind = PROVIDER_KIND_DRBD_DISKLESS;
                                    break;
                                case "LvmDriver":
                                    providerKind = PROVIDER_KIND_LVM;
                                    break;
                                case "LvmThinDriver":
                                    providerKind = PROVIDER_KIND_LVM_THIN;
                                    break;
                                case "ZfsDriver":
                                    providerKind = PROVIDER_KIND_ZFS;
                                    break;
                                case "ZfsThinDriver":
                                    providerKind = PROVIDER_KIND_ZFS_THIN;
                                    break;
                                case "SwordfishInitiatorDriver":
                                    providerKind = PROVIDER_KIND_SF_INITIATOR;
                                    break;
                                case "SwordfishTargetDriver":
                                    providerKind = PROVIDER_KIND_SF_TARGET;
                                    break;
                                default:
                                    throw new ImplementationError("Unknown storage driver name: "  + spDriverName);
                            }

                            executeUpdate(
                                connection,
                                INSERT_STOR_VLM,
                                storRscLayerId,
                                vlmNr,
                                providerKind
                            );
                        }

                    }
                }
            }

            // now that the data are copied, we can remove the old columns
        }
    }

    private int getOrCreateLayerRscIdEntry(
        Connection connection,
        String nodeName,
        String rscName,
        String layerKind,
        Integer parentRscLayerId
    )
        throws SQLException
    {
        Integer layerId = layerRscIds.get(new Pair<>(nodeName, rscName));
        if (layerId == null)
        {
            layerId = nextLayerId();
            executeUpdate(
                connection,
                INSERT_LAYER_RSC_ID,
                layerId,
                nodeName,
                rscName,
                layerKind,
                parentRscLayerId == null ? new SqlNullType(Types.INTEGER) : parentRscLayerId,
                ""
            );
        }
        return layerId;
    }

    private Object querySingleResult(Connection connection, String query, Object... params)
        throws SQLException
    {
        Object ret = null;
        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            fillParams(stmt, params);

            try (ResultSet resultSet = stmt.executeQuery())
            {
                if (resultSet.next())
                {
                    ret = resultSet.getObject(1);
                }
            }
        }
        return ret;
    }

    private void fillParams(PreparedStatement stmt, Object[] params) throws SQLException
    {
        for (int paramIdx = 1; paramIdx <= params.length; ++paramIdx)
        {
            Object param = params[paramIdx - 1];
            if (param instanceof SqlNullType)
            {
                stmt.setNull(paramIdx, ((SqlNullType) param).sqlType);
            }
            else
            {
                stmt.setObject(paramIdx, param);
            }
        }
    }

    private void executeUpdate(Connection connection, String stmtStr, Object... params)
        throws SQLException
    {
        try (PreparedStatement stmt = connection.prepareStatement(stmtStr))
        {
            fillParams(stmt, params);

            stmt.executeUpdate();
        }
    }

    private int nextLayerId()
    {
        return nextRscLayerId++;
    }

    // we do not want to reference the actual Pair class from linstor as migration should still work
    // even if some subtle property of the Pair class changes

    private static class Pair<A, B>
    {
        A objA;
        B objB;

        Pair()
        {
        }

        Pair(A objARef, B objBRef)
        {
            objA = objARef;
            objB = objBRef;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((objA == null) ? 0 : objA.hashCode());
            result = prime * result + ((objB == null) ? 0 : objB.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            boolean eq = obj != null && obj instanceof Pair;
            if (eq)
            {
                Pair<?, ?> other = (Pair<?, ?>) obj;
                eq = Objects.equals(objA, other.objA) &&
                     Objects.equals(objB, other.objB);
            }
            return eq;
        }
    }

    private static class Tripple<A, B, C>
    {
        A objA;
        B objB;
        C objC;

        Tripple()
        {
        }

        Tripple(A objARef, B objBRef, C objCRef)
        {
            objA = objARef;
            objB = objBRef;
            objC = objCRef;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((objA == null) ? 0 : objA.hashCode());
            result = prime * result + ((objB == null) ? 0 : objB.hashCode());
            result = prime * result + ((objC == null) ? 0 : objC.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            boolean eq = obj != null && obj instanceof Tripple;
            if (eq)
            {
                Tripple<?, ?, ?> other = (Tripple<?, ?, ?>) obj;
                eq = Objects.equals(objA, other.objA) &&
                     Objects.equals(objB, other.objB) &&
                     Objects.equals(objC, other.objC);
            }
            return eq;
        }
    }

    private static class SqlNullType
    {
        int sqlType;

        SqlNullType(int sqlTypeRef)
        {
            sqlType = sqlTypeRef;
        }
    }
}
