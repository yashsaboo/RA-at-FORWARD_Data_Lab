/**GoodReads: 
 *			https://stackoverflow.com/questions/40467591/what-is-the-difference-between-termquery-and-queryparser-in-lucene-6-0
 *			https://lucene.apache.org/core/6_0_0/MIGRATE.html
 **/

package experimentalFramework;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class LuceneSearch extends LuceneDatabaseReader{
	
	//Execute the Query and Display the results
	public void searchQuery(IndexReader indexReader, Query query,  int kInTopKRetrievals) throws IOException
	{
        IndexSearcher searcher = new IndexSearcher(indexReader); //Create a Lucene searcher
        searcher.setSimilarity(new BM25Similarity()); // Lucene's default ranking model is VSM, but it has also implemented a wide variety of retrieval models, such as BM@% retrieval model.
		TopDocs docs = searcher.search( query, kInTopKRetrievals ); //Retrieve the top 'k' results; retrieved results are stored in TopDocs

		System.out.printf( "%-10s%-20s%-10s%s\n", "Rank", "URL", "Score", "Title" );
		int rank = 1;
		for ( ScoreDoc scoreDoc : docs.scoreDocs ) {
			int docid = scoreDoc.doc;
			double score = scoreDoc.score;
			String url = getDocno( indexReader, "url", docid );
			String body = getDocno( indexReader, "body", docid );
			System.out.printf( "%-10d%-20s%-10.4f%s\n", rank++, url, score, body );
		}
	}
	
	//Build a Query for n keywords to check in filedName. 'n' is decided by the size of the ArrayList<String> keyword.
	public void queryUsingBooleanQuery(IndexReader indexReader, ArrayList<String> fieldName, ArrayList<String> keyword, int kInTopKRetrievals) throws IOException
	{
		BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
		for(int i=0; i<fieldName.size();i++)
		{
			Query query = new TermQuery(new Term(fieldName.get(i), keyword.get(i)));
			BooleanClause bc = new BooleanClause(query, BooleanClause.Occur.MUST);
			bqBuilder.add(bc);
		}
		BooleanQuery booleanQuery = bqBuilder.build();
		searchQuery(indexReader, booleanQuery,  kInTopKRetrievals);
	}
	
	//Build a Term Query for a single keyword to check in fieldName.
	public void queryUsingTermQuery(IndexReader indexReader, String fieldName, String keyword, int kInTopKRetrievals) throws IOException
	{
		Query query = new TermQuery(new Term(fieldName, keyword));
		searchQuery(indexReader, query,  kInTopKRetrievals);
	}
	
	//Build a QueryParser for a single keyword to check in fieldName.
	public void queryUsingQueryParser(IndexReader indexReader, String fieldName, String keyword, int kInTopKRetrievals) throws ParseException, IOException
	{
		Analyzer analyzer = getAnalyzer(); //Just like building an index, an Analyzer is required to process the query strings when Query Parser is used
		QueryParser parser = new QueryParser( fieldName, analyzer ); //Query Parser transforms a text string into Lucene's query object
		Query query = parser.parse( keyword ); //Lucene's Query object
		searchQuery(indexReader, query,  kInTopKRetrievals);
	}
	
	public void theBrainOfSearch()
	{
		try 
    	{
    		//First, open the directory
    		Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
    		//Then, open an IndexReader to access your index
    		IndexReader indexReader = DirectoryReader.open( dir );

            //queryUsingQueryParser(IndexReader indexReader, String fieldName, String keyword, int kInTopKRetrievals)
//    		queryUsingQueryParser(indexReader, "body", "transferred", 10);
    		
    		//queryUsingTermQuery(IndexReader indexReader, String fieldName, String keyword, int kInTopKRetrievals)
//    		queryUsingTermQuery(indexReader, "body", "transferred", 10);
    		
    		//queryUsingBooleanQuery(IndexReader indexReader, String fieldName, String keyword, int kInTopKRetrievals)
    		queryUsingBooleanQuery(indexReader,new ArrayList<>(Arrays.asList("body", "body")), new ArrayList<>(Arrays.asList("transferred", "patient")), 10);

            //Remember to close the index and the directory
            indexReader.close();
            dir.close();

        } catch ( Exception e ) {
            e.printStackTrace();
        }
	}

    public static void main( String[] args ) {
    	LuceneSearch objLuceneSearchExample = new LuceneSearch();
    	objLuceneSearchExample.theBrainOfSearch();    	
    }

}

//booleanQuery.clauses().add(bc1);
//booleanQuery.clauses().add(bc2);
//	    .add(query1, BooleanClause.Occur.MUST)
//	    .add(query2, BooleanClause.Occur.MUST)
//	    .build();
//System.out.println(booleanQuery.clauses());		
//BooleanQuery qry = new BooleanQuery(new TermQuery(new Term("isbn", querystr)), BooleanClause.Occur.MUST);
//qry.add(new TermQuery(new Term("isbn", querystr)), BooleanClause.Occur.MUST);
//Query q = new QueryParser(Version.LATEST, "isbn", analyzer).parse(qry.toString());
//
//BooleanQuery bq = new BooleanQuery();
//Query query = qp.parse(str);
//bq.add(query, BooleanClause.Occur.MUST);
//bq.add(new TermQuery(new Term("id", id), BooleanClause.Occur.MUST_NOT);
