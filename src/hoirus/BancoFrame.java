/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hoirus;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Yuri
 */
public class BancoFrame extends JFrame {

    JTable table = new JTable();
    DefaultTableModel model = new DefaultTableModel();
    JScrollPane scroll;
    String headers[] = {"Nome", "Data"};
    JFileChooser chooser;
    JButton export;

    public BancoFrame(MainFrame frame) {
        model.setColumnIdentifiers(headers);
        table.setModel(model);
        scroll = new JScrollPane(table);
        export = new JButton("Exportar");

        insert(ConnFactory.ListarDados());
        add(scroll, BorderLayout.CENTER);
        add(export, BorderLayout.SOUTH);
        setSize(800, 600);
        setVisible(true);

        export.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e
            ) {
                String nome = "";
                int column = 0;
                int row = table.getSelectedRow();
                nome = table.getModel().getValueAt(row, column).toString();
                if (!nome.trim().isEmpty()) {
                    frame.alterarDataset(ConnFactory.RecuperarDados(nome));
                    dispose();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e
            ) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseEntered(MouseEvent e
            ) {

            }

            @Override
            public void mouseExited(MouseEvent e
            ) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        }
        );

    }

    public void insert(Map<String, String> data) {
        double norma;
        String txtNorma;
        for (String nome : data.keySet()) {
            model.addRow(new Object[]{
                nome, data.get(nome)
            });

        }
    }
}
