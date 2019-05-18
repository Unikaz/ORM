# ORM

This API let's you use datasources with simple methods, without defining DAO for each entities in your projects.
It's goal is also to give you a standard way to access multiple type of datasource (MySQL, REST API, etc.)

The first implementation is only for MySQL, nexts will come later (and be free to participate !)

## Available datasource
- MySQL

## Available annotations
- Entity : overwrite the entity name in DB
- FieldName : overwrite the field name in DB
- IgnoreField : this field will not be used by the ORM
- Unsigned
- AutoIncrement
- PrimaryKey

## Filters
The Filter class allows the creation of filters for the request. The basic Filter is an operation like "field = value". This can be complete using the MultiFilter which use multiple filters, with a AND or OR operator.

