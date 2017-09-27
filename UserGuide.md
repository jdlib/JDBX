JDBX User Guide

1. [Statement](#stmts)
   * [Intro](#stmts-intro)
   * [Creating and Closing Statements](#stmts-creating)
   * [StaticStmt](#stmts-static)
   * [PrepStmt](#stmts-prep)
   * [CallStmt](#stmts-call)
2. [Running SQL Queries](#queries)
   * [Intro](#queries-intro)
   * [Query Class](#queries-queryclass)
   * [Reading a Single Result Row](#queries-singlerow)
   * [Reading all Result Rows](#queries-allrows)
   * [Skipping Rows](#queries-skipping)
   * [Accessing the ResultSet](#queries-resultset)
   * [Turning a ResultSet into a Query](#queries-result-toquery)
   * [QueryResult Class](#queries-queryresultclass)
3. [Running DML or DDL Updates](#updates)
   * [Update Class](#updates-updateclass)
   * [Run Updates](#updates-running)
   * [Run Updates and Read Auto-generated Primary Key Values](#updates-autogen)
4. [Running a Single Command](#single-cmd)


## <a name="stmts"></a>1. Statements

### <a name="stmts-intro"></a>Intro
        
JDBX provides three alternative statement classes to replace the corresponding JDBC statements:

JDBC|JDBX|Used to 
----|----|-------
`java.sql.Statement`|`org.jdbx.StaticStmt`|execute static, non-parameterized SQL commands
`java.sql.PreparedStatement`|`org.jdbx.PrepStmt`|execute precompiled, parameterized SQL commands
`java.sql.CallableStatement`|`org.jdbx.CallStmt`|call stored procedures

JDBX - as JDBC - differentiates between

1. Running SQL queries, returning result sets
2. Running SQL UPDATE, DELETE, INSERT commands or DDL commands, returning update counts and automatically generated keys
3. Running SQL commands which can return multiple results
4. Running SQL commands in a batch
5. Calling stored procedures

`StaticStmt` and `PrepStmt` can run SQL or DDL commands (1-4), `CallStmt` can call stored procedures (5).


### <a name="stmts-creating"></a>Creating and Closing Statements

In order to create a JDBX statement you need a `java.sql.Connection` or `javax.sql.DataSource`:

     Connection con   = ...
     DataSource ds    = ...
     StaticStmt stmt  = new StaticStmt(con); // or new StaticStmt(ds) 
     PrepStmt   pstmt = new PrepStmt(con);   // or new PrepStmt(ds)
     CallStmt   cstmt = new CallStmt(con);   // or new CallStmt(ds)
      
Statement objects should be actively closed once they are no longer used. Since all statement classes implement `java.io.AutoCloseable` 
the typical pattern is to create and use statement objects within a Java try-with-resources statement:

     Connection con = ...
     try (StaticStmt stmt = new StaticStmt(con)) {
          ... // use the statement
     }   

Statements created from a `DataSource` will use a connection obtained from the `DataSource`. When the statement is closed the 
connection will also be closed automatically.


### <a name="stmts-static"></a>`StaticStmt`

`org.jdbx.StaticStmt` can execute static, i.e. non-parameterized, SQL commands. Example:

    StaticStmt stmt = ...
    int count = stmt.update("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe')");
    

### <a name="stmts-prep"></a>`PrepStmt`

`org.jdbx.PrepStmt` can execute precompiled, parameterized SQL commands. After it is initialized it can
be executed multiple times, using different parameter values. 
Contrary to `java.sql.PreparedStatement` you can also re-initialize the command. Example:

    PrepStmt pstmt = ...
    pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
    pstmt.params("John", "Doe").update();
    pstmt.params("Mary", "Jane").update();
    pstmt.init("UPDATE Users SET name = ? WHERE id = ?");
    ...
     

### <a name="stmts-call"></a>`CallStmt`

`org.jdbx.CallStmt` can call stored procedures. After it is initialized it can
be executed multiple times, using different parameter values. Example:

    CallStmt cstmt = ...
    cstmt.init("{call CreateUser(?,?)}");
    cstmt.params("John", "Doe");
    cstmt.update();


## <a name="queries"></a>2. Running SQL Queries

### <a name="queries-intro"></a>Intro        
In JDBC executing a query returns a `java.sql.ResultSet`. Given the result-set you will loop over its rows, extract 
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
    pstmt.setParameter(1, "fr");                        
    ResultSet result = stmt.executeQuery();
    String name = null;         
    if (result.next())
        name = result.getString(); 
	    
    // JDBX
    PrepStmt pstmt = new PrepStmt(con);
    String name = pstmt.init(sql).params("fr").createQuery().row().col().getString();


### <a name="queries-queryclass">Query Class

`StaticStmt.createQuery(String)` and `PrepStmt.createQuery()` return a `org.jdbx.Query` object
which provides a builder API to run the query and extract values from the result set:

     Query q = stmt.createQuery(sql);
     Query q = pstmt.createQuery();
     
Because of its builder API you will rarely need to store a `Query` object in a variable but rather chain
method calls until you receive the result of the query. In the following variable `q` represents
a query object obtained via `StaticStmt.createQuery(String)` or `PrepStmt.createQuery()`     
     

### <a name="queries-singlerow">Reading a Single Result Row

Call `Query.row()` to retrieve a builder to read values from the first result row:     
     
    q.row()...     
    q.row().col()...              // returns a builder to retrieve a value of the first column
    q.row().col().getString();    // returns the value of the first column as String
    q.row().col(3)...             // returns a builder to retrieve a value of the third column
    q.row().col(3).getInteger();  // returns the value of the third column as Integer
    q.row().col("sort")...;       // returns a builder to retrieve a value of the "sort" column  
    q.row().col("sort").getInt(); // returns the value of "sort" column as int
    q.row().cols();               // returns the value of all columns, as Object[]
    q.row().cols(1,3,7);          // returns the value of columns 1,3,7, as Object[] 
    q.row().map();                // returns a Map<String,Object> mapping column name to value
    q.row().value(City::read);    // returns the value returned by the reader function 	 

If the result is empty, all the examples above will return a null value (or a default value for primitive terminals like `getInt()`).
If you want rule out this case use `.row().required().`

     // will throw an exception if the result contains no rows
     q.row().required().col().getString()` 
     
You may also want detect the case when the result contains more than one row, using `.row().unique()`:
     
     // will throw an exception if the result contains more than one row
     q.row().unique().col().getString()` 


### <a name="queries-allrows"></a>Reading all Result Rows

Use `Query.rows()` if you want to get values from all rows as a `List`:

    q.rows()...
    q.rows().col()...                  // values of first column
    q.rows().col().getString();        // values of first column as List<String>
    q.rows().col(3)...                 // values of column by index
    q.rows().col(3).getDouble();       // values of third column, as List<Double>
    q.rows().col("sort")...;           // values of column by name 
    q.rows().col("sort").getInteger(); // values of "sort" column, as List<Integer>
    q.rows().cols();                   // values of all columns, as List<Object[]>
    q.rows().cols(1,3,7);              // values of columns 1,3,7, as List<Object[]> 
    q.rows().map();                    // a List<Map<String,Object>>
    q.rows().value(City::read);        // returns List<City>
    q.rows().read(...callback...)      // invokes the callback for every result row 
     
You may also limit the number of processed rows, if this is not done within the SQL query itself:

    q.rows(5)...
    

### <a name="queries-skipping"></a>Skipping Rows

Call `Query.skip(int)` if you want to skip a number of rows before you extract values 
by calling `Query.row()`, `rows()` or `rows(int)`:

    q.skip(3).rows()...   // all rows after the first three rows


### <a name="queries-resultset"></a>Accessing the ResultSet  
      
You still can obtain the `java.sql.ResultSet` of a query if you want to process it by yourself:
 
    ResultSet resultSet = q.resultSet();
    while (resultSet.next())
        ... 
    
### <a name="queries-result-toquery"></a>Turning a ResultSet into a Query
    
The other way round, if you have a `java.sql.ResultSet` you can also turn it into a query object for easy value extraction:

    ResultSet resultSet = ...
    List<String> names  = Query.of(resultSet).rows().col("name").getString();
    
     
### <a name="queries-queryresultclass"></a>QueryResult Class

The builder API of `Query` allows easy extraction of values from a result set in forward only manner. For scrollable
result sets and its operations JDBX provides `org.jdbx.QueryResult`: It is a wrapper around `java.sql.ResultSet` 
which improves the `ResultSet` API similar to the effort of the JDBX statement classes with respect to its JDBC counterparts.

You can obtain a `QueryResult` from a `Query`:

     QueryResult qr = q.result();
     
or from a `ResultSet` object:
      
     ResultSet resultSet = ...
     QueryResult qr = QueryResult.of(resultSet);
     
Use the `.next()` method to loop over the result rows: 
     
     while (qr.next()) {
         ...       
     }
     
`QueryResult` offers similar methods like the builder returned by `Query.row()` to extract values from the current result:

    qr.col()...                  // first column as String
    qr.col().getString();        // first column as String
    qr.col(3)...                 // column by index
    qr.col(3).getDouble();       // third column, as Double
    qr.col("sort")...;           // column by name 
    qr.col("sort").getInteger(); // "sort" column, as Integer
    qr.cols();                   // all columns, as Object[]
    qr.cols(1,3,7);              // columns 1,3,7, as Object[] 
    qr.map();                    // returns a Map<String,Object>
    
If your result set is scrollable and/or updatable, you can ask for the position, move the cursor 
or perform operations on the current row. Instead of cluttering the `QueryResult` interface
with these methods (as done in `java.sql.ResultSet`) they are available in service objects returned by `QueryResult.position()`,
`.move()` and `.row()`:

    qr.position().isBeforeFirst() 
    // also: .isAfterLast(), .isLast()  

    qr.move().first() 
    qr.move().absolute(5) 
    qr.move().toInsertRow()
    // also: .relative(), .afterLast(), .beforeFirst(), .first(), .etc.
     
    qr.row().updated()
    qr.row().refresh()
    // also: .insert(), .isUpdated(), .delete(), .isDeleted(), etc.
  

## <a name="updates"></a>3. Running DML or DDL Updates

JDBX - as JDBC - uses the term *Update* for DML (i.e. UPDATE, INSERT, DELETE) and DDL commands.
Running a DML command can return the number of affected records and the auto-generated values of primary key columns.

### <a name="updates-updateclass">Update Class

Updates can be executed by either using a `StaticStmt` or a `PrepStmt`:

`StaticStmt.createUpdate(String)` and `PrepStmt.createUpdate()` return a `org.jdbx.Update` object
which provides a builder API to configure and run the update:

     Update u = stmt.createUpdate(sql);
     Update u = pstmt.createUpdate();
     
Because of its builder API you will rarely need to store an `Update` object in a variable but rather chain
method calls. In the following the variable `u` represents
an `Update` object obtained via `StaticStmt.createUpdate(String)` or `PrepStmt.createUpdate()`     
     

### <a name="updates-running">Run an Update

If you just want to run an update command and are not interested in auto-generated values you simply call
`Update.run()` or `Update.runLarge()` which return the number of affected records as `ìnt` or `long` value.

	int updateCount = u.run();
	// or: long largeUpdateCount = u.runLarge();


### <a name="updates-autogen">Run an Update and Read Auto-generated Primary Key Values

TODO


TODO Execute
	

## <a name="classes-abbr"></a>4. Running a Single Command
        
If you only want to run a single SQL query or DML update you can use the static helper methods in class `org.jdbx.JDBX` 
to avoid explicit creation and closing of a `StaticStmt` or `PrepStmt` object:
TODO 
 

