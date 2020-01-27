package experimentalFramework;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.*;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

public class LuceneBuildIndex extends PubmedParser{
	
	String pathIndex = "src/index";
	
	public Analyzer getAnalyzer()
	{
		// Analyzer specifies options for text processing
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents( String fieldName ) {
            	// Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
//                TokenStreamComponents ts = new TokenStreamComponents( new StandardTokenizer() );
                final StandardTokenizer src = new StandardTokenizer();
                TokenStream tok = new StandardFilter(src);
                
                // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
//                ts = new TokenStreamComponents( ts.getTokenizer(), new LowerCaseFilter( ts.getTokenStream() ) );
                tok = new LowerCaseFilter(tok);
                
                // Step 3: whether to remove stop words
//                ts = new TokenStreamComponents( ts.getTokenizer(), (TokenStream)new StopFilter( ts.getTokenStream(), StandardAnalyzer.ENGLISH_STOP_WORDS_SET ) );
                tok = new StopFilter(tok, StandardAnalyzer.STOP_WORDS_SET);
                                  
                // Step 4: whether to apply stemming
//                tok = new PorterStemFilter(tok);
                // ts = new TokenStreamComponents( ts.getTokenizer(), new KStemFilter( ts.getTokenStream() ) );
                // ts = new TokenStreamComponents( ts.getTokenizer(), new PorterStemFilter( ts.getTokenStream() ) );
                return new TokenStreamComponents(src, tok);
            }
        };
        return analyzer;
	}
	
	public IndexWriterConfig getIndexWriterConfig()
	{
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(getAnalyzer());
        // Note that IndexWriterConfig.OpenMode.CREATE will override the original index in the folder
        indexWriterConfig.setOpenMode( IndexWriterConfig.OpenMode.CREATE );
        
        return indexWriterConfig;
	}
	
	//Field setting for metadata field.
	public FieldType setFiledTypeForMetadata()
	{
		FieldType fieldTypeMetadata = new FieldType();
        fieldTypeMetadata.setOmitNorms( true );
        fieldTypeMetadata.setIndexOptions( IndexOptions.DOCS );
        fieldTypeMetadata.setStored( true );
        fieldTypeMetadata.setTokenized( false );
        fieldTypeMetadata.freeze();
        
		return fieldTypeMetadata;
	}
	
	//Field setting for normal text field.
	public FieldType setFiledTypeForNormalTextField()
	{
		FieldType fieldTypeText = new FieldType();
        fieldTypeText.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS );
        fieldTypeText.setStoreTermVectors( true );
        fieldTypeText.setStoreTermVectorPositions( true );
        fieldTypeText.setTokenized( true );
        fieldTypeText.setStored( true );
        fieldTypeText.freeze();
        
		return fieldTypeText;
	}
	
	//Add a document block to the IndexWriter - url, body, elen
	public void addDocumentBlockToLucene(DocumentProperties dp, IndexWriter indexWriter, FieldType fieldTypeMetadata, FieldType fieldTypeText) throws IOException {
		Document d = new Document();//Create a Document object

		//Add each field to the document with the appropriate field type options
		d.add( new Field( "url", ""+dp.url, fieldTypeMetadata ) );
		d.add( new Field( "body", dp.titleInfo+" "+dp.abstractInfo, fieldTypeText ) );
		d.add( new Field( "elen", ""+dp.entityInfo.size(), fieldTypeText ) );

		indexWriter.addDocument(d); //Add the document to index.
	}
	
	//Iteratively read each document block from the corpus file, create a Document object for the parsed document, and add that Document object by calling addDocument().
	public void addDocuments(PubmedParser objPubmedParser, IndexWriter ixwriter) throws IOException
	{
        FieldType fieldTypeMetadata = setFiledTypeForMetadata(); //This is the field setting for metadata field.
        FieldType fieldTypeText = setFiledTypeForNormalTextField(); //This is the field setting for normal text field.
        
		DocumentProperties dp;
		if(objPubmedParser.filePath==null)
			System.out.println("No file Path");
		else
		{
			while(true)
			{
				if(!objPubmedParser.moreDocumentsLeft())
					break;
				dp = objPubmedParser.getNextDocumentContent();
				if(dp!=null)
				{
					dp.display();
					numdocs++;
					addDocumentBlockToLucene(dp, ixwriter, fieldTypeMetadata, fieldTypeText);
				}
			}
			objPubmedParser.closeReader();
		}
	}

	public void brainOfBuildIndex()
	{
		try {

            Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
           
            IndexWriter ixwriter = new IndexWriter( dir, getIndexWriterConfig() );

            // Add documents
            PubmedParser objPubmedParser = new PubmedParser("src/pubmed.sample");
            addDocuments(objPubmedParser, ixwriter);

            // remember to close both the index writer and the directory
            ixwriter.close();
            dir.close();

        } catch ( Exception e ) {
            e.printStackTrace();
        }
	}

    public static void main( String[] args ) 
    {
    	LuceneBuildIndex obj = new LuceneBuildIndex();
    	obj.brainOfBuildIndex();
    }

}