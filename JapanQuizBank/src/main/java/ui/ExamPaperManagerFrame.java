package ui;

import model.Question;
import model.ExamPaper;
import dao.QuestionDAO;
import dao.ExamPaperDAO;
import dao.ExamPaperQuestionDAO;
import model.ExamPaperQuestion;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class ExamPaperManagerFrame extends JFrame {
    private DefaultListModel<Question> normalQuestionListModel;
    private DefaultListModel<Question> listeningQuestionListModel;
    private JList<Question> normalQuestionJList;
    private JList<Question> listeningQuestionJList;
    private DefaultListModel<Question> selectedNormalListModel;
    private DefaultListModel<Question> selectedListeningListModel;
    private JList<Question> selectedNormalJList;
    private JList<Question> selectedListeningJList;
    private JTextField txtExamName;

    public ExamPaperManagerFrame() {
        setTitle("Tạo đề thi");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        // --- Tab 1: Tạo đề thủ công ---
        JPanel manualPanel = new JPanel(new BorderLayout(10, 10));
        manualPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: 2 lists (bình thường, nghe)
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        leftPanel.setPreferredSize(new Dimension(320, 400));
        JLabel lblNormal = new JLabel("Câu hỏi bình thường");
        JLabel lblListening = new JLabel("Câu hỏi nghe");
        DefaultListModel<Question> normalModel = new DefaultListModel<>();
        DefaultListModel<Question> listeningModel = new DefaultListModel<>();
        JList<Question> normalList = new JList<>(normalModel);
        normalList.setFont(new Font("MS Mincho", Font.PLAIN, 18)); // Hỗ trợ tiếng Nhật
        JList<Question> listeningList = new JList<>(listeningModel);
        listeningList.setFont(new Font("MS Mincho", Font.PLAIN, 18)); // Hỗ trợ tiếng Nhật
        normalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listeningList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        normalList.setCellRenderer(new QuestionCellRenderer());
        listeningList.setCellRenderer(new QuestionCellRenderer());
        JPanel normalPanel = new JPanel(new BorderLayout());
        normalPanel.add(lblNormal, BorderLayout.NORTH);
        normalPanel.add(new JScrollPane(normalList), BorderLayout.CENTER);
        JPanel listeningPanel = new JPanel(new BorderLayout());
        listeningPanel.add(lblListening, BorderLayout.NORTH);
        listeningPanel.add(new JScrollPane(listeningList), BorderLayout.CENTER);
        leftPanel.add(normalPanel);
        leftPanel.add(listeningPanel);
        manualPanel.add(leftPanel, BorderLayout.WEST);

        // Center: buttons + preview
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        // Thêm chỗ đặt tên bộ đề phía trên preview
        JPanel namePanel = new JPanel(new BorderLayout(8, 8));
        JLabel lblExamName = new JLabel("Tên bộ đề:");
        lblExamName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JTextField txtExamNameField = new JTextField();
        txtExamNameField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtExamNameField.setPreferredSize(new Dimension(420, 38));
        namePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        namePanel.add(lblExamName, BorderLayout.WEST);
        namePanel.add(txtExamNameField, BorderLayout.CENTER);
        centerPanel.add(namePanel, BorderLayout.NORTH);
        // Buttons panel
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        btnPanel.setOpaque(false);
        JButton btnAdd = new JButton(">>");
        JButton btnRemove = new JButton("<<");
        btnAdd.setToolTipText("Thêm vào danh sách đã chọn");
        btnRemove.setToolTipText("Bỏ khỏi danh sách đã chọn");
        btnAdd.setBackground(new Color(33, 150, 243));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnRemove.setBackground(new Color(244, 67, 54));
        btnRemove.setForeground(Color.WHITE);
        btnRemove.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnPanel.add(btnAdd);
        btnPanel.add(btnRemove);
        // Đặt btnPanel phía dưới namePanel
        centerPanel.add(btnPanel, BorderLayout.WEST);
        // Preview area (chi tiết, đẹp, fix lỗi HTML)
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(33, 56, 117), 2), "Preview chi tiết câu hỏi", 0, 0, new Font("Segoe UI", Font.BOLD, 16), new Color(33, 56, 117)));
        JEditorPane previewArea = new JEditorPane();
        previewArea.setContentType("text/html");
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        previewArea.setBackground(new Color(245, 248, 255));
        JScrollPane previewScroll = new JScrollPane(previewArea);
        previewScroll.setPreferredSize(new Dimension(420, 220)); // Giảm chiều cao preview
        previewPanel.add(previewScroll, BorderLayout.CENTER);
        centerPanel.add(previewPanel, BorderLayout.CENTER);
        manualPanel.add(centerPanel, BorderLayout.CENTER);

        // Right: selected questions (chia 2 loại)
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        JPanel selectedNormalPanel = new JPanel(new BorderLayout());
        JPanel selectedListeningPanel = new JPanel(new BorderLayout());
        JLabel lblSelectedNormal = new JLabel("Câu hỏi bình thường đã chọn");
        JLabel lblSelectedListening = new JLabel("Câu hỏi nghe đã chọn");
        DefaultListModel<Question> selectedNormalModel = new DefaultListModel<>();
        DefaultListModel<Question> selectedListeningModel = new DefaultListModel<>();
        JList<Question> selectedNormalList = new JList<>(selectedNormalModel);
        selectedNormalList.setFont(new Font("MS Mincho", Font.PLAIN, 18));
        JList<Question> selectedListeningList = new JList<>(selectedListeningModel);
        selectedListeningList.setFont(new Font("MS Mincho", Font.PLAIN, 18));
        selectedNormalList.setCellRenderer(new QuestionCellRenderer());
        selectedListeningList.setCellRenderer(new QuestionCellRenderer());
        selectedNormalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedListeningList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedNormalPanel.add(lblSelectedNormal, BorderLayout.NORTH);
        selectedNormalPanel.add(new JScrollPane(selectedNormalList), BorderLayout.CENTER);
        selectedListeningPanel.add(lblSelectedListening, BorderLayout.NORTH);
        selectedListeningPanel.add(new JScrollPane(selectedListeningList), BorderLayout.CENTER);
        rightPanel.add(selectedNormalPanel);
        rightPanel.add(selectedListeningPanel);
        manualPanel.add(rightPanel, BorderLayout.EAST);

        // Load questions
        List<Question> questions = new QuestionDAO().getAllQuestions();
        for (Question q : questions) {
            if ("nghe".equals(q.getType())) {
                listeningModel.addElement(q);
            } else {
                normalModel.addElement(q);
            }
        }

        // Button actions (chỉ 1 cặp nút)
        btnAdd.addActionListener(e -> {
            Question q = normalList.getSelectedValue();
            if (q != null && !selectedNormalModel.contains(q)) selectedNormalModel.addElement(q);
            q = listeningList.getSelectedValue();
            if (q != null && !selectedListeningModel.contains(q)) selectedListeningModel.addElement(q);
        });
        btnRemove.addActionListener(e -> {
            Question q = selectedNormalList.getSelectedValue();
            if (q != null) selectedNormalModel.removeElement(q);
            q = selectedListeningList.getSelectedValue();
            if (q != null) selectedListeningModel.removeElement(q);
        });

        // Preview logic (chi tiết)
        normalList.addListSelectionListener(e -> {
            Question q = normalList.getSelectedValue();
            if (q != null) previewArea.setText(renderQuestionDetailWithAnswers(q));
        });
        listeningList.addListSelectionListener(e -> {
            Question q = listeningList.getSelectedValue();
            if (q != null) previewArea.setText(renderQuestionDetailWithAnswers(q));
        });
        selectedNormalList.addListSelectionListener(e -> {
            Question q = selectedNormalList.getSelectedValue();
            if (q != null) previewArea.setText(renderQuestionDetailWithAnswers(q));
        });
        selectedListeningList.addListSelectionListener(e -> {
            Question q = selectedListeningList.getSelectedValue();
            if (q != null) previewArea.setText(renderQuestionDetailWithAnswers(q));
        });

        // Bottom: create button
        JButton btnCreate = new JButton("Tạo đề thi");
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnCreate);
        manualPanel.add(bottomPanel, BorderLayout.SOUTH);

        btnCreate.addActionListener(e -> {
            String name = txtExamNameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên bộ đề!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                txtExamNameField.requestFocus();
                return;
            }
            if (selectedNormalModel.size() + selectedListeningModel.size() == 0) {
                JOptionPane.showMessageDialog(this, "Chọn ít nhất 1 câu hỏi!", "Thiếu câu hỏi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ExamPaper ep = new ExamPaper();
            ep.setName(name);
            ep.setCreatedAt(new java.util.Date());
            boolean ok = new ExamPaperDAO().insertExamPaper(ep);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Tạo đề thi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int order = 1;
            for (int i = 0; i < selectedNormalModel.size(); i++) {
                Question q = selectedNormalModel.get(i);
                ExamPaperQuestion epq = new ExamPaperQuestion();
                epq.setExamPaperId(ep.getId());
                epq.setQuestionId(q.getId());
                epq.setOrderNumber(order++);
                new ExamPaperQuestionDAO().insertExamPaperQuestion(epq);
            }
            for (int i = 0; i < selectedListeningModel.size(); i++) {
                Question q = selectedListeningModel.get(i);
                ExamPaperQuestion epq = new ExamPaperQuestion();
                epq.setExamPaperId(ep.getId());
                epq.setQuestionId(q.getId());
                epq.setOrderNumber(order++);
                new ExamPaperQuestionDAO().insertExamPaperQuestion(epq);
            }
            JOptionPane.showMessageDialog(this, "Tạo đề thi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        // Tab 2: Import đề (chỉ còn AI model exe, loại bỏ Azure/OCR.space)
        JPanel importPanel = new JPanel(new BorderLayout(10,10));
        JButton btnSelectImage = new JButton("Chọn ảnh đề thi");
        JLabel lblImagePath = new JLabel("Chưa chọn ảnh");
        JTextArea txtOcrTextGpt4o = new JTextArea();
        txtOcrTextGpt4o.setEditable(false);
        txtOcrTextGpt4o.setLineWrap(true);
        txtOcrTextGpt4o.setWrapStyleWord(true);
        txtOcrTextGpt4o.setBorder(BorderFactory.createTitledBorder("Kết quả JSON từ AI model"));
        JScrollPane ocrScrollGpt4o = new JScrollPane(txtOcrTextGpt4o);
        ocrScrollGpt4o.setPreferredSize(new Dimension(500, 100));
        // Thêm list preview câu hỏi
        DefaultListModel<Question> importQuestionListModel = new DefaultListModel<>();
        JList<Question> importQuestionList = new JList<>(importQuestionListModel);
        importQuestionList.setCellRenderer(new QuestionCellRenderer());
        importQuestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JEditorPane importPreviewPane = new JEditorPane();
        importPreviewPane.setContentType("text/html");
        importPreviewPane.setEditable(false);
        // Map tạm lưu đáp án cho từng câu hỏi import
        Map<Question, java.util.List<model.Answer>> importAnswersMap = new HashMap<>();
        importQuestionList.addListSelectionListener(e -> {
            Question q = importQuestionList.getSelectedValue();
            if (q != null) importPreviewPane.setText(renderQuestionDetailWithAnswersImport(q, importAnswersMap));
        });
        // Nút thao tác
        JButton btnEditQ = new JButton("Chỉnh sửa");
        JButton btnDeleteQ = new JButton("Xóa");
        JButton btnAddQ = new JButton("Thêm câu hỏi mới");
        JButton btnSaveExam = new JButton("Lưu bộ đề");
        btnEditQ.addActionListener(e -> {
            Question q = importQuestionList.getSelectedValue();
            if (q != null) {
                new QuestionFormDialog(this, q).setVisible(true);
                importQuestionList.repaint();
            }
        });
        btnDeleteQ.addActionListener(e -> {
            Question q = importQuestionList.getSelectedValue();
            if (q != null) importQuestionListModel.removeElement(q);
        });
        btnAddQ.addActionListener(e -> {
            Question newQ = new Question();
            newQ.setContent("");
            newQ.setType("tuvung");
            newQ.setCreatedAt(new java.util.Date());
            newQ.setHasAudio(false);
            new QuestionFormDialog(this, newQ).setVisible(true);
            if (newQ.getContent() != null && !newQ.getContent().trim().isEmpty()) {
                importQuestionListModel.addElement(newQ);
            }
        });
        btnSaveExam.addActionListener(e -> {
            if (importQuestionListModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có câu hỏi nào để lưu!");
                return;
            }
            String examName = JOptionPane.showInputDialog(this, "Nhập tên bộ đề:");
            if (examName == null || examName.trim().isEmpty()) return;
            model.ExamPaper ep = new model.ExamPaper();
            ep.setName(examName);
            ep.setCreatedAt(new java.util.Date());
            new dao.ExamPaperDAO().insertExamPaper(ep);
            int order = 1;
            for (int i = 0; i < importQuestionListModel.size(); i++) {
                Question q = importQuestionListModel.get(i);
                new dao.QuestionDAO().insertQuestion(q);
                Question qDb = new dao.QuestionDAO().getAllQuestions().stream().filter(qq -> qq.getContent().equals(q.getContent())).findFirst().orElse(null);
                if (qDb != null) {
                    // Lưu đáp án từ map tạm
                    java.util.List<model.Answer> answerList = importAnswersMap.get(q);
                    if (answerList != null) {
                        for (model.Answer ans : answerList) {
                            ans.setQuestionId(qDb.getId());
                            new dao.AnswerDAO().insertAnswer(ans);
                        }
                    }
                    model.ExamPaperQuestion epq = new model.ExamPaperQuestion();
                    epq.setExamPaperId(ep.getId());
                    epq.setQuestionId(qDb.getId());
                    epq.setOrderNumber(order++);
                    new dao.ExamPaperQuestionDAO().insertExamPaperQuestion(epq);
                }
            }
            JOptionPane.showMessageDialog(this, "Đã lưu bộ đề và các câu hỏi vào ngân hàng đề!");
        });
        JPanel topPanel = new JPanel();
        topPanel.add(btnSelectImage);
        topPanel.add(lblImagePath);
        importPanel.add(topPanel, BorderLayout.NORTH);
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.add(ocrScrollGpt4o);
        importPanel.add(resultPanel, BorderLayout.CENTER);
        // Panel preview và thao tác câu hỏi
        JPanel qPanel = new JPanel(new BorderLayout(8,8));
        qPanel.setBorder(BorderFactory.createTitledBorder("Danh sách câu hỏi lọc được từ AI"));
        qPanel.add(new JScrollPane(importQuestionList), BorderLayout.WEST);
        qPanel.add(new JScrollPane(importPreviewPane), BorderLayout.CENTER);
        JPanel btnPanelImport = new JPanel();
        btnPanelImport.add(btnEditQ); btnPanelImport.add(btnDeleteQ); btnPanelImport.add(btnAddQ); btnPanelImport.add(btnSaveExam);
        qPanel.add(btnPanelImport, BorderLayout.SOUTH);
        importPanel.add(qPanel, BorderLayout.SOUTH);
        // Chọn file ảnh
        btnSelectImage.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn file ảnh đề thi");
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                String path = file.getAbsolutePath();
                if (!isImageFile(path)) {
                    JOptionPane.showMessageDialog(this, "File không phải là ảnh!");
                    return;
                }
                lblImagePath.setText(path);
                txtOcrTextGpt4o.setText("Đang xử lý ảnh bằng AI model...");
                importQuestionListModel.clear();
                importPreviewPane.setText("");
                importAnswersMap.clear();
                new Thread(() -> {
                    try {
                        String ocrJson = ai.LocalOcrUtil.extractQuestionsFromImageByExe(path);
                        SwingUtilities.invokeLater(() -> {
                            txtOcrTextGpt4o.setText(ocrJson);
                            // --- Sửa: tự động cắt đúng đoạn JSON array ---
                            String json = ocrJson.trim();
                            int start = json.indexOf('[');
                            int end = json.lastIndexOf(']');
                            if (start >= 0 && end > start) {
                                json = json.substring(start, end + 1);
                            }
                            try {
                                org.json.JSONArray arr = new org.json.JSONArray(json);
                                for (int i = 0; i < arr.length(); i++) {
                                    org.json.JSONObject obj = arr.getJSONObject(i);
                                    Question q = new Question();
                                    q.setContent(obj.optString("question", ""));
                                    q.setType("tuvung");
                                    q.setCreatedAt(new java.util.Date());
                                    q.setHasAudio(false);
                                    // Lưu đáp án nếu có
                                    java.util.List<model.Answer> answerList = new ArrayList<>();
                                    if (obj.has("answers")) {
                                        org.json.JSONArray ansArr = obj.getJSONArray("answers");
                                        int correct = obj.optInt("correct_answer", -1);
                                        for (int j = 0; j < ansArr.length(); j++) {
                                            org.json.JSONObject ansObj = ansArr.getJSONObject(j);
                                            model.Answer ans = new model.Answer();
                                            ans.setAnswerText(ansObj.optString("text", ""));
                                            ans.setCorrect(ansObj.optInt("number", -1) == correct);
                                            ans.setQuestionId(-1); // sẽ gán sau khi insert
                                            answerList.add(ans);
                                        }
                                    }
                                    importAnswersMap.put(q, answerList);
                                    importQuestionListModel.addElement(q);
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(this, "Lỗi khi phân tích JSON câu hỏi: " + ex.getMessage());
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> txtOcrTextGpt4o.setText("Lỗi khi nhận diện bằng AI model exe: " + ex.getMessage()));
                    }
                }).start();
            }
        });
        tabbedPane.addTab("Tạo đề thủ công", manualPanel);
        tabbedPane.addTab("Import đề", importPanel);
        add(tabbedPane, BorderLayout.CENTER);

        // UI: Đảm bảo hiển thị tiếng Nhật đúng font
        importQuestionList.setFont(new Font("MS Mincho", Font.PLAIN, 18));
        importPreviewPane.setFont(new Font("MS Mincho", Font.PLAIN, 18));
        txtOcrTextGpt4o.setFont(new Font("MS Mincho", Font.PLAIN, 16));
    }

    // Hiển thị chi tiết câu hỏi và đáp án
    private String renderQuestionDetailWithAnswers(Question q) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Segoe UI, Arial, sans-serif;font-size:14px;background:#f8fafd;margin:0;padding:0;'>");
        sb.append("<div style='border:1.5px solid #2196F3;border-radius:10px;padding:14px 16px 10px 16px;background:#fff;max-width:600px;margin:10px auto;'>");
        sb.append("<div style='font-size:13px;color:#888;margin-bottom:5px;'><b>ID:</b> ").append(q.getId()).append("</div>");
        sb.append("<div style='font-size:15px;font-weight:bold;color:#1a237e;margin-bottom:8px;'>").append(q.getContent()).append("</div>");
        String typeLabel;
        switch (q.getType()) {
            case "nghe": typeLabel = "Bài nghe (Listening)"; break;
            case "tuvung": typeLabel = "Từ vựng (Vocabulary)"; break;
            case "nguphap": typeLabel = "Ngữ pháp (Grammar)"; break;
            case "doc_hieu": typeLabel = "Đọc hiểu (Reading)"; break;
            default: typeLabel = q.getType();
        }
        sb.append("<div style='margin-bottom:5px;'><b>Loại:</b> <span style='color:#1976d2;'>").append(typeLabel).append("</span></div>");
        if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
            sb.append("<div style='margin-bottom:5px;'><b>Audio:</b> <span style='color:#388e3c;'>").append(q.getAudioPath()).append("</span></div>");
        }
        sb.append("<div style='margin-bottom:5px;'><b>Ngày tạo:</b> <span style='color:#888;'>").append(q.getCreatedAt()).append("</span></div>");
        sb.append("<div style='margin-bottom:8px;'><b>Có audio:</b> <span style='color:");
        if (q.isHasAudio()) {
            sb.append("#388e3c;'>Có");
        } else {
            sb.append("#b71c1c;'>Không");
        }
        sb.append("</span></div>");
        java.util.List<model.Answer> answers = new dao.AnswerDAO().getAnswersByQuestionId(q.getId());
        if (!answers.isEmpty()) {
            sb.append("<div style='margin-top:8px;margin-bottom:2px;'><b>Đáp án:</b></div>");
            sb.append("<ul style='margin:0 0 0 16px;padding:0;list-style-type:circle;'>");
            for (model.Answer ans : answers) {
                sb.append("<li style='margin-bottom:4px;");
                if (ans.isCorrect()) sb.append("background:#e3f2fd;border-radius:6px;padding:2px 6px 2px 6px;color:#1976d2;font-weight:bold;list-style-type:square;border:1.5px solid #2196F3;");
                sb.append("'>");
                sb.append(ans.getAnswerText());
                if (ans.isCorrect()) sb.append(" <span style='color:#388E3C;font-size:12px;'>(Đúng)</span>");
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("</div></body></html>");
        return sb.toString();
    }

    // Hiển thị chi tiết câu hỏi và đáp án cho import (lấy từ map tạm)
    private String renderQuestionDetailWithAnswersImport(Question q, Map<Question, java.util.List<model.Answer>> importAnswersMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Segoe UI, Arial, sans-serif;font-size:14px;background:#f8fafd;margin:0;padding:0;'>");
        sb.append("<div style='border:1.5px solid #2196F3;border-radius:10px;padding:14px 16px 10px 16px;background:#fff;max-width:600px;margin:10px auto;'>");
        sb.append("<div style='font-size:13px;color:#888;margin-bottom:5px;'><b>ID:</b> ").append(q.getId()).append("</div>");
        sb.append("<div style='font-size:15px;font-weight:bold;color:#1a237e;margin-bottom:8px;'>").append(q.getContent()).append("</div>");
        String typeLabel;
        switch (q.getType()) {
            case "nghe": typeLabel = "Bài nghe (Listening)"; break;
            case "tuvung": typeLabel = "Từ vựng (Vocabulary)"; break;
            case "nguphap": typeLabel = "Ngữ pháp (Grammar)"; break;
            case "doc_hieu": typeLabel = "Đọc hiểu (Reading)"; break;
            default: typeLabel = q.getType();
        }
        sb.append("<div style='margin-bottom:5px;'><b>Loại:</b> <span style='color:#1976d2;'>").append(typeLabel).append("</span></div>");
        if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
            sb.append("<div style='margin-bottom:5px;'><b>Audio:</b> <span style='color:#388e3c;'>").append(q.getAudioPath()).append("</span></div>");
        }
        sb.append("<div style='margin-bottom:5px;'><b>Ngày tạo:</b> <span style='color:#888;'>").append(q.getCreatedAt()).append("</span></div>");
        sb.append("<div style='margin-bottom:8px;'><b>Có audio:</b> <span style='color:");
        if (q.isHasAudio()) {
            sb.append("#388e3c;'>Có");
        } else {
            sb.append("#b71c1c;'>Không");
        }
        sb.append("</span></div>");
        java.util.List<model.Answer> answers = importAnswersMap.getOrDefault(q, new java.util.ArrayList<>());
        if (!answers.isEmpty()) {
            sb.append("<div style='margin-top:8px;margin-bottom:2px;'><b>Đáp án:</b></div>");
            sb.append("<ul style='margin:0 0 0 16px;padding:0;list-style-type:circle;'>");
            for (model.Answer ans : answers) {
                sb.append("<li style='margin-bottom:4px;");
                if (ans.isCorrect()) sb.append("background:#e3f2fd;border-radius:6px;padding:2px 6px 2px 6px;color:#1976d2;font-weight:bold;list-style-type:square;border:1.5px solid #2196F3;");
                sb.append("'>");
                sb.append(ans.getAnswerText());
                if (ans.isCorrect()) sb.append(" <span style='color:#388E3C;font-size:12px;'>(Đúng)</span>");
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("</div></body></html>");
        return sb.toString();
    }

    // Renderer để đổi tên hiển thị câu hỏi trong list
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

    // --- Thêm hàm phụ trợ kiểm tra file ảnh và giả lập AI ---
    private boolean isImageFile(String path) {
        String lower = path.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".bmp") || lower.endsWith(".gif");
    }
    // Gọi AI model exe (GPT-4o hoặc tương đương)
    private String extractTextFromImageAI(String imagePath) {
        try {
            // Gọi hàm tĩnh từ lớp LocalOcrUtil để xử lý ảnh và nhận diện câu hỏi
            String ocrJson = ai.LocalOcrUtil.extractQuestionsFromImageByExe(imagePath);
            return ocrJson;
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }

    // --- Đồng bộ font Segoe UI cho toàn bộ UI liên quan quản lý/import/chỉnh sửa câu hỏi ---
    static {
        // Áp dụng cho toàn bộ UI (JLabel, JButton, JTable, JList, JTextArea, JEditorPane...)
        javax.swing.UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 16));
        javax.swing.UIManager.put("List.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("EditorPane.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("CheckBox.font", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("TitledBorder.font", new Font("Segoe UI", Font.BOLD, 16));
        javax.swing.UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16));
        javax.swing.UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.PLAIN, 16));
    }
}
