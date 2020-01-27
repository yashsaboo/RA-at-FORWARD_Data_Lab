package query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataImporter.TwoRowsHaveSameValueException;

/*
 * All queries will be in lower case
 * Syntax Check 1: Check if the query can be broken into select, from and where clause
 */
public class Query {

	//Checks if the query can be broken down into three components, so basically finds if those keywords are present or not
	private void syntaxCheck1(String query) throws WrongQuerySyntaxException {

		if(!query.contains("select"))
			throw new WrongQuerySyntaxException("Query doesn't have 'select' clause");

		if(!query.contains("from"))
			throw new WrongQuerySyntaxException("Query doesn't have 'from' clause");

		if(!query.contains("where"))
			throw new WrongQuerySyntaxException("Query doesn't have 'where' clause");
	}

	public String substringBetween(String start, String end, String toBePerformedOn)
	{
		return toBePerformedOn.substring(toBePerformedOn.indexOf(start) + start.length() + 1, toBePerformedOn.indexOf(end)); 
	}

	private String[] extractSelectFromWhereIntoArrayList(String query) throws WrongQuerySyntaxException {

		//Basic Syntax Check 1
		syntaxCheck1(query);

		//Trim query
		query = query.trim();

		//Add a semi colon in the end of the string to detect 'where' clause
		if (!query.endsWith("/n"))
			query += "/n";

		//Find select, from, where
		String select = substringBetween("select", "from", query);
		String from = substringBetween("from", "where", query);
		String where = substringBetween("where", "/n", query);

		String[] SPJ = {select, from, where};

		return SPJ;
	}

	private void parseSelectIntoX(String string) throws WrongQuerySyntaxException {

		//Trim the string
		string = string.trim();

		//Split the string with, delimiter
		String[] X = string.split(",");

		//Syntax check - 2: Check if we split each element in X by ".", then [0] should be integer
		for (String str : X) 
		{ 	
//			System.out.println(str); //Debugging
//			System.out.println(str.split(".").length); //Debugging
			try
			{
				int n = (Integer.parseInt(""+str.trim().charAt(0)));
			} 
			catch (NumberFormatException e)
			{
				throw new WrongQuerySyntaxException("Select clause not well-defined");
			}		    	
		}

	}

	private void parseWhereIntoF(String string) {
		// TODO Auto-generated method stub

	}

	private void parseFromIntoBandL(String string) throws WrongQuerySyntaxException {

		//Trim the string
		string = string.trim();

		//Split the string with, delimiter
		String[] From = string.split(",");

		//Parse From into B
		Map<Integer,String> B = new HashMap<>();

		for (String str : From) 
		{ 
			try {
				String[] dummySplit = str.split("as");

				//If that number is already given to some other relation, then syantx is wrong
				if(B.containsKey(Integer.parseInt(dummySplit[1].trim())))
						throw new NumberFormatException();

				B.put(Integer.parseInt(dummySplit[1].trim()), dummySplit[0].trim());
			}
			catch (NumberFormatException e)
			{
				throw new WrongQuerySyntaxException("From clause not well-defined");
			}
		}
		printHashMap(B); //Debugging

		//Using B find L
		List<Integer> L = new ArrayList<>();
		for ( int key : B.keySet() ) {
		    L.add(key);
		}
	}
	
	public void printHashMap(Map<Integer,String> h)
	{
		// using for-each loop for iteration over Map.entrySet() 
        for (Map.Entry<Integer,String> entry : h.entrySet())  
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 
	}

	public void queryParser(String query) throws WrongQuerySyntaxException
	{

		//Convert query to lowercase
		query = query.toLowerCase();

		//Split query into 3 parts = SELECT, FROM, WHERE
		String[] SPJ = extractSelectFromWhereIntoArrayList(query);

		//Parse Select into X
		System.out.println(SPJ[0]);
		parseSelectIntoX(SPJ[0]);

		//Parse From into B and L
		System.out.println(SPJ[1]);
		parseFromIntoBandL(SPJ[1]);

		//Parse Where into F
		System.out.println(SPJ[2]);
		parseWhereIntoF(SPJ[2]);

	}

	public static void main(String[] args) throws WrongQuerySyntaxException {


		String query = "SELECT 1.url, 1.tf, 2.tf, 3.tf\r\n" + 
				"	FROM KD as 1, KD as 2, KD as 3\r\n" + 
				"	WHERE (1.text=x; x\\in \\Omega) AND (2.text=x; x\\in \\Omega) AND (3.text=x; x\\in \\Omega) AND (1.url=2.url) AND (2.url=3.url)";

		Query queryObj1 = new Query();
		queryObj1.queryParser(query);

	}

}
