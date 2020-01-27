# Everything Search Library
Designing entity-aware web indexing systems to search by and for keywords, entities and documents.

## File Details

Report, System Design and Sample Codebase is included in this folder. 

Report outlines the basic work I did during my research assistantship. 
System Design is included in /SystemDesign folder.
The inner details of the complete project is purposefully not given public access, since the paper is yet to be published, and codebase is still under development.

Codebase includes a Java Project with multiple files in ./src folder. This project was built using Eclipse Oxygen with no external libraries requirement. It can be used for following puposes:

1. Convert CSV to Binary index representation of the Everything Search system
```
FileName:	.src/dataImporter/CSVImporter.java
Input:		CSV
  		Here, 6 columns (where first three are stored as column store and rest of them as row store)

Output: 	Binary Files:- ColStore(i).bin where i ranges from 0 to noOfColumnStores and RowStore.bin
  		Here,   ColStore(1).bin
        	ColStore(2).bin
        	ColStore(3).bin
        	RowStore.bin
        
Attributes:	filePath = "src/"
	      	noOfColumnStore = 3
	      	columnDataType = ('s', 's', 's', 'i', 'i', 'i')

Methods:
		setFilePath(String filePath): void
			Sets the file Path of the CSV for which indexing is to be done
		addHeader(String filePathExtended, String storeType): void
			Adds header to the file created
		getCSVTuples(Scanner scannerObject): String[]
			Gets the tuple wherever the pointer is currently located in CSV
		getColumnNamesFromCSVFile(Scanner scannerObject): List<String>
			Extracts the column names of the CSV file
		initialiseValues(): void
			Initialises the variables which will be used to perform CSV to Binary such as:
			int[] countValues
			  Keeps the count of frequency of certain cell item. Length of the array = no of columns
			ArrayList<Object> prevValues;
			  Stores the previously seen tuple
			ArrayList<Object> currentValues;
			  Stores the current tuples (where the pointer is pointing at)
			ArrayList<ArrayList<Object>> rowStore;
			  Stores the sequential data for populating the row store
			int[] offset;
			  Stores the offset of each element for column store for mapping purpose
			int[] prevOffset;
			  Stores the offset of the previously loaded element. This is used since we populate right to left and not vice versa.
		flushColumnStore(int i): void
			Flushes the column (i) data into the binary file based for the i'th column
		flushRowStore(): void
			Flushes the row data into the binary file
		assembleBlocks(String fileName): void
			Streamlines all the above function to make the CSV to Binary conversion possible
```

2. Create relation which will be used for SPJexecution algorithm execution
```
FileName: 	.src/relation/Relation.java
Input:		Binary File of the table
		Here, ColumnStore(0).bin file will be given as an input
Attributes:	String filePathExtended
		int noOfColumnStores
		boolean columnStore
		int headerBytes
		ArrayList<Character> columnDataType

Methods:
		void next(DataInputStream dis)
			Goes to the Next Value of the first column
		ArrayList<Object> currentPointerCountValueAndOffset(DataInputStream dis)
			Value, Count and Offset of first column where the cursor is currently present
		ArrayList<ArrayList<Object>> distinctValuesOfFirstColumn()
			Get the distinct values of the first columns
		int locationOfTuples(Object val)
			Location of the tuples where 1st column value = val
		void jump(Object val, DataInputStream dis)
			Jumps to the location of the file where 1st column value = val
		boolean areThereAnyMoreTuples(DataInputStream dis)
			Check if any more tuples are present or not
		void resetPointer(DataInputStream dis)
			Resets the pointer to the start of the file
		Relation subRelation()
			Performs sub-relation
```

3. Query class for parsing query
```
FileName: 	.src/query/Query.java
Input:		SPJ type query command, where S is Selection, P is Projection and J is Join

Methods:
		void queryParser(String query)
			Parses the query into Everything Search parameter definition: {Labels:L, BaseRelationMapping:B, SelectionProjectionConditions:F,
ProjectedColumns:X;} To know more, please read Everything Search paper which will be published soon.
```


## Requirements to run the code
Java Runtime Environment (JRE)

### Contributor: [Yash Saboo](https://github.com/yashsaboo) as a Member of [FORWARD Data Lab](http://www.forwarddatalab.org/)
