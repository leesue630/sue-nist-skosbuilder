package skos.dao;

import skos.ConnectionManager;
import skos.entity.ACC;
import skos.entity.ASCCP;
import skos.entity.BCCP;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lns16 on 7/27/2017.
 */
public class BCCPDAO {

    public Collection<BCCP> findBccp() throws SQLException {
        String statement = "SELECT PROPERTY_TERM, DEN, DEFINITION FROM BCCP ORDER BY BCCP_ID";
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        Connection conn = connectionManager.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<BCCP> bccpList = new ArrayList();

        try {
            conn = connectionManager.getConnection();
            ps = conn.prepareStatement(statement);
            rs = ps.executeQuery();

            while (rs.next()) {
                BCCP bccp = createBccpFrom(rs);
                bccpList.add(bccp);
            }
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }

        return bccpList;
    }

    private BCCP createBccpFrom(ResultSet rs) throws SQLException {
        BCCP bccp = new BCCP();

        String propertyTerm = rs.getString("PROPERTY_TERM");
        bccp.setTermName(propertyTerm);

        String den = rs.getString("DEN");
        bccp.setDen(den);

        String definition = rs.getString("DEFINITION");
        bccp.setDefinition(definition);

        return bccp;
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
