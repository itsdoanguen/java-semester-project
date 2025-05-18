package ui;

import javax.swing.*;
import java.awt.*;
import model.Answer;

public class QuestionManagerFrame extends JFrame {
    private JTable table;
    private QuestionTableModel tableModel;

    public QuestionManagerFrame() {
        setTitle("Quản lý câu hỏi");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 248, 255));

        JLabel titleLabel = new JLabel("Quản lý câu hỏi", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 56, 117));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new QuestionTableModel();
        table = new JTable(tableModel);
        table.setFont(new Font("MS Mincho", Font.PLAIN, 18)); // Đổi sang font hỗ trợ tiếng Nhật
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(220, 230, 250));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 248, 255));
        JButton btnAdd = new JButton("Thêm câu hỏi");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Làm mới");
        Font btnFont = new Font("Segoe UI", Font.PLAIN, 16);
        JButton[] btns = {btnAdd, btnEdit, btnDelete, btnRefresh};
        for (JButton btn : btns) {
            btn.setFont(btnFont);
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(33, 56, 117));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230), 2),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
            buttonPanel.add(btn);
        }
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(buttonPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> openQuestionForm(null));
        btnEdit.addActionListener(e -> editSelectedQuestion());
        btnDelete.addActionListener(e -> deleteSelectedQuestion());
        btnRefresh.addActionListener(e -> refreshTable());
        refreshTable();

        // Double click để mở quản lý đáp án
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        model.Question q = tableModel.getQuestionAt(row);
                        showAnswerManager(q.getId());
                    }
                }
            }
        });
    }

    private void refreshTable() {
        tableModel.loadData();
    }

    private void openQuestionForm(model.Question q) {
        QuestionFormDialog dialog = new QuestionFormDialog(this, q);
        dialog.setVisible(true);
        refreshTable();
    }

    private void editSelectedQuestion() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            model.Question q = tableModel.getQuestionAt(row);
            openQuestionForm(q);
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn câu hỏi để sửa.");
        }
    }

    private void deleteSelectedQuestion() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            model.Question q = tableModel.getQuestionAt(row);
            int confirm = JOptionPane.showConfirmDialog(this, "Xóa câu hỏi này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new dao.QuestionDAO().deleteQuestion(q.getId());
                refreshTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn câu hỏi để xóa.");
        }
    }

    private void showAnswerManager(int questionId) {
        JDialog dialog = new JDialog(this, "Quản lý đáp án", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        AnswerTableModel answerTableModel = new AnswerTableModel();
        JTable answerTable = new JTable(answerTableModel);
        answerTableModel.loadData(questionId);
        JScrollPane scrollPane = new JScrollPane(answerTable);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnAdd = new JButton("Thêm đáp án");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> {
            if (answerTableModel.getRowCount() >= 4) {
                JOptionPane.showMessageDialog(dialog, "Mỗi câu hỏi chỉ có đúng 4 đáp án!");
                return;
            }
            new AnswerFormDialog(this, null, questionId).setVisible(true);
            answerTableModel.loadData(questionId);
        });
        btnEdit.addActionListener(e -> {
            int row = answerTable.getSelectedRow();
            if (row >= 0) {
                Answer a = answerTableModel.getAnswerAt(row);
                new AnswerFormDialog(this, a, questionId).setVisible(true);
                answerTableModel.loadData(questionId);
            } else {
                JOptionPane.showMessageDialog(dialog, "Chọn đáp án để sửa!");
            }
        });
        btnDelete.addActionListener(e -> {
            int row = answerTable.getSelectedRow();
            if (row >= 0) {
                Answer a = answerTableModel.getAnswerAt(row);
                int confirm = JOptionPane.showConfirmDialog(dialog, "Xóa đáp án này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    new dao.AnswerDAO().deleteAnswer(a.getId());
                    answerTableModel.loadData(questionId);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Chọn đáp án để xóa!");
            }
        });
        dialog.setVisible(true);
    }
}
