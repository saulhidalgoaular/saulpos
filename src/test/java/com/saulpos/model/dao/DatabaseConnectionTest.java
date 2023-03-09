package com.saulpos.model.dao;

import org.junit.jupiter.api.Test;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {
    @Test
    public void testDbConnection() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        System.out.println("====> test db connection test started;");

        DatabaseConnection.getInstance();

        System.out.println("====> test db connection test ended;");
    }

    @Test
    public void testDbConnectionWithDriver() throws ClassNotFoundException, SQLException {
        System.out.println("====> test db connection test started;");

        String JDBC_DRIVER = "org.h2.Driver";
        String DB_URL = "jdbc:h2:mem:test";
        String USER = "sa";
        String PASS = "";
        Connection conn = null;
        Statement stmt = null;

        Class.forName(JDBC_DRIVER);

        //STEP 2: Open a connection
        System.out.println("Connecting to database...");
        conn = DriverManager.getConnection(DB_URL,USER,PASS);

        //STEP 3: Execute a query
        System.out.println("Creating table in given database...");
        stmt = conn.createStatement();
        String sql =  "CREATE TABLE   REGISTRATION " +
                "(id INTEGER not NULL, " +
                " first VARCHAR(255), " +
                " last VARCHAR(255), " +
                " age INTEGER, " +
                " PRIMARY KEY ( id ))";
        stmt.executeUpdate(sql);
        System.out.println("Created table in given database...");


        // STEP 4: insert some records
        stmt = conn.createStatement();
        sql = "INSERT INTO Registration " + "VALUES (100, 'Zara', 'Ali', 18)";
        stmt.executeUpdate(sql);
        sql = "INSERT INTO Registration " + "VALUES (101, 'Mahnaz', 'Fatma', 25)";
        stmt.executeUpdate(sql);
        System.out.println("Inserted records into the table...");

        // STEP 5: select and print

        stmt = conn.createStatement();
        sql = "SELECT id, first, last, age FROM Registration";
        ResultSet rs = stmt.executeQuery(sql);

        // STEP 6: Extract data from result set
        while(rs.next()) {
            // Retrieve by column name
            int id  = rs.getInt("id");
            int age = rs.getInt("age");
            String first = rs.getString("first");
            String last = rs.getString("last");

            // Display values
            System.out.print("ID: " + id);
            System.out.print(", Age: " + age);
            System.out.print(", First: " + first);
            System.out.println(", Last: " + last);
        }

        stmt.close();
        conn.close();

        System.out.println("====> test db connection test ended;");
    }


}