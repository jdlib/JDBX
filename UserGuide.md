JDBX User Guide

1. [Intro](#intro)
2. [Statements](#stmts)
   * [2.1 Statement classes](#stmts-classes)
   * [2.2 Create and close statements](#stmts-create)
   * [2.3 Initialize statements](#stmts-init)
   * [2.4 Configure statements](#stmts-options)
   * [2.5 Setting parameters](#stmts-params)
3. [Running SQL queries](#queries)
   * [3.1 QueryResult class](#queries-queryresult)
   * [3.2 Read a single result row](#queries-singlerow)
   * [3.3 Read all result rows](#queries-allrows)
   * [3.4 Skip rows](#queries-skipping)
   * [3.5 QueryCursor class](#queries-querycursorclass)
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


## <a name="stmts"></a>1. Intro

JDBX offers an alternative way to execute SQL commands and read query or update results.
For that it replaces JDBC `Statement` and `ResultSet` classes with an own API.
Still the starting point of all its operations is a `java.sql.Connection` or `javax.sql.DataSource` object. 
 

## <a name="stmts"></a>2. Statements


### <a name="stmts-classes"></a>2.1 Statement classes

Statements are used to execute SQL commands. 
JDBX provides three alternative statement classes to replace the corresponding JDBC classes. 
(Implementation-wise the JDBX statements wrap their JDBC counterpart):

JDBC|JDBX|Used to 
----|----|-------
`java.sql.Statement`|`org.jdbx.StaticStmt`|execute static, non-parameterized SQL commands
`java.sql.PreparedStatement`|`org.jdbx.PrepStmt`|execute precompiled, parameterized SQL commands
`java.sql.CallableStatement`|`org.jdbx.CallStmt`|call stored procedures

JDBX - as JDBC - differentiates between

1. Running a SQL SELECT query, returning a result
2. Running a SQL update command (INSERT, UPDATE, DELETE or DDL command or other non SELECT command), returning an update count and updated column values
3. Running a SQL command whose result type is unknown or which can return multiple query and/or update results
4. Running SQL commands in a batch
5. Calling stored procedures, returning values of OUT parameters and/or query results.

`StaticStmt` and `PrepStmt` can be used running commands as described in (1-4), `CallStmt` can call stored procedures (5).


### <a name="stmts-create"></a>2.2 Create and close statements

In order to create a JDBX statement you need a `java.sql.Connection` or `javax.sql.DataSource`:

     Connection con   = ...
     DataSource ds    = ...
     StaticStmt stmt  = new StaticStmt(con); // or new StaticStmt(ds) 
     PrepStmt   pstmt = new PrepStmt(con);   // or new PrepStmt(ds)
     CallStmt   cstmt = new CallStmt(con);   // or new CallStmt(ds)
     
Statement objects should be actively **closed** once they are no longer used. Since all JDBX statement classes implement `java.io.AutoCloseable` 
the typical pattern is to create and use a statement object within a Java try-with-resources block:

     Connection con = ...
     try (StaticStmt stmt = new StaticStmt(con)) {
          ... // use the statement
     }   

Statements created from a `DataSource` will use a connection obtained from the `DataSource`. When the statement is closed that 
connection will also be closed automatically.

### <a name="stmts-init"></a>2.3 Initialize statements

You need to initialize `PrepStmt` and `CallStmt` by calling its `init(String)` method with a SQL command before you can execute the statement. 
The SQL command string uses `?` as placeholder for statement parameters:

    PrepStmt pstmt = new PrepStmt(con);
    pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
  
    CallStmt cstmt = new CallStmt(con);
    cstmt.init("{call getUserName(?, ?)}");
    
For more initialization options call the `init()` method on these statements which will return an initialization builder.
The builder allows you to define properties of the query result (for query commands) or update result (for update commands). 
The terminal call of the `.sql(String)` method to specify a SQL command is mandatory in order to complete the initialization:

    // instruct the statement to return the value of the 'id' column (see the chapter on running updates)
    pstmt.init().returnCols("id").sql("INSERT INTO Users VALUES (DEFAULT, ?, ?)");

    // instruct the statement to produce a scroll sensitive result cursor 
    cstmt.init().resultType(ResultType.SCROLL_SENSITIVE).sql("{call getUsers()}");
    
A `StaticStmt` is already initialized when it is created. (The SQL command which is executed by the `StaticStmt`
is not precompiled and passed to the statement when you run a query or update).
But still you might want to override default initialization settings: 

    StaticStmt stmt = new StaticStmt(con);
    stmt.init().resultType(ResultType.SCROLL_INSENSITIVE).resultHoldability(Holdability.HOLD_OVER_COMMIT);

Implementation-wise initialization of a JDBX statement is the equivalent of creating a JDBC statement
and the init-builder allows to specify the create parameters.
You may reinitialize a JDBX statement at any time which internally will recreate a new JDBC statement.    

### <a name="stmts-options"></a>2.4 Configure statements

Once initialized you can set or retrieve statement options by using the builder returned by its `options()` method of
the statement:      

    stmt.options().setQueryTimeoutSeconds(20).setFetchRows(5000);
    int timeoutSecs = stmt.options().getQueryTimeoutSeconds();
    
Once you reinitialize a JDBX statement all its options are reset to their defaults.    

### <a name="stmts-params"></a>2.5 Setting parameters

The SQL command string of a `PrepStmt`and `CallStmt` can (or should) contain parameters, specified as `?`: 
 
    PrepStmt pstmt = ...
    pstmt.init("INSERT INTO Rooms (id, name, area) VALUES (DEFAULT, ?, ?)");
    
Before running the command you need to provide parameter values. Parameters are referred to sequentially by number,
with the first parameter being 1:

    pstmt.param(1).setString("Living Room");     
    pstmt.param(3).setInt(51);
    
The builder returned by `param(int)` provides setters for various types. In most cases the JDBC driver
is able to recognize the type, so you can skip the explicit setter and simply pass parameters as objects: 
  
    pstmt.param(1, "Living Room").param(2, 51);
    
or even shorter if setting all parameter values:
    
    pstmt.params("Living Room", 51);
    
JDBX - unlike JDBC - also supports named parameters. When initializing call the `namedParams()` method of
the init-builder and specify parameters as a colon followed by the parameter name. A named parameter may occur
several times. To set a named parameter value call `param(String)` using the parameter name and then call the appropriate
setter:

    pstmt.init().namedParams()
        .sql("INSERT INTO Users (id, lastname, firstname, fullname) VALUES (DEFAULT, :lastname, :firstname, :lastname|| ', ' || :firstname)");
    pstmt.param("lastname").setString("John");     
    pstmt.param("firstname").setString("Doe");

Setting parameters on a `CallStmt` works exactly the same. Additionally you can register OUT and INOUT parameters, and 
read the values of OUT and INOUT parameters after the statement has been executed:

    Integer id = ... 
    cstmt.init("{call GetUserName(?,?,?)}");         // the SQL cmd has three parameters 
    cstmt.param(1).setInteger(id);                   // set the value of IN parameter 1
    cstmt.param(2).out(java.sql.Types.VARCHAR);      // register type of OUT parameter 2, maybe optional depending on the JDBC driver
    cstmt.param(3).out(java.sql.Types.VARCHAR);      // register type of OUT parameter 3, maybe optional depending on the JDBC driver
    cstmt_.execute();                                // execute the command, explained in next chapters
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


### <a name="queries-queryresult">3.1 QueryResult class

To execute a SQL query you need an [initialized](#stmts-init) JDBX statement. 

`StaticStmt.query(String)`, `PrepStmt.query()`, `CallStmt.query()` return a `org.jdbx.QueryResult` object
which provides a fluent API to extract values from the result:

     QueryResult qr = stmt.query("SELECT * FROM Cities WHERE id = 1");
     QueryResult qr = pstmt.init("SELECT * FROM Cities WHERE id = ?").params(1).query();
     
In the following variable `qr` represents a `QueryResult` object obtained from a call to a statements `query` method.     
But because of its fluent API you rarely will need to store a `QueryResult` in a local variable but rather chain
method calls until you receive the result value of the query.      

Also note that the actual JDBC query is usually not run until you invoke the terminal method of the fluent call chain.


### <a name="queries-singlerow">3.2 Read a single result row

Call `QueryResult.row()` to retrieve a builder to read values from the **first result row**:     
     
    qr.row()...     
    qr.row().col()...              // returns a builder to retrieve a value of the first column
    qr.row().col().getString();    // returns the value of the first column as String
    qr.row().col(3)...             // returns a builder to retrieve a value of the third column
    qr.row().col(3).getInteger();  // returns the value of the third column as Integer
    qr.row().col("sort")...        // returns a builder to retrieve a value of the "sort" column  
    qr.row().col("sort").getInt(); // returns the value of "sort" column as int
    qr.row().cols();               // returns the value of all columns, as Object[]
    qr.row().cols(1,3,7);          // returns the value of columns 1,3,7, as Object[] 
    qr.row().map();                // returns a Map<String,Object> mapping column name to value

If the result is empty, all the examples above will return a null value (or a default value for primitive terminals like `getInt()`).
If you want rule out this case use `QueryResult.row().required()`:

     // will throw an exception if the result contains no rows
     qr.row().required().col().getString()
     
You may also want to detect the case when the result contains more than one row, using `QueryResult.row().unique()`:
     
     // will throw an exception if the result contains more than one row
     qr.row().unique().col().getString()


### <a name="queries-allrows"></a>3.3 Read all result rows

Call `QueryResult.rows()` to retrieve a builder to read values from all rows and return as `java.util.List`:

    qr.rows()...
    qr.rows().col()...                  // return values of first column
    qr.rows().col().getString();        // return values of first column as List<String>
    qr.rows().col(3)...                 // return values of column by index
    qr.rows().col(3).getDouble();       // return values of third column, as List<Double>
    qr.rows().col("sort")...;           // return values of column by name 
    qr.rows().col("sort").getInteger(); // return values of "sort" column, as List<Integer>
    qr.rows().cols();                   // return values of all columns, as List<Object[]>
    qr.rows().cols(1,3,7);              // return values of columns 1,3,7, as List<Object[]> 
    qr.rows().map();                    // return a List<Map<String,Object>>
     
You may also limit the number of processed rows if this is not done within the SQL query itself:

    qr.rows(5)...
    

### <a name="queries-skipping"></a>3.4 Skip rows

Call `QueryResult.skip(int)` if you want to skip a number of rows before you extract values 
by calling `QueryResult.row()`, `.rows()` or `.rows(int)`:

    qr.skip(3).rows()...   // all rows after the first three rows


### <a name="queries-querycursorclass"></a>3.5 QueryCursor class

As shown above the `QueryResult` class makes it easy to extract a column value or an array of column values from a result row
using the various `col()` and `cols()` builder methods.

For more complicated cases JDBX provides the `org.jdbx.QueryCursor` class which replaces/wraps `java.sql.ResultSet` and allows
you to navigate thought the result rows and read values from each row.

When positioned on a result row, `QueryCursor` offers similar methods like the builder returned by `QueryResult.row()` to extract values 
from the row:

    QueryCursor qc = ...         // a QueryCursor, positioned on a result row
    qc.col()...                  // first column
    qc.col().getString();        // first column as String
    qc.col(3)...                 // column by index
    qc.col(3).getDouble();       // third column as double
    qc.col("sort")...;           // column by name 
    qc.col("sort").getInteger(); // "sort" column, as Integer
    qc.cols(1,3,7);              // columns 1,3,7, as Object[] 
    qc.map();                    // returns a Map<String,Object>

Given this API it is easy to create a function which obtains a `QueryCursor` and returns a complex
value constructed from the values of the current row:

    public class City {
         public static City read(QueryCursor qc) {
             City city = new City();
             city.setCode(qc.col(1).getString());
             city.setName(qc.col(2).getString());
             ...
             return city;  
         }
         
         public void setName(String name) { ...
    }    

Now the builders returned by `QueryResult.row()` and `.rows()` accept such a reader function and invoke it for the first row / all rows
to return a single object / a list of objects:

    City city       = qr.row().read(City::read);    // read a single data object from first result row 	 
    List<City> city = qr.rows().read(City::read);   // read a list of data objects from all rows 


#### Self-managed QueryCursor navigation 

If you want to navigate through a `QueryCursor` yourself you can obtain the cursor by calling
`QueryResult.cursor()`. You should actively close the `QueryCursor` once it is no longer used
therefore it is best wrapped in a try-with-resources block:

     QueryResult qr = ...
     try (QueryCursor qc = qr.cursor()) {
         // loop through result and read its rowss
     }

Given a `QueryCursor` it is easy to run through its rows in a forward only manner:

    while (qc.next()) {
        // read the result row
    }
     
If your cursor is scrollable you can ask for the position and freely move the current row,
by using the service objects returned by `QueryCursor.position()` and `.move()`:

	// configure a scroll sensitive cursor	
	StaticStmt stmt = ....
	stmt.options().setResultType(ResultType.SCROLL_SENSITIVE);
	
	// and run the query
	try (QueryCursor qc = stmt.query(sql).cursor()) {
	    // read position
	    boolean beforeLast = qc.position().isBeforeFirst() 
	    // also: .isAfterLast(), .isLast()  

	    // move current row
	    qc.move().first() 
	    qc.move().absolute(5) 
	    qc.move().relative(2)
	    // also: .relative(), .afterLast(), .beforeFirst(), .first(), .etc.
	}
  
#### Update a QueryCursor row
    
If your cursor is updatable, you can or update or delete the current row, or insert a new row:

    // configure the result to be updatable
    StaticStmt stmt = ....
    stmt.options().setResultConcurrency(Concurrency.CONCUR_UPDATABLE);
    QueryCursor qc = stmt.query(sql).cursor();
	
    // position row
    ... 
	
    qc.col("status").setString("ok"); 
    qc.row().update();
    qc.row().refresh();
    // also: .insert(), .isUpdated(), .delete(), .isDeleted(), etc.
     
     
### <a name="queries-resultset"></a>3.6. Converting from/to ResultSet  
      
You can still obtain the underlying `java.sql.ResultSet` of a query cursor if you need to:
 
    ResultSet resultSet = qc.resultSet();
    while (resultSet.next())
        ... 

If you have obtained a `java.sql.ResultSet` from somewhere else you can also turn it into a query cursor or query result using
the factory methods `QueryCursor.of(ResultSet)` and `QueryResult.of(ResultSet)`:

    java.sql.ResultSet resultSet = ...
    List<String> names = QueryResult.of(resultSet).rows().col("name").getString();


## <a name="updates"></a>4. Run DML or DDL updates

JDBX - as JDBC - uses the term *update* for running DML commands (i.e. UPDATE, INSERT, DELETE), DDL commands. 

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

	String sql   = "INSERT INTO ...";
	int inserted = stmt.update(sql).count();
	if (inserted != 1)
	    throw new IllegalStateException("insert failed");

Testing the update count can be shortened by calling `UpdateResult.requireCount`: 

	stmt.update(sql).requireCount(1); // throws an exception if the update count is not 1
	

### <a name="updates-updateclass">4.2 Update class

If you want to retrieve returned columns values, e.g. auto-generated primary key values, or want to enable large update counts 
(represented as `long` value), then call `StaticStmt.createUpdate(String)` and `PrepStmt.createUpdate()` which returns a `org.jdbx.Update` object.
The `Update` class provides a fluent API to first configure and then run the update to return a `UpdateResult`:
    
     Update u = stmt.createUpdate(sql);
     Update u = pstmt.init(sql).params("1", "2").createUpdate();
     
Because of its fluent API you will rarely need to store an `Update` object in a local variable but rather chain
method calls to retrieve the result. 


### <a name="updates-readcols">4.3 Read returned columns values

If you are interested to read returned column values, e.g. auto-generated primary key values, you need to

   1. specify the columns which should be returned
   2. invoke an appropriate method on the `Update` object to read the returned column values and
      store them in the `UpdateResult`    

For `StaticStmt` steps 1) and 2) are done by configuring the `Update` object:
  
    StaticStmt stmt = ...
    UpdateResult<Integer> result = stmt.createUpdate("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe')")
        .returnAutoKeyCols()        // step 1: tell the Update to return the auto-generated key columns
        .runGetCol(Integer.class);  // step 2: run the update, extract the new inserted primary key column as Integer 
    int inserted  = result.count();
    Integer newId = result.value();
    
The convenience method `UpdateResult.requireValue` tests if the column value stored in the result is not null and returns the value.
This allows to write:  
        
    Integer newId = stmt.createUpdate("INSERT INTO ...")
        .returnAutoKeyCols()
        .runGetCol(Integer.class)
        .requireCount(1)  // throws an Exception if update count != 1
        .requireValue();  // throws an Exception if returned col value is null

For step 1 there are alternative ways to specify the returned columns, for instance by using column indexes or names:

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

Since version 4.2 JDBC has an API for large update counts, represented as `long` values. Since not all
JDBC drivers support this feature JDBX gives optional access to large count values:       

    long updated = stmt.createUpdate("UPDATE MegaTable SET timestamp = NOW()") 
        .returnLargeCount()  // configures the Update to retrieve large counts 
        .run()               // runs the Update and returns the UpdateResult
        .largeCount();       // returns the update count as long   
        

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

When doing updates the update result may return column values from the update. If you want to obtain these column values,
TODO  

## <a name="batches"></a>6. Run batches

All JDBX statement classes - like their counterparts in JDBC - allow to group SQL commands in batches to improve
roundtrip performance. Instead of directly incorporating batch related methods into the statement interface,
all JDBX statements know a `batch()` method which returns a `org.jdbc.Batch` object to add or clear commands and run the batch.

When the Batch is run it returns a `org.jdbx.BatchResult` which similar to `UpdateResult` allows you to access the update counts:

    StaticStmt stmt = ...
    stmt.batch()
        .add("INSERT INTO BatchDemo (name) VALUES ('A')")
        .add("INSERT INTO BatchDemo (name) VALUES ('B'), ('C'), ('D')")
        .run()  // returns a BatchResult
        .requireSize(2)      // check that the batch result has 2 entries
        .requireCount(0, 1)  // check the update count of result entry 0 
        .requireCount(1, 3); // check the update count of result entry 1
       
    PStmt pstmt = ... 
    pstmt.init("INSERT INTO BatchDemo (name) VALUES (?)");
    pstmt.params("A").batch().add();
    pstmt.params("B").batch().add()
        .run()  // returns a BatchResult
        .requireSize(2)      // check that the batch result has 2 entries
        .requireCount(0, 1;  // check the update count of result entry 0
        .requireCount(1, 1); // check the update count of result entry 1 
		

## <a name="exceptions"></a>7. Exceptions

JDBC reports database errors as **checked** exceptions using `java.sql.SQLException` and derived classes.

JDBX has a fundamental stance to favor unchecked exceptions and therefore introduces an own **unchecked** exception class
`org.jdbx.JdbxException` which can be thrown by its operations. Especially any `SQLException` thrown by
the underlying JDBC operations is wrapped into a `JdbxException` and reported as its exception cause.  

For easier exception handling a `JdbxExeption` contains an enum `JdbxExeption.Reason` to classify the exception
context. If based on a `SQLException` it also contains an enum `JdbxExeption.SqlExType` to classify the 
the `SQLException`.

## <a name="single-cmd"></a>8. Run a single command
        
If you only want to run a single SQL query or DML update you can use the convenience methods in class `org.jdbx.Jdbx` 
which handle creation and closing of a statement object. 

    Connection con = ...

    // run a static SELECT: creates a StaticStmt internally and returns the QueryResult
    int cityCount = Jdbx.query(con, "SELECT COUNT(*) FROM Cities").row().col().getInt();

    // run a parameterized INSERT: creates a PrepStmt internally and returns the UpdateResult 
    Jdbx.update(con, "INSERT INTO Status (flag) VALUES (?)", "F").requireCount(1);
 
But if you are running a couple of SQL commands it is more efficient to create a statement and reuse it.   
 

TODO
org.jdbx.demo package
resultiterator
querycursor config: setfetchdirection
multistmt?
