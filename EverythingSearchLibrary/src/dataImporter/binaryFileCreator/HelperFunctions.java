package dataImporter.binaryFileCreator;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * - generateStringBinaryForMapColumns(): String 
 *		Generates Binary equivalent for representing the column datatype in String datatype <padding><1stCol><2ndCol>...<nthCol> //Integer = 1, String = 0
 * - stringBinaryToInteger(String str): int 
 * 		Converts the String representation of columns to Integer
 * - getByteSizeBasedOnDataType(String dataType): int 
 * 		For given datatype, it will return how much space will it store in bytes. Note: For String, it will return the storage value of character
 * - removeQuotesFromAtrributes(String[] attributes): String[] 
 * 		Removes the quotes since some CSV format has quotes surrounding the content
 * - intToBytes(int x): byte[]
 * 		Converts integer to Byte array
 * - bytesToInt(byte[] bytes): int 
 * 		Converts Byte array to integer
 */

public class HelperFunctions {
	
	protected int intInBytes = 4; 	//No of Bytes an integer takes
	protected int charInBytes = 1;	//No of Bytes a character takes
	protected int suffixInBytes = 1;//No of Bytes a comma takes			
	
	//Generates Binary equivalent for representing the column datatype in String datatype <padding><1stCol><2ndCol>...<nthCol> //Integer = 1, String = 0
	public String generateStringBinaryForMapColumns(int noOfColumns, String[] columnDatatypes)
	{
		String str = "";
		for(int i=0; i<columnDatatypes.length;i++)
		{
			if(columnDatatypes[i]=="Integer")
				str += "1";
			else
				str += "0";
		}
		return str;
	}
	
	//Converts the String representation of columns to Integer
	public int stringBinaryToInteger(String str)
	{
		if(str!=null)
		{
			int result = 0;
			int power = 0;
			for(int i=str.length()-1; i>=0;i--)//Decrementing since we need to go from right to left
			{
				//			System.out.println("str.charAt(i):"+str.charAt(i));
				result += Integer.parseInt(""+str.charAt(i))*Math.pow(2, power++);
			}
			System.out.println("Result:"+result);
			return result;
		}
		else
		{
			System.out.println("Empty String");
			return 0;
		}
	}
	
	//For given datatype, it will return how much space will it store in bytes. Note: For String, it will return the storage value of character
	public int getByteSizeBasedOnDataType(String dataType)
	{
		if (dataType.equals("String"))
			return charInBytes;
		return intInBytes;
	}
	
	//Removes the quotes since some CSV format has quotes surrounding the content
	public String[] removeQuotesFromAtrributes(String[] attributes)
	{
		for(int i=0; i<attributes.length;i++)
		{
//			System.out.println(attributes[i]);
			if(attributes[i].charAt(0)=='"')
			{
				attributes[i] = attributes[i].substring(1, attributes[i].length()-1);
			}
		}
		return attributes;
	}
	
	//Converts integer to Byte array
	//https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java
	public byte[] intToBytes(int x) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(x);
		return buffer.array();
	}
	
	//Converts Byte array to integer
	public int bytesToInt(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.put(bytes);
		buffer.flip();//need flip 
		return buffer.getInt();
	}
	
	//Displays Array List
	public void displayArrayList (List list)
	{
		System.out.println(Arrays.toString(list.toArray()));
	}
}
