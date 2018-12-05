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
import javax.swing.JOptionPane;
import model.bean.Carro;
import model.bean.Testes;

/**
 *
 * @author Mectronix-Technical
 */
public class CarroDAO {
    public boolean create(Carro c) {
        
        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;
        boolean cadastrado = true;

        try {
            stmt = con.prepareStatement("INSERT INTO carros (nome_cliente,modelo_carro,placa,ano,montadora,km)VALUES(?,?,?,?,?,?)");
            stmt.setString(1, c.getNomeCliente());
            stmt.setString(2, c.getModeloCarro());
            stmt.setString(3, c.getPlaca());
            stmt.setInt(4, c.getAno());
            stmt.setString(5, c.getMontadora());
            stmt.setInt(6, c.getKm());
            
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Carro salvo com sucesso!");
        } catch (SQLException ex) {
            System.out.println(ex);
            JOptionPane.showMessageDialog(null, "O carro nao pode ser salvo no banco de dados");
            cadastrado = false;
        } finally {
            ConnectionFactory.closeConnection(con, stmt);
        }
        return cadastrado;

    }
    
    public int readMaiorID(String nome,String carro) {
        int id = 0;
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

                //Testes teste = new Testes();

                //teste.setId(rs.getInt("id"));
                id = rs.getInt("id");
                
                //testes.add(teste);
            }
            

        } catch (SQLException ex) {
            Logger.getLogger(TestesDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt, rs);
        }

        return id;

    }
    public Carro readAll(String nome,String carro) {
        
        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;
        ResultSet rs = null;

        Carro c = new Carro();

        try {
            stmt = con.prepareStatement("SELECT * FROM carros WHERE nome_cliente LIKE ? AND modelo_carro LIKE ?");
            stmt.setString(1, nome);
            stmt.setString(2, carro);
            
            rs = stmt.executeQuery();

            while (rs.next()) {

                c.setId(rs.getInt("id"));
                c.setNomeCliente(rs.getString("nome_cliente"));
                c.setModeloCarro(rs.getString("modelo_carro"));
                c.setPlaca(rs.getString("placa"));
                c.setAno(rs.getInt("ano"));
                c.setMontadora(rs.getString("montadora"));
                c.setKm(rs.getInt("km"));
                
            }
            

        } catch (SQLException ex) {
            Logger.getLogger(TestesDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt, rs);
        }

        return c;

    }
   

    public List<Carro> read() {

        Connection con = ConnectionFactory.getConnection();
        
        PreparedStatement stmt = null;
        ResultSet rs = null;

        List<Carro> carros = new ArrayList<>();

        try {
            stmt = con.prepareStatement("SELECT * FROM carros");
            rs = stmt.executeQuery();

            while (rs.next()) {

                
                Carro c = new Carro();

                c.setId(rs.getInt("id"));
                c.setNomeCliente(rs.getString("nome_cliente"));
                c.setModeloCarro(rs.getString("modelo_carro"));
                c.setPlaca(rs.getString("placa"));
                c.setAno(rs.getInt("ano"));
                c.setMontadora(rs.getString("montadora"));
                c.setKm(rs.getInt("km"));
                
                
                carros.add(c);
            }

        } catch (SQLException ex) {
            Logger.getLogger(TestesDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionFactory.closeConnection(con, stmt, rs);
        }

        return carros;

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
