# Unikorm

This API let's you use datasources with simple methods, without defining DAO for each entities in your projects.
It's goal is also to give you a standard way to access multiple type of datasource (MySQL, REST API, etc.)

The first implementation is only for MySQL, nexts will come later (and be free to participate !)

## Available datasource
- MySQL

## Available annotations
- **Entity** : overwrite the entity name in DB
- **Field** : options for a field
  - String **name** : overwrite the field name in db
  - boolean **autoIncrement** : default false
  - boolean **primaryKey** : default false
  - boolean **unsigned** : default false
- **RelativeEntity** : indicates that this field refer to another entity
  - Class **entity** : the target entity
  - String **localField** : the field to refer in the current entity
  - String **targetField** : the field to target in the target entity
- **IgnoreField** : this field will not be used by the ORM

## Available operations
- **createTable(Class clazz)** : Create the table based on the specified class
- **insert(Object entity)** : Insert the specified entity in DB
- **find(Class clazz, IFilter filter)** : Select all entries corresponding to the clazz and the filters
- **update(Object entity)** : Update an entity in DB
- **fetch(Object entity)** : Fetch and complete the entity, using entity non-null fields as filters

## Filters
The Filter class allows the creation of filters for the request. The basic Filter is an operation like "localField = value". This can be complete using the MultiFilter which use multiple filters, with a AND or OR operator.

