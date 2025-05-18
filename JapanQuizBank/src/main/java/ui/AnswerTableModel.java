package ui;

import model.Answer;
import dao.AnswerDAO;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

public class AnswerTableModel extends AbstractTableModel {
    private String[] columns = {"ID", "Nội dung đáp án", "Đúng?"};
    private List<Answer> data = new ArrayList<>();

    // Load data from database
    public void loadData(int questionId) {
        data = new AnswerDAO().getAnswersByQuestionId(questionId);
        fireTableDataChanged();
    }

    // Get answer at specific row
    public Answer getAnswerAt(int row) {
        return data.get(row);
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
        Answer a = data.get(row);
        switch (col) {
            case 0: return a.getId();
            case 1: return a.getAnswerText();
            case 2: return a.isCorrect() ? "✔" : "";
            default: return null;
        }
    }
}
