import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class CourseManagementFrame extends JFrame {
    private final Color PRIMARY = new Color(0,153,153);
    private JTable table;
    private DefaultTableModel model;

    public CourseManagementFrame() {
        setTitle("Course Management");
        setSize(750, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // ===== HEADER =====
        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(100, 60));
        JLabel title = new JLabel("Course Management");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title);

        // ===== SEARCH PANEL =====
        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Course by Student ID"));
        JTextField txtStudentId = new JTextField(10);
        JButton btnSearch = flatButton("Search");
        searchPanel.add(new JLabel("Student ID:"));
        searchPanel.add(txtStudentId);
        searchPanel.add(btnSearch);

        JPanel top = new JPanel(new BorderLayout());
        top.add(header, BorderLayout.NORTH);
        top.add(searchPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        String[] cols = {"CourseID", "CourseName", "Credits", "Instructors"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel buttons = new JPanel();
        JButton btnAdd = flatButton("Add");
        JButton btnUpdate = flatButton("Update");
        JButton btnDelete = flatButton("Delete");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        add(buttons, BorderLayout.SOUTH);

        // ===== ACTIONS =====

        // SEARCH - Veritabanındaki gerçek SP ile öğrencinin aldığı dersleri getir
        btnSearch.addActionListener(e -> {
            String studentIdStr = txtStudentId.getText().trim();
            if (studentIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a Student ID.");
                return;
            }

            int studentId;
            try {
                studentId = Integer.parseInt(studentIdStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Student ID must be a number.");
                return;
            }

            model.setRowCount(0); // Tabloyu temizle

            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement stmt = conn.prepareCall("{call SP_GetCoursesByStudent(?)}")) {

                stmt.setInt(1, studentId);
                ResultSet rs = stmt.executeQuery();

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    String CourseId = rs.getString("CourseID");
                    String CourseName = rs.getString("CourseName");   
                    int Credits = rs.getInt("Credits");
                    String Instructors = rs.getString("Instructors"); 

                    model.addRow(new Object[]{
                            CourseId,
                            CourseName,
                            Credits,
                            Instructors != null ? Instructors : ""
                    });
                }

                if (!found) {
                    JOptionPane.showMessageDialog(this, "No courses found for this student.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // ADD
        btnAdd.addActionListener(e -> new AddCourseForm().setVisible(true));

        // UPDATE
        btnUpdate.addActionListener(e -> {
            if (table.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course.");
                return;
            }
            new UpdateCourseForm().setVisible(true);
        });

        // DELETE - Seçili dersi öğrenciden kaldır (Enrollment tablosundan sil)
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course.");
                return;
            }

            String courseId = (String) model.getValueAt(selectedRow, 0);
            String studentIdStr = txtStudentId.getText().trim();

            if (studentIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Student ID field must be filled to delete a course.");
                return;
            }

            int studentId = Integer.parseInt(studentIdStr);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete this course (" + courseId + ") for student " + studentId + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                     CallableStatement stmt = conn.prepareCall("{call SP_DeleteEnrollment(?, ?)}")) {

                    stmt.setInt(1, studentId);
                    stmt.setString(2, courseId);

                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        model.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(this, "Course removed from student.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to remove course.");
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    private JButton flatButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(PRIMARY));
        return btn;
    }
}