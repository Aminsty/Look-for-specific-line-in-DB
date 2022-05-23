package threads;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DbStringSearchWithThreads {
	@SuppressWarnings("unchecked")
	public static void main(String args[]) {
		System.out.println("Entrer la chaine a chercher :");
		Scanner sc = new Scanner(System.in);
		String chaine = sc.next();
		System.out.println("Entrer le nom de la base de données :");
		String dbName = sc.next();
		try {
			String tables[] = new String[10], colonnes[][] = new String[10][10];
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			String urlMysql = "jdbc:mysql://localhost:3306/" + dbName;
			String sLogin = "root";
			String sPass = "";

			Connection conn = DriverManager.getConnection(urlMysql, sLogin, sPass);
			Statement st = conn.createStatement();
			DatabaseMetaData md = conn.getMetaData();
            String[] types = {"TABLE"};
            ResultSet rs = md.getTables(dbName, null, "%", types);

			int i=0, j=1;
            while (rs.next()) {
                tables[i] = rs.getString("TABLE_NAME");
                i++;
            }
			
			int nb;

			for(i=0;tables[i]!=null;i++) {
				j=1;
				String requete = "SELECT * FROM " + tables[i];
				rs = st.executeQuery(requete);
				ResultSetMetaData res = rs.getMetaData();
				int h = res.getColumnCount();
				while(j<h+1) {
					colonnes[i][j-1]=res.getColumnName(j);
					j++;
				}
				j=h;
			}
			nb = i*j;
			Callable<String> taches[] = (Callable<String>[]) new Callable[nb];
			for(int k=0;k<i;k++) {
				String nomTable = tables[k];
				for(int s=0;s<j;s++) {
					String nomCol = colonnes[k][s];
						taches[(k+1)*s] = ()->{
							String emplacement="";
							String requete2 = "SELECT * FROM " + nomTable + " where " + 
						nomCol + " IN ('" + chaine + "')";
							ResultSet re = st.executeQuery(requete2);
							if(re.next()) {
								emplacement = nomTable + "-" + nomCol;
								return emplacement;
							}
							return "";
					};
				}
			}
			List<Callable<String>> listeTaches = new ArrayList<Callable<String>>();
			for(int l=0;l<nb;l++) {
				if(taches[l]==null)
					continue;
				listeTaches.add(taches[l]);
			}
			ExecutorService ex = Executors.newFixedThreadPool(nb);
			List<Future<String>> futures = ex.invokeAll(listeTaches);
			String resultat = "";
			for(Future<String> future : futures) {
				resultat+=future.get();
			}
			System.out.println("La chaine est dans : " + resultat);
			ex.shutdown();
		} catch(ClassNotFoundException e) {
			System.err.println("Erreur : " + e.getMessage());
		} catch(SQLException e) {
			System.err.println("Erreur : " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
