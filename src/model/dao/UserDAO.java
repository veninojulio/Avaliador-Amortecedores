/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import model.bean.Carro;

/**
 *
 * @author Mectronix-Technical
 */
public class UserDAO {
    public boolean read(String nome,String senha) {

        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;
        ResultSet rs = null;

        //List<Carro> carros = new ArrayList<>();

        try {
            stmt = con.prepareStatement("SELECT * FROM users WHERE nome LIKE ? AND senha LIKE ?");
            stmt.setString(1, nome);
            stmt.setString(2, senha);
            rs = stmt.executeQuery();
            
            if(rs.next()){
                return true;
            }
            
            

        } catch (SQLException ex) {
            Logger.getLogger(TestesDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt, rs);
        }
        return false;
    

    }
}
