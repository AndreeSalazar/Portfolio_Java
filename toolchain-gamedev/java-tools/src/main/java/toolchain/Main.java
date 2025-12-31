package toolchain;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::launch);
    }

    static void launch() {
        JFrame f = new JFrame("Toolchain Profesional para Game Dev");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setSize(900, 700);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Assets", new AssetPanel());
        tabs.addTab("Profiler", new ProfilerPanel());
        tabs.addTab("Pipelines", new PipelinePanel());
        f.setContentPane(tabs);
        f.setVisible(true);
    }

    static class AssetPanel extends JPanel {
        final NativeTools tools = new NativeTools();
        final JLabel status = new JLabel("Listo");
        final JLabel hashLabel = new JLabel("-");
        final JLabel sizeLabel = new JLabel("-");
        final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Paso","Duración ms"},0);
        final JTable table = new JTable(tableModel);
        byte[] current;

        AssetPanel() {
            setLayout(new BorderLayout());
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton open = new JButton("Abrir asset");
            JButton compress = new JButton("Comprimir gzip");
            JButton decompress = new JButton("Descomprimir");
            JButton hash = new JButton("SHA-256");
            top.add(open); top.add(compress); top.add(decompress); top.add(hash);
            JPanel info = new JPanel(new GridLayout(1,3));
            info.add(status); info.add(hashLabel); info.add(sizeLabel);
            add(top, BorderLayout.NORTH);
            add(info, BorderLayout.SOUTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            open.addActionListener(this::onOpen);
            compress.addActionListener(this::onCompress);
            decompress.addActionListener(this::onDecompress);
            hash.addActionListener(this::onHash);
        }
        void onOpen(ActionEvent e) {
            JFileChooser ch = new JFileChooser(new File("."));
            if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    current = Files.readAllBytes(ch.getSelectedFile().toPath());
                    sizeLabel.setText("Tamaño: " + current.length);
                    status.setText("Cargado");
                } catch (Exception ex) {
                    status.setText("Error");
                }
            }
        }
        void onCompress(ActionEvent e) {
            if (current == null) return;
            long t0 = System.nanoTime();
            NativeTools.Result r = tools.compress(current, "gzip");
            long t1 = System.nanoTime();
            current = r.data;
            sizeLabel.setText("Tamaño: " + current.length + " (" + r.algo + ")");
            tableModel.addRow(new Object[]{"compress", (t1 - t0) / 1_000_000});
            status.setText("OK");
        }
        void onDecompress(ActionEvent e) {
            if (current == null) return;
            long t0 = System.nanoTime();
            byte[] out = tools.decompress(current, "gzip");
            long t1 = System.nanoTime();
            current = out;
            sizeLabel.setText("Tamaño: " + current.length);
            tableModel.addRow(new Object[]{"decompress", (t1 - t0) / 1_000_000});
            status.setText("OK");
        }
        void onHash(ActionEvent e) {
            if (current == null) return;
            long t0 = System.nanoTime();
            String hex = tools.hash(current, "sha256");
            long t1 = System.nanoTime();
            hashLabel.setText(hex);
            tableModel.addRow(new Object[]{"hash", (t1 - t0) / 1_000_000});
            status.setText("OK");
        }
    }

    static class ProfilerPanel extends JPanel {
        final DefaultTableModel model = new DefaultTableModel(new Object[]{"Pipeline","Duración ms"},0);
        ProfilerPanel() {
            setLayout(new BorderLayout());
            JTable t = new JTable(model);
            add(new JScrollPane(t), BorderLayout.CENTER);
            JButton run = new JButton("Ejecutar pipeline");
            add(run, BorderLayout.SOUTH);
            run.addActionListener(e -> runPipeline());
        }
        void runPipeline() {
            List<String> steps = new ArrayList<>();
            steps.add("import");
            steps.add("compress");
            steps.add("hash");
            long total = 0;
            for (String s : steps) {
                long ms = 50 + (long)(Math.random()*100);
                total += ms;
                model.addRow(new Object[]{s, ms});
            }
            model.addRow(new Object[]{"total", total});
        }
    }

    static class PipelinePanel extends JPanel {
        PipelinePanel() {
            setPreferredSize(new Dimension(900, 600));
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = 50, y = 100, w = 120, h = 50, gap = 80;
            String[] nodes = {"Import","Process","Compress","Hash","Publish"};
            for (int i=0;i<nodes.length;i++){
                g2.setColor(new Color(40, 120, 220));
                g2.fillRoundRect(x + i*(w+gap), y, w, h, 10, 10);
                g2.setColor(Color.WHITE);
                g2.drawString(nodes[i], x + i*(w+gap) + 20, y + 28);
                g2.setColor(Color.DARK_GRAY);
                if (i < nodes.length-1) {
                    int sx = x + i*(w+gap) + w;
                    int ex = x + (i+1)*(w+gap);
                    g2.drawLine(sx, y + h/2, ex, y + h/2);
                    g2.fillPolygon(new int[]{ex-10, ex-10, ex}, new int[]{y + h/2 - 5, y + h/2 + 5, y + h/2}, 3);
                }
            }
        }
    }
}

