package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Japanese Quiz Bank Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(540, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 248, 255));

        JLabel titleLabel = new JLabel("Japanese Quiz Bank Manager", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(33, 56, 117));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 18, 18));
        buttonPanel.setBackground(new Color(245, 248, 255));
        JButton btnManageQuestions = new JButton("Quản lý câu hỏi");
        JButton btnCreateExam = new JButton("Tạo đề thi");
        JButton btnExport = new JButton("Xuất file đề/đáp án");
        JButton btnExit = new JButton("Thoát");
        JButton btnManageExamPapers = new JButton("Quản lý đề thi");

        Font btnFont = new Font("Segoe UI", Font.PLAIN, 18);
        Color btnColor = new Color(33, 56, 117);
        JButton[] btns = {btnCreateExam, btnManageExamPapers, btnManageQuestions, btnExport, btnExit};
        for (JButton btn : btns) {
            btn.setFont(btnFont);
            btn.setBackground(Color.WHITE);
            btn.setForeground(btnColor);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
            buttonPanel.add(btn);
        }
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        add(buttonPanel, BorderLayout.CENTER);

        btnExit.addActionListener(e -> System.exit(0));
        btnManageQuestions.addActionListener(e -> {
            new QuestionManagerFrame().setVisible(true);
        });
        btnCreateExam.addActionListener(e -> {
            new ExamPaperManagerFrame().setVisible(true);
        });
        btnManageExamPapers.addActionListener(e -> {
            new ExamPaperManagerDialog(this).setVisible(true);
        });
        // TODO: Thêm action mở các frame chức năng khác
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
