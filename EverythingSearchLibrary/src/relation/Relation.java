package relation;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import dataImporter.binaryFileCreator.BinaryFileCreator;

/*
 * Attributes:
 * 	String filePathExtended;
	boolean columnStore;
	int headerBytes;
	ArrayList<Character> columnDataType
 * Methods:
 * - distinctValuesOfFirstColumn(): ArrayList<Object>
 * 		Get the distinct values of the first columns
 * - locationOfTuples(Object val): int
 * 		Gets the location(offset) of the value in first column. If not then raise error "ValueNotFoundException"
 * - jump(Object val): void
 * 		Jumps based on the value. Either it points to the specific value or the value greater than the specified value
 * - resetPointer(): void
 * 		Resets the pointer to the point where the header ends
 * - isMapEmpty(): boolean
 * 		If more tuples are available or not
 * - findFrequencyAndValueForGivenOffset(int offset): ArrayList<Object, Integer>
 * 		For a certain offset, returns the frequency and value.
 * - currentPointerCountAndValue(): ArrayList<Object, Integer>
 * 		Value and Count of first column where the cursor is currently present
 * - next(): If columnStore==true, 
				then next value
			 else
				then next tuple
 * - subRelation(): Relation
 * 		Performs sub-relation
 */

public class Relation extends BinaryFileCreator{

	String filePathExtended;
	int noOfColumnStores;
	boolean columnStore;
	int headerBytes;
	ArrayList<Character> columnDataType;
	ArrayList<ArrayList<Object>> rowStoreValues;
	int indexForRowStore;
	int currentColumnIndex;

	//Constructor
	public Relation(String filePathExtended, boolean columnStore, int headerBytes,ArrayList<Character> columnDataType, int noOfColumnStores, int currentColumnIndex)
	{
		super();
		this.filePathExtended = filePathExtended;
		this.columnStore = columnStore;
		this.headerBytes = headerBytes;
		this.columnDataType = columnDataType;
		this.noOfColumnStores = noOfColumnStores;
		this.currentColumnIndex = currentColumnIndex;
		if(!columnStore)
			this.rowStoreValues = new ArrayList<>();
		else
			this.rowStoreValues = null;
		this.indexForRowStore = 0;
	}

	//Converts Byte (pointing currently) to String
	public String convertCurrentPointingByteToString(DataInputStream dis) throws IOException
	{
		if(dis.available()!=0)
		{
			byte[] str = " ".getBytes();
			str[0] = dis.readByte();
			String string = new String(str, "ASCII");
			return string;
		}
		return null;
	}

	//Extract the next token as String and return it. Delimeter is required to mention which marks the end of string
	public String findStringFromGivenOffsetAndDelimeter(int offset, String delimeter, DataInputStream dis) throws IOException
	{
		if(offset!=-1)//Ignore offset
			dis.skipBytes(offset-1);
		String value = "";

		while(true)
		{
			String string = convertCurrentPointingByteToString(dis);
			if(string==null)
				return null;
			if(delimeter.contains(string))
				break;
			else
				value+=string;
		}
		//		System.out.println("findStringFromGivenOffsetAndDelimeter value:"+value);
		return value;
	}

	//Extract the next token as integer and return it
	public int findIntegerFromGivenOffsetAndDelimeter(int offset, DataInputStream dis) throws IOException
	{
		if(offset!=-1)//Ignore offset
			dis.skipBytes(offset-1);
		int value = dis.readInt();
		return value;
	}

	//Getter for currentColumnIndex
	public int getCurrentColumnIndex()
	{
		return this.currentColumnIndex;
	}

	//Setter for currentColumnIndex
	public void setCurrentColumnIndex(int currentColumnIndex)
	{
		this.currentColumnIndex = currentColumnIndex;
	}

	//Moves the dis object to the location of the next tuple
	public void next(DataInputStream dis) throws IOException
	{
		if(this.columnStore)
		{
			if(columnDataType.get(0).equals('s'))
			{
				findStringFromGivenOffsetAndDelimeter(-1, "_", dis);
				dis.skipBytes(intInBytes+intInBytes);
			}
			else
			{
				findIntegerFromGivenOffsetAndDelimeter(-1, dis);
				dis.skipBytes(intInBytes+intInBytes);
			}
		}
		else
		{
			String string = convertCurrentPointingByteToString(dis);
			if(string.equals(","))
			{
				findStringFromGivenOffsetAndDelimeter(-1, "_", dis);
				dis.skipBytes(intInBytes);
			}
			else
			{
				findIntegerFromGivenOffsetAndDelimeter(-1, dis);
				dis.skipBytes(charInBytes+intInBytes);
			}
		}
	}

