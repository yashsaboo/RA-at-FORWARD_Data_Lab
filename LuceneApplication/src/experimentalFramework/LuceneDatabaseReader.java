//Reference: https://github.com/jiepujiang/LuceneTutorial

package experimentalFramework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class LuceneDatabaseReader extends LuceneBuildIndex{
	
	//Gets total number of documents
	public int getNumberOfDocuments(IndexReader indexReader)
	{
		return indexReader.numDocs();
	}

	/**
	 * Get the DocNo (external ID, can be any field, but here we usually will use url) of a document stored in the index by its internal id (here from 0 to maxNoOfDocs).
	 *
	 * @param indexReader      An index reader.
	 * @param fieldDocno The name of the field you used for storing docnos (external document IDs).
	 * @param docid      The internal ID of the document: docID must be >= 0 and < maxDoc; maxDoc is totalNoOfDocuments
	 * @return The docno (external ID) of the document.
	 * @throws IOException
	 */
	public String getDocno( IndexReader indexReader, String fieldName, int docid ) throws IOException {
		// One should consider reuse the fieldset if you need to read docnos for a lot of documents.
		Set<String> fieldset = new HashSet<>();
		fieldset.add( fieldName );
		Document d = indexReader.document( docid, fieldset );
		return d.get( fieldName );
	}

	/**
	 * Find a document in the index by its docno (external ID, can be any field, but here we usually will use url).
	 * Returns the internal ID (here from 0 to maxNoOfDocs) of the document; or -1 if not found.
	 *
	 * @param indexReader      An index reader.
	 * @param fieldDocno The name of the field you used for storing docnos (external document IDs).
	 * @param docno      The docno (external ID) you are looking for.
	 * @return The internal ID of the document in the index; or -1 if not found.
	 * @throws IOException
	 */
	public int findByDocno( IndexReader indexReader, String fieldName, String docno ) throws IOException {
		BytesRef term = new BytesRef( docno );
		PostingsEnum posting = MultiFields.getTermDocsEnum( indexReader, fieldName, term, PostingsEnum.NONE );
		if ( posting != null ) {
			int docid = posting.nextDoc();
			if ( docid != PostingsEnum.NO_MORE_DOCS ) {
				return docid;
			}
		}
		return -1;
	}

	/**
	 * One can retrieve a term's posting list from an index. 
	 * The simplest form is document-frequency posting list, where each entry in the list is a <docid,frequency> pair (only includes the documents containing that term). 
	 * The entries are sorted by docids such that you can efficiently compare and merge multiple lists.    
	 * This is an example for accessing a term-document-frequency posting list from a Lucene index.
	 * @param indexReader   An index reader.
	 * @param fieldName 	The name of the field you used for storing docnos (external document IDs).
	 * @param term      	The term for which frequency is required
	 * @return 
	 * @return 
	 * @throws IOException **/
	public HashMap<Integer, Integer> readFreqPosting(IndexReader indexReader, String fieldName, String term) throws IOException {

		HashMap<Integer, Integer> listOfFreqSortedByDocID = new HashMap<>();
		System.out.printf( "%-10s%-15s%-6s\n", "DOCID", "URL", "FREQ" );//Reads the posting list of the term in a specific index field. You need to encode the term into a BytesRef object, which is the internal representation of a term used by Lucene.
		PostingsEnum posting = MultiFields.getTermDocsEnum( indexReader, fieldName, new BytesRef( term ), PostingsEnum.FREQS );

		if ( posting != null ) { // if the term does not appear in any document, the posting object may be null
			int docid;
			while ( ( docid = posting.nextDoc() ) != PostingsEnum.NO_MORE_DOCS ) {//Each time you call posting.nextDoc(), it moves the cursor of the posting list to the next position and returns the docid of the current entry (document). Note that this is an internal Lucene docid. It returns PostingsEnum.NO_MORE_DOCS if you have reached the end of the posting list.
				String url = getDocno( indexReader, "url", docid );
				int freq = posting.freq(); // get the frequency of the term in the current document
				listOfFreqSortedByDocID.put(docid, freq);
				System.out.printf( "%-10d%-15s%-6d\n", docid, url, freq );
			}
		}

		return listOfFreqSortedByDocID;
	}

	/**
	 * This is an example of counting document field length in Lucene.
	 * Unfortunately, by the time I created the tutorial, Lucene does not store document length in its index.
	 * An acceptable but slow solution is that you calculate document length by yourself based on a document
	 * vector. In case your dataset is static and relatively small (such as just about or less than a few
	 * million documents), you can simply compute all documents' lengths after you've built an index and store
	 * them in an external file (it takes just 4MB to store 1 million docs' lengths as integers). At running
	 * time, you can load all the computed document lengths to avoid loading doc vector and computing length.
	 * @return 
	 * @throws IOException 
	 */
	public ArrayList<Integer> readDocLength(IndexReader indexReader, String fieldName) throws IOException {

		Set<String> fieldset = new HashSet<>(); fieldset.add( "url" );//For printing out external ID
		ArrayList<Integer> listOfDocLengthSortedByDocID = new ArrayList<>();

		// The following loop iteratively print the lengths of the documents in the index.
		System.out.printf( "%-10s%-15s%-10s\n", "DOCID", "URL", "Length" );
		for ( int docid = 0; docid < indexReader.maxDoc(); docid++ ) {
			String url = indexReader.document( docid, fieldset ).get( "url" );
			int doclen = 0;
			// Unfortunately, Lucene does not store document length in its index
			// (because its retrieval model does not rely on document length).
			// An acceptable but slow solution is that you calculate document length by yourself based on
			// document vector. In case your dataset is static and relatively small (such as about or less
			// than a few million documents), you can simply compute the document lengths and store them in
			// an external file (it takes just a few MB). At running time, you can load all the computed
			// document lengths to avoid loading doc vector and computing length.
			TermsEnum termsEnum = indexReader.getTermVector( docid, fieldName ).iterator();
			while ( termsEnum.next() != null ) {
				doclen += termsEnum.totalTermFreq();
			}
			listOfDocLengthSortedByDocID.add(doclen);
			System.out.printf( "%-10d%-15s%-10d\n", docid, url, doclen );
		}
		return listOfDocLengthSortedByDocID;
	}

	//This is an example for accessing a term-document-position posting list from a Lucene index.
	public ArrayList<Integer> readPositionPosting(IndexReader indexReader, String fieldName, String term) throws IOException {

		Set<String> fieldset = new HashSet<>(); fieldset.add( "url" );//For printing out external ID
		ArrayList<Integer> listOfPositionsSortedByDocID = new ArrayList<>();

		// The following line reads the posting list of the term in a specific index field. You need to encode the term into a BytesRef object, which is the internal representation of a term used by Lucene.
		System.out.printf( "%-10s%-15s%-10s%-20s\n", "DOCID", "URL", "FREQ", "POSITIONS" );
		PostingsEnum posting = MultiFields.getTermDocsEnum( indexReader, fieldName, new BytesRef( term ), PostingsEnum.POSITIONS );
		if ( posting != null ) { // if the term does not appear in any document, the posting object may be null
			int docid;
			// Each time you call posting.nextDoc(), it moves the cursor of the posting list to the next position
			// and returns the docid of the current entry (document). Note that this is an internal Lucene docid.
			// It returns PostingsEnum.NO_MORE_DOCS if you have reached the end of the posting list.
			while ( ( docid = posting.nextDoc() ) != PostingsEnum.NO_MORE_DOCS ) {
				String url = indexReader.document( docid, fieldset ).get( "url" );
				int freq = posting.freq(); // get the frequency of the term in the current document
				System.out.printf( "%-10d%-15s%-10d", docid, url, freq );
				for ( int i = 0; i < freq; i++ ) {
					// Get the next occurrence position of the term in the current document.
					// Note that you need to make sure by yourself that you at most call this function freq() times.
					int position = posting.nextPosition();
					listOfPositionsSortedByDocID.add(position);
					System.out.print( ( i > 0 ? "," : "" ) + position);
				}
				System.out.println();
			}
		}
		return listOfPositionsSortedByDocID;
	}

	//This is an example for accessing a stored document vector from a Lucene index.
	public void readDocVector(IndexReader indexReader, String fieldName, int docid) throws IOException {

		Terms vector = indexReader.getTermVector( docid, fieldName ); // Read the document's document vector.

		// You need to use TermsEnum to iterate each entry of the document vector (in alphabetical order).
		System.out.printf( "%-20s%-10s%-20s\n", "TERM", "FREQ", "POSITIONS" );
		TermsEnum terms = vector.iterator();
		PostingsEnum positions = null;
		BytesRef term;
		while ( ( term = terms.next() ) != null ) {

			String termstr = term.utf8ToString(); // Get the text string of the term.
			long freq = terms.totalTermFreq(); // Get the frequency of the term in the document.

			System.out.printf( "%-20s%-10d", termstr, freq );

			// Lucene's document vector can also provide the position of the terms
			// (in case you stored these information in the index).
			// Here you are getting a PostingsEnum that includes only one document entry, i.e., the current document.
			positions = terms.postings( positions, PostingsEnum.POSITIONS );
			positions.nextDoc(); // you still need to move the cursor
			// now accessing the occurrence position of the terms by iteratively calling nextPosition()
			for ( int i = 0; i < freq; i++ ) {
				System.out.print( ( i > 0 ? "," : "" ) + positions.nextPosition() );
			}
			System.out.println();

		}
	}

	//This is an example of accessing corpus statistics and corpus-level term statistics.
	public void readCorpusStats(IndexReader indexReader, String fieldName, String term) throws IOException
	{
		int N = indexReader.numDocs(); // the total number of documents in the index
		int n = indexReader.docFreq(new Term(fieldName, term)); // get the document frequency of the term in the "text" field
		double idf = Math.log((N + 1.0) / (n + 1.0)); // well, we normalize N and n by adding 1 to avoid n = 0

		System.out.printf("%-30sN=%-10dn=%-10dIDF=%-8.2f\n", term, N, n, idf);

		long corpusTF = indexReader.totalTermFreq(new Term(fieldName, term)); // get the total frequency of the term in the "text" field
		long corpusLength = indexReader.getSumTotalTermFreq(fieldName); // get the total length of the "text" field
		double pwc = 1.0 * corpusTF / corpusLength;

		System.out.printf("%-30slen(corpus)=%-10dfreq(%s)=%-10dP(%s|corpus)=%-10.6f\n", term, corpusLength, term, corpusTF, term, pwc);
	}

	//In Lucene, we can store contents in an index document just let a database.
	public void readStoredDocField(IndexReader indexReader, int numberOfDocs) throws IOException {

		// Let's just retrieve the docno (external ID) and title of the first 100 documents in the index

		// store the names of the fields you hope to access in the following set
		Set<String> fieldset = new HashSet<>();
		fieldset.add( "url" );
		fieldset.add( "body" );
		// You can add more fields into the set as long as they have been stored at indexing time.
		// But for efficiency issues, you should only include the fields you are going to use.

		System.out.printf( "%-10s%-15s%-30s\n", "DOCID", "url", "body" );

		// iteratively read and print out the first 100 documents' internal IDs, external IDs (DOCNOs), and titles.
		for ( int docid = 0; docid < indexReader.maxDoc() && docid < numberOfDocs; docid++ ) {

			// The following line retrieves a stored document representation from the index.
			// It will only retrieve the fields you included in the set.
			// There is also a method ixreader.document( docid ), which simply retrieves all the stored fields for a document.
			Document doc = indexReader.document( docid, fieldset );

			// Now you can get the string data previously stored in the field of the document.
			// Again, you can only access the data field's value if you stored the value at index time.
			String url = doc.getField( "url" ).stringValue();
			String body = doc.getField( "body" ).stringValue();

			// You can also store other types of data (such as integers, floats, bytes, etc) in an indexed document.
			// You can access the data using methods such as field.numericValue(), field.binaryValue(), etc.

			System.out.printf( "%-10d%-15s%-30s\n", docid, url, body );
		}
	}

    //The Test Controller
    public void theBrainOfDatabaseReader()
    {
    	try 
    	{
    		// First, open the directory
    		Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
    		// Then, open an IndexReader to access your index
    		IndexReader indexReader = DirectoryReader.open( dir );
    		
    		
    		//getNumberOfDocuments(String pathIndex)
    		int noOfDocs = getNumberOfDocuments(indexReader);
    		System.out.println("theBrainOfDatabaseReader() noOfDocs:"+noOfDocs);

    		//getDocno( IndexReader index, String fieldName, int url )
    		String elen = getDocno(indexReader, "elen", 0);
    		System.out.println("theBrainOfDatabaseReader() elen:"+elen);
    		
    		//findByDocno( IndexReader indexReader, String fieldDocno, String docno )
    		int url = findByDocno(indexReader, "url", ""+27037803);
    		System.out.println("theBrainOfDatabaseReader() url:"+url);
    		
    		//readFreqPosting(IndexReader indexReader, String fieldName, String term)
    		readFreqPosting(indexReader, "body", "transferred");
    		
    		//readDocLength(IndexReader indexReader, String fieldName)
    		readDocLength(indexReader, "body");
    		
    		//readPositionPosting(IndexReader indexReader, String fieldName, String term)
    		readPositionPosting(indexReader, "body", "transferred");
    		
    		//readDocVector(IndexReader indexReader, String fieldName, int docid)
    		readDocVector(indexReader, "body", 0);
    		
    		//readCorpusStats(IndexReader indexReader, String fieldName, String term)
    		readCorpusStats(indexReader, "body", "transferred");
    		
    		//readStoredDocField(IndexReader indexReader)
    		readStoredDocField(indexReader, 20);
    		
    		
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
		LuceneDatabaseReader objLuceneDatabaseReader = new LuceneDatabaseReader();
		objLuceneDatabaseReader.theBrainOfDatabaseReader();
	}	
}
