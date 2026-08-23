import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddCourseForm extends JFrame {
    private final Color PRIMARY = new Color(0,153,153);

    public AddCourseForm() {
        setTitle("Add Course to Student");
        setSize(420, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // HEADER
        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(100, 55));
        JLabel title = new JLabel("Add Course to Student");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // FORM
        JPanel form = new JPanel(new GridLayout(5, 2, 12, 12));
        form.setBorder(BorderFactory.createEmptyBorder(25, 60, 25, 60));

        JTextField txtStudentId = new JTextField();
        JTextField txtCourseID = new JTextField();
        JTextField txtCourseName = new JTextField();
        JTextField txtCredits = new JTextField();
        JTextField txtInstructors = new JTextField();

        form.add(new JLabel("Student ID*:"));
        form.add(txtStudentId);
        form.add(new JLabel("CourseID*:"));
        form.add(txtCourseID);
        form.add(new JLabel("CourseName*:"));
        form.add(txtCourseName);
        form.add(new JLabel("Credits*:"));
        form.add(txtCredits);
        form.add(new JLabel("Instructors:"));
        form.add(txtInstructors);

        add(form, BorderLayout.CENTER);

        // BUTTONS
        JPanel buttons = new JPanel();
        JButton btnAdd = flatButton("Add");
        JButton btnCancel = flatButton("Cancel");
        buttons.add(btnAdd);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // ACTIONS
        btnAdd.addActionListener(e -> {
            String studentIdStr = txtStudentId.getText().trim();
            String courseID = txtCourseID.getText().trim();
            String CourseName = txtCourseName.getText().trim();
            String creditsStr = txtCredits.getText().trim();
            String instructors = txtInstructors.getText().trim();

            if (studentIdStr.isEmpty() || courseID.isEmpty() || CourseName.isEmpty() || creditsStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields (*).");
                return;
            }

            int studentId, credits;
            try {
                studentId = Integer.parseInt(studentIdStr);
                credits = Integer.parseInt(creditsStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Student ID and Credits must be numbers.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false); // Transaction başlat

                // 1. Course tablosuna ekle (eğer yoksa)
                String checkCourse = "SELECT COUNT(*) FROM Course WHERE CourseID = ?";
                PreparedStatement psCheck = conn.prepareStatement(checkCourse);
                psCheck.setString(1, courseID);
                ResultSet rs = psCheck.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count == 0) {
                    String insertCourse = "INSERT INTO Course (CourseID, CourseName, Credits, Instructors) VALUES (?, ?, ?, ?)";
                    PreparedStatement psCourse = conn.prepareStatement(insertCourse);
                    psCourse.setString(1, courseID);
                    psCourse.setString(2, CourseName);
                    psCourse.setInt(3, credits);
                    psCourse.setString(4, instructors.isEmpty() ? null : instructors);
                    psCourse.executeUpdate();
                }

                // 2. Enrollment tablosuna öğrenciye dersi ekle
                String insertEnrollment = "INSERT INTO Enrollment (StudentID, CourseID) VALUES (?, ?)";
                // Eğer tablo adı farklıysa (StudentCourse vs.) burayı değiştir
                PreparedStatement psEnroll = conn.prepareStatement(insertEnrollment);
                psEnroll.setInt(1, studentId);
                psEnroll.setString(2, courseID);
                psEnroll.executeUpdate();

                conn.commit(); // İşlem başarılı, onayla

                JOptionPane.showMessageDialog(this, "Course added and assigned to student successfully!");
                // Alanları temizle
                txtStudentId.setText("");
                txtCourseID.setText("");
                txtCourseName.setText("");
                txtCredits.setText("");
                txtInstructors.setText("");

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        btnCancel.addActionListener(e -> dispose());
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