	//Adds the value, frequecy/count and offset to the list
	public ArrayList<Object> currentPointerCountValueAndOffset(DataInputStream dis) throws IOException
	{
		ArrayList<Object> list = new ArrayList<Object>();
		dis.mark(50);

		if(this.columnStore)
		{
			if(this.columnDataType.get(0).equals('s'))
				list.add(findStringFromGivenOffsetAndDelimeter(-1, "_", dis));
			else
				list.add(findIntegerFromGivenOffsetAndDelimeter(-1, dis));
			list.add(findIntegerFromGivenOffsetAndDelimeter(-1, dis));//Value
			list.add(findIntegerFromGivenOffsetAndDelimeter(-1, dis));//Offset
		}
		else
		{
			String string = convertCurrentPointingByteToString(dis);
			if(string.equals(","))
				list.add(findStringFromGivenOffsetAndDelimeter(-1, "_", dis));
			else
				list.add(findIntegerFromGivenOffsetAndDelimeter(-1, dis));
			list.add(findIntegerFromGivenOffsetAndDelimeter(-1, dis));//Value
			list.add(null);//Offset
		}
		dis.reset();
		return list;
	}

	//Reads from binary file and display it's content, used as a debugger
	public void readFromFile(String filePathExtended, boolean columnStore, char datatype) throws FileNotFoundException, IOException
	{
		DataInputStream dis = getDataInputStreamObject(filePathExtended);
		String value = "";
		System.out.println();
		System.out.print("readFromFile()  List:");
		//		this.columnStore = columnStore;
		//		this.columnDataType.add(datatype);
		if(columnStore)
		{
			ArrayList<Object> list = new ArrayList<Object>();
			while(dis.available()!=0)
			{
				list.addAll(currentPointerCountValueAndOffset(dis));
				next(dis);
			}
			displayArrayList(list);
		}
		else
		{
			System.out.print("[");
			while(dis.available()>0)
			{
				String string = convertCurrentPointingByteToString(dis);
				if(string.equals("_"))
				{
					if(value.length()>0)
					{
						System.out.print("," + value);
						value = "";
					}
					int integer = dis.readInt();
					System.out.print("_"+ integer);
				}
				else if(string.equals(","))
				{
					continue;
				}
				else if(string.equals(" "))
				{
					System.out.print(", ");
					value = "";
				}
				else if(string.equals(";"))
				{
					System.out.print(",;");
					value = "";
				}
				else	
				{
					value += string;
				}
			}
			System.out.print("]");
		}
	}

	//Deletes the top of row store
	public void popTheTopOfListToRowStoreValues(ArrayList<ArrayList<Object>> list) {
		this.rowStoreValues.add(list.get(0));
		list.remove(0);
		//		if(list.size()>0)
		//			return false;
		//		else
		//			return true;		
	}

	//For ColumnStore, the return value holds significance since the list contains the info, but for rowStore, the this.rowStoreValues holds the actual result
	public ArrayList<ArrayList<Object>> distinctValuesOfFirstColumn() throws Exception
	{
		ArrayList<ArrayList<Object>> list = new ArrayList<ArrayList<Object>>();
		for(int i=0; i<this.columnDataType.size();i++)
		{
			list.add(new ArrayList<Object>());
		}
		//https://www.tutorialspoint.com/java/io/datainputstream_read.htm

		DataInputStream dis = getDataInputStreamObject(this.filePathExtended);
		if(this.headerBytes>0)
			dis.skipBytes(this.headerBytes-1);
		if(this.columnStore)
		{
			while(dis.available()!=0)
			{
				list.get(0).add(currentPointerCountValueAndOffset(dis).get(0));
				next(dis);
			}
			//			displayArrayList(list);
			return list;
		}
		else
		{
			//			counterForList = 0
			//					list = []
			//					firstColumnValues = []
			//					while dis.availabale=true:
			//						if(next prefix is NOT ',')
			//							value, freq <- Based on prefix, get next element and also get the frequency of it
			//							Based on freq, add the value in arraylist at that index.:
			//								if counterForList = 0, create a new arraylist and then add
			//								else i=0..freq, get(i).add(value)
			//						else
			//							flush the top value of list to returnValueOfNext()
			//			boolean counterForList = true;

			while(dis.available()>0)
			{
				String string = convertCurrentPointingByteToString(dis);
				System.out.println("distinctValuesOfFirstColumn string:"+string);

				if(string.equals(","))
				{
					dis.mark(50);
					String str = findStringFromGivenOffsetAndDelimeter(-1, "_,", dis);
					System.out.println("distinctValuesOfFirstColumn str:"+str);
					if(str==null)
					{
						//						System.out.println("End of File");
						break;
					}
					if(str.equals(";"))
					{
						popTheTopOfListToRowStoreValues(list);
						dis.reset();
						dis.skipBytes(1);
					}
					else
					{
						//						dis.skipBytes(1);//FrequencyPrefix
						int frequency = findIntegerFromGivenOffsetAndDelimeter(-1, dis);
						for(int i=0;i<frequency;i++)
						{
							list.add(new ArrayList<Object>());
							list.get(i).add(str);
						}
					}
				}
				else
				{
					//					System.out.println("?");
					int value = findIntegerFromGivenOffsetAndDelimeter(-1, dis);
					System.out.println("distinctValuesOfFirstColumn value:"+value);
					dis.skipBytes(1);//FrequencyPrefix
					int frequency = findIntegerFromGivenOffsetAndDelimeter(-1, dis);
					//					System.out.println(frequency);
					for(int i=0;i<frequency;i++)
					{
						//						System.out.println("?");
						list.add(new ArrayList<Object>());
						list.get(i).add(value);
					}
				}
				displayArrayList(list);
				System.out.println(this.rowStoreValues);
			}
			//			System.out.print("]");
			return list;
		}
	}

