package source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SummaryCache 
{
	private static Connection con;	
	private static SummaryCache summaryCache;
	private static Statement st;
	private ResultSet rs;

	public static SummaryCache getInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException 
	{
		if(summaryCache == null)
		{
			summaryCache = new SummaryCache();
		}
		return summaryCache;
	}

	private static void createConnection() throws InstantiationException,
	IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		con = DriverManager.getConnection("jdbc:mysql://66.147.244.79:3306/adityapa_nlp_summary","adityapa_nlpyelp","nlpyelp");
		st=con.createStatement();
	}


	public CategorySummaryBean fetchSummaryBean(String businessName) throws SQLException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		CategorySummaryBean catSumBean = null;
		try{
			createConnection();
			rs=st.executeQuery("select * from SummaryCache where Business_Name='"+businessName+"'");
			if(rs.next())
			{
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rs.getBytes(2));
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
				catSumBean = (CategorySummaryBean)objectInputStream.readObject();
				objectInputStream.close();
			}
			rs.close();
			releaseResources();
		}catch(Exception e){
			System.out.println("Exception thrown in fetchSummaryBean: " + e.getMessage());
		}

		return catSumBean;
	}

	public void saveSummaryBean(String businessName, CategorySummaryBean catSumBean) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		try{
			createConnection();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(catSumBean);

			PreparedStatement pst = con.prepareStatement("insert into SummaryCache values(?, ?)");
			pst.setString(1, businessName);
			pst.setBytes(2, byteArrayOutputStream.toByteArray());
			pst.executeUpdate();
			objectOutputStream.close();
			pst.close();
			releaseResources();
		}catch(Exception e){
			System.out.println("Exception thrown in saveSummaryBean: " + e.getMessage());
		}
	}

	public void releaseResources() throws SQLException
	{
		st.close();
		con.close();
	}
}
