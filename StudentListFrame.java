import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class StudentListFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private final Color PRIMARY = new Color(0,153,153);
    private JTextField txtSearch;
    private JComboBox<String> cmbFilter;

    public StudentListFrame() {
        setTitle("Student Management System");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // ===== LEFT MENU =====
        JPanel menu = new JPanel(new GridLayout(8,1,10,10));
        menu.setBackground(PRIMARY);
        menu.setPreferredSize(new Dimension(200, 100));

        JButton btnUpdateStudent = flatButton("Update Student");
        JButton btnCourses = flatButton("Courses");
        JButton btnEmergency = flatButton("Emergency Contact");
        JButton btnAddStudent = flatButton("Add Student");

        btnUpdateStudent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCourses.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEmergency.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddStudent.setFont(new Font("Segoe UI", Font.BOLD, 14));

        menu.add(new JLabel());
        menu.add(btnUpdateStudent);
        menu.add(btnCourses);
        menu.add(btnEmergency);
        menu.add(btnAddStudent);

        add(menu, BorderLayout.WEST);

        // ===== HEADER =====
        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(100, 60));
        JLabel title = new JLabel("Student Management System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // ===== CENTER =====
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        // SEARCH PANEL
        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Student"));

        txtSearch = new JTextField(12);
        cmbFilter = new JComboBox<>(new String[]{
                "StudentID","Name","Surname","Phone","Gender","Birth","Age"
        });
        JButton btnSearch = flatButton("Search");
        JButton btnRefresh = flatButton("Refresh");

        searchPanel.add(new JLabel("Keyword:"));
        searchPanel.add(txtSearch);
        searchPanel.add(new JLabel("Filter:"));
        searchPanel.add(cmbFilter);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        center.add(searchPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {
                "StudentID","Name","Surname","Phone","Gender","Birth","Age"
        };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        // DELETE BUTTON
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnDelete = flatButton("Delete");
        btnDelete.setForeground(Color.RED);
        btnDelete.setBorder(BorderFactory.createLineBorder(Color.RED));
        bottomPanel.add(btnDelete);
        center.add(bottomPanel, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        // ===== INITIAL DATA LOAD =====
        loadAllStudents();

        // ===== ACTIONS =====

        // SEARCH - ComboBox filtresine göre manuel arama
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            String filter = (String) cmbFilter.getSelectedItem();

            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a search keyword.");
                return;
            }

            searchStudents(keyword, filter);
        });

        // REFRESH
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbFilter.setSelectedIndex(0);
            loadAllStudents();
        });

        // UPDATE
        btnUpdateStudent.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a student.");
                return;
            }
            new UpdateStudentForm().setVisible(true);
        });

        // COURSES
        btnCourses.addActionListener(e -> new CourseManagementFrame().setVisible(true));

        // EMERGENCY
        btnEmergency.addActionListener(e -> new EmergencyContactFrame().setVisible(true));

        // ADD STUDENT
        btnAddStudent.addActionListener(e -> new AddStudentForm().setVisible(true));

        // DELETE - SP ile silme
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a student to delete.");
                return;
            }

            int studentId = (Integer) model.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete student ID " + studentId + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                     CallableStatement stmt = conn.prepareCall("{call SP_DeleteStudent(?)}")) {

                    stmt.setInt(1, studentId);
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(this, "Student deleted successfully.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete student.");
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    // Tüm öğrencileri yükle
    private void loadAllStudents() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT StudentID, name, surname, Phone, Gender, BirthDate AS Birth, Age " +
                     "FROM Student ORDER BY StudentID")) {

            ResultSet rs = ps.executeQuery();
            fillTable(rs);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading all students: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Filtreli arama
    private void searchStudents(String keyword, String filter) {
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "";
            PreparedStatement ps;

            switch (filter) {
                case "StudentID":
                    sql = "SELECT StudentID, name, surname, Phone, Gender, BirthDate AS Birth, Age " +
                          "FROM Student WHERE StudentID = ? ORDER BY StudentID";
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, Integer.parseInt(keyword));
                    break;

                case "Name":
                    sql = "SELECT StudentID, name, surname, Phone, Gender, BirthDate AS Birth, Age " +
                          "FROM Student WHERE name LIKE ? ORDER BY name";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, "%" + keyword + "%");
                    break;

                case "Surname":
                    sql = "SELECT StudentID, name, surname, Phone, Gender, BirthDate AS Birth, Age " +
                          "FROM Student WHERE surname LIKE ? ORDER BY surname";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, "%" + keyword + "%");
                    break;

                case "Phone":
                    sql = "SELECT StudentID, name, surname, Phone, Gender, BirthDate AS Birth, Age " +
                          "FROM Student WHERE Phone LIKE ? ORDER BY StudentID";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, "%" + keyword + "%");
                    break;

                case "Gender":
                    sql = "SELECT StudentID, name, surname, Phone, Gender, BirthDate AS Birth, Age " +
                          "FROM Student WHERE Gender = ? ORDER BY StudentID";
                    ps = conn.prepareStatement(sql);
                    String genderVal = keyword.toUpperCase().startsWith("M") ? "M" : "F";
                    ps.setString(1, genderVal);
                    break;

                case "Birth":
                    sql = "SELECT StudentID, name, surname, Phone, Gender, BirthDate AS Birth, Age " +
                          "FROM Student WHERE CONVERT(varchar, BirthDate, 23) LIKE ? ORDER BY BirthDate";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, "%" + keyword + "%");
                    break;

                case "Age":
                    sql = "SELECT StudentID, name, surname, Phone, Gender, BirthDate AS Birth, Age " +
                          "FROM Student WHERE Age = ? ORDER BY Age";
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, Integer.parseInt(keyword));
                    break;

                default:
                    JOptionPane.showMessageDialog(this, "Invalid filter.");
                    return;
            }

            ResultSet rs = ps.executeQuery();
            fillTable(rs);

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No students found matching the criteria.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for " + filter + ".");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Tabloyu dolduran ortak metod
    private void fillTable(ResultSet rs) throws SQLException {
        while (rs.next()) {
            model.addRow(new Object[]{
                    rs.getInt("StudentID"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("Phone"),
                    rs.getString("Gender"),
                    rs.getDate("Birth"),
                    rs.getObject("Age") != null ? rs.getInt("Age") : ""
            });
        }
    }

    // ===== BUTTON STYLE =====
    private JButton flatButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentListFrame().setVisible(true));
    }
}