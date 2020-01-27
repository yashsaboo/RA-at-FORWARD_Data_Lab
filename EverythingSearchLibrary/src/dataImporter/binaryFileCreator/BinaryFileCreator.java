package dataImporter.binaryFileCreator;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/*
 * - createBinaryFile(String filePath): void <exception handling required>
 * 		creates Binary file with the intended name, and closes it
 * - writeIntoFile(ArrayList<Object> block): void <exception handling required>
 * 		appends the block into the file. If file is not present, raise exception
 */

public class BinaryFileCreator extends HelperFunctions{
	
	//creates Binary file with the intended name, and closes it
	public void createBinaryFile(String filePathExtended)
	{
		System.out.println("createBinaryFile(String filePathExtended)");
		try {
			
			File file = new File(filePathExtended); 

			if (file.exists())
				file.delete(); //you might want to check if delete was successful

			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public DataOutputStream getDataOutputStreamObject(String filePathExtended) throws FileNotFoundException
	{
		File file = new File(filePathExtended);

		if (file.exists())
		{
			OutputStream outputStream = new FileOutputStream(filePathExtended);
			DataOutputStream dos = new DataOutputStream(outputStream); //DOS because Double can only be loaded into file using this library
			return dos;
		} 
		return null;
	}
	
	public DataInputStream getDataInputStreamObject(String filePathExtended) throws FileNotFoundException
	{
		File file = new File(filePathExtended);

		if (file.exists())
		{
			InputStream inputStream = new FileInputStream(filePathExtended);
			DataInputStream dis = new DataInputStream(new BufferedInputStream(inputStream)); //DOS because Double can only be loaded into file using this library
			return dis;
		} 
		return null;
	}
	
	public FileOutputStream getFileOutputStreamObject(String filePathExtended) throws FileNotFoundException
	{
		File file = new File(filePathExtended);

		if (file.exists())
		{
			FileOutputStream fileOutputStream = new FileOutputStream(filePathExtended, true); 
			return fileOutputStream;
		}
		return null;
	}
	
	//appends the block into the file. If file is not present, raise exception
	public void writeIntoFile(ArrayList<Object> data, String filePathExtended, boolean columnStore) throws IOException
	{
		FileOutputStream fileOutputStream = getFileOutputStreamObject(filePathExtended);

		for(int i=0; i<data.size();i++)
		{
			if(data.get(i).getClass().getName().equals("java.lang.Integer"))//Integer: _
			{
				if(!columnStore)
					fileOutputStream.write("_".getBytes());
				fileOutputStream.write(intToBytes((int) data.get(i)));
			}
			else
			{
				if(!columnStore)
					fileOutputStream.write(",".getBytes());
				fileOutputStream.write(((String) data.get(i)).getBytes());
				if(columnStore)
					fileOutputStream.write("_".getBytes());//String is always followed by an integer(no matter columnStore rowStore
			}
		}
		fileOutputStream.close();
	}
}
