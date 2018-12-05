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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import model.bean.Testes;


public class TestesDAO {

    public void create(Testes p) {
        
        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;
        String data = "";
        
        GregorianCalendar calendar = new GregorianCalendar();
        String hora = ""+calendar.get(Calendar.HOUR_OF_DAY);
        String dia = ""+calendar.get(Calendar.DAY_OF_MONTH);
        int m = calendar.get(Calendar.MONTH)+1;
        String mes = m+"";
        String ano = ""+calendar.get(Calendar.YEAR);
        String minuto = ""+calendar.get(Calendar.MINUTE);
        
        if(Integer.parseInt(minuto) < 10){
            minuto = "0" + minuto;
        }
        if(Integer.parseInt(hora) < 10){
            hora = "0" + hora;
        }
        if(Integer.parseInt(dia) < 10){
            dia = "0" + dia;
        }
        if(Integer.parseInt(mes) < 10){
            mes = "0" + mes;
        }
        
        data = dia+"/"+mes+"/"+ano+" as "+hora+":"+minuto;
        

        try {
            stmt = con.prepareStatement("INSERT INTO testes (nome,carro,data,avaliador)VALUES(?,?,?,?)");
            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getCarro());
            stmt.setString(3, data);
            stmt.setString(4, p.getAvaliador());
            
            
            stmt.executeUpdate();

            //JOptionPane.showMessageDialog(null, "Salvo com sucesso!");
        } catch (SQLException ex) {
            System.out.println(ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt);
        }

    }
    
    public List<Integer> readMaiorID(String nome,String carro) {
        //int id[];
        List<Integer> id= new ArrayList<>();
        int quantidade = 0;
        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;
        ResultSet rs = null;

        List<Testes> testes = new ArrayList<>();

        try {
            stmt = con.prepareStatement("SELECT id FROM testes WHERE nome LIKE ? AND carro LIKE ?");
            stmt.setString(1, nome);
            stmt.setString(2, carro);
            
            rs = stmt.executeQuery();

            while (rs.next()) {
                

                //id = rs.getInt("id");
                id.add(rs.getInt("id"));
        
            }
            

        } catch (SQLException ex) {
            Logger.getLogger(TestesDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt, rs);
        }

        return id;

    }
    public String readForIdCarro(String nome,String carro) {
        
        String date = "";
        
        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement("SELECT data FROM testes WHERE nome LIKE ? AND carro LIKE ?");
            stmt.setString(1, nome);
            stmt.setString(2, carro);
            
            
            rs = stmt.executeQuery();

            while (rs.next()) {
                
                date = rs.getString("data");
                System.out.println("data do banco"+date);
                
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(TestesDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt, rs);
        }
        return date;
    }
   

    public List<Testes> readForNomeCarro(String nome,String carro) {

        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;
        ResultSet rs = null;

        List<Testes> testes = new ArrayList<>();

        try {
            stmt = con.prepareStatement("SELECT * FROM testes WHERE nome LIKE ? AND carro LIKE ?");
            stmt.setString(1, nome);
            stmt.setString(2, carro);
            
            rs = stmt.executeQuery();

            while (rs.next()) {

                Testes teste = new Testes();

                teste.setId(rs.getInt("id"));
                teste.setNome(rs.getString("nome"));
                teste.setCarro(rs.getString("carro"));
                teste.setData(rs.getString("data"));
                
                testes.add(teste);
            }

        } catch (SQLException ex) {
            Logger.getLogger(TestesDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt, rs);
        }

        return testes;

    }
//    public List<Produto> readForDesc(String desc) {
//
//        Connection con = ConnectionFactory.getConnection();
//        
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//
//        List<Produto> produtos = new ArrayList<>();
//
//        try {
//            stmt = con.prepareStatement("SELECT * FROM produto WHERE descricao LIKE ?");
//            stmt.setString(1, "%"+desc+"%");
//            
//            rs = stmt.executeQuery();
//
//            while (rs.next()) {
//
//                Produto produto = new Produto();
//
//                produto.setId(rs.getInt("id"));
//                produto.setDescricao(rs.getString("descricao"));
//                produto.setQtd(rs.getInt("qtd"));
//                produto.setPreco(rs.getDouble("preco"));
//                produto.setQtdmin(rs.getInt("qtdmin"));
//                produto.setFornecedor(rs.getString("fornecedor"));
//                produtos.add(produto);
//            }
//
//        } catch (SQLException ex) {
//            Logger.getLogger(TestesDAO.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            ConnectionFactory.closeConnection(con, stmt, rs);
//        }
//
//        return produtos;
//
//    }

    public void update(Testes p) {

        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement("UPDATE produto SET nome = ? ,carro = ? WHERE id = ?");
            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getCarro());

            stmt.executeUpdate();

            //JOptionPane.showMessageDialog(null, "Atualizado com sucesso!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar: " + ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt);
        }

    }
    public void delete(Testes p) {

        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement("DELETE FROM testes WHERE id = ?");
            stmt.setInt(1, p.getId());

            stmt.executeUpdate();

            //JOptionPane.showMessageDialog(null, "Excluido com sucesso!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir: " + ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt);
        }

    }

}
