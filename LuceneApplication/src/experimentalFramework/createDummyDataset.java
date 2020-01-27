package experimentalFramework;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class createDummyDataset {
	
	String path = "src/";
	
	public ArrayList<String> generateSingleLetters()
	{
		ArrayList<String> list = new ArrayList<String>();
		for(int i=97; i<97+26;i++)
			list.add((char)i+"");
		return list;
	}
	
	public ArrayList<String> generateListOfKeywordsByAppendingEachLetterToEachElement(ArrayList<String> list)
	{
		ArrayList<String> newList = new ArrayList<String>();
		for(int i=0; i<list.size(); i++)
		{
			for(int j=97; j<97+26;j++)
			{
				newList.add(list.get(i)+(char)j);
			}
		}
		return newList;
	}
	
	public ArrayList<String> generateKeywords(int maxLength)
	{
		ArrayList<String> list = generateSingleLetters();
		for(int i=1; i<maxLength;i++)
		{
			list.addAll(generateListOfKeywordsByAppendingEachLetterToEachElement(list));
		}
		return list;
	}
	
	public void displayListOfKeywords(@SuppressWarnings("rawtypes") ArrayList list)
	{
		System.out.println(list);
	}
	
	public int getRandomNumberForARange(int min, int max)//both min and max inclusive
	{
		Random rand = new Random();
		return rand.nextInt((max - min) + 1) + min;
	}
	
	public String getRandomPara( ArrayList<String> listOFKeywords, int rangeOfLengthOfParaMin, int rangeOfLengthOfParaMax)
	{
		int getLength = getRandomNumberForARange(rangeOfLengthOfParaMin, rangeOfLengthOfParaMax);
		String para = "";
		for(int i=0; i<getLength; i++)
		{
			para += listOFKeywords.get(getRandomNumberForARange(0, listOFKeywords.size()-1)) + " ";
		}
		return para.trim();
	}
	
	public String appendABlockToDataset(int url, ArrayList<String> listOFKeywords)
	{
		String titleLine = url + "|t|" + getRandomPara(listOFKeywords, 4, 25);
		String abstractLine = url + "|a|" + getRandomPara(listOFKeywords, 20, 250);
		String entityLine = url + "\t" + "0" + "\t" + "0" + "\t" + "entityName" + "\t" + "category" + "\t" + "someId";
		String emptyLine = "";
		return (titleLine + "\n" +  abstractLine + "\n" +  entityLine + "\n" +  emptyLine + "\n");
	}
	
	public void createArtificialPubmedDataset()
	{
		try
		{
			ArrayList<String> listOFKeywords = generateKeywords(2);
			int noOfDocuments = 50;
			
			PrintWriter pubmedWriter = new PrintWriter(new File(path+"artificialPubmed.sample"));//create new file
			
			for(int i=0; i<noOfDocuments;i++)
			{
				pubmedWriter.append(appendABlockToDataset(i, listOFKeywords));
			}

			pubmedWriter.flush();
			pubmedWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not create Table K");
		}
	}
	
	public void theBrainOfCreatedummyDataset()
	{
		createArtificialPubmedDataset();
	}
	
	public static void main(String args[])
	{
		createDummyDataset obj = new createDummyDataset();
		obj.theBrainOfCreatedummyDataset();
	}

}
