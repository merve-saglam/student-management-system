import javax.swing.*;
import java.awt.*;
import java.sql.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class StudentAddDeleteForm extends JFrame {
    private final Color PRIMARY = new Color(0,153,153);

    public StudentAddDeleteForm() {
        setTitle("Add / Delete Student");
        setSize(480, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // ===== HEADER =====
        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(100, 55));
        JLabel title = new JLabel("Add / Delete Student");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // ===== FORM =====
        JPanel form = new JPanel(new GridLayout(6,2,12,12));
        form.setBorder(BorderFactory.createEmptyBorder(25, 60, 25, 60));

        JTextField txtId = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtSurname = new JTextField();
        JTextField txtPhone = new JTextField();
        JComboBox<String> cmbGender = new JComboBox<>(new String[]{"Male","Female"});
        JTextField txtAge = new JTextField();

        form.add(new JLabel("Student ID:"));
        form.add(txtId);
        form.add(new JLabel("Name:"));
        form.add(txtName);
        form.add(new JLabel("Surname:"));
        form.add(txtSurname);
        form.add(new JLabel("Phone:"));
        form.add(txtPhone);
        form.add(new JLabel("Gender:"));
        form.add(cmbGender);
        form.add(new JLabel("Age:"));
        form.add(txtAge);

        add(form, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel buttons = new JPanel();
        JButton btnAdd = flatButton("Add Student");
        JButton btnDelete = flatButton("Delete Student");
        JButton btnCancel = flatButton("Cancel");
        buttons.add(btnAdd);
        buttons.add(btnDelete);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // ===== ACTIONS =====

        // ADD STUDENT
        btnAdd.addActionListener(e -> {
            String idStr = txtId.getText().trim();
            String name = txtName.getText().trim();
            String surname = txtSurname.getText().trim();
            String phone = txtPhone.getText().trim();
            String gender = (String) cmbGender.getSelectedItem();
            String ageStr = txtAge.getText().trim();

            if (idStr.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields (Student ID, Name, Surname).");
                return;
            }

            int studentId;
            try {
                studentId = Integer.parseInt(idStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Student ID must be a number.");
                return;
            }

            int age = 0;
            if (!ageStr.isEmpty()) {
                try {
                    age = Integer.parseInt(ageStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Age must be a number.");
                    return;
                }
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO Student (StudentID, name, surname, Phone, Gender, Age) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, studentId);
                stmt.setString(2, name);
                stmt.setString(3, surname);
                stmt.setString(4, phone.isEmpty() ? null : phone);
                stmt.setString(5, gender.equals("Male") ? "M" : "F");
                stmt.setObject(6, age == 0 ? null : age);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Student added successfully.");
                    // Alanları temizle
                    txtId.setText("");
                    txtName.setText("");
                    txtSurname.setText("");
                    txtPhone.setText("");
                    cmbGender.setSelectedIndex(0);
                    txtAge.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add student.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // DELETE STUDENT
        btnDelete.addActionListener(e -> {
            String idStr = txtId.getText().trim();

            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Student ID to delete.");
                return;
            }

            int studentId;
            try {
                studentId = Integer.parseInt(idStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Student ID must be a number.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete student ID " + studentId + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "DELETE FROM Student WHERE StudentID = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, studentId);

                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Student deleted successfully.");
                        // Sadece ID alanını temizle (diğer alanlar dolu kalabilir)
                        txtId.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "No student found with this ID.");
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    private JButton flatButton(String text){
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(PRIMARY));
        return btn;
    }
}