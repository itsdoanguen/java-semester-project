package ui;

import model.Question;
import dao.QuestionDAO;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

public class QuestionTableModel extends AbstractTableModel {
    private String[] columns = {"ID", "Nội dung", "Loại", "Audio", "Ngày tạo", "Có audio"};
    private List<Question> data = new ArrayList<>();

    public void loadData() {
        data = new QuestionDAO().getAllQuestions();
        fireTableDataChanged();
    }

    public Question getQuestionAt(int row) {
        return data.get(row);
    }

    public void setData(List<Question> data) {
        this.data = data;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        Question q = data.get(row);
        switch (col) {
            case 0: return q.getId();
            case 1: return q.getContent();
            case 2: return q.getType();
            case 3: return q.getAudioPath();
            case 4: return q.getCreatedAt();
            case 5: return q.isHasAudio();
            default: return null;
        }
    }
}
