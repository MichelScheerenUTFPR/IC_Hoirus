/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hoirus;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

import javax.swing.JMenuItem;
import javax.swing.event.MenuListener;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author Yuri
 */
public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    //definitions
    private DaemonThread myThread = null;
    VideoCapture webSource = null;
    Mat frame = new Mat();
    MatOfByte mem = new MatOfByte();
    int clipX1, clipX2, clipY1, clipY2;
    double[] diferenciador = new double[3];
    DefaultCategoryDataset dataset;
    ArrayList<Dado> dados;

    public MainFrame() {
        initComponents();
        listarCameras();
        initSets();
        initCam();
        desenharGrafico(new DefaultCategoryDataset());

    }

    public void listarCameras() {
        int num = 0;
        VideoCapture auxSource;
        for (int i = 0; i < 5; i++) {
            auxSource = new VideoCapture(i);
            if (auxSource.isOpened()) {
                auxSource.release();

                JMenuItem menuItem = new JMenuItem("Camera " + String.valueOf(i + 1));
                if (i == 0) {
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            webSource.release();
                            webSource = new VideoCapture(0);
                        }
                    });
                }
                if (i == 1) {
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            webSource.release();
                            webSource = new VideoCapture(1);
                        }
                    });
                }
                if (i == 2) {
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            webSource.release();
                            webSource = new VideoCapture(2);
                        }
                    });
                }
                if (i == 3) {
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            webSource.release();
                            webSource = new VideoCapture(3);
                        }
                    });
                }
                if (i == 4) {
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            webSource.release();
                            webSource = new VideoCapture(4);
                        }
                    });
                }
                camMenu.add(menuItem);
            }
        }
    }

    public void initCam() {
        webSource = new VideoCapture(0);
        myThread = new DaemonThread();
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();
    }

    private void initSets() {
        txtX1.setText("0");
        txtX2.setText("0");
        txtY1.setText("0");
        txtY2.setText("0");
        clipX1 = 0;
        clipX2 = 0;
        clipY1 = 0;
        clipY2 = 0;
        cleanTexts();
    }

    public void cleanTexts() {
        lblRed.setText("R: ");
        lblGreen.setText("G: ");
        lblBlue.setText("B: ");
        lblSinal.setText("0");
    }

    public void alterarDataset(ArrayList<Dado> dados) {
        for (Dado d : dados) {
            System.out.println(d.captura);
        }
        this.dados = dados;
        this.dataset = dadosToView(dados);

        desenharGrafico(dataset);
    }

    public DefaultCategoryDataset dadosToView(ArrayList<Dado> dados) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Dado d : dados) {
            dataset.addValue(d.sinal, "Sinal", String.valueOf(d.captura));
        }
        return dataset;
    }

    public void desenharGrafico(DefaultCategoryDataset dataset) {
        JFreeChart lineChart = ChartFactory.createLineChart(
                "Gráfico do Sinal",
                "Capturas", "Sinal",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot lnChart = lineChart.getCategoryPlot();
        lnChart.setRangeGridlinePaint(Color.ORANGE);

        ChartPanel chartPanel = new ChartPanel(lineChart, true);
        chartPanel.setSize(graphPanel.getWidth(), graphPanel.getHeight());
        chartPanel.setVisible(true);

        graphPanel.removeAll();
        graphPanel.add(chartPanel);

        graphPanel.revalidate();
        graphPanel.repaint();

    }

    public static void loadOpenCV_Lib() {
        try {
            // get the model
            String model = System.getProperty("sun.arch.data.model");
            // the path the .dll lib location
            String libraryPath = "opencv/x86/";
            // check if system is 64 or 32
            if (model.equals("64")) {
                libraryPath = "opencv/x64/";
            }
            // set the path
            System.setProperty("java.library.path", libraryPath);
            Field sysPath = ClassLoader.class.getDeclaredField("sys_paths");
            sysPath.setAccessible(true);
            sysPath.set(null, null);
            // load the lib
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class DaemonThread implements Runnable {

        protected volatile boolean runnable = false;

        @Override
        public void run() {
            synchronized (this) {
                while (runnable) {

                    Graphics g = videoPanel.getGraphics();
                    g.setColor(Color.GREEN);
                    g.drawRect(clipX1, clipY1, clipX2 - clipX1, clipY2 - clipY1);
                    if (webSource.grab()) {
                        try {
                            webSource.retrieve(frame);
                            Imgcodecs.imencode(".bmp", frame, mem);
                            Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                            BufferedImage buff = (BufferedImage) im;

                            if (g.drawImage(buff, 0, 0, videoPanel.getWidth(), videoPanel.getHeight(), 0, 0, videoPanel.getWidth(), videoPanel.getHeight(), null)) {
                                if (runnable == false) {
                                    System.out.println("Going to wait()");
                                    this.wait();
                                }
                            }
                        } catch (Exception ex) {
                            //System.out.println("Error");
                        }
                    }
                }
            }
        }
    }

    private void selectArea() {
        clipX1 = Integer.parseInt(txtX1.getText());
        clipX2 = Integer.parseInt(txtX2.getText());
        clipY1 = Integer.parseInt(txtY1.getText());
        clipY2 = Integer.parseInt(txtY2.getText());
    }

    public void paintROI() {
        selectArea();
        Graphics g = videoPanel.getGraphics();
        //g.clearRect(0, 0, videoPanel.getWidth(), videoPanel.getHeight());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        graphPanel = new javax.swing.JPanel();
        videoPanel = new javax.swing.JPanel();
        btnPassadas = new javax.swing.JButton();
        btnXLS = new javax.swing.JButton();
        txtX1 = new javax.swing.JTextField();
        txtX2 = new javax.swing.JTextField();
        txtY1 = new javax.swing.JTextField();
        txtY2 = new javax.swing.JTextField();
        btnAlterar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        lblSinal = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        lblEstado = new javax.swing.JLabel();
        btnCapturar = new javax.swing.JButton();
        btnSelecionar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtTempo = new javax.swing.JTextField();
        sldCapturas = new javax.swing.JSlider();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblNome = new javax.swing.JTextField();
        cbGravar = new javax.swing.JCheckBox();
        btnIniciar = new javax.swing.JButton();
        pbBarra = new javax.swing.JProgressBar();
        jLabel2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        btnResetar = new javax.swing.JButton();
        lblRed = new javax.swing.JLabel();
        lblGreen = new javax.swing.JLabel();
        lblBlue = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        camMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hoirus");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        graphPanel.setBackground(new java.awt.Color(255, 255, 255));
        graphPanel.setPreferredSize(new java.awt.Dimension(500, 480));

        javax.swing.GroupLayout graphPanelLayout = new javax.swing.GroupLayout(graphPanel);
        graphPanel.setLayout(graphPanelLayout);
        graphPanelLayout.setHorizontalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        graphPanelLayout.setVerticalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 460, Short.MAX_VALUE)
        );

        videoPanel.setBackground(new java.awt.Color(51, 51, 51));
        videoPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                videoPanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                videoPanelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout videoPanelLayout = new javax.swing.GroupLayout(videoPanel);
        videoPanel.setLayout(videoPanelLayout);
        videoPanelLayout.setHorizontalGroup(
            videoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        videoPanelLayout.setVerticalGroup(
            videoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        btnPassadas.setText("Análises Passadas");
        btnPassadas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnPassadasMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnPassadasMousePressed(evt);
            }
        });
        btnPassadas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPassadasActionPerformed(evt);
            }
        });

        btnXLS.setText("Gerar Arquivo XLS");
        btnXLS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnXLSMouseClicked(evt);
            }
        });
        btnXLS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXLSActionPerformed(evt);
            }
        });

        txtX1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                txtX1InputMethodTextChanged(evt);
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
        });
        txtX1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtX1ActionPerformed(evt);
            }
        });

        txtX2.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                txtX2InputMethodTextChanged(evt);
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
        });

        txtY1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                txtY1InputMethodTextChanged(evt);
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
        });

        txtY2.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                txtY2InputMethodTextChanged(evt);
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
        });

        btnAlterar.setText("Alterar Manual");
        btnAlterar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAlterarMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnAlterarMousePressed(evt);
            }
        });
        btnAlterar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Sinal:");

        lblSinal.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblSinal.setText("0");

        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel6.setText("Configurações de Diferenciador");

        lblEstado.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblEstado.setText("Esperando");

        btnCapturar.setText("Capturar");
        btnCapturar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCapturarActionPerformed(evt);
            }
        });

        btnSelecionar.setText("Selecionar");
        btnSelecionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelecionarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btnCapturar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSelecionar, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblEstado)))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCapturar, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelecionar, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEstado))
                .addGap(34, 34, 34))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setText("Configurações de Captura");

        jLabel4.setText("Capturas (p/s)");

        jLabel5.setText("Tempo (s)");

        txtTempo.setText("10");
        txtTempo.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtTempo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTempoActionPerformed(evt);
            }
        });

        sldCapturas.setMajorTickSpacing(1);
        sldCapturas.setMaximum(10);
        sldCapturas.setMinimum(1);
        sldCapturas.setMinorTickSpacing(1);
        sldCapturas.setPaintLabels(true);
        sldCapturas.setPaintTicks(true);
        sldCapturas.setValue(1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sldCapturas, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTempo, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel4)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtTempo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(sldCapturas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel7.setText("Configurações de Gravação");

        jLabel9.setText("Nome");

        lblNome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lblNomeActionPerformed(evt);
            }
        });

        cbGravar.setText("Gravar Imagens");
        cbGravar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbGravarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNome, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbGravar)
                        .addGap(47, 47, 47))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(32, 32, 32)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(lblNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbGravar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnIniciar.setText("Iniciar");
        btnIniciar.setEnabled(false);
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });

        jLabel2.setText("Cord. Ini");

        jLabel8.setText("Cord. Fin");

        btnResetar.setText("Resetar Marcação");
        btnResetar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnResetarMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnResetarMousePressed(evt);
            }
        });
        btnResetar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetarActionPerformed(evt);
            }
        });

        lblRed.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        lblRed.setText("R:");

        lblGreen.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        lblGreen.setText("G:");

        lblBlue.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        lblBlue.setText("B:");

        camMenu.setText("Selecionar Camera");
        jMenuBar1.add(camMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnIniciar)
                        .addGap(2, 2, 2)
                        .addComponent(pbBarra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnPassadas, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnXLS, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(videoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(47, 47, 47)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel8))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtX2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtY2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtX1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtY1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAlterar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnResetar, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblSinal, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblRed, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblGreen, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(lblBlue, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(10, 10, 10)))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(videoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(txtX1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtY1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtX2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtY2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnResetar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1)
                                    .addComponent(lblSinal))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblRed)
                                    .addComponent(lblGreen)
                                    .addComponent(lblBlue)))))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnPassadas, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnXLS, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pbBarra, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed

    }//GEN-LAST:event_btnAlterarActionPerformed

    private void btnXLSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXLSActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnXLSActionPerformed

    private void txtX1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtX1ActionPerformed

    }//GEN-LAST:event_txtX1ActionPerformed

    private void lblNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lblNomeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lblNomeActionPerformed

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarActionPerformed
        if (lblNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite um nome para iniciar as capturas!");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                dataset = new DefaultCategoryDataset();
                dados = new ArrayList<Dado>();
                pbBarra.setMaximum(Integer.parseInt(txtTempo.getText()));
                for (int t = 0; t < Integer.parseInt(txtTempo.getText()) * sldCapturas.getValue(); t++) {
                    Mat captura = new Mat();
                    double[] saidaBGR = new double[3];
                    if (clipX1 != 0 && clipX2 != 0) {
                        captura = frame.submat(clipY1, clipY2, clipX1, clipX2);
                    } else {
                        captura = frame.clone();
                    }
                    if (cbGravar.isSelected()) {
                        File diretorio = new File(System.getProperty("user.home") + "/Pictures/Hoirus/" + lblNome.getText());
                        diretorio.mkdirs();
                        Imgcodecs.imwrite(System.getProperty("user.home") + "/Pictures/Hoirus/" + lblNome.getText() + "/" + lblNome.getText() + "_" + t + ".bmp", captura);
                    }
                    for (int i = 0; i < captura.rows(); i++) {
                        for (int j = 0; j < captura.cols(); j++) {
                            double[] bgr = captura.get(i, j);
                            if (i == 0 && j == 0) {
                                saidaBGR[0] = 0;
                                saidaBGR[1] = 0;
                                saidaBGR[2] = 0;
                            }
                            saidaBGR[0] += bgr[0];
                            saidaBGR[1] += bgr[1];
                            saidaBGR[2] += bgr[2];
                        }
                    }
                    for (int i = 0; i < 3; i++) {
                        saidaBGR[i] /= (captura.rows() * captura.cols());
                    }
                    double sinal = Math.sqrt(Math.pow(diferenciador[0] - saidaBGR[0], 2)
                            + Math.pow(diferenciador[1] - saidaBGR[1], 2)
                            + Math.pow(diferenciador[2] - saidaBGR[2], 2));

                    lblSinal.setText(String.valueOf(sinal));
                    lblRed.setText("R: " + String.valueOf(saidaBGR[2]));
                    lblGreen.setText("G: " + String.valueOf(saidaBGR[1]));
                    lblBlue.setText("B: " + String.valueOf(saidaBGR[0]));
                    Dado tempDado = new Dado(lblNome.getText(), t + 1, saidaBGR[0], saidaBGR[1], saidaBGR[2], sinal);

                    dados.add(tempDado);

                    dataset.addValue(sinal, "Sinal", String.valueOf(tempDado.captura));

                    System.out.println(sinal);
                    System.out.println(diferenciador[0] + " " + diferenciador[1] + " " + diferenciador[2]);
                    try {
                        Thread.sleep((int) (1000 / sldCapturas.getValue()));
                        pbBarra.setValue((int) ((t + 1) / sldCapturas.getValue()));
                        desenharGrafico(dataset);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ConnFactory.GuardarDados(dados, lblNome.getText());
                pbBarra.setValue(0);
                cleanTexts();
            }
        }.start();
    }//GEN-LAST:event_btnIniciarActionPerformed

    private void cbGravarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbGravarActionPerformed
    }//GEN-LAST:event_cbGravarActionPerformed

    private void txtTempoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTempoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTempoActionPerformed

    private void videoPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_videoPanelMousePressed
        txtX1.setText(String.valueOf(evt.getX()));
        txtY1.setText(String.valueOf(evt.getY()));        // TODO add your handling code here:
    }//GEN-LAST:event_videoPanelMousePressed

    private void videoPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_videoPanelMouseReleased
        txtX2.setText(String.valueOf(evt.getX()));
        txtY2.setText(String.valueOf(evt.getY()));
        if (Integer.parseInt(txtX2.getText()) < Integer.parseInt(txtX1.getText())) {
            String aux = txtX1.getText();
            txtX1.setText(txtX2.getText());
            txtX2.setText(aux);
        }
        if (Integer.parseInt(txtY2.getText()) < Integer.parseInt(txtY1.getText())) {
            String aux = txtY1.getText();
            txtY1.setText(txtY2.getText());
            txtY2.setText(aux);
        }
        paintROI();
    }//GEN-LAST:event_videoPanelMouseReleased

    private void btnAlterarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAlterarMouseClicked

    }//GEN-LAST:event_btnAlterarMouseClicked

    private void btnCapturarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCapturarActionPerformed
        Mat captura = new Mat();
        if(lblNome.getText().trim().isEmpty()){
            JOptionPane.showMessageDialog(null,"Insira o nome da gravação!");
            return;
        }
        if (clipX1 != 0 && clipX2 != 0) {
            captura = frame.submat(clipY1, clipY2, clipX1, clipX2);
        } else {
            captura = frame.clone();
        }
        File diretorio = new File(System.getProperty("user.home") + "/Pictures/Hoirus/" + lblNome.getText());
        diretorio.mkdirs();
        Imgcodecs.imwrite(System.getProperty("user.home") + "/Pictures/Hoirus/" + lblNome.getText() + "/" + lblNome.getText() + "_diferenciador" + ".bmp", captura);
        for (int i = 0; i < captura.rows(); i++) {
            for (int j = 0; j < captura.cols(); j++) {
                double[] bgr = captura.get(i, j);
                if (i == 0 && j == 0) {
                    diferenciador[0] = 0;
                    diferenciador[1] = 0;
                    diferenciador[2] = 0;
                }
                diferenciador[0] += bgr[0];
                diferenciador[1] += bgr[1];
                diferenciador[2] += bgr[2];

            }
        }
        for (int i = 0; i < 3; i++) {
            diferenciador[i] /= (captura.rows() * captura.cols());
            System.out.println(diferenciador[i]);
        }
        lblEstado.setText("Pronto");
        btnIniciar.setEnabled(true);
    }//GEN-LAST:event_btnCapturarActionPerformed

    private void txtX1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txtX1InputMethodTextChanged
        paintROI();
        System.out.println("oi");
    }//GEN-LAST:event_txtX1InputMethodTextChanged

    private void txtY1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txtY1InputMethodTextChanged

    }//GEN-LAST:event_txtY1InputMethodTextChanged

    private void txtX2InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txtX2InputMethodTextChanged

    }//GEN-LAST:event_txtX2InputMethodTextChanged

    private void txtY2InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txtY2InputMethodTextChanged

    }//GEN-LAST:event_txtY2InputMethodTextChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        webSource.release();
        myThread = null;
    }//GEN-LAST:event_formWindowClosing

    private void btnAlterarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAlterarMousePressed
        selectArea();
        paintROI();
    }//GEN-LAST:event_btnAlterarMousePressed

    private void btnXLSMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnXLSMouseClicked
        File file = selectFile();
        toExcel(dados, file);
    }//GEN-LAST:event_btnXLSMouseClicked

    private void btnPassadasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPassadasActionPerformed

    }//GEN-LAST:event_btnPassadasActionPerformed

    private void btnPassadasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPassadasMouseClicked

    }//GEN-LAST:event_btnPassadasMouseClicked

    private void btnPassadasMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPassadasMousePressed
        new BancoFrame(this);
    }//GEN-LAST:event_btnPassadasMousePressed

    private void btnResetarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnResetarMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnResetarMouseClicked

    private void btnResetarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnResetarMousePressed
        initSets();
        paintROI();
    }//GEN-LAST:event_btnResetarMousePressed

    private void btnResetarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnResetarActionPerformed

    private void btnSelecionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelecionarActionPerformed
        JFileChooser chooser = new JFileChooser();
        BufferedImage img = null;
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "JPG & GIF Images", "jpg", "gif", "bmp");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            int num = 0, red = 0, green = 0, blue = 0;
            try {
                img = ImageIO.read(file);
                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        Color c = new Color(img.getRGB(i, j));
                        num++;
                        red += c.getRed();
                        green += c.getGreen();
                        blue += c.getBlue();
                    }
                }
                diferenciador[2] = red/num;
                diferenciador[1] = green/num;
                diferenciador[0] = blue/num;
                for(int i = 0 ; i < 3 ; i++){
                    System.out.println(diferenciador[i]);
                }
                lblEstado.setText("Pronto");
                btnIniciar.setEnabled(true);
            } catch (Exception e) {
            }
        }        // TODO add your handling code here:
    }//GEN-LAST:event_btnSelecionarActionPerformed

    private File selectFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "JPG & GIF Images", "jpg", "gif", "bmp");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File file = chooser.getSelectedFile();
            File returnFile = new File(file.getAbsolutePath() + ".xls");

            return returnFile;
        }
        return null;
    }

    /*public void toExcel(DefaultCategoryDataset data, File file) {
        try {
            FileWriter excel = new FileWriter(file);
            excel.write("Captura\t");
            excel.write("Sinal\n");
            for (int i = 0; i < data.getRowCount(); i++) {
                for (int j = 0; j < data.getColumnCount(); j++) {
                    //excel.write(String.valueOf(j + 1).replace(".", ",") + "\t");
                    //  excel.write(data.getValue(i, j).toString().replace(".", ",") + "\n");
                    excel.write(String.valueOf(j + 1).replace(".", ",") + "\t");
                    excel.write(data.getValue(i, j).toString().replace(".", ",") + "\n");
                }
                excel.write("\n");
            }
            excel.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
     */
    public void toExcel(ArrayList<Dado> dados, File file) {
        try {
            FileWriter excel = new FileWriter(file);
            excel.write("Captura\t");
            excel.write("Blue\t");
            excel.write("Green\t");
            excel.write("Red\t");
            excel.write("Sinal\n");
            for (Dado d : dados) {
                excel.write(String.valueOf(d.captura).replace(".", ",") + "\t");
                excel.write(String.valueOf(d.blue).replace(".", ",") + "\t");
                excel.write(String.valueOf(d.green).replace(".", ",") + "\t");
                excel.write(String.valueOf(d.red).replace(".", ",") + "\t");
                excel.write(String.valueOf(d.sinal).replace(".", ",") + "\n");
            }
            excel.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                loadOpenCV_Lib();
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnCapturar;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnPassadas;
    private javax.swing.JButton btnResetar;
    private javax.swing.JButton btnSelecionar;
    private javax.swing.JButton btnXLS;
    private javax.swing.JMenu camMenu;
    private javax.swing.JCheckBox cbGravar;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel lblBlue;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblGreen;
    private javax.swing.JTextField lblNome;
    private javax.swing.JLabel lblRed;
    private javax.swing.JLabel lblSinal;
    private javax.swing.JProgressBar pbBarra;
    private javax.swing.JSlider sldCapturas;
    private javax.swing.JTextField txtTempo;
    private javax.swing.JTextField txtX1;
    private javax.swing.JTextField txtX2;
    private javax.swing.JTextField txtY1;
    private javax.swing.JTextField txtY2;
    private javax.swing.JPanel videoPanel;
    // End of variables declaration//GEN-END:variables
}
