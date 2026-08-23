import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class UpdateCourseForm extends JFrame {
    private final Color PRIMARY = new Color(0,153,153);
    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtCredits;
    private JTextField txtInstructors; 

    public UpdateCourseForm() {
        setTitle("Update Course");
        setSize(420, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(100, 55));
        JLabel title = new JLabel("Update Course");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(title);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4,2,12,12)); 
        form.setBorder(BorderFactory.createEmptyBorder(25, 60, 25, 60));

        form.add(new JLabel("Course ID:"));
        txtId = new JTextField();
        txtId.setEditable(false);
        form.add(txtId);

        form.add(new JLabel("Course Name:"));
        txtName = new JTextField();
        form.add(txtName);

        form.add(new JLabel("Credits:"));
        txtCredits = new JTextField();
        form.add(txtCredits);

        form.add(new JLabel("Instructors:"));
        txtInstructors = new JTextField();
        form.add(txtInstructors);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton btnSave = flatButton("Save");
        JButton btnCancel = flatButton("Cancel");

        btnCancel.addActionListener(e -> dispose());

        btnSave.addActionListener(e -> {
            String CourseId = txtId.getText().trim();
            String CourseName = txtName.getText().trim();
            String creditsStr = txtCredits.getText().trim();
            String instructors = txtInstructors.getText().trim();

            if (CourseId.isEmpty() || CourseName.isEmpty() || creditsStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.");
                return;
            }

            int credits;
            try {
                credits = Integer.parseInt(creditsStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Credits must be a number.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE Course SET CourseName = ?, Credits = ?, Instructors = ? WHERE CourseID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, CourseName);
                stmt.setInt(2, credits);
                stmt.setString(3, instructors.isEmpty() ? null : instructors);
                stmt.setString(4, CourseId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Course updated successfully.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No changes made or course not found.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        buttons.add(btnSave);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        loadSelectedCourseData(); // Mevcut verileri yükle
    }

    private void loadSelectedCourseData() {
        // Aynı reflection yöntemiyle CourseManagementFrame'den veri al
        JTable table = getTableFromCourseManagementFrame();
        if (table == null || table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "No course selected.");
            dispose();
            return;
        }

        int row = table.getSelectedRow();
        txtId.setText((String) table.getValueAt(row, 0));
        txtName.setText((String) table.getValueAt(row, 1));
        txtCredits.setText(table.getValueAt(row, 2).toString());
        txtInstructors.setText((String) table.getValueAt(row, 3)); // Instructors sütunu dolu gelecek
    }

    private JTable getTableFromCourseManagementFrame() {
        // Önceki gibi reflection
        Frame[] frames = Frame.getFrames();
        for (Frame frame : frames) {
            if (frame instanceof CourseManagementFrame && frame.isVisible()) {
                try {
                    java.lang.reflect.Field field = CourseManagementFrame.class.getDeclaredField("table");
                    field.setAccessible(true);
                    return (JTable) field.get(frame);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
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