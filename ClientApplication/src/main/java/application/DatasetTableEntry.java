package application;

public class DatasetTableEntry {
	
	private String name;
	private String uploadDate;
	
	public DatasetTableEntry(String name, String uploadDate)
	{
		this.name = name;
		this.uploadDate = uploadDate;
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUploadDate() {
		return uploadDate;
	}
	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}
	
	public String toString()
	{
		return "name: "+name + " date: "+uploadDate;
	}
	
	

}
