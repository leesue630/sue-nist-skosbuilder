package skos.dao;

import skos.ConnectionManager;
import skos.entity.ACC;
import skos.entity.ASCCP;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lns16 on 7/27/2017.
 */
public class ASCCPDAO{

    public Collection<ASCCP> findAsccp() throws SQLException {
        String statement = "SELECT PROPERTY_TERM, DEN, ROLE_OF_ACC_ID, DEFINITION FROM ASCCP ORDER BY ASCCP_ID";
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        Connection conn = connectionManager.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ASCCP> asccpList = new ArrayList<>();

        try {
            ps = conn.prepareStatement(statement);
            rs = ps.executeQuery();

            while (rs.next()) {
                ASCCP asccp = createAsccpFrom(rs);
                asccpList.add(asccp);
            }
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }

        return asccpList;
    }

    private ASCCP createAsccpFrom(ResultSet rs) throws SQLException {
        ASCCP asccp = new ASCCP();

        String propertyTerm = rs.getString("PROPERTY_TERM");
        asccp.setTermName(propertyTerm);

        String den = rs.getString("DEN");
        asccp.setDen(den);

        long roleOfAccId = rs.getLong("ROLE_OF_ACC_ID");
        if (roleOfAccId > 0L) {
            asccp.setRoleOfAccId(roleOfAccId);
        }

        String definition = rs.getString("DEFINITION");
        asccp.setDefinition(definition);

        return asccp;
    }

    public void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public void closeQuietly(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public void closeQuietly(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
