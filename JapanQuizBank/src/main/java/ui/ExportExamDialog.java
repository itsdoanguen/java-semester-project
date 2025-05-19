package ui;

import dao.ExamPaperDAO;
import dao.QuestionDAO;
import model.ExamPaper;
import model.Question;
import org.apache.poi.xwpf.usermodel.*;
import java.io.FileOutputStream;
import dao.AnswerDAO;
import model.Answer;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExportExamDialog extends JDialog {
    private JComboBox<ExamPaper> examComboBox;
    private JTextField folderField;
    private JButton browseButton;
    private JSpinner shuffleCountSpinner;
    private JTextField exportSetNameField;
    private JComboBox<String> formatComboBox;
    private JButton exportButton;
    private JButton cancelButton;
    private JFrame parent;

    public ExportExamDialog(JFrame parent) {
        super(parent, "Xuất đề thi", true);
        this.parent = parent;
        setSize(500, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Chọn đề thi
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Chọn đề thi:"), gbc);
        gbc.gridx = 1;
        examComboBox = new JComboBox<>();
        loadExamPapers();
        mainPanel.add(examComboBox, gbc);

        // Chọn nơi lưu
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Nơi lưu bộ đề:"), gbc);
        gbc.gridx = 1;
        folderField = new JTextField(20);
        folderField.setEditable(false);
        mainPanel.add(folderField, gbc);
        gbc.gridx = 2;
        browseButton = new JButton("Chọn...");
        mainPanel.add(browseButton, gbc);

        // Số lượng bộ đề
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Số lượng bộ đề:"), gbc);
        gbc.gridx = 1;
        shuffleCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        mainPanel.add(shuffleCountSpinner, gbc);

        // Tên bộ đề
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Tên bộ đề:"), gbc);
        gbc.gridx = 1;
        exportSetNameField = new JTextField(20);
        mainPanel.add(exportSetNameField, gbc);

        // Định dạng xuất
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Định dạng xuất:"), gbc);
        gbc.gridx = 1;
        formatComboBox = new JComboBox<>(new String[]{"PDF", "Word"});
        mainPanel.add(formatComboBox, gbc);

        // Nút xuất và hủy
        JPanel buttonPanel = new JPanel();
        exportButton = new JButton("Xuất");
        cancelButton = new JButton("Hủy");
        buttonPanel.add(exportButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        browseButton.addActionListener(e -> chooseFolder());
        cancelButton.addActionListener(e -> dispose());
        exportButton.addActionListener(e -> exportExam());
    }

    private void loadExamPapers() {
        ExamPaperDAO dao = new ExamPaperDAO();
        List<ExamPaper> papers = dao.getAllExamPapers();
        examComboBox.removeAllItems();
        for (ExamPaper ep : papers) {
            examComboBox.addItem(ep);
        }
        examComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ExamPaper) {
                    ExamPaper ep = (ExamPaper) value;
                    setText(ep.getName());
                }
                return this;
            }
        });
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            folderField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void exportExam() {
        ExamPaper selectedExam = (ExamPaper) examComboBox.getSelectedItem();
        String folderPath = folderField.getText();
        int shuffleCount = (Integer) shuffleCountSpinner.getValue();
        String exportSetName = exportSetNameField.getText().trim();
        String format = (String) formatComboBox.getSelectedItem();

        if (selectedExam == null || folderPath.isEmpty() || exportSetName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        File exportSetFolder = new File(folderPath, exportSetName);
        if (!exportSetFolder.exists()) exportSetFolder.mkdirs();

        QuestionDAO questionDAO = new QuestionDAO();
        List<Question> questions = questionDAO.getQuestionsByExamPaperId(selectedExam.getId());

        for (int i = 1; i <= shuffleCount; i++) {
            // Xáo trộn mới cho mỗi version
            List<Question> shuffled = shuffleQuestions(questions);
            File versionFolder = new File(exportSetFolder, "De_" + i);
            versionFolder.mkdirs();
            // Xuất file đề và đáp án
            if (format.equals("PDF")) {
                exportExamToPDF(shuffled, versionFolder, i);
            } else {
                exportExamToWord(shuffled, versionFolder, i);
            }
            // Copy file nghe nếu có
            copyAudioFilesIfNeeded(shuffled, versionFolder);
        }
        JOptionPane.showMessageDialog(this, "Xuất đề thành công!");
        dispose();
    }

    private void exportExamToPDF(List<Question> questions, File versionFolder, int version) {
        try {
            Document doc = new Document();
            File f = new File(versionFolder, "DeThi.pdf");
            PdfWriter writer = PdfWriter.getInstance(doc, new java.io.FileOutputStream(f));
            doc.open();
            // Sử dụng DejaVu Sans cho tiếng Việt & Nhật
            String fontPath = "src/main/resources/fonts/DejaVuSans.ttf";
            com.itextpdf.text.pdf.BaseFont bf = com.itextpdf.text.pdf.BaseFont.createFont(fontPath, com.itextpdf.text.pdf.BaseFont.IDENTITY_H, com.itextpdf.text.pdf.BaseFont.EMBEDDED);
            com.itextpdf.text.Font fontTitle = new com.itextpdf.text.Font(bf, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fontQ = new com.itextpdf.text.Font(bf, 13, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fontAudio = new com.itextpdf.text.Font(bf, 12, com.itextpdf.text.Font.ITALIC, new com.itextpdf.text.BaseColor(0,121,107));
            com.itextpdf.text.Font fontAns = new com.itextpdf.text.Font(bf, 12, com.itextpdf.text.Font.NORMAL);
            // Tiêu đề
            doc.add(new Paragraph("ĐỀ THI - Phiên bản " + version, fontTitle));
            // Part 1: Câu hỏi bình thường
            doc.add(new Paragraph("\nPart 1: Câu hỏi bình thường", fontTitle));
            int idx = 1;
            for (Question q : questions) {
                if (!"nghe".equalsIgnoreCase(q.getType())) {
                    doc.add(new Paragraph(idx + ". " + q.getContent(), fontQ));
                    List<Answer> answers = new AnswerDAO().getAnswersByQuestionId(q.getId());
                    char ansChar = 'A';
                    for (Answer ans : answers) {
                        doc.add(new Paragraph("    " + ansChar + ". " + ans.getAnswerText(), fontAns));
                        ansChar++;
                    }
                    idx++;
                }
            }
            // Part 2: Câu hỏi nghe
            doc.add(new Paragraph("\nPart 2: Câu hỏi nghe", fontTitle));
            idx = 1;
            for (Question q : questions) {
                if ("nghe".equalsIgnoreCase(q.getType())) {
                    doc.add(new Paragraph(idx + ". " + q.getContent(), fontQ));
                    // Đường dẫn audio: chỉ lấy tên file, không lấy path gốc
                    if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
                        String audioFileName = new File(q.getAudioPath()).getName();
                        doc.add(new Paragraph("[Audio: ./" + audioFileName + "]", fontAudio));
                    }
                    List<Answer> answers = new AnswerDAO().getAnswersByQuestionId(q.getId());
                    char ansChar = 'A';
                    for (Answer ans : answers) {
                        doc.add(new Paragraph("    " + ansChar + ". " + ans.getAnswerText(), fontAns));
                        ansChar++;
                    }
                    idx++;
                }
            }
            doc.close();
            // Đáp án
            Document docAns = new Document();
            File a = new File(versionFolder, "DapAn.pdf");
            PdfWriter writerAns = PdfWriter.getInstance(docAns, new java.io.FileOutputStream(a));
            docAns.open();
            docAns.add(new Paragraph("ĐÁP ÁN - Phiên bản " + version, fontTitle));
            // Part 1 đáp án
            docAns.add(new Paragraph("\nPart 1: Câu hỏi bình thường", fontTitle));
            idx = 1;
            for (Question q : questions) {
                if (!"nghe".equalsIgnoreCase(q.getType())) {
                    docAns.add(new Paragraph(idx + ". " + q.getContent(), fontQ));
                    List<Answer> answers = new AnswerDAO().getAnswersByQuestionId(q.getId());
                    char ansChar = 'A';
                    for (Answer ans : answers) {
                        if (ans.isCorrect()) {
                            docAns.add(new Paragraph("    " + ansChar + ". " + ans.getAnswerText() + " (Đúng)", fontAns));
                        }
                        ansChar++;
                    }
                    idx++;
                }
            }
            // Part 2 đáp án
            docAns.add(new Paragraph("\nPart 2: Câu hỏi nghe", fontTitle));
            idx = 1;
            for (Question q : questions) {
                if ("nghe".equalsIgnoreCase(q.getType())) {
                    docAns.add(new Paragraph(idx + ". " + q.getContent(), fontQ));
                    List<Answer> answers = new AnswerDAO().getAnswersByQuestionId(q.getId());
                    char ansChar = 'A';
                    for (Answer ans : answers) {
                        if (ans.isCorrect()) {
                            docAns.add(new Paragraph("    " + ansChar + ". " + ans.getAnswerText() + " (Đúng)", fontAns));
                        }
                        ansChar++;
                    }
                    idx++;
                }
            }
            docAns.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void exportExamToWord(List<Question> questions, File versionFolder, int version) {
        try {
            File examFile = new File(versionFolder, "DeThi.docx");
            File answerFile = new File(versionFolder, "DapAn.docx");
            writeShuffledExamToWord(questions, examFile, version);
            writeShuffledAnswerToWord(questions, answerFile, version);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Xáo trộn danh sách câu hỏi và trả về danh sách mới
    private List<Question> shuffleQuestions(List<Question> questions) {
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    // Ghi đề đã xáo vào file Word (dùng cho mỗi version)
    private void writeShuffledExamToWord(List<Question> questions, File file, int version) throws Exception {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runTitle = title.createRun();
        runTitle.setText("ĐỀ THI - Phiên bản " + version);
        runTitle.setFontFamily("MS Mincho");
        runTitle.setFontSize(18);
        runTitle.setBold(true);
        // Part 1: Câu hỏi bình thường
        XWPFParagraph part1 = doc.createParagraph();
        XWPFRun runPart1 = part1.createRun();
        runPart1.setText("\nPart 1: Câu hỏi bình thường");
        runPart1.setFontFamily("MS Mincho");
        runPart1.setFontSize(16);
        runPart1.setBold(true);
        int idx = 1;
        for (Question q : questions) {
            if (!"nghe".equalsIgnoreCase(q.getType())) {
                XWPFParagraph pq = doc.createParagraph();
                pq.setSpacingAfter(0);
                XWPFRun rq = pq.createRun();
                rq.setText(idx + ". " + q.getContent());
                rq.setFontFamily("MS Mincho");
                rq.setFontSize(14);
                List<Answer> answers = new AnswerDAO().getAnswersByQuestionId(q.getId());
                char ansChar = 'A';
                for (Answer ans : answers) {
                    XWPFParagraph pa = doc.createParagraph();
                    pa.setSpacingAfter(0);
                    pa.setIndentationLeft(400);
                    XWPFRun ra = pa.createRun();
                    ra.setText("  " + ansChar + ". " + ans.getAnswerText());
                    ra.setFontFamily("MS Mincho");
                    ra.setFontSize(13);
                    ansChar++;
                }
                idx++;
            }
        }
        // Part 2: Câu hỏi nghe
        XWPFParagraph part2 = doc.createParagraph();
        XWPFRun runPart2 = part2.createRun();
        runPart2.setText("\nPart 2: Câu hỏi nghe");
        runPart2.setFontFamily("MS Mincho");
        runPart2.setFontSize(16);
        runPart2.setBold(true);
        idx = 1;
        for (Question q : questions) {
            if ("nghe".equalsIgnoreCase(q.getType())) {
                XWPFParagraph pq = doc.createParagraph();
                pq.setSpacingAfter(0);
                XWPFRun rq = pq.createRun();
                rq.setText(idx + ". " + q.getContent());
                rq.setFontFamily("MS Mincho");
                rq.setFontSize(14);
                // Nếu có audio, chỉ lấy tên file
                if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
                    String audioFileName = new File(q.getAudioPath()).getName();
                    XWPFRun ra = pq.createRun();
                    ra.setText(" [Audio: ./" + audioFileName + "]");
                    ra.setFontFamily("MS Mincho");
                    ra.setFontSize(12);
                    ra.setColor("00796B");
                }
                List<Answer> answers = new AnswerDAO().getAnswersByQuestionId(q.getId());
                char ansChar = 'A';
                for (Answer ans : answers) {
                    XWPFParagraph pa = doc.createParagraph();
                    pa.setSpacingAfter(0);
                    pa.setIndentationLeft(400);
                    XWPFRun ra = pa.createRun();
                    ra.setText("  " + ansChar + ". " + ans.getAnswerText());
                    ra.setFontFamily("MS Mincho");
                    ra.setFontSize(13);
                    ansChar++;
                }
                idx++;
            }
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            doc.write(fos);
        }
        doc.close();
    }

    // Ghi đáp án đã xáo vào file Word (dùng cho mỗi version)
    private void writeShuffledAnswerToWord(List<Question> questions, File file, int version) throws Exception {
        XWPFDocument docAns = new XWPFDocument();
        XWPFParagraph titleAns = docAns.createParagraph();
        titleAns.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runTitleAns = titleAns.createRun();
        runTitleAns.setText("ĐÁP ÁN - Phiên bản " + version);
        runTitleAns.setFontFamily("MS Mincho");
        runTitleAns.setFontSize(18);
        runTitleAns.setBold(true);
        // Part 1 đáp án
        XWPFParagraph part1 = docAns.createParagraph();
        XWPFRun runPart1 = part1.createRun();
        runPart1.setText("\nPart 1: Câu hỏi bình thường");
        runPart1.setFontFamily("MS Mincho");
        runPart1.setFontSize(16);
        runPart1.setBold(true);
        int idx = 1;
        for (Question q : questions) {
            if (!"nghe".equalsIgnoreCase(q.getType())) {
                XWPFParagraph pq = docAns.createParagraph();
                XWPFRun rq = pq.createRun();
                rq.setText(idx + ". " + q.getContent());
                rq.setFontFamily("MS Mincho");
                rq.setFontSize(14);
                List<Answer> answers = new AnswerDAO().getAnswersByQuestionId(q.getId());
                char ansChar = 'A';
                for (Answer ans : answers) {
                    if (ans.isCorrect()) {
                        XWPFParagraph pa = docAns.createParagraph();
                        pa.setIndentationLeft(400);
                        XWPFRun ra = pa.createRun();
                        ra.setText("  " + ansChar + ". " + ans.getAnswerText() + " (Đúng)");
                        ra.setFontFamily("MS Mincho");
                        ra.setFontSize(13);
                        ra.setColor("1976D2");
                    }
                    ansChar++;
                }
                idx++;
            }
        }
        // Part 2 đáp án
        XWPFParagraph part2 = docAns.createParagraph();
        XWPFRun runPart2 = part2.createRun();
        runPart2.setText("\nPart 2: Câu hỏi nghe");
        runPart2.setFontFamily("MS Mincho");
        runPart2.setFontSize(16);
        runPart2.setBold(true);
        idx = 1;
        for (Question q : questions) {
            if ("nghe".equalsIgnoreCase(q.getType())) {
                XWPFParagraph pq = docAns.createParagraph();
                XWPFRun rq = pq.createRun();
                rq.setText(idx + ". " + q.getContent());
                rq.setFontFamily("MS Mincho");
                rq.setFontSize(14);
                List<Answer> answers = new AnswerDAO().getAnswersByQuestionId(q.getId());
                char ansChar = 'A';
                for (Answer ans : answers) {
                    if (ans.isCorrect()) {
                        XWPFParagraph pa = docAns.createParagraph();
                        pa.setIndentationLeft(400);
                        XWPFRun ra = pa.createRun();
                        ra.setText("  " + ansChar + ". " + ans.getAnswerText() + " (Đúng)");
                        ra.setFontFamily("MS Mincho");
                        ra.setFontSize(13);
                        ra.setColor("1976D2");
                    }
                    ansChar++;
                }
                idx++;
            }
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            docAns.write(fos);
        }
        docAns.close();
    }

    private void copyAudioFilesIfNeeded(List<Question> questions, File versionFolder) {
        for (Question q : questions) {
            if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
                try {
                    File src = new File(q.getAudioPath());
                    if (src.exists()) {
                        File dest = new File(versionFolder, src.getName());
                        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
