package ui;

import model.ExamPaper;
import model.Question;
import model.Answer;
import dao.ExamPaperDAO;
import dao.ExamPaperQuestionDAO;
import dao.QuestionDAO;
import dao.AnswerDAO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ExamPaperManagerDialog extends JDialog {
    private DefaultListModel<ExamPaper> examPaperListModel;
    private JList<ExamPaper> examPaperJList;
    private JPanel detailPanel;
    private JLabel lblExamName, lblCreatedAt;
    private JTable questionTable;
    private QuestionTableModel questionTableModel;

    public ExamPaperManagerDialog(JFrame parent) {
        super(parent, "Quản lý đề thi", true);
        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Left: Danh sách đề thi
        examPaperListModel = new DefaultListModel<>();
        examPaperJList = new JList<>(examPaperListModel);
        examPaperJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examPaperJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ExamPaper) {
                    ExamPaper ep = (ExamPaper) value;
                    setText("[" + ep.getId() + "] " + ep.getName());
                }
                return this;
            }
        });
        JScrollPane examScroll = new JScrollPane(examPaperJList);
        examScroll.setPreferredSize(new Dimension(220, 500));
        add(examScroll, BorderLayout.WEST);

        // Right: Thông tin chi tiết đề thi
        detailPanel = new JPanel(new BorderLayout(8, 8));
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        lblExamName = new JLabel();
        lblExamName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCreatedAt = new JLabel();
        lblCreatedAt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        infoPanel.add(lblExamName);
        infoPanel.add(lblCreatedAt);
        detailPanel.add(infoPanel, BorderLayout.NORTH);

        // Table câu hỏi
        questionTableModel = new QuestionTableModel();
        questionTable = new JTable(questionTableModel);
        questionTable.setRowHeight(28);
        questionTable.setFont(new Font("MS Mincho", Font.PLAIN, 17));
        questionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        javax.swing.table.TableColumnModel columnModel = questionTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            if ("Nội dung".equals(columnModel.getColumn(i).getHeaderValue())) {
                columnModel.getColumn(i).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        c.setFont(new Font("MS Mincho", Font.PLAIN, 17));
                        return c;
                    }
                });
            }
        }
        JScrollPane tableScroll = new JScrollPane(questionTable);
        detailPanel.add(tableScroll, BorderLayout.CENTER);

        // Nút chỉnh sửa câu hỏi/đáp án và thêm câu hỏi
        JPanel btnPanel = new JPanel();
        JButton btnEditQuestion = new JButton("Chỉnh sửa câu hỏi");
        JButton btnAddQuestion = new JButton("Thêm câu hỏi vào đề");
        JButton btnRemoveQuestion = new JButton("Xóa câu hỏi khỏi đề");
        btnPanel.add(btnEditQuestion);
        btnPanel.add(btnAddQuestion);
        btnPanel.add(btnRemoveQuestion);
        detailPanel.add(btnPanel, BorderLayout.SOUTH);

        add(detailPanel, BorderLayout.CENTER);

        // Load danh sách đề thi
        loadExamPapers();

        // Khi chọn đề thi
        examPaperJList.addListSelectionListener(e -> {
            ExamPaper ep = examPaperJList.getSelectedValue();
            if (ep != null) loadExamPaperDetail(ep);
        });

        // Chỉnh sửa câu hỏi/đáp án
        btnEditQuestion.addActionListener(e -> {
            int row = questionTable.getSelectedRow();
            if (row >= 0) {
                Question q = questionTableModel.getQuestionAt(row);
                new QuestionFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), q).setVisible(true);
                // Sau khi chỉnh sửa, reload lại chi tiết đề thi
                ExamPaper ep = examPaperJList.getSelectedValue();
                if (ep != null) loadExamPaperDetail(ep);
            } else {
                JOptionPane.showMessageDialog(this, "Chọn câu hỏi để chỉnh sửa!");
            }
        });

        // Thêm câu hỏi vào đề
        btnAddQuestion.addActionListener(e -> {
            ExamPaper ep = examPaperJList.getSelectedValue();
            if (ep == null) {
                JOptionPane.showMessageDialog(this, "Chọn đề thi để thêm câu hỏi!");
                return;
            }
            new AddQuestionToExamDialog((JFrame) SwingUtilities.getWindowAncestor(this), ep).setVisible(true);
            loadExamPaperDetail(ep);
        });

        // Xóa câu hỏi khỏi đề
        btnRemoveQuestion.addActionListener(e -> {
            int row = questionTable.getSelectedRow();
            if (row >= 0) {
                Question q = questionTableModel.getQuestionAt(row);
                ExamPaper ep = examPaperJList.getSelectedValue();
                if (ep == null) {
                    JOptionPane.showMessageDialog(this, "Chọn đề thi!");
                    return;
                }
                // Tìm ExamPaperQuestion tương ứng
                java.util.List<model.ExamPaperQuestion> epqList = new dao.ExamPaperQuestionDAO().getQuestionsByExamPaperId(ep.getId());
                model.ExamPaperQuestion toRemove = null;
                for (model.ExamPaperQuestion epq : epqList) {
                    if (epq.getQuestionId() == q.getId()) {
                        toRemove = epq;
                        break;
                    }
                }
                if (toRemove != null) {
                    // Xóa khỏi DB
                    try (java.sql.Connection conn = dao.DBConnection.getConnection();
                         java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM ExamPaperQuestions WHERE Id=?")) {
                        ps.setInt(1, toRemove.getId());
                        ps.executeUpdate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Lỗi khi xóa câu hỏi khỏi đề!");
                        return;
                    }
                    loadExamPaperDetail(ep);
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy câu hỏi trong đề để xóa!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Chọn câu hỏi để xóa khỏi đề!");
            }
        });
    }

    private void loadExamPapers() {
        examPaperListModel.clear();
        List<ExamPaper> papers = new ExamPaperDAO().getAllExamPapers();
        for (ExamPaper ep : papers) examPaperListModel.addElement(ep);
    }

    private void loadExamPaperDetail(ExamPaper ep) {
        lblExamName.setText("Tên đề: " + ep.getName());
        lblCreatedAt.setText("Ngày tạo: " + ep.getCreatedAt());
        // Lấy danh sách câu hỏi của đề
        List<model.ExamPaperQuestion> epqList = new ExamPaperQuestionDAO().getQuestionsByExamPaperId(ep.getId());
        java.util.List<Question> questions = new java.util.ArrayList<>();
        for (model.ExamPaperQuestion epq : epqList) {
            Question q = new QuestionDAO().getAllQuestions().stream().filter(qq -> qq.getId() == epq.getQuestionId()).findFirst().orElse(null);
            if (q != null) questions.add(q);
        }
        questionTableModel.setData(questions);
    }

    // --- Thêm class nội bộ cho dialog chọn câu hỏi ---
    class AddQuestionToExamDialog extends JDialog {
        private JList<Question> normalJList, listeningJList;
        private DefaultListModel<Question> normalModel, listeningModel;
        private JEditorPane previewPane;
        private ExamPaper examPaper;
        public AddQuestionToExamDialog(JFrame parent, ExamPaper ep) {
            super(parent, "Thêm câu hỏi vào đề", true);
            this.examPaper = ep;
            setSize(800, 440);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout(10, 10));
            // List câu hỏi chưa có trong đề, tách 2 loại
            normalModel = new DefaultListModel<>();
            listeningModel = new DefaultListModel<>();
            java.util.List<Question> all = new QuestionDAO().getAllQuestions();
            java.util.List<model.ExamPaperQuestion> epqList = new ExamPaperQuestionDAO().getQuestionsByExamPaperId(ep.getId());
            java.util.Set<Integer> usedIds = new java.util.HashSet<>();
            for (model.ExamPaperQuestion epq : epqList) usedIds.add(epq.getQuestionId());
            for (Question q : all) {
                if (!usedIds.contains(q.getId())) {
                    if ("nghe".equals(q.getType())) listeningModel.addElement(q);
                    else normalModel.addElement(q);
                }
            }
            normalJList = new JList<>(normalModel);
            listeningJList = new JList<>(listeningModel);
            normalJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            listeningJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            normalJList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Question) {
                        Question q = (Question) value;
                        setText("[" + q.getId() + "] " + q.getContent());
                    }
                    return this;
                }
            });
            listeningJList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Question) {
                        Question q = (Question) value;
                        setText("[" + q.getId() + "] " + q.getContent());
                    }
                    return this;
                }
            });
            JPanel listPanel = new JPanel(new GridLayout(2, 1, 8, 8));
            JPanel normalPanel = new JPanel(new BorderLayout());
            normalPanel.add(new JLabel("Câu hỏi bình thường"), BorderLayout.NORTH);
            normalPanel.add(new JScrollPane(normalJList), BorderLayout.CENTER);
            JPanel listeningPanel = new JPanel(new BorderLayout());
            listeningPanel.add(new JLabel("Câu hỏi nghe"), BorderLayout.NORTH);
            listeningPanel.add(new JScrollPane(listeningJList), BorderLayout.CENTER);
            listPanel.add(normalPanel);
            listPanel.add(listeningPanel);
            listPanel.setPreferredSize(new Dimension(340, 340));
            add(listPanel, BorderLayout.WEST);
            // Preview
            previewPane = new JEditorPane();
            previewPane.setContentType("text/html");
            previewPane.setEditable(false);
            JScrollPane previewScroll = new JScrollPane(previewPane);
            previewScroll.setPreferredSize(new Dimension(400, 340));
            add(previewScroll, BorderLayout.CENTER);
            // Khi chọn câu hỏi thì preview
            normalJList.addListSelectionListener(e -> {
                Question q = normalJList.getSelectedValue();
                if (q != null) previewPane.setText(renderQuestionDetailWithAnswers(q));
            });
            listeningJList.addListSelectionListener(e -> {
                Question q = listeningJList.getSelectedValue();
                if (q != null) previewPane.setText(renderQuestionDetailWithAnswers(q));
            });
            // Nút xác nhận
            JPanel btnPanel = new JPanel();
            JButton btnAdd = new JButton("Thêm vào đề");
            JButton btnCancel = new JButton("Hủy");
            btnPanel.add(btnAdd);
            btnPanel.add(btnCancel);
            add(btnPanel, BorderLayout.SOUTH);
            btnAdd.addActionListener(e -> {
                java.util.List<Question> selected = new java.util.ArrayList<>();
                selected.addAll(normalJList.getSelectedValuesList());
                selected.addAll(listeningJList.getSelectedValuesList());
                if (selected.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Chọn ít nhất 1 câu hỏi!");
                    return;
                }
                int order = new ExamPaperQuestionDAO().getQuestionsByExamPaperId(examPaper.getId()).size() + 1;
                for (Question q : selected) {
                    model.ExamPaperQuestion epq = new model.ExamPaperQuestion();
                    epq.setExamPaperId(examPaper.getId());
                    epq.setQuestionId(q.getId());
                    epq.setOrderNumber(order++);
                    new ExamPaperQuestionDAO().insertExamPaperQuestion(epq);
                }
                dispose();
            });
            btnCancel.addActionListener(e -> dispose());
        }
    }

    // --- Thêm: Xử lý JSON từ AI model exe, preview và xác nhận lưu vào ngân hàng đề ---
    // Hàm này nhận JSON string, parse thành danh sách câu hỏi, cho phép preview/chỉnh sửa/xóa, xác nhận lưu vào DB
    private void importQuestionsFromJson(String json) {
        java.util.List<Question> importedQuestions = new java.util.ArrayList<>();
        final org.json.JSONArray arr;
        try {
            arr = new org.json.JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                Question q = new Question();
                q.setContent(obj.getString("question"));
                q.setType("tuvung"); // hoặc cho người dùng chọn loại
                q.setCreatedAt(new java.util.Date());
                q.setHasAudio(false);
                importedQuestions.add(q);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi phân tích JSON câu hỏi: " + ex.getMessage());
            return;
        }
        // Hiển thị preview danh sách câu hỏi, cho phép xóa/chỉnh sửa
        JDialog dlg = new JDialog(this, "Preview & Chỉnh sửa câu hỏi import", true);
        DefaultListModel<Question> model = new DefaultListModel<>();
        for (Question q : importedQuestions) model.addElement(q);
        JList<Question> list = new JList<>(model);
        list.setCellRenderer(new QuestionCellRenderer());
        JEditorPane preview = new JEditorPane();
        preview.setContentType("text/html");
        preview.setEditable(false);
        list.addListSelectionListener(e -> {
            Question q = list.getSelectedValue();
            if (q != null) preview.setText(renderQuestionDetailWithAnswers(q));
        });
        JButton btnEdit = new JButton("Chỉnh sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnSave = new JButton("Lưu vào ngân hàng đề");
        btnEdit.addActionListener(e -> {
            Question q = list.getSelectedValue();
            if (q != null) {
                new QuestionFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), q).setVisible(true);
                list.repaint();
            }
        });
        btnDelete.addActionListener(e -> {
            Question q = list.getSelectedValue();
            if (q != null) model.removeElement(q);
        });
        btnSave.addActionListener(e -> {
            if (model.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Không có câu hỏi nào để lưu!");
                return;
            }
            // Lưu từng câu hỏi vào DB
            for (int i = 0; i < model.size(); i++) {
                Question q = model.get(i);
                new dao.QuestionDAO().insertQuestion(q);
                // Sau khi insert, lấy lại id từ DB
                Question qDb = new dao.QuestionDAO().getAllQuestions().stream().filter(qq -> qq.getContent().equals(q.getContent())).findFirst().orElse(null);
                if (qDb != null) {
                    // Tìm object JSON tương ứng
                    for (int k = 0; k < arr.length(); k++) {
                        org.json.JSONObject obj = arr.getJSONObject(k);
                        if (obj.getString("question").equals(qDb.getContent()) && obj.has("answers")) {
                            org.json.JSONArray ansArr = obj.getJSONArray("answers");
                            int correct = obj.optInt("correct_answer", -1);
                            for (int j = 0; j < ansArr.length(); j++) {
                                org.json.JSONObject ansObj = ansArr.getJSONObject(j);
                                model.Answer ans = new model.Answer();
                                ans.setQuestionId(qDb.getId());
                                ans.setAnswerText(ansObj.getString("text"));
                                ans.setCorrect(ansObj.getInt("number") == correct);
                                new dao.AnswerDAO().insertAnswer(ans);
                            }
                        }
                    }
                }
            }
            // Tạo bộ đề mới
            String examName = JOptionPane.showInputDialog(dlg, "Nhập tên bộ đề:");
            if (examName == null || examName.trim().isEmpty()) return;
            model.ExamPaper ep = new model.ExamPaper();
            ep.setName(examName);
            ep.setCreatedAt(new java.util.Date());
            new dao.ExamPaperDAO().insertExamPaper(ep);
            int order = 1;
            for (int i = 0; i < model.size(); i++) {
                Question q = model.get(i);
                Question qDb = new dao.QuestionDAO().getAllQuestions().stream().filter(qq -> qq.getContent().equals(q.getContent())).findFirst().orElse(null);
                if (qDb != null) {
                    model.ExamPaperQuestion epq = new model.ExamPaperQuestion();
                    epq.setExamPaperId(ep.getId());
                    epq.setQuestionId(qDb.getId());
                    epq.setOrderNumber(order++);
                    new dao.ExamPaperQuestionDAO().insertExamPaperQuestion(epq);
                }
            }
            JOptionPane.showMessageDialog(dlg, "Đã lưu bộ đề và các câu hỏi vào ngân hàng đề!");
            dlg.dispose();
        });
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnEdit); btnPanel.add(btnDelete); btnPanel.add(btnSave);
        JPanel main = new JPanel(new BorderLayout(8,8));
        main.add(new JScrollPane(list), BorderLayout.WEST);
        main.add(new JScrollPane(preview), BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        dlg.setContentPane(main);
        dlg.setSize(900, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // --- Renderer để đổi tên hiển thị câu hỏi trong list (dùng cho import preview) ---
    private static class QuestionCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Question) {
                Question q = (Question) value;
                setText("[" + q.getId() + "] " + q.getContent());
            }
            return this;
        }
    }

    // --- Hàm render preview chi tiết câu hỏi (dùng cho import preview) ---
    private String renderQuestionDetailWithAnswers(Question q) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Segoe UI;font-size:13pt;'>");
        sb.append("<b>ID:</b> ").append(q.getId()).append("<br>");
        sb.append("<b>Nội dung:</b> ").append(q.getContent()).append("<br>");
        String typeLabel;
        switch (q.getType()) {
            case "nghe": typeLabel = "Bài nghe (Listening)"; break;
            case "tuvung": typeLabel = "Từ vựng (Vocabulary)"; break;
            case "nguphap": typeLabel = "Ngữ pháp (Grammar)"; break;
            case "doc_hieu": typeLabel = "Đọc hiểu (Reading)"; break;
            default: typeLabel = q.getType();
        }
        sb.append("<b>Loại:</b> ").append(typeLabel).append("<br>");
        if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
            sb.append("<b>Audio:</b> ").append(q.getAudioPath()).append("<br>");
        }
        sb.append("<b>Ngày tạo:</b> ").append(q.getCreatedAt()).append("<br>");
        sb.append("<b>Có audio:</b> ").append(q.isHasAudio() ? "Có" : "Không").append("<br>");
        java.util.List<model.Answer> answers = new dao.AnswerDAO().getAnswersByQuestionId(q.getId());
        if (!answers.isEmpty()) {
            sb.append("<b>Đáp án:</b><ul style='margin-top:2px;margin-bottom:2px;'>");
            for (model.Answer ans : answers) {
                sb.append("<li");
                if (ans.isCorrect()) sb.append(" style='color:#2196F3;font-weight:bold;list-style-type:square;' ");
                sb.append(">");
                sb.append(ans.getAnswerText());
                if (ans.isCorrect()) sb.append(" <span style='color:#388E3C;'>(Đúng)</span>");
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }
}
