package experimentalFramework;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class createCSVforExperimentalFramework extends LuceneDatabaseReader{
	
	String path = "src/";
	
	//https://stackoverflow.com/a/13911747
	public HashSet<String> getDistinctKeywords(IndexReader indexReader, String fieldName) throws IOException
	{
		HashSet<String> distinctTerms = new HashSet<String>();
		Fields fields = MultiFields.getFields(indexReader);
		Terms terms = fields.terms(fieldName);
		TermsEnum iterator = terms.iterator();
		BytesRef byteRef = null;
		while((byteRef = iterator.next()) != null) {
			String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
			distinctTerms.add(term);
		}
		
		// traversing over HashSet 
		System.out.println("Traversing over Set using Iterator uniqueTerms.size():"+distinctTerms.size()); 
//		for(String term : distinctTerms){
//			   System.out.println(term);
//			}

		return distinctTerms;
	}
	
	public double getIDF(IndexReader indexReader, String fieldName, String term) throws IOException
	{
		int N = indexReader.numDocs(); // the total number of documents in the index
		int n = indexReader.docFreq(new Term(fieldName, term)); // get the document frequency of the term in the "text" field
		double idf = Math.log((N + 1.0) / (n + 1.0)); // well, we normalize N and n by adding 1 to avoid n = 0
		
		return idf;
	}
	
	//Creates CSV for Table D: did,len,elen,url
	public void createCSVforD(IndexReader indexReader)
	{
		try
		{
			int noOfDocs = getNumberOfDocuments(indexReader);
			ArrayList<Integer> lengthOfDocs = readDocLength(indexReader, "body");
			
			PrintWriter csvWriter = new PrintWriter(new File(path+"D.csv"));//create new file
			csvWriter.append("did,len,elen,url\n");//write column names
			
			for(int i=0; i<noOfDocs; i++)
			{
				int elen = Integer.parseInt(getDocno(indexReader, "elen", i));
				String url = getDocno(indexReader, "url", i);
				csvWriter.append(String.format("\"%d\",\"%d\",\"%d\",\"%s\"\n", i, lengthOfDocs.get(i), elen, url));
			}
			
			csvWriter.flush();
			csvWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not create Table K");
		}
	}
	
	//Creates CSV for Table K: kid,value,idf
	public void createCSVforK(IndexReader indexReader)
	{
		try
		{
			HashSet<String> distinctTerms = getDistinctKeywords(indexReader, "body");
			
			PrintWriter csvWriter = new PrintWriter(new File(path+"K.csv"));//create new file
			csvWriter.append("kid,value,idf\n");//write column names
			
			int i = 0;
			for(String term : distinctTerms)
			{
				float idf = (float)getIDF(indexReader, "body", term);
				csvWriter.append(String.format("\"%d\",\"%s\",\"%.6f\"\n", i, term, idf));//append row values	
				i++;
			}
			
			csvWriter.flush();
			csvWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not create Table K");
		}
	}
	
	//Creates CSV for Table KD: kid,dids,tfs
	public void createCSVforKD(IndexReader indexReader)
	{
		try
		{
			HashSet<String> distinctTerms = getDistinctKeywords(indexReader, "body");
			
			PrintWriter csvWriter = new PrintWriter(new File(path+"KD.csv"));//create new file
			csvWriter.append("kid,did,tf\n");//write column names
			
			int i = 0;
			for(String term : distinctTerms)
			{
				HashMap<Integer, Integer> listOfFreqSortedByDocID = readFreqPosting(indexReader, "body", term);
				for (Map.Entry<Integer,Integer> entry : listOfFreqSortedByDocID.entrySet())
				{
					csvWriter.append(String.format("\"%d\",\"%s\",\"%d\"\n", i, entry.getKey(), entry.getValue()));//append row values
				}
				i++;
			}
			
			csvWriter.flush();
			csvWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not create Table K");
		}
	}
	
	//The Test Controller
    public void theBrainOfCreateCSVforExperimentalFramework()
    {
    	try 
    	{
    		// First, open the directory
    		Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
    		// Then, open an IndexReader to access your index
    		IndexReader indexReader = DirectoryReader.open( dir );
    		
    		//createCSVforD(IndexReader indexReader)
    		createCSVforD(indexReader);
    		
    		//createCSVforK(IndexReader indexReader) 
    		createCSVforK(indexReader);
    		
    		//createCSVforKD(IndexReader indexReader)
    		createCSVforKD(indexReader);
    		
    		// Remember to close both the IndexReader and the Directory after use 
    		indexReader.close();
    		dir.close();
    		
    	} 
    	catch (IOException e) 
    	{
    		e.printStackTrace();
    	}
    }
    
	public static void main(String[] args) {
		createCSVforExperimentalFramework objCreateCSVforExperimentalFramework = new createCSVforExperimentalFramework();
		objCreateCSVforExperimentalFramework.theBrainOfCreateCSVforExperimentalFramework();
	}

}
