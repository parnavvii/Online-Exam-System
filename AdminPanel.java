import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminPanel extends JFrame implements ActionListener {

    JTextField txtQuestion, txtA, txtB, txtC, txtD, txtCorrect;
    JButton btnAdd, btnClear;
    Connection conn;

    // 🎨 Colors
    Color primary = new Color(59, 89, 182);
    Color secondary = new Color(92, 184, 92);
    Color background = new Color(245, 247, 250);
    Color cardColor = new Color(255, 255, 255);

    public AdminPanel() {
        setTitle("🧑‍🏫 Admin Panel - Add Questions");
        setSize(600, 500);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ---------- Header ----------
        JPanel headerPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, primary, getWidth(), getHeight(), secondary);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(600, 70));
        JLabel lblHeader = new JLabel("📘 Add New Question");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(Color.WHITE);
        headerPanel.add(lblHeader);

        // ---------- Form Panel ----------
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(background);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel lblQ = new JLabel("Question:");
        JLabel lblA = new JLabel("Option A:");
        JLabel lblB = new JLabel("Option B:");
        JLabel lblC = new JLabel("Option C:");
        JLabel lblD = new JLabel("Option D:");
        JLabel lblAns = new JLabel("Correct Option (A/B/C/D):");

        lblQ.setFont(labelFont); lblA.setFont(labelFont); lblB.setFont(labelFont);
        lblC.setFont(labelFont); lblD.setFont(labelFont); lblAns.setFont(labelFont);

        txtQuestion = new JTextField();
        txtA = new JTextField();
        txtB = new JTextField();
        txtC = new JTextField();
        txtD = new JTextField();
        txtCorrect = new JTextField();

        txtQuestion.setFont(fieldFont);
        txtA.setFont(fieldFont);
        txtB.setFont(fieldFont);
        txtC.setFont(fieldFont);
        txtD.setFont(fieldFont);
        txtCorrect.setFont(fieldFont);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(lblQ, gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(txtQuestion, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(lblA, gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(txtA, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(lblB, gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(txtB, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(lblC, gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(txtC, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(lblD, gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(txtD, gbc);

        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(lblAns, gbc);
        gbc.gridx = 1; gbc.gridy = 5; formPanel.add(txtCorrect, gbc);

        // ---------- Buttons ----------
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(background);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnAdd = new JButton("➕ Add Question");
        btnClear = new JButton("🧹 Clear");

        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 14));

        btnAdd.setBackground(new Color(59, 89, 182));
        btnAdd.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(255, 99, 71));
        btnClear.setForeground(Color.WHITE);

        btnAdd.setFocusPainted(false);
        btnClear.setFocusPainted(false);

        btnAdd.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnClear.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        btnPanel.add(btnAdd);
        btnPanel.add(btnClear);

        btnAdd.addActionListener(this);
        btnClear.addActionListener(this);

        // ---------- Add Panels ----------
        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // ---------- DB Connection ----------
        try {
            conn = DBConnection.getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Database connection failed: " + e.getMessage());
        }

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------- Actions ----------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) {
            addQuestion();
        } else if (e.getSource() == btnClear) {
            clearFields();
        }
    }

    void addQuestion() {
        String q = txtQuestion.getText().trim();
        String a = txtA.getText().trim();
        String b = txtB.getText().trim();
        String c = txtC.getText().trim();
        String d = txtD.getText().trim();
        String correct = txtCorrect.getText().trim().toUpperCase();

        if (q.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty() || correct.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Please fill all fields!");
            return;
        }

        if (!correct.matches("[ABCD]")) {
            JOptionPane.showMessageDialog(this, "⚠️ Correct option must be A, B, C, or D!");
            return;
        }

        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Questions (question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setString(1, q);
            ps.setString(2, a);
            ps.setString(3, b);
            ps.setString(4, c);
            ps.setString(5, d);
            ps.setString(6, correct);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Question added successfully!");
            clearFields();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
        }
    }

    void clearFields() {
        txtQuestion.setText("");
        txtA.setText("");
        txtB.setText("");
        txtC.setText("");
        txtD.setText("");
        txtCorrect.setText("");
    }

    public static void main(String[] args) {
        new AdminPanel();
    }
}
