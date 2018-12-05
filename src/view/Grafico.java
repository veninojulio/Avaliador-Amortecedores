package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import model.bean.Carro;
import model.bean.Testes;
import model.bean.Valores;
import model.dao.CarroDAO;
import model.dao.TestesDAO;
import model.dao.ValoresDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author Venino-julio
 */
public class Grafico extends javax.swing.JFrame {

    static int enterHexa = 0x0d;
    static int enterHexa2 = 0x0a;
    final int tam = 100;
    private String res[] = new String[tam];
    private String res2[] = new String[tam];

    ChartPanel chartPanel;
    Thread tr1, tr2, buff;
    SerialPort serialPort;
    int contadorEntradas = 0;
    boolean verif = true;
    int vales[] = new int[tam / 2];
    int picos[] = new int[tam / 2];
    Double x, a, b, c;
    String ligaPacote = "P1";
    String desligaPacote = "P0";
    String ativaZeramento = "Z1";
    String desativaZeramento = "Z0";
    String reset = "$";
    String ligaSolenoide = "M3";
    String desligaSaidas = "#";
    String setaVelocidadeAlta = "S10";

    int ponVir[] = {0, 5, 10, 14, 18};
    int valores[] = new int[5];
    boolean desligaSolenoide = false;
    //int instant, zero = 0;
    boolean portOpen = false;
    String nome = "-", carro = "-";
    static String retornoNome, retornoCarro;
    boolean entradasSalvas = true;
    private int idCarro = 0;
    final String avaliador;
    Double valorMaximoE = 0.0, valorMaximoD = 0.0, valorMinimoE = 10000.0, valorMinimoD = 10000.0;
    boolean portaExiste = false;
    Double coeficienteE = 1.0,coeficienteD = 1.0;
    
    public Grafico(String nomeAvaliador) {
        super("Avaliador de amortecedores");
        
        initComponents();
        setSize();
        GraficoZerado();
        avaliador = nomeAvaliador;

        for (int i = 0; i < picos.length; i++) {
            vales[i] = -1;
            picos[i] = -1;
        }
        this.pack();
    }

    public void teste() {
        System.out.println("ok");
    }

    public void setRetornoNome(String retornoNomex) {
        retornoNome = retornoNomex;
    }

    public void setNomeBD(String nomee) {
        nome = nomee;
    }

    public void setCarroBD(String carroo) {
        carro = carroo;
    }

    public void setIdTeste(int idCarroo) {
        idCarro = idCarroo;
        plotHistorico();
    }

    public void setRetornoCarro(String retornoCarrox) {
        retornoCarro = retornoCarrox;

    }

    public void carregarHistorico() {

        //int id;
        List<Integer> id = new ArrayList<>();

        Testes t = new Testes();
        Valores valores = new Valores();

        TestesDAO dao = new TestesDAO();

        id = dao.readMaiorID(retornoNome, retornoCarro);

        //if(id != 0){
        if (!id.isEmpty()) {

            System.out.println(id);

            if (id.size() > 1) {
                //chamar a outra tela
                new VariosTestes(this, retornoNome, retornoCarro).setVisible(true);
                idCarro = id.get(0);
                System.out.println("if mais de um carro");
            } else {
                idCarro = id.get(0);
                System.out.println("else so um carro");
                plotHistorico();
                setMM();
            }

        } else {
            JOptionPane.showMessageDialog(null, "Este carro nao existe no banco de dados");
            new transicao(this).setVisible(true);
        }
    }

