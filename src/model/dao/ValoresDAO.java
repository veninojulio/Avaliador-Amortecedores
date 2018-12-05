package model.dao;

import connection.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import model.bean.Testes;
import model.bean.Valores;


public class ValoresDAO {

    public boolean checkLogin(String login, String senha) {

        Connection con = ConnectionFactory.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        boolean check = false;
//id valor referencia
        try {

            stmt = con.prepareStatement("SELECT * FROM usuario WHERE login = ? and senha = ?");
            stmt.setString(1, login);
            stmt.setString(2, senha);

            rs = stmt.executeQuery();
        
            
            if (rs.next()) {

                
                check = true;
            }
           

        } catch (SQLException ex) {
            Logger.getLogger(ValoresDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt, rs);
        }

        return check;

    }
    public void create(Valores p) {
        
        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement("INSERT INTO valores (valor,referencia,lado)VALUES(?,?,?)");
            stmt.setInt(1, p.getValor());
            stmt.setInt(2, p.getReferencia());
            stmt.setString(3, p.getLado());
            
            stmt.executeUpdate();

            //JOptionPane.showMessageDialog(null, "Salvo com sucesso!");
        } catch (SQLException ex) {
            System.out.println(ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt);
        }
//search-ms:displayname=Resultados%20da%20Pesquisa%20em%20julio&crumb=location:C%3A%5CUsers%5CMectronix-Technical%5CDocuments%5Cjulio\jdbc
    }
    public List<Valores> vetorValores(int id,String lado) {
        
        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Valores> vetorValores = new ArrayList<>();
        
        

        try {
            stmt = con.prepareStatement("SELECT * FROM valores WHERE referencia = ? AND lado LIKE ?");
            stmt.setInt(1, id);
            stmt.setString(2, lado);
            
            rs = stmt.executeQuery();
            while (rs.next()) {

                Valores valores = new Valores();

                
                valores.setValor(rs.getInt("valor"));
                
                vetorValores.add(valores);
            }

            //JOptionPane.showMessageDialog(null, "Salvo com sucesso!");
        } catch (SQLException ex) {
            System.out.println(ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt);
        }
        return vetorValores;
//search-ms:displayname=Resultados%20da%20Pesquisa%20em%20julio&crumb=location:C%3A%5CUsers%5CMectronix-Technical%5CDocuments%5Cjulio\jdbc
    }

}
