import javax.swing.*;
import java.awt.*;
import java.sql.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class UpdateEmergencyContactForm extends JFrame {
    private final Color PRIMARY = new Color(0,153,153);
    private JTextField txtContactName;
    private JTextField txtRelation;
    private JTextField txtPhone;
    private JTextField txtAddress; // Address alanı eklendi

    public UpdateEmergencyContactForm() {
        setTitle("Update Emergency Contact");
        setSize(450, 400); // Address için yer
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(100, 55));
        JLabel title = new JLabel("Update Emergency Contact");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(5,2,12,12));
        form.setBorder(BorderFactory.createEmptyBorder(25, 60, 25, 60));

        form.add(new JLabel("Contact Name:"));
        txtContactName = new JTextField();
        form.add(txtContactName);

        form.add(new JLabel("Relation:"));
        txtRelation = new JTextField();
        form.add(txtRelation);

        form.add(new JLabel("Phone:"));
        txtPhone = new JTextField();
        form.add(txtPhone);

        form.add(new JLabel("Address:"));
        txtAddress = new JTextField();
        form.add(txtAddress);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton btnSave = flatButton("Save");
        JButton btnCancel = flatButton("Cancel");

        btnCancel.addActionListener(e -> dispose());

        btnSave.addActionListener(e -> {
            String contactName = txtContactName.getText().trim();
            String relation = txtRelation.getText().trim();
            String phone = txtPhone.getText().trim();
            String address = txtAddress.getText().trim();

            if (contactName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Contact Name is required.");
                return;
            }

            if (EmergencyContactFrame.selectedStudentId == -1) {
                JOptionPane.showMessageDialog(this, "No student selected.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement stmt = conn.prepareCall("{call SP_UpdateEmergencyContact(?, ?, ?, ?, ?)}")) {

                // Parametre sırası tam SP'deki gibi olmalı
                stmt.setInt(1, EmergencyContactFrame.selectedStudentId);
                stmt.setString(2, contactName);
                stmt.setString(3, relation.isEmpty() ? null : relation);
                stmt.setString(4, phone.isEmpty() ? null : phone);
                stmt.setString(5, address.isEmpty() ? null : address);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Emergency contact updated successfully.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No changes were made (data is the same) or contact not found.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        buttons.add(btnSave);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        if (EmergencyContactFrame.selectedStudentId == -1) {
            JOptionPane.showMessageDialog(this, "Unable to load data: No student selected.");
            dispose();
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{call SP_GetEmergencyContactsByStudent(?)}")) {

            stmt.setInt(1, EmergencyContactFrame.selectedStudentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                txtContactName.setText(rs.getString("ContactName") != null ? rs.getString("ContactName") : "");
                txtRelation.setText(rs.getString("Relation") != null ? rs.getString("Relation") : "");
                txtPhone.setText(rs.getString("ContactPhone") != null ? rs.getString("ContactPhone") : "");
                txtAddress.setText(rs.getString("Address") != null ? rs.getString("Address") : "");
            } else {
                JOptionPane.showMessageDialog(this, "No emergency contact found for this student.");
                dispose();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            ex.printStackTrace();
            dispose();
        }
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