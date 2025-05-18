package ui;

import model.Question;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;

public class QuestionFormDialog extends JDialog {
    private JTextField txtContent, txtAudioPath;
    private JComboBox<String> cbType;
    private JCheckBox chkHasAudio;
    private JButton btnSave, btnCancel;
    private Question question;
    private boolean isEdit = false;

    static {
        javax.swing.UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("CheckBox.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("RadioButton.font", new Font("Segoe UI", Font.PLAIN, 16));
    }

    public QuestionFormDialog(JFrame parent, Question q) {
        super(parent, true);
        setTitle(q == null ? "Thêm câu hỏi" : "Sửa câu hỏi");
        setSize(520, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 248, 255));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblContent = new JLabel("Nội dung câu hỏi:");
        lblContent.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(lblContent, gbc);
        gbc.gridx = 1;
        txtContent = new JTextField(q != null ? q.getContent() : "", 28);
        txtContent.setFont(new Font("MS Mincho", Font.PLAIN, 18)); // Đổi sang font hỗ trợ tiếng Nhật
        formPanel.add(txtContent, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel lblType = new JLabel("Thể loại câu hỏi:");
        lblType.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(lblType, gbc);
        gbc.gridx = 1;
        String[] types = {"Bài nghe (Listening)", "Từ vựng (Vocabulary)", "Ngữ pháp (Grammar)", "Đọc hiểu (Reading)"};
        cbType = new JComboBox<>(types);
        cbType.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(cbType, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel lblAudio = new JLabel("Audio path:");
        lblAudio.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(lblAudio, gbc);
        gbc.gridx = 1;
        txtAudioPath = new JTextField(q != null ? q.getAudioPath() : "", 24);
        txtAudioPath.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(txtAudioPath, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel lblHasAudio = new JLabel("Có audio?");
        lblHasAudio.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(lblHasAudio, gbc);
        gbc.gridx = 1;
        chkHasAudio = new JCheckBox();
        chkHasAudio.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(chkHasAudio, gbc);

        // Tự động tick và disable khi chọn bài nghe, enable khi chọn loại khác
        cbType.addActionListener(e -> {
            if (cbType.getSelectedIndex() == 0) { // Bài nghe
                chkHasAudio.setSelected(true);
                chkHasAudio.setEnabled(false);
                txtAudioPath.setEnabled(true);
            } else {
                chkHasAudio.setSelected(false);
                chkHasAudio.setEnabled(false);
                txtAudioPath.setText("");
                txtAudioPath.setEnabled(false);
            }
        });
        // Khởi tạo trạng thái ban đầu cho audio
        if (cbType.getSelectedIndex() == 0) {
            chkHasAudio.setSelected(true);
            chkHasAudio.setEnabled(false);
            txtAudioPath.setEnabled(true);
        } else {
            chkHasAudio.setSelected(false);
            chkHasAudio.setEnabled(false);
            txtAudioPath.setText("");
            txtAudioPath.setEnabled(false);
        }

        // Nút chọn file audio chỉ cho phép khi là bài nghe
        JButton btnBrowseAudio = new JButton("Chọn file...");
        btnBrowseAudio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnBrowseAudio.setEnabled(cbType.getSelectedIndex() == 0);
        btnBrowseAudio.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn file audio");
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                txtAudioPath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        gbc.gridx = 2;
        formPanel.add(btnBrowseAudio, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Ngày tạo:"), gbc);
        gbc.gridx = 1;
        JTextField txtCreatedAt = new JTextField(q != null && q.getCreatedAt() != null ? q.getCreatedAt().toString() : new java.util.Date().toString());
        txtCreatedAt.setEditable(false);
        txtCreatedAt.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(txtCreatedAt, gbc);

        // Panel đáp án
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel answerPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        answerPanel.setBackground(new Color(245, 248, 255));
        answerPanel.setBorder(BorderFactory.createTitledBorder("Đáp án (4 đáp án, 1 đúng)"));
        JTextField[] answerFields = new JTextField[4];
        JRadioButton[] correctRadios = new JRadioButton[4];
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            JPanel row = new JPanel(new BorderLayout(5, 5));
            row.setBackground(new Color(245, 248, 255));
            answerFields[i] = new JTextField(22);
            answerFields[i].setFont(new Font("MS Mincho", Font.PLAIN, 17)); // Đổi sang font hỗ trợ tiếng Nhật
            correctRadios[i] = new JRadioButton("Đúng");
            correctRadios[i].setFont(new Font("Segoe UI", Font.PLAIN, 15));
            correctRadios[i].setBackground(new Color(245, 248, 255));
            group.add(correctRadios[i]);
            row.add(new JLabel("Đáp án " + (i+1) + ": "), BorderLayout.WEST);
            row.add(answerFields[i], BorderLayout.CENTER);
            row.add(correctRadios[i], BorderLayout.EAST);
            answerPanel.add(row);
        }
        formPanel.add(answerPanel, gbc);
        gbc.gridwidth = 1;

        // Nếu sửa thì load đáp án cũ
        if (q != null) {
            java.util.List<model.Answer> answers = new dao.AnswerDAO().getAnswersByQuestionId(q.getId());
            for (int i = 0; i < answers.size() && i < 4; i++) {
                answerFields[i].setText(answers.get(i).getAnswerText());
                if (answers.get(i).isCorrect()) correctRadios[i].setSelected(true);
            }
        }

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(245, 248, 255));
        btnSave = new JButton("Lưu");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnSave.setBackground(new Color(33, 56, 117));
        btnSave.setForeground(Color.WHITE);
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(new Color(33, 56, 117));
        btnSave.setFocusPainted(false);
        btnCancel.setFocusPainted(false);
        bottomPanel.add(btnSave);
        bottomPanel.add(btnCancel);

        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Khởi tạo đúng đối tượng question
        this.question = (q != null) ? q : new model.Question();
        this.isEdit = (q != null);

        btnSave.addActionListener((ActionEvent e) -> {
            question.setContent(txtContent.getText());
            // Lưu type đúng mã DB
            switch (cbType.getSelectedIndex()) {
                case 0: question.setType("nghe"); break;
                case 1: question.setType("tuvung"); break;
                case 2: question.setType("nguphap"); break;
                case 3: question.setType("doc_hieu"); break;
            }
            question.setAudioPath(txtAudioPath.getText());
            question.setHasAudio(chkHasAudio.isSelected());
            if (!isEdit || question.getCreatedAt() == null) {
                question.setCreatedAt(new java.util.Date());
            }
            // Ràng buộc: Nếu là bài nghe thì phải chọn file audio
            if (cbType.getSelectedIndex() == 0 && (txtAudioPath.getText() == null || txtAudioPath.getText().trim().isEmpty())) {
                JOptionPane.showMessageDialog(this, "Bài nghe phải chọn file audio!");
                return;
            }
            // Ràng buộc: Đáp án không được để trống và phải chọn 1 đáp án đúng
            int correctIdx = -1;
            for (int i = 0; i < 4; i++) {
                if (answerFields[i].getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Không được để trống đáp án!");
                    return;
                }
                if (correctRadios[i].isSelected()) correctIdx = i;
            }
            if (correctIdx == -1) {
                JOptionPane.showMessageDialog(this, "Chọn 1 đáp án đúng!");
                return;
            }            boolean ok;
            if (isEdit) {
                ok = new dao.QuestionDAO().updateQuestion(question);
            } else {
                ok = new dao.QuestionDAO().insertQuestion(question);
            }
            // Lưu đáp án (đảm bảo đã có id cho question mới)
            if (ok && question.getId() > 0) {
                // Xóa đáp án cũ nếu sửa
                if (isEdit) new dao.AnswerDAO().deleteAnswersByQuestionId(question.getId());
                for (int i = 0; i < 4; i++) {
                    model.Answer ans = new model.Answer();
                    ans.setQuestionId(question.getId());
                    ans.setAnswerText(answerFields[i].getText());
                    ans.setCorrect(i == correctIdx);
                    new dao.AnswerDAO().insertAnswer(ans);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lưu thất bại!");
            }
        });
        btnCancel.addActionListener(e -> dispose());
    }
}
