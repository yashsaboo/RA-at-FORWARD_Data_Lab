package dataImporter.storeType;

import java.util.ArrayList;

public interface Store {
	
	public void createAndAddHeader(String fileName, ArrayList<String> columnNames, int columnDatatype);
	
	public void addData(ArrayList<Object> tuples);

}
