package skos.dao;

/**
 * Created by lns16 on 7/27/2017.
 */

import skos.ConnectionManager;
import skos.entity.ACC;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ACCDAO{

    Connection conn;

    public Collection<ACC> findAcc() throws SQLException {
        String statement = "SELECT OBJECT_CLASS_TERM, DEN, BASED_ACC_ID, DEFINITION, OAGIS_COMPONENT_TYPE FROM ACC WHERE OAGIS_COMPONENT_TYPE < 4 ORDER BY ACC_ID";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ACC> accList = new ArrayList<>();

        try {
            ps = conn.prepareStatement(statement);
            rs = ps.executeQuery();

            while (rs.next()) {
                ACC acc = createAccFrom(rs);
                accList.add(acc);
            }
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }

        return accList;
    }

    public ACC findAccByAccId(Long accId) throws SQLException {
        String statement = "SELECT OBJECT_CLASS_TERM, DEN, BASED_ACC_ID, DEFINITION, OAGIS_COMPONENT_TYPE FROM ACC WHERE ACC_ID = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        ACC acc = null;
        try {
            ps = conn.prepareStatement(statement);
            int parameterIndex = 1;
            ps.setLong(parameterIndex++, accId);
            rs = ps.executeQuery();

            if (rs.next()) {
                acc = createAccFrom(rs);
            }
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }

        return acc;
    }

    private ACC createAccFrom(ResultSet rs) throws SQLException {
        ACC acc = new ACC();

        String objectClassTerm = rs.getString("OBJECT_CLASS_TERM");
        acc.setTermName(objectClassTerm);

        String den = rs.getString("DEN");
        acc.setDen(den);

        long basedAccId = rs.getLong("BASED_ACC_ID");
        if (basedAccId > 0L) {
            acc.setBasedAccId(basedAccId);
        }

        String definition = rs.getString("DEFINITION");
        acc.setDefinition(definition);

        Boolean validOCT = true;
        if(rs.getInt("OAGIS_COMPONENT_TYPE") > 3){
            validOCT = false;
        }
        acc.setValidOCT(validOCT);

        return acc;
    }

    public void openConnection(){
        try {
            ConnectionManager connectionManager = ConnectionManager.getInstance();
            conn = connectionManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeQuietly() {
        if (conn != null) {
            try {
                conn.close();
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
