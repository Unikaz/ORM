package fr.unikaz.unikorm;

import fr.unikaz.unikorm.filters.Filter;
import fr.unikaz.unikorm.filters.Op;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLDatabaseTest {

    public SQLDatabase database = new SQLDatabase("orm_test", "root", "");
    public User et1 = new User("John Snow");
    public Connection connection;


    void clearTable() {
        try {
            // some introspection to drop the table without changing the scope of `connection`
            Field connectionF = database.getClass().getDeclaredField("connection");
            connectionF.setAccessible(true);
            connection = ((Connection) connectionF.get(database));
            String req = "drop table " + Database.getEntityName(User.class) + ";";
            connection.prepareStatement(req).execute();
            String req2 = "drop table " + Database.getEntityName(Message.class) + ";";
            connection.prepareStatement(req2).execute();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException ignored) {
            // not relevant as we delete table that probably not exists
        }
    }

    @Test
    void createTable() {
        clearTable();
        assertTrue(database.createTable(User.class));
    }


    @Test
    void insert() {
        database.createTable(User.class);
        assert database.insert(et1);
    }

    @Test
    void find() {
        database.createTable(User.class);
        database.insert(et1);
        User et = database.find(User.class, new Filter(User.class, "id", Op.EQ, 1)).get(0);
        assertEquals(et.name, et1.name);

        String aValue = "An interesting value";

        // insert some more data
        for (int i = 0; i < 10; i++) {
            database.insert(new User(aValue));
        }
        for (int i = 0; i < 10; i++) {
            database.insert(new User(i + ""));
        }

        assert database.find(User.class, new Filter(User.class, "name", Op.EQ, aValue)).size() == 10;
        assert database.find(User.class, new Filter(User.class, "id", Op.EQ, 10)).get(0).name.equals(aValue);
        assert !database.find(User.class, new Filter(User.class, "id", Op.EQ, 15)).get(0).name.equals(aValue);


    }

    @Test
    void update() {
        clearTable();
        database.createTable(User.class);
        String initialValue = "update: initial value";
        String updatedValue = "update: updated value and ' test";
        User e1 = new User(initialValue);
        database.insert(e1);
        e1 = database.find(User.class, new Filter(User.class, "name", Op.EQ, initialValue)).get(0);
        e1.name = updatedValue;
        database.update(e1);
        User e2 = database.find(User.class, new Filter(User.class, "id", Op.EQ, e1.id)).get(0);
        assert e1.id.equals(e2.id);
        assert e2.name.equals(updatedValue);
    }

    @Test
    void compositeTest(){
        clearTable();
        database.createTable(User.class);
        database.createTable(Message.class);
        // create a test user
        User user = new User("John Snow");
        database.insert(user);
        user = database.find(User.class, new Filter(User.class, "name", Op.EQ, "John Snow")).get(0);
        assert user != null;
        // create a message
        Message message = new Message(user, "This is the message", Calendar.getInstance().getTime());
        assert database.insert(message);
        Message readMessage = database.find(Message.class, new Filter(User.class, "id", Op.EQ, user.id)).get(0);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(readMessage);
        assert readMessage.getMessage().equals(message.getMessage());
    }
}