# JDBX User Guide

1. [Installation](#installation)
2. [Statement classes](#statement-classes)
3. [Creating and closing statements](#creating-and-closing)
4. [Running SQL queries](#queries)

## <a name="installation"></a>1. Installation

JBCX requires Java version 8+.<br> 
Download the [latest version](https://github.com/jdlib/jdbx/releases/latest)
and put `jdbx.jar` into the classpath.


## <a name="statement-classes"></a>2. Statement classes

JDBX provides three alternative statement classes to replace the corresponding JDBC classes
while providing 100% of the original functionality:

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

`StaticStmt` and `PrepStmt` can run SQL or DDL commands (1-4).  
`StaticStmt` uses static, non-parameterized SQL commands. Example:

    StaticStmt stmt = ...
    int count = stmt.update("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe');
    
`PrepStmt` uses precompiled, parameterized SQL commands. After it is initialized it can
be executed multiple times, using the current parameters. Example:

    PrepStmt pstmt = ...
    pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?");
    pstmt.params("John", "Doe");
    int count = pstmt.update();
     
`CallStmt` is used to call stored procedures. After it is initialized it can
be executed multiple times, using the current parameters. Example:

    CallStmt cstmt = ...
    cstmt.init("{call CreateUser(?,?)}");
    cstmt.params("John", "Doe");
    cstmt.update();


### <a name="creating-and-closing"></a>3. Creating and closing statements

In order to create a JDBX statement you need a `java.sql.Connection` or `javax.sql.DataSource`:

     Connection con        = ...
     DataSource ds         = ...
     StaticStmt staticStmt = new StaticStmt(con); // or new StaticStmt(ds) 
     PrepStmt prepStmt     = new PrepStmt(con);   // or new PrepStmt(ds)
     CallStmt callStmt     = new CallStmt(con);   // or new CallStmt(ds)
      
Statement objects should be actively closed once they are no longer used.<br>
All statement classes implement `java.io.AutoCloseable` so the typical pattern is to create and use them within a Java try-with-resources statement:

     Connection con = ...
     try (StaticStmt stmt = new StaticStmt(con) {
          ... // use the statement
     }   


## <a name="queries"></a>4. Running SQL queries

In JDBC executing a query returns a result set. Given a result set you will loop over its rows, extract 
values from the rows and return the values in appropriate form.

JDBX applies the builder pattern and functional programming to avoid most of the boilerplate code needed in JDBC.

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

**The Query object**:

`StaticStmt.createQuery(String)` and `PrepStmt.createQuery()` return a `Query` object
which provides a builder API to define the query and extract values from the result set:

     Query q = stmt.createQuery(sql);
     Query q = pstmt.createQuery();
     
#### Reading a single result row     
     
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
     
You also want detect the case when the result contains more than one row:
     
      // will throw an exception if result has more than one row
     q.row().unique().col().getString()` 

#### Reading all result rows

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
    
#### Skipping rows

Optionally you may also skip a number of rows before you call `Query.row()`, `rows()` or `rows(int)`:

    q.skip(1).rows()...   // all rows except the first
      
#### Accessing the ResultSet  

You still can obtain the ResultSet directly, if you want to process it manually:
 
    ResultSet resultSet = pstmt.createQuery().getResultSet();
    while (resultSet.next())
        ... 
    
#### Turning a ResultSet into a Query
    
In the opposite if you have obtained a `ResultSet` you can turn it into a query object for easy value extraction:

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
  
