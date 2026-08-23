import javax.swing.*;
import java.awt.*;
import java.sql.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class AddEmergencyForm extends JFrame {
    private final Color PRIMARY = new Color(0,153,153);

    public AddEmergencyForm() {
        setTitle("Add Emergency Contact");
        setSize(420, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // ===== HEADER =====
        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(100, 55));
        JLabel title = new JLabel("Add Emergency Contact");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // ===== FORM =====
        JPanel form = new JPanel(new GridLayout(5, 2, 12, 12));
        form.setBorder(BorderFactory.createEmptyBorder(25, 60, 25, 60));

        JTextField txtStudentId = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtRelation = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtAddress = new JTextField();

        form.add(new JLabel("Student ID:"));
        form.add(txtStudentId);
        form.add(new JLabel("Contact Name:"));
        form.add(txtName);
        form.add(new JLabel("Relationship:"));
        form.add(txtRelation);
        form.add(new JLabel("Phone Number:"));
        form.add(txtPhone);
        form.add(new JLabel("Address:"));
        form.add(txtAddress);

        add(form, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel buttons = new JPanel();
        JButton btnAdd = flatButton("Add");
        JButton btnCancel = flatButton("Cancel");
        buttons.add(btnAdd);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // ===== ACTIONS =====
        btnAdd.addActionListener(e -> {
            String studentIdStr = txtStudentId.getText().trim();
            String contactName = txtName.getText().trim();
            String relation = txtRelation.getText().trim();
            String phone = txtPhone.getText().trim();
            String address = txtAddress.getText().trim();

            if (studentIdStr.isEmpty() || contactName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields.");
                return;
            }

            int studentId;
            try {
                studentId = Integer.parseInt(studentIdStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Student ID must be a number.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO EmergencyContact (StudentID, ContactName, Relation, ContactPhone, Address) " +
                            "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, studentId);
                stmt.setString(2, contactName);
                stmt.setString(3, relation.isEmpty() ? null : relation);
                stmt.setString(4, phone.isEmpty() ? null : phone);
                stmt.setString(5, address.isEmpty() ? null : address);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Emergency contact added successfully.");
                    // Alanları temizle
                    txtStudentId.setText("");
                    txtName.setText("");
                    txtRelation.setText("");
                    txtPhone.setText("");
                    txtAddress.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add emergency contact.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    // ===== BUTTON STYLE =====
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