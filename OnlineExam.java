import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class OnlineExam extends JFrame implements ActionListener {
    JTextArea lblQuestion;
    JLabel lblHeader, lblProgress, lblTimer;
    JRadioButton optA, optB, optC, optD;
    JButton btnNext, btnPrev, btnFinish, btnReview;
    ButtonGroup optionsGroup;
    JPanel mainPanel, headerPanel, optionsPanel, bottomPanel, sidePanel;

    Connection conn;
    ResultSet rs;
    int qCount = 0;
    int totalQuestions = 0;
    String studentName;
    String[] userAnswers;
    String[] correctAnswers;
    String[] questionTexts;

    Timer timer;
    int timeRemaining = 15 * 60; // ⏳ 15 minutes in seconds

    // 🎨 Colors
    Color primary = new Color(59, 89, 182);
    Color secondary = new Color(92, 184, 92);
    Color background = new Color(245, 247, 250);
    Color cardColor = new Color(255, 255, 255);

    public OnlineExam(String student) {
        studentName = student;
        setTitle("Online Exam System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // ---------- Header ----------
        headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, primary, getWidth(), getHeight(), secondary);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(600, 80));

        lblHeader = new JLabel("Welcome, " + studentName + " 👋", SwingConstants.CENTER);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblHeader.setForeground(Color.WHITE);
        headerPanel.add(lblHeader, BorderLayout.CENTER);

        // Timer label (right side)
        lblTimer = new JLabel("⏰ 15:00", SwingConstants.RIGHT);
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTimer.setForeground(Color.WHITE);
        lblTimer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        headerPanel.add(lblTimer, BorderLayout.EAST);

        // ---------- Side Panel ----------
        sidePanel = new JPanel();
        sidePanel.setBackground(new Color(240, 243, 247));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(300, getHeight()));

        lblProgress = new JLabel();
        lblProgress.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblProgress.setForeground(primary);
        lblProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(Box.createVerticalStrut(50));
        sidePanel.add(lblProgress);

        // ---------- Main Panel ----------
        mainPanel = new JPanel();
        mainPanel.setBackground(background);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 150, 10, 150));

        // ---------- Question Area ----------
        lblQuestion = new JTextArea();
        lblQuestion.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblQuestion.setForeground(primary);
        lblQuestion.setBackground(background);
        lblQuestion.setLineWrap(true);
        lblQuestion.setWrapStyleWord(true);
        lblQuestion.setEditable(false);
        lblQuestion.setFocusable(false);
        lblQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblQuestion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // ---------- Options ----------
        optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBackground(background);
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        optA = new JRadioButton();
        optB = new JRadioButton();
        optC = new JRadioButton();
        optD = new JRadioButton();

        JRadioButton[] opts = {optA, optB, optC, optD};
        for (JRadioButton o : opts) {
            o.setBackground(cardColor);
            o.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            o.setVerticalAlignment(SwingConstants.CENTER);
            o.setHorizontalAlignment(SwingConstants.LEFT);
            o.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }

        optionsGroup = new ButtonGroup();
        for (JRadioButton o : opts) optionsGroup.add(o);
        for (JRadioButton o : opts) optionsPanel.add(o);

        mainPanel.add(lblQuestion);
        mainPanel.add(optionsPanel);

        // ---------- Buttons ----------
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        bottomPanel.setBackground(background);

        btnPrev = new JButton("Previous");
        btnNext = new JButton("Next");
        btnFinish = new JButton("Submit");
        btnReview = new JButton("Review Answers");

        JButton[] btns = {btnPrev, btnNext, btnFinish, btnReview};
        for (JButton b : btns) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 16));
            b.setFocusPainted(false);
            b.setForeground(Color.WHITE);
            b.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        }

        btnPrev.setBackground(new Color(102, 153, 255));
        btnNext.setBackground(primary);
        btnFinish.setBackground(new Color(255, 77, 77));
        btnReview.setBackground(secondary);

        bottomPanel.add(btnPrev);
        bottomPanel.add(btnNext);
        bottomPanel.add(btnReview);
        bottomPanel.add(btnFinish);

        btnPrev.addActionListener(this);
        btnNext.addActionListener(this);
        btnFinish.addActionListener(this);
        btnReview.addActionListener(this);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // ---------- Load Questions ----------
        try {
            conn = DBConnection.getConnection();
            Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = st.executeQuery("SELECT * FROM Questions");

            rs.last();
            totalQuestions = rs.getRow();
            rs.beforeFirst();

            userAnswers = new String[totalQuestions];
            correctAnswers = new String[totalQuestions];
            questionTexts = new String[totalQuestions];

            int i = 0;
            while (rs.next()) {
                questionTexts[i] = rs.getString("question_text");
                correctAnswers[i] = rs.getString("correct_option");
                i++;
            }

            rs.first();
            showQuestion();
            updateProgress();
            startTimer(); // ⏳ Start countdown when exam begins

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }

        setVisible(true);
    }

    // ⏳ TIMER FUNCTION
    void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (timeRemaining <= 0) {
                        timer.cancel();
                        JOptionPane.showMessageDialog(null, "⏰ Time’s up! Your exam will now be submitted.");
                        showResults();
                        dispose();
                        return;
                    }
                    int minutes = timeRemaining / 60;
                    int seconds = timeRemaining % 60;
                    lblTimer.setText(String.format("%02d:%02d", minutes, seconds));

                    // Change color when 2 minutes left
                    if (timeRemaining <= 120) lblTimer.setForeground(Color.YELLOW);
                    if (timeRemaining <= 60) lblTimer.setForeground(Color.RED);

                    timeRemaining--;
                });
            }
        }, 0, 1000);
    }

    void showQuestion() throws SQLException {
        if (qCount >= totalQuestions) {
            JOptionPane.showMessageDialog(this, "🎉 You’ve finished all the questions!");
            btnNext.setEnabled(false);
            return;
        }

        optionsGroup.clearSelection();
        lblQuestion.setText(questionTexts[qCount]);

        rs.absolute(qCount + 1);
        optA.setText("A. " + rs.getString("option_a"));
        optB.setText("B. " + rs.getString("option_b"));
        optC.setText("C. " + rs.getString("option_c"));
        optD.setText("D. " + rs.getString("option_d"));

        String saved = userAnswers[qCount];
        if (saved != null) {
            switch (saved) {
                case "A": optA.setSelected(true); break;
                case "B": optB.setSelected(true); break;
                case "C": optC.setSelected(true); break;
                case "D": optD.setSelected(true); break;
            }
        }
        updateProgress();
    }

    void recordAnswer() {
        String ans = null;
        if (optA.isSelected()) ans = "A";
        else if (optB.isSelected()) ans = "B";
        else if (optC.isSelected()) ans = "C";
        else if (optD.isSelected()) ans = "D";
        userAnswers[qCount] = ans;
        updateProgress();
    }

    void updateProgress() {
        int answered = 0;
        for (String a : userAnswers) if (a != null) answered++;
        int remaining = totalQuestions - answered;

        lblProgress.setText("<html><div style='text-align:center; width:200px;'>"
                + "<b>📊 Progress</b><br>"
                + "Answered: " + answered + "<br>"
                + "Remaining: " + remaining + "<br>"
                + "<span style='white-space:nowrap; color:#3b59b6;'>Question "
                + (qCount + 1) + " / " + totalQuestions + "</span>"
                + "</div></html>");
    }

    void showReview() {
        int answered = 0, unanswered = 0;
        StringBuilder summary = new StringBuilder("Review Your Progress\n\n");

        for (int i = 0; i < totalQuestions; i++) {
            if (userAnswers[i] == null) {
                unanswered++;
                summary.append("❌ Q").append(i + 1).append(" – Not Answered\n");
            } else {
                answered++;
                summary.append("✅ Q").append(i + 1).append(" – Answered\n");
            }
        }

        summary.append("\nTotal Answered: ").append(answered)
                .append("\nTotal Unanswered: ").append(unanswered);

        JTextArea textArea = new JTextArea(summary.toString());
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Review Progress", JOptionPane.INFORMATION_MESSAGE);
    }

    void showResults() {
        if (timer != null) timer.cancel(); // ⏹ stop timer on finish

        int correct = 0, wrong = 0;
        ArrayList<String> wrongDetails = new ArrayList<>();

        for (int i = 0; i < totalQuestions; i++) {
            if (userAnswers[i] == null) continue;
            if (userAnswers[i].equalsIgnoreCase(correctAnswers[i])) correct++;
            else {
                wrong++;
                wrongDetails.add("❌ Q" + (i + 1) + ": " + questionTexts[i] +
                        "\nYour Answer: " + userAnswers[i] +
                        "\nCorrect Answer: " + correctAnswers[i] + "\n");
            }
        }

        StringBuilder result = new StringBuilder();
        result.append("🎯 Exam Complete!\n\n")
                .append("Total Questions: ").append(totalQuestions).append("\n")
                .append("Correct Answers: ").append(correct).append("\n")
                .append("Wrong Answers: ").append(wrong).append("\n\n");

        if (!wrongDetails.isEmpty()) {
            result.append("Here are the ones you missed:\n\n");
            for (String w : wrongDetails) result.append(w).append("\n");
        }

        JTextArea textArea = new JTextArea(result.toString());
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(650, 450));

        JOptionPane.showMessageDialog(this, scrollPane, "Exam Results", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == btnNext) {
                recordAnswer();
                qCount++;
                showQuestion();
            } else if (e.getSource() == btnPrev) {
                recordAnswer();
                if (qCount > 0) qCount--;
                showQuestion();
                btnNext.setEnabled(true);
            } else if (e.getSource() == btnReview) {
                recordAnswer();
                showReview();
            } else if (e.getSource() == btnFinish) {
                recordAnswer();
                showResults();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        JFrame welcome = new JFrame("🎓 Online Exam System");
        welcome.setExtendedState(JFrame.MAXIMIZED_BOTH);
        welcome.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcome.getContentPane().setBackground(new Color(245, 247, 250));
        welcome.setLayout(new BorderLayout());

        JPanel top = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(59, 89, 182),
                        getWidth(), getHeight(), new Color(92, 184, 92));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        top.setPreferredSize(new Dimension(600, 120));
        JLabel title = new JLabel("🎓 Online Exam System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        top.add(title);

        JPanel center = new JPanel();
        center.setBackground(new Color(245, 247, 250));
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel lblName = new JLabel("Enter your name to start the exam:");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtName = new JTextField(25);
        txtName.setMaximumSize(new Dimension(300, 40));
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnStart = new JButton("🚀 Start Exam");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnStart.setBackground(new Color(59, 89, 182));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusPainted(false);
        btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(Box.createVerticalStrut(80));
        center.add(lblName);
        center.add(Box.createVerticalStrut(20));
        center.add(txtName);
        center.add(Box.createVerticalStrut(30));
        center.add(btnStart);

        welcome.add(top, BorderLayout.NORTH);
        welcome.add(center, BorderLayout.CENTER);

        btnStart.addActionListener(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(welcome, "Please enter your name!");
            } else {
                welcome.dispose();
                new OnlineExam(name);
            }
        });

        welcome.setVisible(true);
    }
}
