import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ClockApp extends JFrame {
    private JLabel clockLabel, stopwatchLabel;
    private JButton startBtn, stopBtn, resetBtn, closeBtn;
    private Timer clockTimer, stopwatchTimer;
    private long startTime;
    private boolean running = false;

    private AnalogClockPanel analogClockPanel;

    public ClockApp() {
        setTitle("Clock & Stopwatch");
        setSize(500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Digital clock label (no animation)
        clockLabel = new JLabel("", SwingConstants.CENTER);
        clockLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Stopwatch label
        stopwatchLabel = new JLabel("Stopwatch: 00:00:00.000", SwingConstants.CENTER);
        stopwatchLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Button panel
        JPanel panel = new JPanel();
        startBtn = new JButton("Start");
        stopBtn = new JButton("Stop");
        resetBtn = new JButton("Reset");
        closeBtn = new JButton("Close");

        panel.add(startBtn);
        panel.add(stopBtn);
        panel.add(resetBtn);
        panel.add(closeBtn);

        // Analog clock panel (reduced size)
        analogClockPanel = new AnalogClockPanel();

        // Top panel for digital clock and stopwatch
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(clockLabel);
        topPanel.add(stopwatchLabel);

        add(topPanel, BorderLayout.NORTH);
        add(analogClockPanel, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);

        // Clock Timer (updates time and analog clock)
        clockTimer = new Timer(1000, e -> {
            updateClock();
            analogClockPanel.repaint();
        });
        clockTimer.start();

        // Stopwatch Timer
        stopwatchTimer = new Timer(10, e -> {
            long now = System.currentTimeMillis();
            long elapsed = now - startTime;
            stopwatchLabel.setText("Stopwatch: " + formatTime(elapsed));
        });

        // Button actions
        startBtn.addActionListener(e -> {
            if (!running) {
                startTime = System.currentTimeMillis() - getElapsed();
                stopwatchTimer.start();
                running = true;
            }
        });

        stopBtn.addActionListener(e -> {
            stopwatchTimer.stop();
            running = false;
        });

        resetBtn.addActionListener(e -> {
            stopwatchTimer.stop();
            running = false;
            stopwatchLabel.setText("Stopwatch: 00:00:00.000");
        });

        closeBtn.addActionListener(e -> System.exit(0));

        clockLabel.setText("Clock: --:--:--");
    }

    private void updateClock() {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        clockLabel.setText("Clock: " + time);
    }

    private long getElapsed() {
        String[] parts = stopwatchLabel.getText().replace("Stopwatch: ", "").split("[:.]");
        int h = Integer.parseInt(parts[0]), m = Integer.parseInt(parts[1]), s = Integer.parseInt(parts[2]), ms = Integer.parseInt(parts[3]);
        return ((h * 3600 + m * 60 + s) * 1000L + ms);
    }

    private String formatTime(long ms) {
        long h = ms / 3600000, m = (ms / 60000) % 60, s = (ms / 1000) % 60;
        return String.format("%02d:%02d:%02d.%03d", h, m, s, ms % 1000);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClockApp().setVisible(true));
    }
}

// ?? Analog Clock Panel with numbers and smaller size
class AnalogClockPanel extends JPanel {
    public AnalogClockPanel() {
        setPreferredSize(new Dimension(250, 250)); // Slightly smaller clock
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();
        int radius = Math.min(w, h) / 2 - 20;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = w / 2;
        int centerY = h / 2;

        // Clock face
        g2.setColor(Color.WHITE);
        g2.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
        g2.setColor(Color.BLACK);
        g2.drawOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);

        // Draw numbers
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 1; i <= 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int numX = (int) (centerX + Math.cos(angle) * (radius - 25));
            int numY = (int) (centerY + Math.sin(angle) * (radius - 25)) + 5;
            String numStr = String.valueOf(i);
            int strWidth = g2.getFontMetrics().stringWidth(numStr);
            g2.drawString(numStr, numX - strWidth / 2, numY);
        }

        // Ticks
        for (int i = 0; i < 60; i++) {
            double angle = Math.toRadians(i * 6);
            int x1 = (int) (centerX + Math.cos(angle) * (radius - (i % 5 == 0 ? 10 : 5)));
            int y1 = (int) (centerY + Math.sin(angle) * (radius - (i % 5 == 0 ? 10 : 5)));
            int x2 = (int) (centerX + Math.cos(angle) * radius);
            int y2 = (int) (centerY + Math.sin(angle) * radius);
            g2.drawLine(x1, y1, x2, y2);
        }

        // Get current time
        Calendar now = Calendar.getInstance();
        int sec = now.get(Calendar.SECOND);
        int min = now.get(Calendar.MINUTE);
        int hour = now.get(Calendar.HOUR);

        // Angles for hands
        double secAngle = Math.toRadians((sec * 6) - 90);
        double minAngle = Math.toRadians((min * 6 + sec * 0.1) - 90);
        double hourAngle = Math.toRadians((hour * 30 + min * 0.5) - 90);

        // Draw hands
        drawHand(g2, centerX, centerY, secAngle, radius - 20, Color.RED, 2);    // seconds
        drawHand(g2, centerX, centerY, minAngle, radius - 30, Color.BLACK, 4);    // minutes
        drawHand(g2, centerX, centerY, hourAngle, radius - 60, Color.BLACK, 6);   // hours (shortened)

        // Center circle
        g2.setColor(Color.BLACK);
        g2.fillOval(centerX - 4, centerY - 4, 8, 8);
    }

    private void drawHand(Graphics2D g2, int x, int y, double angle, int length, Color color, int thickness) {
        int x2 = (int) (x + Math.cos(angle) * length);
        int y2 = (int) (y + Math.sin(angle) * length);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawLine(x, y, x2, y2);
    }
}
