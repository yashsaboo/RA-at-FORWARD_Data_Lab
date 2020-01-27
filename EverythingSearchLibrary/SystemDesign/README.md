# Everything Search Implementation

The system pipeline is composed of three major components:
1. Initialization - Data Input: this is first part of initializing the system where an admin populates the data into the system. 
2. Initialization - Indexing: in this step indexing is performed in order to efficient answer the query workload. Indices are created in this step.
3. Operation: this component deals with answering the query instances at run time after the indexing is done.

Figures detailing the design of the three components can be found in 'design' directory.

## Classes

Following are different classes that needs to be implemented:
1. DataStream: 
	* Member variables: basic column metadata (name, etc.), stream of tuple data
	* Functions: readnext() or something equivalent
2. Index:
	* Member variables: metadata (L, B, F, O from the write-up), statistics, file pointers
	* Functions: "create()" creates the index in disk. "read()" reads the data for a column from disk. "estimateSize()" estimates the size of an index.
3. Query: 
	* Member variables: metadata (L, B, F, X from the write-up) 
	* Functions: "parse()" parses the metadata from the string representation of query given by admin. "normalize()" adds implied conditions in F based on FDs and logical implication. "optimize()" returns an optimized query plan. "executeInstance()" executes an instance of the query for given parameters and query plan.
5. IndexMention:
	* Member variables: pointers to index and query instances, mapping between the labels of index and query
	* Functions: 
6. QueryPlan:
	* Member variables: query pointer, list of index mentions
	* Functions: "estimateCost()" estimates the cost of a query plan.
7. DiskManager:
	* Member variables: cached data blocks, cache size
	* Functions: "read()", "write()" and "delete()" reads writes and deletes data from the disk. "evict()" evicts some data block to free up some cache space based on LRU strategy. "maintain()" makes sure that a given data block is always in cache. This will be useful when say we want to keep term dictionary of inverted index always in memory. "fetch_cache_status()" returns a list of data blocks that are in cache.
8. Schema:
	* Member variables: Base relations, functional dependencies
	* Functions:
9. Main: this the main class that orchestrates everything
	* Member variables: schema, query workload, main indices
	* Functions: "populateData()", "indexData()" and "executeQuery()" implements the three pipelines Data Input, Indexing and Operation respectively which were described above. 

## Algorithms
Following are main algorithms that need to be implemented:
1. Index.create() : it first stores the input data stream in disk, sorts the data based on given column order and finally creates files for each column.
2. Main.indexData() : it first normalizes the query workload. Then it obtains candidate indices and index mentions. Next the search space of all solutions is explored using A*-search and Branch-and-bound heuristics. Finally indexes are created that are given by the algorithm.
3. Query.normalize() : normalization of query.
4. Query.executeInstance() : query execution engine that uses late materialization and handles cross products in a space efficient manner.
5. Query.optimize() : obtains a least cost query plan using better cost estimates at runtime.

## Timeline
The implementation is broken into the following parts:
1. Implement classes: Schema, DataStream, Index, DiskManager
2. Implement algorithms: Index.create(), Main.populateData()
3. Implement methods: Query.parse() and Query.normalize()
4. Implement classes: IndexMention, QueryPlan
5. Implement methods: Index.estimateSize(), QueryPlan.estimateCost()
6. Enumerate Candidate Indices and Index Mentions.
7. Implement main A*-search and BnB algorithms for state-space exploration.
8. Implement method: Query.executeInstance() (query execution engine)
9. Implement method: Query.optimize() (query optimizer that runs at runtime)
