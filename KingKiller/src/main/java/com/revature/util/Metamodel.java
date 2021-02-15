package com.revature.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Metamodel<T> {

    private Class<T> clazz;

    /**
     * Creates a model of the incoming class
     * @param clazz the class of the new metamodel
     * @param <T> generic class type
     * @return the new metamodel created
     */
    public static <T> Metamodel<T> of(Class<T> clazz) {
        return new Metamodel<T>(clazz);
    }

    /**
     * sets the class type to the class coming in
     * @param clazz the class type of the metamodel
     */
    private Metamodel(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Class<T> getModelClass(){
        return clazz;
    }

    /**
     * get class name for model
     * @return the string representation of the full class name
     */
    public String getClassName() {
        return clazz.getName();
    }

    /**
     * Get the simple class name for the metamodel, no package names
     * @return the string representation of the simple class name
     */
    public String getSimpleClassName() {
        return clazz.getSimpleName();
    }

    /**
     * Get columns for given model
     * @return a linked list of column names for the class
     */
    public List<ColumnField> getColumns() {
        String className = clazz.getName();

        List<ColumnField> columnFields = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            field.setAccessible(true);
            try {
                columnFields.add(new ColumnField(field));
            } catch (Exception e) {
                //logger.debug("error getting obj values" + e.getMessage());
                e.printStackTrace();
            }
        }
        if (columnFields.isEmpty()) {
            throw new RuntimeException("No columns found in: " + clazz.getName());
        }
        return columnFields;
    }

    /**
     * Get all col names
     * @return a list of the string representations of the column names
     */
    public List<String> getColumnNames(){
        List<String> columnNames = new LinkedList<>();
        for(ColumnField c : this.getColumns()){
            columnNames.add(c.getColumnName());
        }
        return columnNames;
    }


    /**
     * Finds the type of column
     * @param columnName the name of the column
     * @return the class type of column, null if not found
     */
    public Class<?> findColumnType(String columnName){
        for(ColumnField c : this.getColumns()){
            if(c.getColumnName().equals(columnName)){
                return c.getType();
            }
        }

        return null;
    }

    /**
     * Finds the field name of a given column
     * @param columnName the name of the column
     * @return the field name of the column, null if not found
     */
    public String findFieldNameOfColumn(String columnName){
        for(ColumnField c : this.getColumns()){
            if(c.getColumnName().equals(columnName)){
                return c.getName();
            }
        }
        return null;
    }

    /**
     * Checks to see if the object compared equals the model
     * @param o the object being compared
     * @return true if they are equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metamodel<?> metamodel = (Metamodel<?>) o;
        return Objects.equals(clazz, metamodel.clazz);
    }

    /**
     * Will hash the current object
     * @return the hashcode for the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(clazz);
    }
}
