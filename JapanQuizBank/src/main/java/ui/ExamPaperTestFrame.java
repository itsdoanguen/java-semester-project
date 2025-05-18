package ui;

import model.ExamPaper;
import dao.ExamPaperDAO;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ExamPaperTestFrame extends JFrame {
    public ExamPaperTestFrame() {
        setTitle("Danh sách đề thi đã tạo");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] columns = {"ID", "Tên đề thi", "Ngày tạo"};
        List<ExamPaper> papers = new ExamPaperDAO().getAllExamPapers();
        Object[][] data = new Object[papers.size()][3];
        for (int i = 0; i < papers.size(); i++) {
            data[i][0] = papers.get(i).getId();
            data[i][1] = papers.get(i).getName();
            data[i][2] = papers.get(i).getCreatedAt();
        }
        JTable table = new JTable(data, columns);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExamPaperTestFrame().setVisible(true));
    }
}
