package dataImporter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import dataImporter.binaryFileCreator.BinaryFileCreator;
import relation.Relation;

public class CSVImporter extends BinaryFileCreator
{
	
	String filePath;
	int noOfColumns;
	int noOfColumnStore;
	ArrayList<Character> columnDataType = new ArrayList<Character>();
	
	private int[] countValues;
	private ArrayList<Object> prevValues;
	private ArrayList<Object> currentValues;
	private ArrayList<ArrayList<Object>> rowStore;
	private int[] offset;
	private int[] prevOffset;
	
	//Constructor
	public CSVImporter(String filePath, int noOfColumnStore, ArrayList<Character> columnDataType) {
		super();
		this.filePath = filePath;
		this.noOfColumnStore = noOfColumnStore;
		this.columnDataType = columnDataType;
		this.noOfColumns = this.columnDataType.size();
		
		this.countValues = new int[noOfColumns];
		this.prevValues = new ArrayList<Object>();
		this.currentValues = new ArrayList<Object>();
		this.rowStore = new ArrayList<ArrayList<Object>>();
		this.offset = new int[noOfColumnStore];
		this.prevOffset = new int[noOfColumnStore-1];
	}
	
	//Sets the file Path of the CSV for which indexing is to be done
	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}
	
	//Adds Header for the file
	public void addHeader(String filePathExtended, String storeType) throws IOException
	{
//		ArrayList<Object> header = new ArrayList<Object>();
//		writeIntoFile(header, filePathExtended);
	}
	
	//Gets Scanner Object
	public Scanner getScannerObject(String filePathExtended) throws Exception
	{
		FileInputStream inputStream = null;
		Scanner scannerObject = null;
		try {
		    inputStream = new FileInputStream(filePathExtended);
		    scannerObject = new Scanner(inputStream, "UTF-8");
		    // note that Scanner suppresses exceptions
		    if (scannerObject.ioException() != null) {
		        throw scannerObject.ioException();
		    }
		}catch (Exception e) {
			e.printStackTrace();
		}
		return scannerObject;
	}
	
	//Extracted tuples from CSV are in string format. Here, it get's converted to respective data type
	public ArrayList<Object> convertStringTuplesToRespectiveDataType(String[] attributes) {
		ArrayList<Object> list = new ArrayList<Object>();
		if(attributes!=null && attributes.length==this.noOfColumns)
		{
//			System.out.println("convertStringTuplesToRespectiveDataType attributes:"+attributes);
			for(int i=0; i<this.noOfColumns;i++)
			{
				if(this.columnDataType.get(i).equals('s'))
				{
					list.add(attributes[i]);
				}
				else
				{
					list.add(Integer.parseInt(attributes[i].trim()));
				}
			}
		}
		return list;
	}
	
	//Returns single row of CSV, that is single tuple
	public String[] getCSVTuples(Scanner scannerObject)
	{
		if (scannerObject.hasNextLine()) {
	        String line = scannerObject.nextLine();
	        String[] attributes = line.split(","); // use string.split to load a string array with the values from each line of the file, using a comma as the delimiter
	    	attributes = removeQuotesFromAtrributes(attributes);
	    	return attributes;
	    }
	    return null;
	}
	
	//Gets next block of data from CSV
	public ArrayList<Object> getNextBlockOfData(Scanner scannerObject)
	{
	    if (scannerObject.hasNextLine()) {
	    	return convertStringTuplesToRespectiveDataType(getCSVTuples(scannerObject));
	    }
	    return null;
	}
	
	//Extract column names from CSV file
	public List<String> getColumnNamesFromCSVFile(Scanner scannerObject)
	{
	    if (scannerObject.hasNextLine()) {
	    	return Arrays.asList(getCSVTuples(scannerObject));
	    }
	    return null;
	}
	
	//Get absolute path
	public String getFilePathExtended(String filePath, String fileName)
	{
		if(filePath.trim().endsWith("/") && fileName.startsWith("/"))
			return filePath.trim().substring(0, filePath.length()-1) + fileName.trim();
		else if(filePath.trim().endsWith("/") || fileName.startsWith("/"))
			return filePath.trim() + fileName.trim();
		else
			return filePath + "/" + fileName;
	}
	
	//Initialise values to get the importor action upgraded
	public void initialiseValues() throws IOException
	{
		for(int i=0; i<this.noOfColumnStore; i++)
		{
			createBinaryFile(this.filePath + "/ColumnStore("+i+").bin");
			addHeader(this.filePath + "/ColumnStore("+i+").bin", "ColumnStore");
		}
		
		for(int i=this.noOfColumnStore;i<this.noOfColumns;i++)
		{
			this.rowStore.add(new ArrayList<Object>());
		}
		
		createBinaryFile(this.filePath + "/RowStore.bin");
		addHeader(this.filePath + "/RowStore.bin", "RowStore");
	}
	
	//Increases the frequency counter by 1
	public void increaseCountValueBy1(int tillWhichIndexNumber)
	{
		for(int i=0; i<=tillWhichIndexNumber; i++)
		{
			this.countValues[i]++;
		}
	}
	
	//Compares previous tuple and current tuple to find where the change occurs
	public int findTheIndexWhereTheFirstChangeHappenedAndIncrementTheCountTillThat()
	{
//		displayArrayList(prevValues);
//		displayArrayList(currentValues);
		for(int i=0; i<this.noOfColumns; i++)
		{
//			System.out.println("findTheIndexWhereTheFirstChangeHappenedAndIncrementTheCountTillThat this.columnDataType.get(i):"+this.columnDataType.get(i));
			if(this.columnDataType.get(i).equals('s') && !this.prevValues.get(i).equals(this.currentValues.get(i)))
				return i;
			if(this.columnDataType.get(i).equals('i') && !this.prevValues.get(i).equals(this.currentValues.get(i)))
				return i;
			this.countValues[i]++;
		}
		return -1;
	}
	
	//Write the column store values into the file
	private int flushColumnStore(int i) throws IOException {
		if(!this.prevValues.isEmpty())
		{
			ArrayList<Object> dataToBeWritten = new ArrayList<Object>();
			int blockSize = 0;

			dataToBeWritten.add(this.prevValues.get(i)); blockSize += (this.columnDataType.get(i).equals('s')) ? (""+this.prevValues.get(i)).length():intInBytes;
			dataToBeWritten.add(this.countValues[i]); blockSize += intInBytes;
			
			System.out.println("flushColumnStore i:"+i);
			System.out.println("flushColumnStore this.offset[i]:"+this.offset[i]);
			System.out.println("flushColumnStore offset:"+Arrays.toString(this.offset));
			
			if(i!=this.noOfColumnStore-1)//Did this because the last column gets updated before second last column, thus we get updated offset value.
			{
				dataToBeWritten.add(this.prevOffset[i]); blockSize += intInBytes;
				this.prevOffset[i] = this.offset[i];
			}
			else
			{
				dataToBeWritten.add(this.offset[i]); blockSize += intInBytes;
			}
			
			writeIntoFile(dataToBeWritten, this.filePath + "/ColumnStore("+i+").bin", true);
			if(this.columnDataType.get(i).equals('s'))
				blockSize += suffixInBytes;

			return blockSize;
		}
		return 0;
	}
	
	//Updates the row store block
	private void updateRowStoreBlock() {
		int j = 0;
		int noOfColumnsForRowStore = this.noOfColumns-this.noOfColumnStore;
		System.out.println("updateRowStoreBlock noOfColumnsForRowStore:"+noOfColumnsForRowStore);
		
		for(int i=this.noOfColumnStore; i<this.noOfColumns;i++)//Increments the count till which the values are same from left to right column order
		{
			if(this.rowStore.get(j).get(this.rowStore.get(j).size()-2).equals(this.currentValues.get(i)))
			{
				this.rowStore.get(j).set(this.rowStore.get(j).size()-1, (int)this.rowStore.get(j).get(this.rowStore.get(j).size()-1)+1);
				j++;
			}
			else
				break;
		}
		System.out.println("updateRowStoreBlock j:"+j);
		for(int i=noOfColumnsForRowStore-1; i>j;i--)
		{
			System.out.println("updateRowStoreBlock i:"+i);
			this.rowStore.get(i-1).addAll(this.rowStore.get(i));
			if(i==noOfColumnsForRowStore-1)
				this.rowStore.get(i-1).add(";");
			this.rowStore.get(i).clear();
		}
		System.out.print("updateRowStoreBlock() ");
		displayArrayList(rowStore);
		for(int i=j;i<noOfColumnsForRowStore;i++)
		{
			this.rowStore.get(i).add(this.currentValues.get(noOfColumnStore+i));
			this.rowStore.get(i).add(1);
		}
		System.out.print("updateRowStoreBlock() ");
		displayArrayList(rowStore);
	}
	
	//Write the row store values into the file
	private int flushRowStore() throws IOException {
		
		int noOfColumnsForRowStore = this.noOfColumns-this.noOfColumnStore;
		int blockSize = 0;
		for(int i=noOfColumnsForRowStore-1; i>0; i--)
		{
			if(!this.rowStore.get(i).isEmpty())
			{
				this.rowStore.get(i-1).addAll(this.rowStore.get(i));
				if(i==noOfColumnsForRowStore-1)
					this.rowStore.get(i-1).add(";");
			}
		}
		writeIntoFile(this.rowStore.get(0), this.filePath + "/RowStore.bin", false);
		for(int i=0; i<this.rowStore.get(0).size();i++)
		{
			blockSize += (this.rowStore.get(0).get(i).getClass().getName().equals("java.lang.Integer")) ? (""+this.rowStore.get(0).get(i)).length():intInBytes;
			blockSize += suffixInBytes;
		}
		return blockSize;
	}
	
	/**
	 * @param fileName
	 * @throws Exception
	 * 1. Get the absolute filepath of the flat file
	 * 2. Initialize:
	 * 		Scanner Object
	 * 		Column names
	 * 		Get the first row and initialize it to previous columns
	 * 		CountValues, that is frequency with 1 for each column
	 * 		Add the values of row store column directly into row store
	 * 3. Initialise the next block of data to current value
	 * 4. Iterate while there are more tuples
	 * 		Find where the next change happens in the tuple compared to previous one and based on that flush columnstores and row stores
	 * 5. Flush the remaining row stores and column stores
	 */
	public void assembleBlocks(String fileName) throws Exception {
		
		String filePathExtended = getFilePathExtended(this.filePath, fileName);
//			System.out.println("assembleBlocks filePathExtended:"+filePathExtended);
		Scanner scannerObject = getScannerObject(filePathExtended);
		List<String> columnNames = getColumnNamesFromCSVFile(scannerObject);
		displayArrayList(columnNames);
		this.prevValues = getNextBlockOfData(scannerObject);
//			displayArrayList(prevValues);
//			System.out.println("assembleBlocks prevValues:"+prevValues);
		
		if(this.prevValues!=null)
		{
			System.out.println("assembleBlocks Inside first If");
			initialiseValues();
			increaseCountValueBy1(this.noOfColumns-1);
			for(int i=this.noOfColumnStore, j=0; i<this.noOfColumns;i++)
			{
				this.rowStore.get(j).add(this.prevValues.get(i));
				this.rowStore.get(j++).add(this.countValues[i]);
			}
		}
		
		this.currentValues = getNextBlockOfData(scannerObject);
		while(this.currentValues!=null)
		{
			System.out.println("");
			displayArrayList(prevValues);
			displayArrayList(currentValues);
			displayArrayList(rowStore);
			System.out.println("assembleBlocks offset:"+Arrays.toString(offset));
			int changeIndex = findTheIndexWhereTheFirstChangeHappenedAndIncrementTheCountTillThat();
			System.out.println("assembleBlocks changeIndex:"+changeIndex);
			if(changeIndex==-1)
				throw new TwoRowsHaveSameValueException("Same tuples in two different and consecutive rows");
			else
			{
				for(int i=this.noOfColumnStore-1; i>=changeIndex;i--)//Flushes Column Store since the value changed for the corresponding columns
				{
					System.out.println("assembleBlocks offset:"+Arrays.toString(offset));
					if(i==0)
						flushColumnStore(i);
					else
						this.offset[i-1] += flushColumnStore(i);
					this.countValues[i] = 1;
				}
				System.out.println("assembleBlocks offset:"+Arrays.toString(offset));
				if(changeIndex<=this.noOfColumnStore-1)//If change in value happened in columnStore columns then new Row Store starts so flush the previous one onto the file
				{
					this.offset[this.noOfColumnStore-1] += flushRowStore();
					System.out.println("assembleBlocks offset:"+Arrays.toString(offset));
					for(int i=this.noOfColumnStore, j=0; i<this.noOfColumns;i++)
					{
						this.rowStore.get(j).clear();
						this.rowStore.get(j).add(this.currentValues.get(i));
						this.rowStore.get(j++).add(this.countValues[i]);
					}
					for(int i=this.noOfColumnStore;i<this.noOfColumns;i++)
					{
						this.countValues[i] = 1;
					}
				}
				else//If the values of all columns in columnStore remained constant then update the rowStore
				{
					updateRowStoreBlock();
					for(int i=changeIndex+1;i<this.noOfColumns;i++)
					{
						this.countValues[i] = 1;
					}
				}
			}
			this.prevValues = this.currentValues;
			this.currentValues = getNextBlockOfData(scannerObject);
			displayArrayList(rowStore);
		}
		flushRowStore();
		for(int i=this.noOfColumnStore-1; i>=0;i--)//Flushes Column Store
		{
			flushColumnStore(i);
		}
		
		if (scannerObject != null)
			scannerObject.close();
		
	}

	public void theBrainOfCSVImportor(String fileName)
	{
		try {
			assembleBlocks(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) throws FileNotFoundException, IOException
	{
		CSVImporter objOfCSVImporter = new CSVImporter("src/", 4, new ArrayList<Character>(Arrays.asList('s', 's', 's', 'i', 'i', 'i')));
		objOfCSVImporter.theBrainOfCSVImportor("dummyTable.csv");

//		CSVImporter objOfCSVImporter2 = new CSVImporter("src/", 1, new ArrayList<Character>(Arrays.asList('s', 's', 's', 'i', 'i', 'i')));
//		objOfCSVImporter2.theBrainOfCSVImportor("dummyTable.csv");
		
		Relation objOfRelation = new Relation("src/ColumnStore(0).bin", true, 0, new ArrayList<Character>(Arrays.asList('s', 's', 's', 'i', 'i', 'i')), 5, 0);
		objOfRelation.readFromFile("src/ColumnStore(0).bin", true, 's');
		
		Relation objOfRelation1 = new Relation("src/ColumnStore(1).bin", true, 0, new ArrayList<Character>(Arrays.asList('s', 's', 'i', 'i', 'i')), 4, 0);
		objOfRelation1.readFromFile("src/ColumnStore(1).bin", true, 's');
		
		Relation objOfRelation2 = new Relation("src/ColumnStore(2).bin", true, 0, new ArrayList<Character>(Arrays.asList('s', 'i', 'i', 'i')), 3, 0);
		objOfRelation2.readFromFile("src/ColumnStore(2).bin", true, 's');
		
		Relation objOfRelation3 = new Relation("src/ColumnStore(3).bin", true, 0, new ArrayList<Character>(Arrays.asList('i', 'i', 'i')), 2, 0);
//		objOfRelation3.readFromFile("src/RowStore.bin", false, 'i');
		objOfRelation3.readFromFile("src/ColumnStore(3).bin", true, 'i');
		
		Relation objOfRelation4 = new Relation("src/ColumnStore(4).bin", true, 0, new ArrayList<Character>(Arrays.asList('i', 'i')), 1, 0);
		objOfRelation4.readFromFile("src/ColumnStore(4).bin", true, 'i');
		
		Relation objOfRelation5 = new Relation("src/ColumnStore(5).bin", true, 0, new ArrayList<Character>(Arrays.asList('i')), 1, 0);
		objOfRelation5.readFromFile("src/ColumnStore(5).bin", true, 'i');
		
		Relation objOfRelation6 = new Relation("src/RowStore.bin", true, 0, new ArrayList<Character>(Arrays.asList('i', 'i', 'i')), 3, 0);
		objOfRelation6.readFromFile("src/RowStore.bin", false, 'i');
	}
}
