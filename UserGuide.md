JDBX User Guide

1. [Intro](#intro)
2. [Statements](#stmts)
   * [Creating and closing statements](#stmts-creating)
   * [StaticStmt](#stmts-static)
   * [PrepStmt](#stmts-prep)
   * [CallStmt](#stmts-call)
3. [Running SQL queries](#queries)
   * [Query class](#queries-query)
   * [Reading a single result row](#queries-singlerow)
   * [Reading all result rows](#queries-allrows)
   * [Skipping rows](#queries-skipping)
   * [QueryCursor class](#queries-querycursorclass)
4. [Running DML or DDL updates](#updates)
   * [Run the update](#updates-run)
   * [Update class](#updates-updateclass)
   * [Read returned columns values](#updates-readcols)
   * [Returning large update counts](#updates-large)
5. [Executing arbitrary SQL commands](#execute)
6. [Running a single command](#single-cmd)


## <a name="stmts"></a>1. Intro

JDBX offers an alternative way to execute SQL or DDL commands and read query or update results.
For that it wraps JDBC statement and result-set classes with an own API.
Still the starting point of all its operations is a `java.sql.Connection` or `javax.sql.DataSource` object. 
 

## <a name="stmts"></a>2. Statements

JDBX provides three alternative statement classes to replace the corresponding JDBC classes:

JDBC|JDBX|Used to 
----|----|-------
`java.sql.Statement`|`org.jdbx.StaticStmt`|execute static, non-parameterized SQL commands
`java.sql.PreparedStatement`|`org.jdbx.PrepStmt`|execute precompiled, parameterized SQL commands
`java.sql.CallableStatement`|`org.jdbx.CallStmt`|call stored procedures

JDBX - as JDBC - differentiates between

1. Running SQL queries, returning results
2. Running SQL UPDATE, DELETE, INSERT commands or DDL commands, returning update counts and automatically generated keys
3. Running SQL commands which can return multiple results
4. Running SQL commands in a batch
5. Calling stored procedures

`StaticStmt` and `PrepStmt` can run SQL or DDL commands (1-4), `CallStmt` can call stored procedures (5).


### <a name="stmts-creating"></a>Creating and closing statements

In order to create a JDBX statement you need a `java.sql.Connection` or `javax.sql.DataSource`:

     Connection con   = ...
     DataSource ds    = ...
     StaticStmt stmt  = new StaticStmt(con); // or new StaticStmt(ds) 
     PrepStmt   pstmt = new PrepStmt(con);   // or new PrepStmt(ds)
     CallStmt   cstmt = new CallStmt(con);   // or new CallStmt(ds)
      
Statement objects should be actively closed once they are no longer used. Since all statement classes implement `java.io.AutoCloseable` 
the typical pattern is to create and use a statement object within a Java try-with-resources block:

     Connection con = ...
     try (StaticStmt stmt = new StaticStmt(con)) {
          ... // use the statement
     }   

Statements created from a `DataSource` will use a connection obtained from the `DataSource`. When the statement is closed the 
connection will also be closed automatically.


### <a name="stmts-static"></a>StaticStmt

`org.jdbx.StaticStmt` can execute static (non-parameterized) SQL commands. Example:

    StaticStmt stmt = ...
    int count = stmt.update("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe')");
    
To configure a `StaticStmt` use the builder returned by its `init()` method.      

    stmt.init()
        .resultType(ResultType.SCROLL_SENSITIVE)
        .resultConcurrency(ResultConcurrency.READ_ONLY);


### <a name="stmts-prep"></a>PrepStmt

`org.jdbx.PrepStmt` can execute precompiled, parameterized SQL commands. After it is initialized it can
be executed multiple times using different parameter values. 
Contrary to `java.sql.PreparedStatement` you can also re-initialize the command. Example:

    PrepStmt pstmt = ...
    pstmt.init("INSERT INTO Users VALUES (DEFAULT, ?, ?)");
    pstmt.params("John", "Doe").update();
    pstmt.params("Mary", "Jane").update();
    pstmt.init("UPDATE Users SET name = ? WHERE id = ?");
    ...
     
    
Like in `StaticStmt` you can use the builder returned by the `init()` method to configure the `PrepStmt`.
      
    pstmt.init()
        .resultType(ResultType.SCROLL_INSENSITIVE)
        .resultHoldability(ResultHoldability.HOLD_OVER_COMMIT)
        .cmd("SELECT * FROM Cities WHERE name LIKE ?");
     

### <a name="stmts-call"></a>CallStmt

`org.jdbx.CallStmt` can call stored procedures. After it is initialized it can
be executed multiple times using different parameter values. Example:

    CallStmt cstmt = ...
    cstmt.init("{call getUserName(?, ?)");
    cstmt.param(1).setInt(12045);
    cstmt.param(2).out(Types.VARCHAR);
	cstmt.execute();
	String userName = cstmt.param(2).getString();


## <a name="queries"></a>3. Running SQL queries

In JDBC executing a query returns a `java.sql.ResultSet`. Given the `ResultSet` you can loop over its rows and extract 
values from the rows in appropriate form.

JDBX uses the builder pattern and functional programming to avoid most of the boilerplate code needed in JDBC.

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
     
**Example 2:** Extract a single value from a result set which contains 0 or 1 rows

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


### <a name="queries-queryclass">Query class

In order to execute a SQL query you need a `StaticStmt` or `PrepStmt`. 

`StaticStmt.query(String)` and `PrepStmt.query()` return a `org.jdbx.Query` object
which provides a builder API to run the query and extract values from the result:

     Query q = stmt.query(sql);
     Query q = pstmt.init(sql).params("a", "b").query();
     
If you have obtained a `java.sql.ResultSet` from somewhere else you can also turn it into a `Query` object for easy value extraction:

    java.sql.ResultSet resultSet = ...
    List<String> names = Query.of(resultSet).rows().col("name").getString();
     
In the following variable `q` represents a `Query` object obtained from a `StaticStmt` or `PrepStmt`.     
But because of its builder API you rarely will need to store a `Query` object in a local variable but rather chain
method calls until you receive the result of the query.      

Also note that the actual JDBC query is usually not run until you invoke the terminal method of the builder chain.


### <a name="queries-singlerow">Reading a single result row

Call `Query.row()` to retrieve a builder to read values from the first result row:     
     
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
If you want rule out this case use `Query.row().required()`:

     // will throw an exception if the result contains no rows
     q.row().required().col().getString()
     
You may also want detect the case when the result contains more than one row, using `Query.row().unique()`:
     
     // will throw an exception if the result contains more than one row
     q.row().unique().col().getString()


### <a name="queries-allrows"></a>Reading all result rows

Call `Query.rows()` to retrieve a builder to read values from all rows and return as `java.util.List`:

    q.rows()...
    q.rows().col()...                  // return values of first column
    q.rows().col().getString();        // return values of first column as List<String>
    q.rows().col(3)...                 // return values of column by index
    q.rows().col(3).getDouble();       // return values of third column, as List<Double>
    q.rows().col("sort")...;           // return values of column by name 
    q.rows().col("sort").getInteger(); // return values of "sort" column, as List<Integer>
    q.rows().cols();                   // return values of all columns, as List<Object[]>
    q.rows().cols(1,3,7);              // return values of columns 1,3,7, as List<Object[]> 
    q.rows().map();                    // return a List<Map<String,Object>>
     
You may also limit the number of processed rows if this is not done within the SQL query itself:

    q.rows(5)...
    

### <a name="queries-skipping"></a>Skipping rows

Call `Query.skip(int)` if you want to skip a number of rows before you extract values 
by calling `Query.row()`, `.rows()` or `.rows(int)`:

    q.skip(3).rows()...   // all rows after the first three rows


### <a name="queries-querycursorclass"></a>QueryCursor class

As shown above the `Query` class makes it easy to extract a column value or an array of column values from a result row
using the various `col()` and `cols()` builder methods.

For more complicated cases JDBX provides the `QueryCursor` class which replaces/wraps `java.sql.ResultSet` and allows
you to navigate thought the result rows and read values from each row.

When positioned on a result row, `QueryCursor` offers similar methods like the builder returned by `Query.row()` to extract values 
from the row:

    QueryCursor qc = ...         // a QueryCursor, positioned on a result row (as explained later)
    qc.col()...                  // first column
    qc.col().getString();        // first column as String
    qc.col(3)...                 // column by index
    qc.col(3).getDouble();       // third column as double
    qc.col("sort")...;           // column by name 
    qc.col("sort").getInteger(); // "sort" column, as Integer
    qc.cols(1,3,7);              // columns 1,3,7, as Object[] 
    qc.map();                    // returns a Map<String,Object>

Given this API it is easy to create a function which obtains a `QueryCursor` and returns a complex
value constructed from row values:

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

Now the builders returned by `Query.row()` and `.rows()` accept such a reader function and invoke it for the first row / all rows
to return a single object / a list of objects:

    City city       = q.row().read(City::read); 	 
    List<City> city = q.rows().read(City::read); 	 


#### Self-managed QueryCursor navigation 

If you want to navigate through a `QueryCursor` yourself you can obtain the cursor by calling
`Query.cursor()`. You should actively close the `QueryCursor` once it is no longer used
therefore it is best wrapped in a try-with-resources block:

     Query q = ...
     try (QueryCursor qc = q.cursor()) {
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
	stmt.init().resultType(ResultType.SCROLL_SENSITIVE);
	
	// and run the query
	try (QueryCursor qc = stmt.query(sql).cursor()) {
	    // read position
	    qc.position().isBeforeFirst() 
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
	stmt.init().resultConcurrency(ResultConcurrency.CONCUR_UPDATABLE);
	QueryCursor qc = stmt.query(sql).cursor();
	
	// position row
	... 
	
   	qc.col("status").setString("ok"); 
    qc.row().update();
    qc.row().refresh();
    // also: .insert(), .isUpdated(), .delete(), .isDeleted(), etc.
     
     
#### Accessing the ResultSet  
      
You still can obtain the `java.sql.ResultSet` of a query cursor if you want to process it by yourself:
 
    ResultSet resultSet = qc.resultSet();
    while (resultSet.next())
        ... 
    
## <a name="updates"></a>4. Running DML or DDL updates

JDBX - as JDBC - uses the term *update* for running DML commands (i.e. UPDATE, INSERT, DELETE), DDL commands and all SQL commands which
return nothing. 

Running an update command returns the number of affected records and optionally the values of changed columns, e.g.
most important the auto-generated values of primary key columns.

### <a name="updates-run">Run the update

Updates are executed by either using a `StaticStmt` or a `PrepStmt`.

If you just want to run an update command and are not interested in returned column values then call
`StaticStmt.update(String)` or `PrepStmt.update()`:

   	String sql      = ... 
   	StaticStmt stmt = ...
	stmt.update(sql);
	
	// or: 
   	PrepStmt ptmt   = ...
   	pstmt.update();
   	
Both calls return the update result as an `org.jdbx.UpdateResult` object. The method `UpdateResult.count()` 
gives the update count, i.e. the number of affected records:

	String sql   = "INSERT INTO ...";
	int inserted = stmt.update(sql).count();
	if (inserted != 1)
	    throw new IllegalStateException("insert failed");

Testing the update count can be shortened by calling `UpdateResult.requireCount`: 

	stmt.update(sql).requireCount(1); // throws an exception if the update count is not 1
	

### <a name="updates-updateclass">Update class

If you want to retrieve returned columns values, e.g. auto-generated primary key values, or want to enable large (`long`) update counts, 
then call `StaticStmt.createUpdate(String)` and `PrepStmt.createUpdate()` which returns a `org.jdbx.Update` object.
The `Update` class provides a builder API to configure and then run the update to return a `UpdateResult`:
    
     Update u = stmt.createUpdate(sql);
     Update u = pstmt.init(sql).params("1", "2").createUpdate();
     
Because of its builder API you will rarely need to store an `Update` object in a local variable but rather chain
method calls to retrieve the result. 


### <a name="updates-readcols">Read returned columns values

If you are interested to read returned column values, e.g. auto-generated primary key values, you need to

   1. specify the columns which should be returned
   2. invoke an appropriate method on the `Update` object to read the returned column values and
      store them in the `UpdateResult`    

For `StaticStmt` steps 1) and 2) are done by configuring the `Update` object:
  
    StaticStmt stmt = ...
    UpdateResult<Integer> result = stmt.createUpdate("INSERT INTO Users VALUES (DEFAULT, 'John', 'Doe'")
        .returnAutoKeyCols()        // step 1: tell the Update to return the auto-generated key value
        .runGetCol(Integer.class);  // step 2: run the update, extract the new inserted primary key value as Integer 
    int inserted  = result.count();
    Integer newId = result.value();
    
The convenience method `UpdateResult.requireValue` tests if the column value stored in the result is not null and returns the value.
This allows to write:  
        
    Integer newId = stmt.createUpdate("INSERT INTO ...")
        .returnAutoKeyCols()
        .runGetCol(Integer.class)
        .requireCount(1)  // could throw an Exception
        .requireValue();  // could throw an Exception

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
    Integer newId  = pstmt.params("John", "Doe").createUpdate()
        .runGetCol(Integer.class)
        .requireValue();
        
### <a name="updates-large">Returning large update counts

Since version 4.2 JDBC has an API for large update counts, represented as `long` values. Since not all
JDBC drivers support this feature JDBX gives optional access to large count values:       

    long updated = stmt.createUpdate("UPDATE MegaTable SET timestamp = NOW()") 
        .returnLargeCount()  // configures the Update 
        .run()               // runs the Update and returns the UpdateResult
        .largeCount();       // returns the update count as long   
        

## <a name="execute"></a>5. Executing arbitrary SQL commands

JDBX - as JDBC - uses the term *execute* for running SQL commands with yet unknown result type - update or query - or which can return multiple
update and query results.

You can execute a command by calling `StaticStmt.execute(String sql)` or `PrepStmt.execute()` on a initialized `PrepStmt`.
Both methods return a `org.jdbx.ExecuteResult` which allows you to loop through the individual results, detect if they are
update or query results and evalute the `UpdateResult` or `QueryResult`.  

	String sql = ...
	StaticStmt stmt = ...
	ExecuteResult result = stmt.execute(sql);
	while (result.next()) {
		if (result.isQuery())
			// process result.getQueryResult() 
		else
			// process result.getUpdateResult()
	}  	

## <a name="single-cmd"></a>6. Running a single command
        
If you only want to run a single SQL query or DML update you can use the static helper methods in class `org.jdbx.JDBX` 
to avoid explicit creation and closing of a `StaticStmt` or `PrepStmt` object:
TODO 
 

TODO
org.jdbx.demo package
exceptions
resultiterator
querycursor config: setfetchdirection
batch
multistmt
named params