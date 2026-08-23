import javax.swing.*;
import java.awt.*;
import java.sql.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class AddStudentForm extends JFrame {
    private final Color PRIMARY = new Color(0,153,153);

    public AddStudentForm() {
        setTitle("Add Student");
        setSize(460, 430);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // ===== HEADER =====
        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(100, 60));
        JLabel title = new JLabel("Add Student");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // ===== FORM =====
        JPanel form = new JPanel(new GridLayout(7, 2, 12, 12));
        form.setBorder(BorderFactory.createEmptyBorder(25, 70, 25, 70));

        JTextField txtId = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtSurname = new JTextField();
        JTextField txtPhone = new JTextField();
        JComboBox<String> cmbGender = new JComboBox<>(new String[]{"Male", "Female"});
        JTextField txtBirth = new JTextField(); // yyyy-mm-dd
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
        form.add(new JLabel("Birth Date:"));
        form.add(txtBirth);
        form.add(new JLabel("Age:"));
        form.add(txtAge);

        add(form, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnAdd = flatButton("Add");
        JButton btnCancel = flatButton("Cancel");

        // hover effects
        addHover(btnAdd);
        addHover(btnCancel);

        btnCancel.addActionListener(e -> dispose());

        btnAdd.addActionListener(e -> {
            String idStr = txtId.getText().trim();
            String name = txtName.getText().trim();
            String surname = txtSurname.getText().trim();
            String phone = txtPhone.getText().trim();
            String gender = (String) cmbGender.getSelectedItem();
            String birthDate = txtBirth.getText().trim();
            String ageStr = txtAge.getText().trim();

            // Zorunlu alan kontrolü
            if (idStr.isEmpty() || name.isEmpty() || surname.isEmpty() || birthDate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields (ID, Name, Surname, Birth Date).");
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

            // BirthDate format kontrolü (basit)
            if (!birthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Birth Date must be in yyyy-MM-dd format.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO Student (StudentID, name, surname, Phone, Gender, BirthDate, Age) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, studentId);
                stmt.setString(2, name);
                stmt.setString(3, surname);
                stmt.setString(4, phone.isEmpty() ? null : phone);
                stmt.setString(5, gender.charAt(0) == 'M' ? "M" : "F"); // M veya F olarak kaydet
                stmt.setString(6, birthDate);
                stmt.setObject(7, age == 0 ? null : age); // Age boşsa null

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Student added successfully.");
                    // Tüm alanları temizle
                    txtId.setText("");
                    txtName.setText("");
                    txtSurname.setText("");
                    txtPhone.setText("");
                    cmbGender.setSelectedIndex(0);
                    txtBirth.setText("");
                    txtAge.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add student.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        buttons.add(btnAdd);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);
    }

    // ===== BUTTON STYLE =====
    private JButton flatButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(PRIMARY));
        btn.setPreferredSize(new Dimension(95, 34));
        return btn;
    }

    // ===== HOVER EFFECT =====
    private void addHover(JButton btn) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(230,250,250));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
    }
}