    public void plotHistorico() {
        //preencherTxts(nome,carro,placa,ano,montadora,km,avaliador,data);
        ValoresDAO daoValores = new ValoresDAO();
        int contador = 0;
        for (Iterator<Valores> it = daoValores.vetorValores(idCarro, "E").iterator(); it.hasNext() && contador < tam; contador++) {
            Valores p = it.next();
            res[contador] = Integer.toString(p.getValor());
        }
        contador = 0;
        for (Iterator<Valores> it = daoValores.vetorValores(idCarro, "D").iterator(); it.hasNext() && contador < tam; contador++) {
            Valores p = it.next();
            res2[contador] = Integer.toString(p.getValor());
        }
        System.out.println(idCarro);
        try {

            plotarGrafico(painelPrincipal, res,"Lado Esquerdo");
            plotarGrafico(painelPrincipal2, res2,"Lado Direito");
            idCarro = 0;
        } catch (FileNotFoundException | InterruptedException ex) {
            Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
        }
        String date;
        TestesDAO tdao = new TestesDAO();
        date = tdao.readForIdCarro(retornoNome, retornoCarro);
        //retornoNome, retornoCarro
        CarroDAO cdao = new CarroDAO();
        Carro c = cdao.readAll(retornoNome, retornoCarro);
        String placa, ano, montadora, km;
        placa = c.getPlaca();
        ano = "" + c.getAno();
        montadora = c.getMontadora();
        km = "" + c.getKm();

        preencherTxts(retornoNome, retornoCarro, placa, ano, montadora, km, avaliador, date);

    }

    
    public void setSize(){
        //[66,244,107]
        int a;
        //Color.HSBtoRGB(66,244,107)
        //jButtonComecar.setBackground(a);
        //jButtonComecar.setBackground(Color.BLUE);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int i = (int)(d.width*0.7);
        int j = (int)(d.height*0.7);
        setSize(i,j);
        System.out.println("Screen width = " + d.width);
        System.out.println("Screen height = " + d.height);
    }

