# JDBX User Guide

1. [Installation](#installation)
2. [Statement Classes](#classes)
   * [StaticStmt](#classes-static)
   * [PrepStmt](#classes-prep)
   * [CallStmt](#classes-call)
3. [Creating and Closing Statements](#creating-and-closing)
4. [Running SQL Queries](#queries)
   * [Intro](#queries-intro)
   * [Query class](#queries-class)
   * [Reading a Single Result Row](#queries-singlerow)
   * [Reading all Result Rows](#queries-allrows)
   * [Skipping Rows](#queries-skipping)
5. [Running DML or DDL Updates](#updates)
6. [Running a Single Command](#single-cmd)

## <a name="installation"></a>1. Installation

JDBX requires Java version 8+.<br> 
Download the [latest version](https://github.com/jdlib/jdbx/releases/latest)
and put `jdbx.jar` into the classpath.


## <a name="classes"></a>2. Statement Classes

JDBX provides three alternative statement classes to replace the corresponding JDBC classes:

JDBC|JDBX|Used to 
----|----|-------
`java.sql.Statement`|`org.jdbx.StaticStmt`|execute static, non-parameterized SQL commands
`java.sql.PreparedStatement`|`org.jdbx.PrepStmt`|execute precompiled, parameterized SQL commands
`java.sql.CallableStatement`|`org.jdbx.CallStmt`|call stored procedures

The JDBX classes are all derived from `org.jdbx.Stmt` which defines operations supported by all its implementations.

JDBX - as JDBC - differentiates between

1. Running SQL queries, returning result sets
2. Running SQL update, delete, insert commands or DDL commands, returning update counts and automatically generated keys
3. Running SQL commands which can return multiple results
4. Running SQL commands in a batch
5. Calling stored procedures

`StaticStmt` and `PrepStmt` can run SQL or DDL commands (1-4). `CallStmt` can call stored procedures (5).

### <a name="classes-static"></a>StaticStmt
uses static (i.e. non-parameterized) SQL commands. Example:

    StaticStmt stmt = ...
    int count = stmt.update("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe')");
    
### <a name="classes-prep"></a>PrepStmt    
uses precompiled, parameterized SQL commands. After it is initialized it can
be executed multiple times, using the current parameters. Example:

    PrepStmt pstmt = ...
    pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
    pstmt.params("John", "Doe");
    int count = pstmt.update();
     
### <a name="classes-call"></a>CallStmt        
is used to call stored procedures. After it is initialized it can
be executed multiple times, using the current parameters. Example:

    CallStmt cstmt = ...
    cstmt.init("{call CreateUser(?,?)}");
    cstmt.params("John", "Doe");
    cstmt.update();


## <a name="creating-and-closing"></a>3. Creating and Closing Statements

In order to create a JDBX statement you need a `java.sql.Connection` or `javax.sql.DataSource`:

     Connection con        = ...
     DataSource ds         = ...
     StaticStmt staticStmt = new StaticStmt(con); // or new StaticStmt(ds) 
     PrepStmt prepStmt     = new PrepStmt(con);   // or new PrepStmt(ds)
     CallStmt callStmt     = new CallStmt(con);   // or new CallStmt(ds)
      
Statement objects should be actively closed once they are no longer used. Since all statement classes implement `java.io.AutoCloseable` 
the typical pattern is to create and use statement objects within a Java try-with-resources statement:

     Connection con = ...
     try (StaticStmt stmt = new StaticStmt(con) {
          ... // use the statement
     }   


## <a name="queries"></a>4. Running SQL Queries

### <a name="queries-intro"></a>Intro        
In JDBC executing a query returns a result set. Given a result set you will loop over its rows, extract 
values from the rows and return the values in appropriate form.

JDBX uses the builder pattern and functional programming to avoid most of the boilerplate code needed in JDBC.

**Example 1:** Extract a list of beans from a result set

    Connection con = ...
    String sql = "SELECT * FROM Countries ORDER BY name";
     
    // JDBC:             
    Statement stmt = con.createStatement();                        
    ResultSet result = stmt.executeQuery(sql);         
    List<Country> countries = new ArrayList<>();
    while (result.next()) {
        Country country = Country.read(result); 
        countries.add(country);
    }
	    
    // JDBX
    StaticStmt stmt = new StaticStmt(con);
    List<Country> countries = stmt.createQuery(sql).rows().value(Country::read);
     
**Example 2:** Extract a single value from a result set which contains 0 or 1 rows:

    Connection con = ...
    String sql = "SELECT name FROM Countries WHERE code = ?";
    
    // JDBC:             
    PreparedStatement pstmt = con.createPreparedStatement(sql);
    pstmt.setParameter(2, "fr");                        
    ResultSet result = stmt.executeQuery();
    String name = null;         
    if (result.next())
        name = result.getString(); 
	    
    // JDBX
    PrepStmt stmt = new PrepStmt(con);
    String name = pstmt.init(sql).params("fr").createQuery().row().value().getString();


### <a name="queries-class">Query Class

`StaticStmt.createQuery(String)` and `PrepStmt.createQuery()` return a `org.jdbx.Query` object
which provides a builder API to run the query and extract values from the result set:

     Query q = stmt.createQuery(sql);
     Query q = pstmt.createQuery();
     
Because of its builder API you will rarely need to store it in a variable but rather chain
method calls until you receive the result of the query. In the following variable `q` represents
a query object obtained via `StaticStmt.createQuery(String)` or `PrepStmt.createQuery()`     
     

### <a name="queries-singlerow">Reading a Single Result Row

Use `Query.row()` if you want to get values from the first result row:     
     
    q.row()...     
    q.row().col()...              // first column
    q.row().col().getString();    // first column, as string
    q.row().col(3)...             // column by index
    q.row().col(3).getInteger();  // third colum, as Integer
    q.row().col("sort")...;       // column by name 
    q.row().col("sort").getInt(); // "sort" colum, as int
    q.row().cols();               // all columns, as `Object[]`
    q.row().cols(1,3,7);          // columns 1,3,7, as `Object[]` 
    q.row().map();                // a `Map<String,Object> mapping col name to value
    q.row().value(City::read);    // the value returned by the reader function 	 

If the result is empty, all the examples above will return a null value.
If you want exclude this case use `.row().required().`

     // will throw an exception if result has no rows
     q.row().required().col().getString()` 
     
You may also want detect the case when the result contains more than one row:
     
     // will throw an exception if result has more than one row
     q.row().unique().col().getString()` 


### <a name="queries-allrows">Reading all Result Rows

Use `Query.rows()` if you want to get values from all rows as a `List`:

    q.rows()...
    q.rows().col()...                  // first columns
    q.rows().col().getString();        // first columns as List<String>
    q.rows().col(3)...                 // columns by index
    q.rows().col(3).getDouble();       // third columns, as List<Double>
    q.rows().col("sort")...;           // columns by name 
    q.rows().col("sort").getInteger(); // "sort" columns, as List<Integer>
    q.rows().cols();                   // all columns, as `List<Object[]>`
    q.rows().cols(1,3,7);              // columns 1,3,7, as `List<Object[]>` 
    q.rows().map();                    // returns a `List<Map<String,Object>>
    q.rows().value(City::read);        // returns `List<City>`
    q.rows().read(...callback...)		// invokes the callback for every result row 
     
You may also limit the number of rows, if this is not done within the SQL query itself:

    q.rows(5)...
    

### <a name="queries-skipping">Skipping Rows

Optionally you may also skip a number of rows before you extract values by calling `Query.row()`, `rows()` or `rows(int)`:

    q.skip(1).rows()...   // all rows except the first
      
#### Accessing the ResultSet  

You still can obtain the `ResultSet` directly, if you want to process it manually:
 
    ResultSet resultSet = pstmt.createQuery().getResultSet();
    while (resultSet.next())
        ... 
    
#### Turning a ResultSet into a Query
    
If you have obtained a `ResultSet` you can also turn it into a query object for easy value extraction:

    ResultSet resultSet = ...
    List<String> names  = Query.of(resultSet).rows().col("name").getString();
     
#### Accessing the ResultSet as `org.jdbx.QueryResult`

`QueryResult` is a wrapper around a `ResultSet` which wants to improve the `ResultSet` API:
You can obtain it from a `Query`:

     QueryResult result = pstmt.createQuery().getResult();
     
or from a `ResultSet` object:
      
     ResultSet resultSet = ...
     QueryResult result = QueryResult.of(resultSet);
     
Looping over the result is done via the `.next()` method:
     
     while (result.next()) {
         ...       
     }
     
Like `Query.row()` you can easily extract values from the current result row:

    result.col()...                  // first column as String
    result.col().getString();        // first column as String
    result.col(3)...                 // column by index
    result.col(3).getDouble();       // third column, as Double
    result.col("sort")...;           // column by name 
    result.col("sort").getInteger(); // "sort" column, as Integer
    result.cols();                   // all columns, as Object[]
    result.cols(1,3,7);              // columns 1,3,7, as Object[] 
    result.map();                    // returns a Map<String,Object>
    result.value(City::read);        // returns a City object
    
If your result set is scrollable and/or updatable, you can ask for the position, move the cursor 
or perform operations on the current row. Instead of cluttering the `QueryResult` interface
with these methods they are available in service objects returned by `QueryResult.position()`
`.move()` and `.row()`

     result.position.isBeforeFirst() 
     // also: .isAfterLast(), .isLast()  

     result.move().first() 
     result.move().absolute(5) 
     result.move().toInsertRow()
     // also: .relative(), .afterLast(), .beforeFirst(), .first(), .etc.
     
     result.row().updated()
     result.row().refresh()
     // also: .insert(), .isUpdated(), .delete(), .isDeleted(), etc.
  

## <a href="updates"></a>5. Running DML or DDL Updates

Like in JDBC the term "update" includes DML UPDATE, INSERT, DELETE and DDL commands.
TODO   


## <a name="classes-abbr"></a>6. Running a Single Command
        
If you only want to run a single SQL query or DML update you can use the static helper methods in class `org.jdbx.JDBX` 
to avoid explicit creation and closing of a `StaticStmt` or `PrepStmt` object:
TODO 
 

