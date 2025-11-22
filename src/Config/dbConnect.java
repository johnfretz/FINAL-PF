package config;

import java.sql.*;
import java.io.File;

public class dbConnect {
    
    public static Connection connectDB() {
    Connection con = null;
    try {
        Class.forName("org.sqlite.JDBC");
        // Use absolute path or path relative to project root
        String dbPath = System.getProperty("user.dir") + File.separator + "car_rental.db";
        con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        System.out.println("Connection Successful");
    } catch (ClassNotFoundException e) {
        System.out.println("SQLite JDBC driver not found!");
        System.out.println("Error: " + e.getMessage());
        e.printStackTrace();
    } catch (SQLException e) {
        System.out.println("Connection Failed:");
        System.out.println("Error: " + e.getMessage());
        e.printStackTrace();
    }
    return con;
}

    
    public void addRecord(String sql, Object... values) {
    Connection conn = dbConnect.connectDB();
    if (conn == null) {
        System.out.println("Error: Database connection is null. Cannot add record.");
        return;
    }
    
    try (Connection conn2 = conn;
         PreparedStatement pstmt = conn2.prepareStatement(sql)) {

        // Loop through the values and set them in the prepared statement dynamically
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) values[i]); // If the value is Integer
            } else if (values[i] instanceof Double) {
                pstmt.setDouble(i + 1, (Double) values[i]); // If the value is Double
            } else if (values[i] instanceof Float) {
                pstmt.setFloat(i + 1, (Float) values[i]); // If the value is Float
            } else if (values[i] instanceof Long) {
                pstmt.setLong(i + 1, (Long) values[i]); // If the value is Long
            } else if (values[i] instanceof Boolean) {
                pstmt.setBoolean(i + 1, (Boolean) values[i]); // If the value is Boolean
            } else if (values[i] instanceof java.util.Date) {
                pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime())); // If the value is Date
            } else if (values[i] instanceof java.sql.Date) {
                pstmt.setDate(i + 1, (java.sql.Date) values[i]); // If it's already a SQL Date
            } else if (values[i] instanceof java.sql.Timestamp) {
                pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]); // If the value is Timestamp
            } else {
                pstmt.setString(i + 1, values[i].toString()); // Default to String for other types
            }
        }

        pstmt.executeUpdate();
        System.out.println("Record added successfully!");
    } catch (SQLException e) {
        System.out.println("Error adding record: " + e.getMessage());
        e.printStackTrace();
    }
}
    
// Dynamic view method to display records from any table
    public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames) {
        // Check that columnHeaders and columnNames arrays are the same length
        if (columnHeaders.length != columnNames.length) {
            System.out.println("Error: Mismatch between column headers and column names.");
            return;
        }

        Connection conn = dbConnect.connectDB();
        if (conn == null) {
            System.out.println("Error: Database connection is null. Cannot retrieve records.");
            return;
        }
        
        try (Connection conn2 = conn;
             PreparedStatement pstmt = conn2.prepareStatement(sqlQuery);
             ResultSet rs = pstmt.executeQuery()) {

                // Print the headers dynamically
                StringBuilder headerLine = new StringBuilder();
                headerLine.append("--------------------------------------------------------------------------------\n| ");
                for (String header : columnHeaders) {
                    headerLine.append(String.format("%-20s | ", header)); // Adjust formatting as needed
                }
                headerLine.append("\n--------------------------------------------------------------------------------");

                System.out.println(headerLine.toString());

                // Print the rows dynamically based on the provided column names
                while (rs.next()) {
                    StringBuilder row = new StringBuilder("| ");
                    for (String colName : columnNames) {
                        String value = rs.getString(colName);
                        row.append(String.format("%-20s | ", value != null ? value : "")); // Adjust formatting
                    }
                    System.out.println(row.toString());
                }
                System.out.println("--------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("Error retrieving records: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    //-----------------------------------------------
    // UPDATE METHOD
    //-----------------------------------------------
    
    public void updateRecord(String sql, Object... values) {
        Connection conn = dbConnect.connectDB();
        if (conn == null) {
            System.out.println("Error: Database connection is null. Cannot update record.");
            return;
        }
        
        try (Connection conn2 = conn;
             PreparedStatement pstmt = conn2.prepareStatement(sql)) {

            // Loop through the values and set them in the prepared statement dynamically
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]); // If the value is Integer
                } else if (values[i] instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) values[i]); // If the value is Double
                } else if (values[i] instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) values[i]); // If the value is Float
                } else if (values[i] instanceof Long) {
                    pstmt.setLong(i + 1, (Long) values[i]); // If the value is Long
                } else if (values[i] instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) values[i]); // If the value is Boolean
                } else if (values[i] instanceof java.util.Date) {
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime())); // If the value is Date
                } else if (values[i] instanceof java.sql.Date) {
                    pstmt.setDate(i + 1, (java.sql.Date) values[i]); // If it's already a SQL Date
                } else if (values[i] instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]); // If the value is Timestamp
                } else {
                    pstmt.setString(i + 1, values[i].toString()); // Default to String for other types
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating record: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Add this method in the config class
public void deleteRecord(String sql, Object... values) {
    Connection conn = dbConnect.connectDB();
    if (conn == null) {
        System.out.println("Error: Database connection is null. Cannot delete record.");
        return;
    }
    
    try (Connection conn2 = conn;
         PreparedStatement pstmt = conn2.prepareStatement(sql)) {

        // Loop through the values and set them in the prepared statement dynamically
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) values[i]); // If the value is Integer
            } else {
                pstmt.setString(i + 1, values[i].toString()); // Default to String for other types
            }
        }

        pstmt.executeUpdate();
        System.out.println("Record deleted successfully!");
    } catch (SQLException e) {
        System.out.println("Error deleting record: " + e.getMessage());
        e.printStackTrace();
    }
}


public java.util.List<java.util.Map<String, Object>> fetchRecords(String sqlQuery, Object... values) {
    java.util.List<java.util.Map<String, Object>> records = new java.util.ArrayList<>();

    Connection conn = dbConnect.connectDB();
    if (conn == null) {
        System.out.println("Error: Database connection is null. Cannot fetch records.");
        return records;
    }
    
    try (Connection conn2 = conn;
         PreparedStatement pstmt = conn2.prepareStatement(sqlQuery)) {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }

        try (ResultSet rs = pstmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                records.add(row);
            }
        }

    } catch (SQLException e) {
        System.out.println("Error fetching records: " + e.getMessage());
        e.printStackTrace();
    }

    return records;
}


    
}