    public void preencher() {
        for (int i = 0; i < res.length; i++) {
            res[i] = "0";
            res2[i] = "0";
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButtonHistorico = new javax.swing.JButton();
        jButtonParar = new javax.swing.JButton();
        jButtonComecar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        nomeClienteTxt = new javax.swing.JLabel();
        carroTxt = new javax.swing.JLabel();
        placaTxt = new javax.swing.JLabel();
        anoTxt = new javax.swing.JLabel();
        montadoraTxt = new javax.swing.JLabel();
        kmTxt = new javax.swing.JLabel();
        avaliadorTxt = new javax.swing.JLabel();
        dataTxt = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        PesoE = new javax.swing.JLabel();
        PesoD = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        coeficienteETXT = new javax.swing.JTextField();
        coeficienteDTXT = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jButtonZerarGrafico = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        painelPrincipal = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        MaximoE = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        MinimoE = new javax.swing.JLabel();
        painelPrincipal2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        MaximoD = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        MinimoD = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        jButton2.setText("Selecionar carro");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton1.setText("Cadastrar Carro");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButtonHistorico.setText("Historico");
        jButtonHistorico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHistoricoActionPerformed(evt);
            }
        });

        jButtonParar.setBackground(new java.awt.Color(193, 9, 9));
        jButtonParar.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        jButtonParar.setForeground(new java.awt.Color(252, 252, 252));
        jButtonParar.setText("parar");
        jButtonParar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPararActionPerformed(evt);
            }
        });

        jButtonComecar.setBackground(new java.awt.Color(66, 244, 107));
        jButtonComecar.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jButtonComecar.setText("Começar");
        jButtonComecar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonComecarActionPerformed(evt);
            }
        });

        nomeClienteTxt.setText("nome cliente");

        carroTxt.setText("carro");

        placaTxt.setText("placa");

        anoTxt.setText("ano");

        montadoraTxt.setText("montadora");

        kmTxt.setText("km");

        avaliadorTxt.setText("avaliador");

        dataTxt.setText("data");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(nomeClienteTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(carroTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(placaTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(anoTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(montadoraTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                    .addComponent(kmTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(avaliadorTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dataTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(119, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(nomeClienteTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(carroTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(placaTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(anoTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(montadoraTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(kmTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(avaliadorTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataTxt)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        jButton3.setText("Testes");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Peso atual");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        PesoE.setText("Peso E");

        PesoD.setText("Peso D");

        jButton5.setText("ok");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        coeficienteETXT.setText("0");

        coeficienteDTXT.setText("0");

        jButton6.setText("Calibrar");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButtonZerarGrafico.setText("Zerar grafico");
        jButtonZerarGrafico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonZerarGraficoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(PesoE, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PesoD, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(62, 62, 62)
                                        .addComponent(coeficienteETXT, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(26, 26, 26)
                                        .addComponent(coeficienteDTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jButton4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton6)
                                        .addGap(33, 33, 33)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonComecar, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(122, 122, 122)
                        .addComponent(jButtonParar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonHistorico)
                        .addGap(37, 37, 37)
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonZerarGrafico)
                        .addGap(153, 153, 153)))
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonHistorico)
                            .addComponent(jButton5)
                            .addComponent(jButtonZerarGrafico))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jButton1))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jButton2)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButtonComecar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButtonParar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton3)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton4)
                            .addComponent(jButton6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(PesoE)
                            .addComponent(PesoD)
                            .addComponent(coeficienteETXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(coeficienteDTXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 52, Short.MAX_VALUE))))
        );

        getContentPane().add(jPanel1);
        jPanel1.setBounds(0, 449, 1120, 250);

        painelPrincipal.setBackground(new java.awt.Color(153, 153, 153));
        painelPrincipal.setBorder(javax.swing.BorderFactory.createMatteBorder(10, 10, 10, 10, new java.awt.Color(0, 0, 0)));
        painelPrincipal.setMaximumSize(new java.awt.Dimension(460, 370));
        painelPrincipal.setLayout(new java.awt.BorderLayout());

        jLabel1.setText("Valor Maximo:");

        MaximoE.setText("MaximoE");

        jLabel2.setText("Valor Minimo:");

        MinimoE.setText("MinimoE");

        painelPrincipal2.setBackground(new java.awt.Color(200, 20, 200));
        painelPrincipal2.setBorder(javax.swing.BorderFactory.createMatteBorder(10, 10, 10, 10, new java.awt.Color(0, 0, 0)));
        painelPrincipal2.setLayout(new java.awt.BorderLayout());

        jLabel3.setText("Valor Maximo:");

        MaximoD.setText("MaximoD");

        jLabel4.setText("Valor Minimo:");

        MinimoD.setText("MinimoD");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(MaximoE)
                        .addGap(33, 33, 33)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(MinimoE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(painelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(1, 1, 1)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(MaximoD)
                        .addGap(37, 37, 37)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(MinimoD)
                        .addContainerGap(240, Short.MAX_VALUE))
                    .addComponent(painelPrincipal2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelPrincipal2, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(painelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(MaximoE)
                    .addComponent(jLabel2)
                    .addComponent(MinimoE)
                    .addComponent(jLabel3)
                    .addComponent(MaximoD)
                    .addComponent(jLabel4)
                    .addComponent(MinimoD))
                .addGap(36, 36, 36))
        );

        getContentPane().add(jPanel3);
        jPanel3.setBounds(0, 0, 1140, 0);

        jMenu1.setText("Opções");

        jMenuItem1.setText("Amentar velocidade");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Diminuir velocidade");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem3.setText("Zeramento");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem4.setText("Desativar zeramento");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuItem5.setText("Ativar Solenoide");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

        jMenuItem6.setText("Desativar Solenoide");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem6);

        jMenuItem7.setText("Reset");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem7);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Configurações");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public void salvarEntradas() {
        //int id;

        Testes t = new Testes();
        Valores valores = new Valores();

        TestesDAO daoTestes = new TestesDAO();
        ValoresDAO daoValores = new ValoresDAO();

        t.setNome(nome);
        t.setCarro(carro);
        t.setAvaliador(avaliador);

        daoTestes.create(t);
        List<Integer> ids = new ArrayList<>();
        ids = daoTestes.readMaiorID(nome, carro);

        //lado esquerdo -> res
        for (int i = 0; i < res.length; i++) {
            valores.setValor(Integer.parseInt(res[i]));
            valores.setReferencia(ids.get(ids.size() - 1));
            valores.setLado("E");
            daoValores.create(valores);
        }

        //lado direito -> res2
        for (int i = 0; i < res2.length; i++) {
            valores.setValor(Integer.parseInt(res2[i]));
            valores.setReferencia(ids.get(ids.size() - 1));
            valores.setLado("D");
            daoValores.create(valores);
        }

//        txtNome.setText("nome");
//        txtCarro.setText("carro");
        nome = "-";
        carro = "-";
        zerarTxts();

    }

    public void zerarTxts() {

        nomeClienteTxt.setText("");
        carroTxt.setText("");
        placaTxt.setText("");
        anoTxt.setText("");
        montadoraTxt.setText("");
        kmTxt.setText("");
        avaliadorTxt.setText("");
        dataTxt.setText("");

    }

    public void preencherTxts(String nome, String carro, String placa, String ano, String montadora, String km, String avaliador, String data) {

        nomeClienteTxt.setText("Cliente:  " + nome);
        carroTxt.setText("Carro:  " + carro);
        placaTxt.setText("Placa:  " + placa);
        anoTxt.setText("Ano:  " + ano);
        montadoraTxt.setText("Montadora:  " + montadora);
        kmTxt.setText("Kilometragem:  " + km);
        avaliadorTxt.setText("Responsavel:  " + avaliador);
        dataTxt.setText("Data:  " + data);

    }

    private void jButtonComecarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonComecarActionPerformed
        if (!portOpen) {
            try {
                zerarMaximoMinimos();
                abrirCom();

                pegarBuffer();
                //entradasEsquerda(painelPrincipal);
                //entradasDireita(painelPrincipal2);

            } catch (InterruptedException | FileNotFoundException | SerialPortException ex) {
                Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        entradasSalvas = true;
    }//GEN-LAST:event_jButtonComecarActionPerformed

    private void jButtonPararActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPararActionPerformed
        //botao parar

        tr2.stop();
        tr1.stop();
        buff.stop();

        try {

            System.out.println("Port closed: " + serialPort.closePort());
            portOpen = false;
        } catch (SerialPortException ex) {
            Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_jButtonPararActionPerformed

    private void jButtonZerarGraficoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonZerarGraficoActionPerformed
        GraficoZerado();

    }//GEN-LAST:event_jButtonZerarGraficoActionPerformed

    private void jButtonHistoricoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHistoricoActionPerformed

        new transicao(this).setVisible(true);

    }//GEN-LAST:event_jButtonHistoricoActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        new CadastrarCarro().setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        new SelecionarCarro(this).setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        printPicosVales(res);
        testes();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        String convertido = "";
        double vetor[] = new double[5];

        try {
            abrirCom();
            byte[] buffer1 = null;
            //try {
            String teste = null;
            do {
                buffer1 = serialPort.readBytes(1);
                teste = new String(buffer1);
            } while (!teste.equals("["));

            buffer1 = serialPort.readBytes(23);

//            } catch (SerialPortException ex) {
//                Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
//            }
            convertido = new String(buffer1);
            convertido = "[" + convertido;

            for (int k = 0, j = 0; k < valores.length - 1; k++) {
                //vetor[k] = Integer.parseInt(convertido.substring(ponVir[j] + 1, ponVir[++j]));
                vetor[k] = Double.parseDouble(convertido.substring(ponVir[j] + 1, ponVir[++j]));
            }
            System.out.println("Port closed: " + serialPort.closePort());
        } catch (InterruptedException | FileNotFoundException | SerialPortException ex) {
            Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
        }

        portOpen = false;
        
        vetor[0] = vetor[0]* coeficienteE;
        vetor[1] = vetor[1]* coeficienteD;
        int aaa = (int) (vetor[0]);
        PesoE.setText("" + vetor[0]);
        PesoD.setText("" + vetor[1]);

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        //if (!serialPort.isOpened() && !buff.isAlive()) {

            setMM();

        //}
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        enviarParaSerial(ligaPacote, serialPort);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        if(Double.parseDouble(PesoE.getText()) != 0.0 ){           
            coeficienteE = (Double.parseDouble(coeficienteETXT.getText())/Double.parseDouble(PesoE.getText()));
        }
        
        if(Double.parseDouble(PesoD.getText()) != 0.0){
            coeficienteD = (Double.parseDouble(coeficienteDTXT.getText())/Double.parseDouble(PesoD.getText()));
        }
        
        System.out.println(coeficienteE);
        System.out.println(coeficienteD);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        enviarParaSerial(desligaPacote, serialPort);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        enviarParaSerial(ativaZeramento, serialPort);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        enviarParaSerial(desativaZeramento, serialPort);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        enviarParaSerial(ligaSolenoide, serialPort);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        enviarParaSerial(desligaSaidas, serialPort);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        enviarParaSerial(reset, serialPort);
    }//GEN-LAST:event_jMenuItem7ActionPerformed
    public void GraficoZerado() {
        preencher();
        zerarTxts();
        zerarMaximoMinimos();

        try {
            plotarGrafico(painelPrincipal, res,"Lado Esquerdo");
            plotarGrafico(painelPrincipal2, res2,"Lado Direito");
        } catch (FileNotFoundException | InterruptedException ex) {
            Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void zerarMaximoMinimos() {
        MaximoE.setText("");
        MaximoD.setText("");
        MinimoE.setText("");
        MinimoD.setText("");

        valorMaximoE = 0.0;
        valorMaximoD = 0.0;
        valorMinimoE = 10000.0;
        valorMinimoD = 10000.0;
    }

    public void setMM() {
        for (int i = 0; i < res.length; i++) {

            if (Double.parseDouble(res[i]) > valorMaximoE) {
                valorMaximoE = Double.parseDouble(res[i]);
            }
            if (Double.parseDouble(res[i]) < valorMinimoE) {
                valorMinimoE = Double.parseDouble(res[i]);
            }
            if (Double.parseDouble(res2[i]) > valorMaximoD) {
                valorMaximoD = Double.parseDouble(res2[i]);
            }

            if (Double.parseDouble(res2[i]) < valorMinimoD) {
                valorMinimoD = Double.parseDouble(res2[i]);
            }
        }

        MaximoE.setText("" + valorMaximoE);
        MaximoD.setText("" + valorMaximoD);
        MinimoE.setText("" + valorMinimoE);
        MinimoD.setText("" + valorMinimoD);
    }

    private void plotarGrafico(JPanel painel, String valores[],String nomeGrafico) throws FileNotFoundException, InterruptedException {
        JFreeChart graf = getChart("x", valores,nomeGrafico);

        //ChartPanel chart = new ChartPanel(painelPrincipal);
        ChartPanel chart = new ChartPanel(graf);

        atualizarPanelGrafico(chart, painel);

    }

    public void atualizarPanelGrafico(ChartPanel chart, JPanel painel) throws InterruptedException {

        if (Objects.nonNull(chartPanel)) {
            painel.remove(chartPanel);
        }

        chartPanel = chart;
        painel.add(chartPanel);

        painel.revalidate();
    }

    public synchronized JFreeChart getChart(String title, String valores[],String nomeGrafico) throws FileNotFoundException, InterruptedException {

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        Double x;
        String segundo;

        for (int j = 0; j < (valores.length - 1); j++) {

            x = Double.parseDouble(valores[j]);

            segundo = j + "s";

            ds.addValue(x, "valor", segundo);

        }

        JFreeChart grafico2 = ChartFactory.createLineChart(nomeGrafico, "Tempo",
                "Peso", ds, PlotOrientation.VERTICAL, true, true, false);
        return grafico2;
    }

    public void abrirCom() throws InterruptedException, FileNotFoundException, SerialPortException {

        String[] portNames = SerialPortList.getPortNames();
        for (String portName : portNames) {
            System.out.println(portName);
        }

        serialPort = new SerialPort(portNames[0]);
        portOpen = true;
        portaExiste = true;

        try {
            System.out.println("Port opened: " + serialPort.openPort());
            System.out.println("Params setted: " + serialPort.setParams(230400, 8, 1, 0));
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    public void enviarParaSerial(String comando, SerialPort serialPort) {
        if ( portaExiste) {
            if (serialPort.isOpened() ) {

                try {
                    System.out.println("write: " + comando + serialPort.writeString(comando));

                    serialPort.writeInt(enterHexa);
                    serialPort.writeInt(enterHexa2);
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
            else{

            try {
                abrirCom();

                System.out.println("write: " + comando + serialPort.writeString(comando));

                System.out.println("Port closed: " + serialPort.closePort());
            } catch (InterruptedException | FileNotFoundException | SerialPortException ex) {
                Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
            }

            portOpen = false;

            }
        }
        if ( !portaExiste) {
            try {
                abrirCom();

                System.out.println("write: " + comando + serialPort.writeString(comando));

                System.out.println("Port closed: " + serialPort.closePort());
            } catch (InterruptedException | FileNotFoundException | SerialPortException ex) {
                Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
            }

            portOpen = false;
            
        }
    }

    public int[] cortarVetor(int array[]) {
        int valorInicial = array[0];
        int fim = array.length - 1;

        int pos = 1;
        for (; pos < array.length - 1; pos++) {
            if (array[pos] == valorInicial) {
                if (array[pos + 1] == valorInicial) {
                    fim = pos;
                    break;
                }
            }
        }
        int vetorPronto[] = new int[fim + 1];
        for (int t = 0; t < vetorPronto.length; t++) {
            vetorPronto[t] = array[t];
        }
        return vetorPronto;
    }

    public void pegarBuffer() throws InterruptedException, FileNotFoundException, SerialPortException {
        Runnable buffer = new Runnable() {
            public void run() {

                int contador = 0;
                String convertido = "";

                while (contador < tam) {

//                    try {
//                        Thread.sleep(11);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                    byte[] buffer1 = null;
                    try {
                        String teste = null;
                        do {
                            buffer1 = serialPort.readBytes(1);
                            teste = new String(buffer1);
                        } while (!teste.equals("["));

                        buffer1 = serialPort.readBytes(23);
                    } catch (SerialPortException ex) {
                        Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    convertido = new String(buffer1);
                    convertido = "[" + convertido;

                    if (desligaSolenoide) {
                        enviarParaSerial(desligaSaidas, serialPort);
                        desligaSolenoide = false;
                    }
                    if (contador == 10) {
                        enviarParaSerial(ligaSolenoide, serialPort);
                        desligaSolenoide = true;
                    }

                    try {
                        entradasEsquerda(painelPrincipal, convertido, contador);
                        entradasDireita(painelPrincipal2, convertido, contador);
                    } catch (InterruptedException | FileNotFoundException | SerialPortException ex) {
                        Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    contador++;
                }
            }
        };
        buff = new Thread(buffer);

        buff.start();
    }

    public void fecharCOM(int contador) {
        if (serialPort.isOpened() && contador == tam - 1) {
            if (verif) {
                try {

                    System.out.println("Port closed: " + serialPort.closePort());
                    portOpen = false;
                    verif = false;
                    //escreverTXT(res);
                    printPicosVales(res);
                    //testes();
                    //setMM();
                    if (nome != "-" && carro != "-" && entradasSalvas) {
                        entradasSalvas = false;
                        salvarEntradas();
                    }
                } catch (SerialPortException ex) {
                    Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                verif = true;
            }
        }
        
        // Espera indefinidamente pelo final de processamento da thread 
        try { buff.join(); // Thread terminou seu processamento
        
        setMM();
        } catch (InterruptedException e) { // Thread foi interrompida por alguma excessão lançada
        }
        
//        if (!serialPort.isOpened() && !buff.isAlive()) {
//
//            setMM();
//
//        }
    }

    public void entradasEsquerda(JPanel painel, String convertido, int i1) throws InterruptedException, FileNotFoundException, SerialPortException {

        Runnable threadLer = new Runnable() {
            public void run() {

                int formValor;

                for (int k = 0, j = 0; k < valores.length - 1; k++) {
                    valores[k] = Integer.parseInt(convertido.substring(ponVir[j] + 1, ponVir[++j]));
                }

                formValor = valores[0];
//                instant = formValor;
//                formValor = formValor - zero;

                formValor = (int) (formValor * coeficienteE);
                res[i1] = Integer.toString(formValor);
                //System.out.println("E->" + formValor);
                try {
                    plotarGrafico(painel, res,"Lado Esquerdo");
                    //chartPanel = geradorDeGrafico.plotarGrafico(painel,res,chartPanel);

                } catch (FileNotFoundException | InterruptedException ex) {
                    Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
                }
                fecharCOM(i1);
            }
        };
        tr1 = new Thread(threadLer);
        tr1.start();
    }

    public void entradasDireita(JPanel painel, String convertido, int i2) throws InterruptedException, FileNotFoundException, SerialPortException {
        Runnable threadLer = new Runnable() {
            public void run() {

                int formValor2;

                for (int k = 0, j = 0; k < valores.length - 1; k++) {
                    valores[k] = Integer.parseInt(convertido.substring(ponVir[j] + 1, ponVir[++j]));
                    //System.out.println(valores[i]);
                }
                formValor2 = valores[1];
//                    jjj = (valores[0])*(70);
//                    jjj = jjj/100;
//                    jjj = jjj-1100;
                formValor2 = (int) (formValor2 * coeficienteD);
                res2[i2] = Integer.toString(formValor2);
                //System.out.println("E2->" + formValor2);
                try {
                    plotarGrafico(painel, res2,"Lado Direito");
                    //geradorDeGrafico.plotarGrafico(painel,res2,chartPanel);
                } catch (FileNotFoundException | InterruptedException ex) {
                    Logger.getLogger(Grafico.class.getName()).log(Level.SEVERE, null, ex);
                }

                fecharCOM(i2);
            }
        };
        tr2 = new Thread(threadLer);
        tr2.start();
    }

    public void escreverTXT(String res[]) throws IOException {
        FileWriter arq = new FileWriter("pesos.txt");
        PrintWriter gravarArq = new PrintWriter(arq);
        //gravarArq.printf("%n");        
        for (int i = 0; i < res.length; i++) {
            gravarArq.println(res[i]);
        }
        arq.close();
    }

    public void testes() {
        System.out.println("Entrou testes");
        int qtdCiclos = 0;
        Double relAmplitudes[] = new Double[picos.length];
        Double relMinimos[] = new Double[picos.length];
        int amplitudes[] = new int[picos.length];

        for (int i = 0; i < picos.length; i++) {
            if (picos[i] != -1) {
                System.out.println("picos:" + picos[i]);
            }
        }
        for (int i = 0; i < vales.length; i++) {
            if (vales[i] != -1) {
                System.out.println("vales:" + vales[i]);
            }
        }

        for (int i = 0; i < picos.length && picos[i] != -1; i++) {
            qtdCiclos = i;
        }

        for (int i = 0; i < picos.length - 1; i++) {
            amplitudes[i] = picos[i] - vales[i + 1];
        }

        System.out.println("metrica 1->relação entre as amplitude: ");
        if (amplitudes[0] == 0) {
            amplitudes[0] = 1;
        }

        System.out.println("amplitude 0: " + amplitudes[0]);
        for (int i = 1; i < amplitudes.length; i++) {

            if (amplitudes[i] != 0) {
                System.out.println("amplitude: " + i + " = " + amplitudes[i]);
                a = (double) amplitudes[0];
                b = (double) amplitudes[i];
                x = b / a;
                x = x * 100;

                relAmplitudes[i - 1] = x;

                System.out.printf("relaçao: %.2f%%%n", x);
            }
        }

        System.out.println("");
        System.out.println("");

        //////////////////////////////////////////////////////////////////////////////////////
        System.out.println("metrica 2->relação entre os minimos: ");
        System.out.println("minimo 0: " + vales[0]);
        //
        //
        for (int i = 1; i < vales.length && vales[i] != vales[0] && vales[i] != -1; i++) {

            System.out.println("vale: " + i + " = " + vales[i]);
            a = (double) vales[0];
            b = (double) vales[i];
            x = b / a;
            x = x * 100;
            relMinimos[i] = x;
            //System.out.printf("relaçao "+x+"%");
            System.out.printf("relaçao: %.2f%%%n", x);
        }

        System.out.println("");
        System.out.println("");

        //////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("metrica 3->quantidade de ciclos: " + qtdCiclos);

        System.out.println("");
        System.out.println("");
        boolean reprovado = true;
//------------------------------------------------------------------
        if (relAmplitudes[0] != null && relAmplitudes[1] != null && relAmplitudes[2] != null) {

            if (qtdCiclos < 4) {
                if (relAmplitudes[0] < 60 && relAmplitudes[1] < 40 && relAmplitudes[2] < 20) {
                    if (relMinimos[0] > 60 && relMinimos[1] > 80 && relMinimos[2] > 90) {
                        System.out.println("carro aprovado");
                        reprovado = false;

                    }
                }
            }
        }
//-------------------------------------------------------------------
        if (reprovado) {
            System.out.println("carro reprovado");
        }
        for (int p = 0; p < relAmplitudes.length; p++) {
            if (relAmplitudes[p] != null) {
                System.out.printf("amplitudes: %d-> %.2f", p, relAmplitudes[p]);
                System.out.println();
            }

        }
        System.out.println("Saiu testes");
    }

    public void printPicosVales(String entradas[]) {

        int pesoOld[] = new int[entradas.length];
        for (int x = 0; x < pesoOld.length; x++) {
            pesoOld[x] = Integer.parseInt(entradas[x]);
        }
        int peso[] = cortarVetor(pesoOld);

        System.out.println(peso.length);
        int i = 0, contVales = 0, contPicos = 0;

        while (i < peso.length - 2) {

            if (peso[i] >= peso[i + 1]) {

                for (; peso[i] >= peso[i + 1] && i < peso.length - 2; i++) {

                }
                if (i != peso.length - 1) {
                    vales[contVales] = peso[i];
                    contVales++;
                    //System.out.println("vale:" + peso[i]);
                }
            } else if (peso[i] <= peso[i + 1]) {

                for (; peso[i] <= peso[i + 1] && i < peso.length - 2; i++) {

                }
                if (i != peso.length - 1) {
                    picos[contPicos] = peso[i];
                    contPicos++;
                    //System.out.println("pico:" + peso[i]);
                }
            }
        }
    }

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Grafico("").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel MaximoD;
    private javax.swing.JLabel MaximoE;
    private javax.swing.JLabel MinimoD;
    private javax.swing.JLabel MinimoE;
    private javax.swing.JLabel PesoD;
    private javax.swing.JLabel PesoE;
    private javax.swing.JLabel anoTxt;
    private javax.swing.JLabel avaliadorTxt;
    private javax.swing.JLabel carroTxt;
    private javax.swing.JTextField coeficienteDTXT;
    private javax.swing.JTextField coeficienteETXT;
    private javax.swing.JLabel dataTxt;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButtonComecar;
    private javax.swing.JButton jButtonHistorico;
    private javax.swing.JButton jButtonParar;
    private javax.swing.JButton jButtonZerarGrafico;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel kmTxt;
    private javax.swing.JLabel montadoraTxt;
    private javax.swing.JLabel nomeClienteTxt;
    private javax.swing.JPanel painelPrincipal;
    private javax.swing.JPanel painelPrincipal2;
    private javax.swing.JLabel placaTxt;
    // End of variables declaration//GEN-END:variables
}
