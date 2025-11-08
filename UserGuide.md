JDBX User Guide

1. [Intro](#intro)
2. [Statements](#stmts)
   * [2.1 Statement classes](#stmts-classes)
   * [2.2 Create and close statements](#stmts-create)
   * [2.3 Initialize statements](#stmts-init)
   * [2.4 Configure statements](#stmts-options)
   * [2.5 Setting parameters](#stmts-params)
3. [Running SQL queries](#queries)
   * [3.1 Query class](#queries-query)
   * [3.2 Read a single result row](#queries-singlerow)
   * [3.3 Read all result rows](#queries-allrows)
   * [3.4 Skip rows](#queries-skipping)
   * [3.5 ResultCursor class](#queries-resultcursorclass)
   * [3.6 Converting from/to ResultSet](#queries-resultset)
4. [Running DML or DDL updates](#updates)
   * [4.1 Run the update](#updates-run)
   * [4.2 Update class](#updates-updateclass)
   * [4.3 Read returned columns values](#updates-readcols)
   * [4.4 Return large update counts](#updates-large)
5. [Execute arbitrary SQL commands](#execute)
6. [Run batches](#batches)
7. [Exceptons](#exceptions)
8. [Run a single command](#single-cmd)
9. [Handling multiple statements](#multi-stmts)
10. [More Examples](#more-examples)


## <a name="stmts"></a>1. Intro

JDBX offers a JDBC alternative to execute SQL commands and read query or update results.
For that it replaces JDBC `Statement` and `ResultSet` classes with an own API.
Still the starting point of all its operations is a `java.sql.Connection` or `javax.sql.DataSource` object. 
 

## <a name="stmts"></a>2. Statements


### <a name="stmts-classes"></a>2.1 Statement classes

Statements are used to execute SQL commands. 
JDBX provides three alternative statement classes to replace the corresponding JDBC classes. 
Implementation-wise the JDBX statements wrap their JDBC counterparts:

JDBC|JDBX|Used to 
----|----|-------
`java.sql.Statement`|`org.jdbx.StaticStmt`|execute static, non-parameterized SQL commands
`java.sql.PreparedStatement`|`org.jdbx.PrepStmt`|execute precompiled, parameterized SQL commands
`java.sql.CallableStatement`|`org.jdbx.CallStmt`|call stored procedures

JDBX - as JDBC - differentiates between

1. Running a SQL SELECT query, returning a result.
2. Running a SQL update command (INSERT, UPDATE, DELETE or DDL command or other non SELECT command), returning an update count and generated column values.
3. Running a SQL command whose result type is unknown or which can return multiple query and/or update results.
4. Running SQL commands in a batch.
5. Calling stored procedures, returning values of OUT parameters and/or query results.

`StaticStmt` and `PrepStmt` can be used to run commands as described in (1-4), `CallStmt` can call stored procedures (5).


### <a name="stmts-create"></a>2.2 Create and close statements

In order to create a JDBX statement you need a `java.sql.Connection` or `javax.sql.DataSource`:

     Connection con   = ...
     DataSource ds    = ...
     StaticStmt stmt  = new StaticStmt(con); // or new StaticStmt(ds) 
     PrepStmt   pstmt = new PrepStmt(con);   // or new PrepStmt(ds)
     CallStmt   cstmt = new CallStmt(con);   // or new CallStmt(ds)
     
Statement objects should be actively **closed** once they are no longer used. Since all JDBX statement classes implement `java.io.AutoCloseable` 
the typical pattern is to create and use a statement object within a try-with-resources block:

     Connection con = ...
     try (StaticStmt stmt = new StaticStmt(con)) {
          ... // use the statement
     }   

Statements created from a `DataSource` will use a connection obtained from the `DataSource`. When the statement is closed that 
connection will also be closed automatically.

### <a name="stmts-init"></a>2.3 Initialize statements

You need to initialize `PrepStmt` and `CallStmt` by calling its `init(String)` method with a SQL command before you can execute the statement. 
The SQL command string uses `?` as placeholder for positional parameters:

    PrepStmt pstmt = new PrepStmt(con);
    pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
  
    CallStmt cstmt = new CallStmt(con);
    cstmt.init("{call getUserName(?, ?)}");
    
PrepStmt allows for more initialization options: Calling `init()` on a PrepStmt will return an initialization builder.
The builder allows you to define the returned columns or if the sql statement uses named parameters.
The terminal call of the `.sql(String)` method to specify a SQL command is mandatory in order to complete the initialization:

    // instruct the statement to return the value of the 'id' column (see the chapter on running updates)
    pstmt.init().returnCols("id").sql("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
    
    // use named parameters instead of numbered parameters
    pstmt.init().namedParams().sql("UPDATE Users SET name = :name WHERE id = :id");

A `StaticStmt` is already initialized when it is created - the SQL command which is executed by the `StaticStmt`
is not precompiled and passed to the statement when you run a query or update.

Implementation-wise initialization of a JDBX statement is the equivalent of creating a JDBC statement
You may reinitialize a JDBX statement at any time which internally will recreate a new JDBC statement object.    

### <a name="stmts-options"></a>2.4 Configure statements

To set or retrieve statement options use the builder returned by the `options()` method of the statement. JDBX also
introduces proper enums for result type, concurreny, fetch direction and holdability.

    stmt.options()
        .setQueryTimeoutSeconds(20)
        .setFetchRows(5000)
        .setResultType(ResultType.SCROLL_INSENSITIVE);
    int timeoutSecs = stmt.options().getQueryTimeoutSeconds();
    
### <a name="stmts-params"></a>2.5 Setting parameters

The SQL command string of a `PrepStmt`and `CallStmt` can (or should) contain positional parameters, specified as `?` within the SQL string: 
 
    PrepStmt pstmt = ...
    pstmt.init("INSERT INTO Rooms (id, name, area) VALUES (DEFAULT, ?, ?)");
    
Before running the command you need to provide parameter values. Parameters are referred to sequentially by number,
with the first parameter being 1:

    pstmt.param(1).setString("Living Room");     
    pstmt.param(2).setInt(51);
    
The builder returned by `param(int)` provides setters for various types. In most cases the JDBC driver
is able to recognize the type, so you can skip the explicit setter and simply pass parameters as objects: 
  
    pstmt.param(1, "Living Room").param(2, 51);
    
or even shorter, setting all parameter values in one call:
    
    pstmt.params("Living Room", 51);
    
JDBX - unlike JDBC - also supports named parameters. On the builder returned by `PrepStmt.init()` call the method `namedParams()`
and in the SQL string specify parameters as a colon followed by the parameter name. A named parameter may occur
several times. To set a named parameter value call `param(String)` using the parameter name and then call the appropriate
setter:

    pstmt.init().namedParams()
        .sql("INSERT INTO Users (id, lastname, firstname, fullname) VALUES (DEFAULT, :last, :first, :last || ', ' || :first)");
    pstmt.param("last").setString("John");     
    pstmt.param("first").setString("Doe");

Setting parameters on a `CallStmt` works exactly the same. Additionally you can register OUT and INOUT parameters, and 
read the values of OUT and INOUT parameters after the statement has been executed:

	CallStmt ctstmt = ...
    cstmt.init("{call GetUserName(?,?,?)}");         // the SQL cmd has three parameters 
    cstmt.param(1).setLong(831L);                    // set the value of IN parameter 1
    cstmt.param(2).out(java.sql.Types.VARCHAR);      // register type of OUT parameter 2
    cstmt.param(3).out(java.sql.Types.VARCHAR);      // register type of OUT parameter 3
    cstmt.execute();                                 // execute the command, explained in next chapters
    String lastName  = cstmt.param(2).getString();   // read the value of OUT parameter 2 
    String firstName = cstmt.param(3).getString();   // read the value of OUT parameter 3
            
To clear current parameters of a `PrepStmt` or `CallStmt` call:

    pstmt.clearParams();              
    cstmt.clearParams();              


## <a name="queries"></a>3. Run SQL queries

In JDBC executing a query returns a `java.sql.ResultSet`. Given the `ResultSet` you can loop over its rows and extract 
values from the rows.

JDBX uses fluent APIs and functional programming to avoid most of the boilerplate code needed in JDBC.

**Example 1:** Read all rows from a query result and convert each row into a data object

    Connection con = ...
    String sql = "SELECT * FROM Cities ORDER BY name";
     
    // JDBC:             
    try (Statement stmt = con.createStatement()) {                        
        ResultSet result = stmt.executeQuery(sql);         
        List<City> cities = new ArrayList<>();
        while (result.next()) {
            City city = City.read(result); 
            cities.add(city);
        }
        return cities;
    }
	    
    // JDBX:
    try (StaticStmt stmt = new StaticStmt(con)) {
        return stmt.query(sql).rows().read(City::read);
    }
     
**Example 2:** Extract a single value from the first row of a result set

    Connection con = ...
    String sql = "SELECT name FROM Cities WHERE code = ?";
    
    // JDBC:             
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        pstmt.setString(1, "MUC");                        
        ResultSet result = pstmt.executeQuery();
        String name = null;         
        if (result.next())
            name = result.getString(1);
        return name;
    } 
	    
    // JDBX:
    try (PrepStmt pstmt = new PrepStmt(con)) {
        return pstmt.init(sql).params("MUC").query().row().col().getString();
    }


### <a name="queries-query">3.1 Query class

To execute a SQL query you need an [initialized](#stmts-init) JDBX statement. 

`StaticStmt.query(String)`, `PrepStmt.query()`, `CallStmt.query()` return a `org.jdbx.Query` object
which provides a fluent API to extract values from the result:

     Query q = stmt.query("SELECT * FROM Cities WHERE id = 51");
     Query q = pstmt.init("SELECT * FROM Cities WHERE id = ?").params(51).query();
     
In the following variable `q` represents a `Query` object obtained from a call to a statement's `query` method.     
Thanks to its fluent API you rarely will need to store a `Query` in a local variable but rather chain
method calls until you receive the result value of the query.      

Note that the actual JDBC query is usually not run until you invoke the terminal method of the call chain.


### <a name="queries-singlerow">3.2 Read a single result row

Call `Query.row()` to retrieve a `QueryOneRow` builder to read values from the **first result row**:     
     
    q.row()...     
    q.row().col()...              // returns a builder to retrieve a value of the first column
    q.row().col().getString();    // returns the value of the first column as String
    q.row().col(3)...             // returns a builder to retrieve a value of the third column
    q.row().col(3).getInteger();  // returns the value of the third column as Integer
    q.row().col("sort")...        // returns a builder to retrieve a value of the "sort" column  
    q.row().col("sort").getInt(); // returns the value of "sort" column as int
    q.row().cols();               // returns the value of all columns, as Object[]
    q.row().cols(1,3,7);          // returns the value of columns 1,3,7, as Object[] 
    q.row().map();                // returns a Map<String,Object> mapping column name to value

If the result is empty, all the examples above will return a null value (or a default value for primitive terminals like `getInt()`).
If you want to rule out this case use `Query.row().required()`:

     // will throw a JdbxException if the result contains no rows
     q.row().required().col().getString()
     
You may also want to detect the case when the result contains more than one row, using `Query.row().unique()`:
     
     // will throw a JdbxException if the result contains more than one row
     q.row().unique().col().getString()


### <a name="queries-allrows"></a>3.3 Read all result rows

Call `Query.rows()` to retrieve a `QResultRows` builder to read values from all rows and return as `java.util.List`:

    q.rows()...
    q.rows().col()...                  // return values of first column
    q.rows().col().getString();        // return values of first column as List<String>
    q.rows().col(3)...                 // return values of column by number
    q.rows().col(3).getDouble();       // return values of third column, as List<Double>
    q.rows().col("sort")...;           // return values of column by name 
    q.rows().col("sort").getInteger(); // return values of "sort" column, as List<Integer>
    q.rows().cols();                   // return values of all columns, as List<Object[]>
    q.rows().cols(1,3,7);              // return values of columns 1,3,7, as List<Object[]> 
    qr.rows().map();                    // return a List<Map<String,Object>>
     
You may also limit the number of processed rows if this is not done within the SQL query itself:

    q.rows().max(5) ...
    

### <a name="queries-skipping"></a>3.4 Skip rows

Call `Query.skip(int)` if you want to skip a number of rows before you extract values 
by calling `Query.row()`, `.rows()` or `.rows(int)`:

    qr.skip(3).rows()...   // all rows after the first three rows


### <a name="queries-resultcursorclass"></a>3.5 ResultCursor class

As shown above the `Query` class makes it easy to extract a column value or an array of column values from a result row
using the various `col()` and `cols()` builder methods.

For more complicated cases JDBX provides the `org.jdbx.ResultCursor` class which replaces/wraps `java.sql.ResultSet` and allows
you to navigate thought the result rows and read values from each row.

When positioned on a result row, `ResultCursor` offers similar methods like the builder returned by `Query.row()` to extract values 
from the row:

    ResultCursor rc = ...        // a ResultCursor, positioned on a result row
    rc.col()...                  // first column
    rc.col().getString();        // first column as String
    rc.col(3)...                 // column by number
    rc.col(3).getDouble();       // third column as double
    rc.col("sort")...;           // column by name 
    rc.col("sort").getInteger(); // "sort" column, as Integer
    rc.cols(1,3,7);              // columns 1,3,7, as Object[] 
    rc.map();                    // returns a Map<String,Object>

Given this API it is easy to create a function which obtains a `ResultCursor` and returns a complex
value constructed from the values of the current row:

    public class City {
         public static City read(ResultCursor rc) {
             City city = new City();
             city.setCode(rc.col(1).getString());
             city.setName(rc.col(2).getString());
             ...
             return city;  
         }
         
         public void setName(String name) { ...
    }    

Now the builders returned by `Query.row()` and `.rows()` accept such a reader function and invoke it for the first row / all rows
to return a single object / a list of objects:

    City city       = q.row().read(City::read);    // read a single data object from first result row 	 
    List<City> city = q.rows().read(City::read);   // read a list of data objects from all rows 


#### Self-managed ResultCursor navigation 

If you want to navigate through a `ResultCursor` yourself you can obtain the cursor by calling
`Query.cursor()`. You should actively close the `ResultCursor` once it is no longer used
therefore it is best wrapped in a try-with-resources block:

     Query q = ...
     try (ResultCursor rc = q.cursor()) {
         // loop through result and read its rowss
     }

Given a `ResultCursor` it is easy to run through its rows in a forward only manner:

    while (q.nextRow()) {
        // read the result row
    }
     
If your cursor is scrollable you can ask for the position and freely move the current row,
by using the service objects returned by `ResultCursor.position()` and `.move()`:

	// configure a scroll sensitive cursor	
	StaticStmt stmt = ....
	stmt.options().setResultType(ResultType.SCROLL_SENSITIVE);
	
	// and run the query
	try (ResultCursor rc = stmt.query(sql).cursor()) {
	    // read position
	    boolean beforeFirst = qc.position().isBeforeFirst(); 
	    // also: .isAfterLast(), .isLast()  

	    // move current row
	    rc.move().first() 
	    rc.move().absolute(5) 
	    rc.move().relative(2)
	    // also: .relative(), .afterLast(), .beforeFirst(), .first(), .etc.
	}
  
#### Update a ResultCursor row
    
If your cursor is updatable, you can or update or delete the current row, or insert a new row:

    // configure the result to be updatable
    StaticStmt stmt = ....
    stmt.options().setResultConcurrency(Concurrency.CONCUR_UPDATABLE);
    ResultCursor rc = stmt.query(sql).cursor();
	
    // position row
    ... 
	
    rc.col("status").setString("ok"); 
    rc.row().update();
    rc.row().refresh();
    // also: .insert(), .isUpdated(), .delete(), .isDeleted(), etc.
     
     
### <a name="queries-resultset"></a>3.6. Converting from/to ResultSet  
      
You can still obtain the underlying `java.sql.ResultSet` of a query cursor if you need to:
 
    ResultSet resultSet = qc.resultSet();
    while (resultSet.next())
        ... 

If you have obtained a `java.sql.ResultSet` from somewhere else you can also turn it into a result cursor or query result using
the factory methods `Query.of(ResultSet)` or `ResultCursor.of(ResultSet)`:

    java.sql.ResultSet resultSet = ...
    List<String> names = Query.of(resultSet).rows().col("name").getString();


## <a name="updates"></a>4. Run DML or DDL updates

JDBX - as JDBC - uses the term *update* for running DML commands (i.e. UPDATE, INSERT, DELETE) or DDL commands. 

Running an update command returns the number of affected records and optionally the values of changed columns, e.g.
most important the auto-generated values of primary key columns.

### <a name="updates-run">4.1 Run the update

Updates are executed by either using a `StaticStmt` or a `PrepStmt`.

If you just want to run an update command and are not interested in returned column values then simply call
`StaticStmt.update(String)` or `PrepStmt.update()`:

   	String sql      = ... 
   	StaticStmt stmt = ...
	stmt.update(sql);
	
	// or: 
   	PrepStmt ptmt   = ... // pstmt is initialized
   	pstmt.update();
   	
Both calls return the update result as an `org.jdbx.UpdateResult` object. The method `UpdateResult.count()` 
gives the update count, i.e. the number of affected records:

	String sql    = "INSERT INTO ...";
	long inserted = stmt.update(sql).count();
	if (inserted != 1)
	    throw new IllegalStateException("insert failed");

Testing the update count can be shortened by calling `UpdateResult.requireCount`: 

	stmt.update(sql).requireCount(1); // throws a JdbxException if the update count is not 1
	

### <a name="updates-updateclass">4.2 Update class

If you want to retrieve returned columns values, e.g. auto-generated primary key values, or want to enable large update counts 
then call `StaticStmt.createUpdate(String)` and `PrepStmt.createUpdate()` which returns a `org.jdbx.Update` object.
The `Update` class provides a fluent API to first configure and then run the update to return a `UpdateResult`:
    
     Update u = stmt.createUpdate(sql);
     Update u = pstmt.init(sql).params("1", "2").createUpdate();
     
Because of its fluent API you will rarely need to store an `Update` object in a local variable but rather chain
method calls to retrieve the result. 


### <a name="updates-readcols">4.3 Read returned columns values

If you want to obtain returned column values, e.g. auto-generated primary key values then

   1. specify the columns which should be returned
   2. invoke an appropriate method on the `Update` object to read the returned column values and
      store them in the `UpdateResult`    

For `StaticStmt` steps 1) and 2) are done by configuring the `Update` object:
  
    StaticStmt stmt = ...
    UpdateResult<Integer> result = stmt.createUpdate("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe')")
        .returnAutoKeyCols()        // step 1: tell the Update to return the auto-generated key columns
        .runGetCol(Integer.class);  // step 2: run the update, extract the new inserted primary key column as Integer 
    long inserted = result.count();
    Integer newId = result.value();
    
The convenience method `UpdateResult.requireValue` tests if the column value stored in the result is not null and returns the value.
This allows to write:  
        
    Integer newId = stmt.createUpdate("INSERT INTO ...")
        .returnAutoKeyCols()
        .runGetCol(Integer.class)
        .requireCount(1)  // throws an Exception if update count != 1
        .requireValue();  // throws an Exception if returned col value is null

For step 1 there are alternative ways to specify the returned columns, for instance by using column numbers or names:

    stmt.createUpdate(sql).returnCols(1, 5, 7)...   
    stmt.createUpdate(sql).returnCols("id", "timestamp")...
    
For step 2 there are alternative ways to retrieve the returned column values. If multiple records are affected, and you want
to retrieve one column value from each row you can call `Update.runGetCols(Class<T>)` to retrieve the values as `java.util.List<T>`:

    List<Integer> newIds = stmt.createUpdate("INSERT INTO Names (name) VALUES ('Peter'), ('Paul')") 
        .returnAutoKeyCols()
        .runGetCols(Integer.class)
        .value();

For the general case `Update` has a `runGetCols` method  which accepts a reader function: The function then receives the result set
containing the returned values and must read and construct a final value which is then stored in the `UpdateResult`. 
   
For `PrepStmt` step 1) must be done during the initialization phase:

    PrepStmt pstmt = ...
    pstmt.init().returnAutoKeyCols().cmd("INSERT INTO Users VALUES (DEFAULT, ?, ?)")
    Integer newId = pstmt.params("John", "Doe").createUpdate()
        .runGetCol(Integer.class)
        .requireValue();
        
### <a name="updates-large">4.4 Return large update counts

JDBC reports the update count as `int` value. Since version 4.2 JDBC can also return large update counts as a `long` value.
JDBX always reports the update count as `long` but since not all JDBC drivers support large update counts you will
need to explicitly ask for large update counts - else the returned count will always be in the `int` range.
 
    long updated = stmt.createUpdate("UPDATE MegaTable SET timestamp = NOW()") 
        .enableLargeCount()  // configures the Update to retrieve large counts 
        .run()               // runs the Update and returns the UpdateResult
        .count();            // returns the update count
        

## <a name="execute"></a>5. Execute arbitrary SQL commands

JDBX - as JDBC - uses the term *execute* for running SQL commands with yet unknown result type - update or query - or which can return multiple
update and query results.

Given an initialized statement you can execute a command by calling `StaticStmt.execute(String sql)`, `PrepStmt.execute()` or `CallStmt.execute()`.
These methods return a `org.jdbx.ExecuteResult` which allows you to loop through the individual results, detect the result type
and evaluate the `UpdateResult` or `QueryResult`.  

    String sql = ...
    StaticStmt stmt = ...
    ExecuteResult result = stmt.execute(sql);
    while (result.next()) {
        if (result.isQuery())
            ... // process result.getQueryResult() 
        else
            ... // process result.getUpdateResult()
    }  	

## <a name="batches"></a>6. Run batches

All JDBX statement classes - like their counterparts in JDBC - allow to bundle SQL commands in batches to improve
roundtrip performance. Instead of directly incorporating batch related methods into the statement interface,
all JDBX statements know a `batch()` method which returns a `org.jdbc.Batch` object to add or clear commands and run the batch.

When the Batch is run it returns a `org.jdbx.BatchResult` which similar to `UpdateResult` allows you to access the update counts:

    StaticStmt stmt = ...
    stmt.batch()
        .add("INSERT INTO BatchDemo (name) VALUES ('A')")
        .add("INSERT INTO BatchDemo (name) VALUES ('B'), ('C'), ('D')")
        .run()               // returns a BatchResult
        .requireSize(2)      // check that the batch result has 2 entries
        .requireCount(0, 1)  // check the update count of result entry 0 
        .requireCount(1, 3); // check the update count of result entry 1
       
    PStmt pstmt = ... 
    pstmt.init("INSERT INTO BatchDemo (name) VALUES (?)")
    pstmt.params("A").batch().add();
    pstmt.params("B").batch().add()
        .run()               // returns a BatchResult
        .requireSize(2)      // check that the batch result has 2 entries
        .requireCount(0, 1;  // check the update count of result entry 0
        .requireCount(1, 1); // check the update count of result entry 1 
		

## <a name="exceptions"></a>7. Exceptions

JDBC reports database errors as **checked** exceptions using `java.sql.SQLException` and derived classes.

JDBX instead favors unchecked exceptions and introduces an own **unchecked** exception class
`org.jdbx.JdbxException` which can be thrown by its operations. Especially any `SQLException` thrown by
the underlying JDBC operations is wrapped into a `JdbxException` and reported as its exception cause.  

For easier exception handling a `JdbxExeption` contains an enum `JdbxExeption.Reason` to classify the exception
context. If based on a `SQLException` it also contains an enum `JdbxExeption.SqlExType` to classify the 
the `SQLException`.


## <a name="single-cmd"></a>8. Run a single command
        
If you only want to run a single SQL query or DML update you can use the convenience methods in class `org.jdbx.Jdbx` 
which handle creation and closing of a statement object. 

    Connection con = ...

    // run a static SELECT: creates a StaticStmt internally and returns the Query
    int cityCount = Jdbx.query(con, "SELECT COUNT(*) FROM Cities").row().col().getInt();

    // run a parameterized INSERT: creates a PrepStmt internally and returns the UpdateResult 
    Jdbx.update(con, "INSERT INTO Status (flag) VALUES (?)", "F").requireCount(1);
 
But if you need to run a couple of SQL commands it is more efficient to create a statement and reuse it.   


## <a name="multi-stmts"></a>9. Handling multiple statements

`org.jdbx.MultiStmt` is a utility class which creates and keeps track of multiple statements.
When the `MultiStmt` object is closed it automatically closes all created statements:

    Connection con = ...
    try (MultiStmt mstmt = new MultiStmt(con)) {
        StaticStmt s1 = mstmt.newStaticStmt();  // no need to close explicitly
        PrepStmt s2   = mstmt.newPrepStmt();    // no need to close explicitly
        ... // use s1 and s2
	} 


## <a name="more-examples"></a>10. More Examples

The package `org.jdbx.demo` contains more code snippets to demonstrate the JDBX API.

