package experimentalFramework;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

public class PubmedParser {
	
	protected BufferedReader br;
	protected int numdocs;
	protected String filePath;
	
	public PubmedParser() {
		super();
		this.br = null;
		this.numdocs = 0;
		this.filePath = null;
	}
	
	public PubmedParser(String filePath) {
		this.filePath = filePath;
		this.br = getBuffer(filePath);
		this.numdocs = 0;
	}
	
	//Initialises the bufferedReader
	private BufferedReader getBuffer(String filePath) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			return br;
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("couldn't get the buffered reader");
		}
		return null;
	}
	
	//Resetting the bufferedReader
	public void reset() {
		br=getBuffer(this.filePath);
		numdocs=0;
	}
	
	//Finds if we reached the end of file
	public boolean moreDocumentsLeft()
	{
		if(br!=null)
			return true;
		else
			return false;
	}
	
	//After reading title and abstract in other function, this function extracts entities from the subsequent lines to reach the next block
	public DocumentProperties findEntities(DocumentProperties dp) throws IOException
	{
		while(true)
		{
			String entitiesLine = br.readLine();
//			System.out.println(str);
			if(entitiesLine==null||entitiesLine.equals(""))
				break;
			else//url eref1 eef2(optional) entityName category someID
			{
				String[] entity = entitiesLine.split("[ \t]");
				dp.eref1.add(Integer.parseInt(entity[1]));
				if(entity.length==6)
					dp.eref2.add(Integer.parseInt(entity[2]));
				dp.entityInfo.add(entity[entity.length-3]);
				dp.categories.add(entity[entity.length-2]);
			}
		}
		return dp;
	}
	
	//Closes the reader
	public void closeReader() throws IOException
	{
		if(br!=null)
		{
			br.close();
			br = null;
		}
	}
	
	//Gets the info of each document
	public DocumentProperties getNextDocumentContent()
	{
		DocumentProperties dp = null;
		if (moreDocumentsLeft())
		{
			try
			{
				//Title
				String titleLine = br.readLine();
				if(titleLine==null||titleLine.equals(""))//reached the last of the sample, which has no content
				{
					closeReader();
					return dp;
				}
				else
				{
					dp = new DocumentProperties();
				}
				dp.url = Integer.parseInt(titleLine.split("\\|")[0].trim());
				dp.titleInfo = titleLine.split("\\|")[2].trim();
				
				//Abstract
				String abstractLine = br.readLine();
				if(abstractLine.split("\\|").length>=3)//some documents might have empty abstracts so we check if its present first
					dp.abstractInfo = abstractLine.split("\\|")[2].trim();
				
				//Entities
				dp = findEntities(dp);
				
			}catch(IOException e)
			{
				System.out.println("Problem reading the document block");
			}
		}
		else
		{
			System.out.println("No more documents left");
		}
		return dp;
	}
	
	public void displayNoOfDocuments()
	{
		System.out.print("displayNoOfDocuments():"+numdocs);
	}
	
	//Test controller
	public void theBrainOfPubmedParser() throws IOException
	{
		DocumentProperties dp;
		if(this.filePath==null)
			System.out.println("No file Path");
		else
		{
			while(true)
			{
				if(!moreDocumentsLeft())
					break;
				dp = getNextDocumentContent();
				if(dp!=null)
				{
					dp.display();
					numdocs++;
				}
			}
			displayNoOfDocuments();
			closeReader();
		}
		
	}

	public static void main(String[] args) throws IOException {
		
		PubmedParser obj = new PubmedParser("src/pubmed.sample");
		obj.theBrainOfPubmedParser();	

	}

}
