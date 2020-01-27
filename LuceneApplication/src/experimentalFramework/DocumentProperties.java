package experimentalFramework;

import java.util.ArrayList;

public class DocumentProperties {
	
	int url;
	String titleInfo;
	String abstractInfo;
	
	ArrayList<Integer> eref1;
	ArrayList<Integer> eref2;
	ArrayList<String> entityInfo;
	ArrayList<String> categories;
	
	
	
	public DocumentProperties() {
		super();

		url = 0;
		titleInfo = null;
		abstractInfo = null;
		
		eref1 = new ArrayList<>();
		eref2 = new ArrayList<>();
		entityInfo = new ArrayList<>();
		categories = new ArrayList<>();
	}



	public void display()
	{
		System.out.println("url:"+url);
		System.out.println("titleInfo:"+titleInfo);
		System.out.println("abstractInfo:"+abstractInfo);
		System.out.println("entityInfo:"+entityInfo);
		System.out.println("categories:"+categories);
	}
}
