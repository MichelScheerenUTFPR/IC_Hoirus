/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hoirus;

/**
 *
 * @author Yuri
 */
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jfree.data.category.DefaultCategoryDataset;

public class ConnFactory {

    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");

            Connection connection = DriverManager.getConnection("jdbc:sqlite:banco.db");
            return connection;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void GuardarDados(ArrayList<Dado> dados, String nome) {
        // inserindo registros
        
        try (Connection c = getConnection();Statement stmt = c.createStatement();) {
            stmt.execute("CREATE TABLE IF NOT EXISTS HISTORICO( ID INTEGER PRIMARY KEY AUTOINCREMENT, NOME TEXT, CAPTURA INTEGER , BLUE NUMERIC, GREEN NUMERIC, RED NUMERIC, SINAL NUMERIC, DATA TEXT)");  
          /*
            stmt.execute("CREATE TABLE IF NOT EXISTS HISTORICO( ID INTEGER PRIMARY KEY AUTOINCREMENT, NOME TEXT ,CAPTURA NUM, SINAL NUMERIC, DATA TEXT)");          
            for (int i = 0; i < dataset.getRowCount(); i++) {
                for (int j = 0; j < dataset.getColumnCount(); j++) {
                    stmt.execute("INSERT INTO HISTORICO(NOME, CAPTURA, SINAL,DATA) VALUES ('" + nome + "', " + (j + 1) + " ," + dataset.getValue(i, j) + ", datetime('now','localtime'))");
                    
                }
            }
        */
         for(Dado d: dados){
             stmt.execute("INSERT INTO HISTORICO(NOME, CAPTURA, BLUE, GREEN, RED, SINAL, DATA) VALUES ("
                     + "'" + nome + "', " 
                     + d.captura + " ," 
                     + d.blue + " ," 
                     + d.green + " ," 
                     + d.red + ", "
                     + d.sinal + ", "
                     + "datetime('now','localtime'))");
         }
            

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<Dado> RecuperarDados(String nome) {
        // lendo os registros
        //DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        ArrayList<Dado> dados = new ArrayList<Dado>();
        try {
            PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM HISTORICO WHERE NOME = '" + nome + "'");
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                int rsCaptura = resultSet.getInt("CAPTURA");
                
                double rsBlue = resultSet.getDouble("BLUE");
                double rsGreen = resultSet.getDouble("GREEN");
                double rsRed = resultSet.getDouble("RED");
                double rsSinal = resultSet.getDouble("SINAL");
                dados.add(new Dado(nome,rsCaptura,rsBlue,rsGreen,rsRed,rsSinal));
            }
            return dados;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dados;
    }
    
    public static Map<String,String> ListarDados(){
        Map<String,String> map = new HashMap<String,String>();
        try {
            PreparedStatement stmt = getConnection().prepareStatement("SELECT DISTINCT NOME, DATA FROM HISTORICO ORDER BY ID ASC");
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                String rsNome = resultSet.getString("NOME");
                String rsData = resultSet.getString("DATA");
                map.put(rsNome, rsData);
            }
            return map;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return map;
    }
}
