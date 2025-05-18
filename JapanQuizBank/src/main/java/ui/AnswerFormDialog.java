package ui;

import model.Answer;
import dao.AnswerDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AnswerFormDialog extends JDialog {
    private JTextField txtAnswerText;
    private JCheckBox chkCorrect;
    private JButton btnSave, btnCancel;
    private Answer answer;
    private boolean isEdit = false;
    private int questionId;

    static {
        javax.swing.UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("CheckBox.font", new Font("Segoe UI", Font.PLAIN, 16));
    }

    public AnswerFormDialog(JFrame parent, Answer a, int questionId) {
        super(parent, true);
        setTitle(a == null ? "Thêm đáp án" : "Sửa đáp án");
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(3, 2, 5, 5));
        this.answer = a == null ? new Answer() : a;
        this.isEdit = (a != null);
        this.questionId = questionId;

        JLabel lblAnswer = new JLabel("Nội dung đáp án:");
        lblAnswer.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(lblAnswer);
        txtAnswerText = new JTextField(a != null ? a.getAnswerText() : "");
        txtAnswerText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(txtAnswerText);

        JLabel lblCorrect = new JLabel("Là đáp án đúng?");
        lblCorrect.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(lblCorrect);
        chkCorrect = new JCheckBox();
        chkCorrect.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        chkCorrect.setSelected(a != null && a.isCorrect());
        add(chkCorrect);

        btnSave = new JButton("Lưu");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(btnSave);
        add(btnCancel);

        btnSave.addActionListener((ActionEvent e) -> {
            answer.setAnswerText(txtAnswerText.getText());
            answer.setCorrect(chkCorrect.isSelected());
            answer.setQuestionId(questionId);
            if (chkCorrect.isSelected()) {
                // Nếu chọn đáp án đúng, tự động bỏ tick đáp án đúng ở các đáp án khác
                java.util.List<model.Answer> answers = new dao.AnswerDAO().getAnswersByQuestionId(questionId);
                for (model.Answer ans : answers) {
                    if (ans.isCorrect() && (isEdit ? ans.getId() != answer.getId() : true)) {
                        ans.setCorrect(false);
                        new dao.AnswerDAO().updateAnswer(ans);
                    }
                }
            } else {
                // Nếu không có đáp án nào đúng sau khi bỏ chọn, cảnh báo
                java.util.List<model.Answer> answers = new dao.AnswerDAO().getAnswersByQuestionId(questionId);
                boolean hasOtherCorrect = false;
                for (model.Answer ans : answers) {
                    if (ans.isCorrect() && (isEdit ? ans.getId() != answer.getId() : true)) {
                        hasOtherCorrect = true;
                        break;
                    }
                }
                if (!hasOtherCorrect && !isEdit) {
                    JOptionPane.showMessageDialog(this, "Mỗi câu hỏi phải có ít nhất 1 đáp án đúng!");
                    return;
                }
            }
            boolean ok;
            if (isEdit) {
                ok = new AnswerDAO().updateAnswer(answer);
            } else {
                ok = new AnswerDAO().insertAnswer(answer);
            }
            if (ok) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lưu thất bại!");
            }
        });
        btnCancel.addActionListener(e -> dispose());
    }
}
