package com.revature.crudao;

import com.revature.query.RequestGenerator;
import com.revature.util.Metamodel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DbDao {

    private Connection conn;

    public DbDao(Connection conn){
        this.conn = conn;
    }

    /**
     * creates some user specified data in db
     * @param model the model of the class being created
     * @param object the data being persisted
     */
    public void create(Metamodel<?> model, Object object){
        RequestGenerator generator = new RequestGenerator(model, object, "Create");
        ArrayList<String> objectValues = getObjectValues(object);

        System.out.println("Create Request: " + generator.getRequest());
        try{
            PreparedStatement pstmt = conn.prepareStatement(generator.getRequest());
            //THERE NEEDS TO BE SOME CHANGING ON THE VALUES GATHERED TO BETTER REPRESENT THEM IN SQL
            for(int i = 0; i < objectValues.size(); i++){
                pstmt.setObject(i + 1, objectValues.get(i));
            }

            pstmt.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Updates some data into a database
     * @param model the model of the class being created
     * @param newObject the data being updated
     * @param oldObject the data being overwritten
     */
    public void update(Metamodel<?> model, Object newObject, Object oldObject){
        RequestGenerator generator = new RequestGenerator(model, oldObject, "Update");
        ArrayList<String> oldObjectValues = getObjectValues(oldObject);
        ArrayList<String> newObjectValues = getObjectValues(newObject);

        try{
            PreparedStatement pstmt = conn.prepareStatement(generator.getRequest());

            for(int i = 0; i < oldObjectValues.size(); i++){
                pstmt.setObject(i+1, newObjectValues.get(i));
                pstmt.setObject(i+5, oldObjectValues.get(i));
            }

            pstmt.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Deletes some user specified data into a database
     * @param model the model of the class being deleted
     * @param object the data being deleted
     */
    public void delete(Metamodel<?> model, Object object){
        RequestGenerator generator = new RequestGenerator(model, object, "Delete");
        ArrayList<String> objectValues = getObjectValues(object);
        try{
            PreparedStatement pstmt = conn.prepareStatement(generator.getRequest());

            for(int i = 0; i < objectValues.size(); i++){
                pstmt.setObject(i + 1, objectValues.get(i));
            }

            pstmt.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * reads all the data from a table
     * @param model the model of the class being read
     */
    public List<?> read(Metamodel<?> model, Object object){
        RequestGenerator generator = new RequestGenerator(model, object, "ReadAll");
        List<Object> listOfObjects = new LinkedList<>();
        try{
            PreparedStatement pstmt = conn.prepareStatement(generator.getRequest());
            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            listOfObjects = mapResultSet(rs, rsmd, object, model);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return listOfObjects;
    }

    /**
     * reads from specific columns
     * @param model the table being readed
     * @param object the object being passed by the user
     * @param columnNames the list of column names specified by the user
     */
    public List<?> read(Metamodel<?> model, Object object, ArrayList<String> columnNames){
        RequestGenerator generator = new RequestGenerator(model, object, "ReadCols");
        List<Object> listOfObjects = new LinkedList<>();
        try{
            PreparedStatement pstmt = conn.prepareStatement(generator.getRequest());
            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            listOfObjects = mapResultSet(rs, rsmd, object, model);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return listOfObjects;
    }

    public ArrayList<String> getObjectValues(Object userObj){
        ArrayList<String> objectValues = new ArrayList<>();
        Field[] fields = userObj.getClass().getDeclaredFields();
        for(Field f : fields){
            f.setAccessible(true);
            try {
                Object value = f.get(userObj);
                objectValues.add(value.toString());
            } catch (Exception e) {
                //logger.debug("error getting obj values" + e.getMessage());
                e.printStackTrace();
            }
        }
        return objectValues;
    }

    /**
     * Maps result to object list
     * @param rs the result set returned from the database
     * @param rsmd the result set meta data
     * @param object the object composing the list
     * @return the list of objects returned from the database
     */
    private List<Object> mapResultSet(ResultSet rs, ResultSetMetaData rsmd, Object object, Metamodel<?> model){
        List<Object> objectsFromQuery = new LinkedList<>();
        try {
            List<String> columnsInResultSet = new LinkedList<>();
            int bound = rsmd.getColumnCount();
            for(int i = 0; i < bound; i++){
                columnsInResultSet.add(rsmd.getColumnName(i+1));
            }

            while(rs.next()){

                Object newObject = object.getClass().getConstructor().newInstance();

                for(String s : columnsInResultSet){
                    Class<?> type = model.findColumnType(s);
                    Object objectValue = rs.getObject(s);

                    String name = model.findFieldNameOfColumn(s);
                    String methodName = name.substring(0,1).toUpperCase() + name.substring(1);

                    Method method = object.getClass().getMethod("set" + methodName, type);
                    method.invoke(newObject, objectValue);
                }
                objectsFromQuery.add(newObject);
            }
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return objectsFromQuery;
    }
}