	//Returns the location of value
	public int locationOfTuples(Object val) throws NumberFormatException, IOException
	{
		DataInputStream dis = getDataInputStreamObject(this.filePathExtended);
		int lengthOfTheFile = dis.available();
		if(this.headerBytes>0)
			dis.skipBytes(this.headerBytes-1);
		if(this.columnStore)
		{
			while(dis.available()!=0)
			{
				//				System.out.println(this.columnDataType.get(0).equals('s'));
				int location = lengthOfTheFile-dis.available();
				if(this.columnDataType.get(0).equals('s'))
				{
					if((""+val).equals(currentPointerCountValueAndOffset(dis).get(0)))
						return location;
				}
				else
				{
					if((Integer.parseInt(""+val)==((int)currentPointerCountValueAndOffset(dis).get(0))))
						return location;	
				}
				next(dis);
			}
		}
		return -1;
	}

	//Jumps to the value
	public void jump(Object val, DataInputStream dis) throws NumberFormatException, IOException
	{
		if(this.columnStore)
		{
			while(dis.available()!=0)
			{
				if(this.columnDataType.get(0).equals('s'))
				{
					if((""+val).equals(currentPointerCountValueAndOffset(dis).get(0)))
						break;
				}
				else
				{
					if((Integer.parseInt(""+val)<=((int)currentPointerCountValueAndOffset(dis).get(0))))
						break;	
				}
				next(dis);
			}
		}
	}

	//Checks if there any more tuples remaining
	public boolean areThereAnyMoreTuples(DataInputStream dis) throws IOException
	{
		return dis.available()!=0;
	}

	//Resets the pointer to the start
	public void resetPointer(DataInputStream dis) throws Exception
	{
		dis = getDataInputStreamObject(this.filePathExtended);
		if(this.headerBytes>0)
			dis.skipBytes(this.headerBytes-1);
	}

	//Performs sub-relation
	public Relation subRelation() throws SubRelationNotPossibleException
	{
		if(this.columnStore)
		{
			boolean columnStoreSubRel;
			String filePathExtendedSubRel;
			if(this.noOfColumnStores==1)
			{
				columnStoreSubRel = false;
				filePathExtendedSubRel = "src/RowStore.bin";
			}
			else
			{
				columnStoreSubRel = true;
				filePathExtendedSubRel = "src/ColumnStore("+ (Integer.parseInt(""+this.filePathExtended.charAt(this.filePathExtended.length()-6))+1) + ").bin";
			}
			int noOfColumnStoresSubRel = this.noOfColumnStores-1;
			int headerBytesSubRel = 0;
			int currentColumnIndexSubRel = this.currentColumnIndex + 1;
			ArrayList<Character> columnDataTypeSubRel = new ArrayList<Character>(this.columnDataType);columnDataTypeSubRel.remove(0);

			return new Relation(filePathExtendedSubRel, columnStoreSubRel, headerBytesSubRel, columnDataTypeSubRel, noOfColumnStoresSubRel, currentColumnIndexSubRel);
		}
		else
		{
			throw new SubRelationNotPossibleException("Can't perform subrelation on Row Store");
		}
	}

	public void theBrainOfRelation()
	{
		try 
		{
			ArrayList<ArrayList<Object>> list = distinctValuesOfFirstColumn();
//			locationOfTuples("a");
			System.out.println("Debugging");
			DataInputStream dis = getDataInputStreamObject(this.filePathExtended);
			//			if(this.columnStore)
			//				displayArrayList(list.get(0));
			//			else
			//			{
			//				for(int i=0;i<list.size();i++)
			//				{
			//					displayArrayList(list.get(i));
			//				}
			//			}
//			next(dis);
			ArrayList<Object> retList = currentPointerCountValueAndOffset(dis);
			for(int i=0; i<(int)retList.get(1);i++)
			{
				System.out.println(retList.get(0));
				
			}
			System.out.println("areThereAnyMoreTuples(dis)"+areThereAnyMoreTuples(dis));

			System.out.println("locationOfTuples(g):"+locationOfTuples("g"));

			System.out.println("subRelation()"+subRelation().filePathExtended);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws Exception
	{
		Relation objOfRelation = new Relation("src/ColumnStore(0).bin", true, 0, new ArrayList<Character>(Arrays.asList('s', 's', 's', 'i', 'i', 'i')), 4, 0);
		objOfRelation.theBrainOfRelation();
		System.out.println("Done with first part");
		Relation sub = objOfRelation.subRelation();
		sub.theBrainOfRelation();

		//		Relation sub2 = sub.subRelation();
		//		System.out.println("subRelation()"+sub2.filePathExtended);
		//		sub2.theBrainOfRelation();

		//		Relation objOfRelation2 = new Relation("src/RowStore.bin", true, 0, new ArrayList<Character>(Arrays.asList('i', 'i', 'i')));
		//		objOfRelation2.theBrainOfRelation();
	}
}
