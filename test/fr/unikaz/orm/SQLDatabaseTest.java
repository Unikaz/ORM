package fr.unikaz.orm;

import fr.unikaz.orm.filters.Filter;
import fr.unikaz.orm.filters.Op;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLDatabaseTest {

    public SQLDatabase database = new SQLDatabase("orm_test", "root", "");
    public EntityTest et1 = new EntityTest("value1", "value2");
    public Connection connection;



    void clearTable(){
        try {
            // some introspection to drop the table without changing the scope of `connection`
            Field connectionF = database.getClass().getDeclaredField("connection");
            connectionF.setAccessible(true);
            connection = ((Connection)connectionF.get(database));
            String req = "drop table " + Database.getEntityName(EntityTest.class) + ";";
            connection.prepareStatement(req).execute();
        } catch (NoSuchFieldException | IllegalAccessException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createTable() {
        clearTable();
        assertTrue(database.createTable(EntityTest.class));
    }


    @Test
    void insert() {
        database.createTable(EntityTest.class);
        assert database.insert(et1);
    }

    @Test
    void find() {
        database.createTable(EntityTest.class);
        database.insert(et1);
        EntityTest et = database.find(EntityTest.class, new Filter(EntityTest.class, "id", Op.EQ, 1)).get(0);
        assertEquals(et.somevalue, et1.somevalue);

        String aValue = "An interesting value";

        // insert some more data
        for (int i = 0; i < 10; i++) {
            database.insert(new EntityTest(aValue, "osef"));
        }
        for (int i = 0; i < 10; i++) {
            database.insert(new EntityTest(i+"", "osef"));
        }

        assert database.find(EntityTest.class, new Filter(EntityTest.class, "somevalue", Op.EQ, aValue)).size() == 10;
        assert database.find(EntityTest.class, new Filter(EntityTest.class, "id", Op.EQ, 10)).get(0).somevalue.equals(aValue);
        assert !database.find(EntityTest.class, new Filter(EntityTest.class, "id", Op.EQ, 15)).get(0).somevalue.equals(aValue);


    }

}