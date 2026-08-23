import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class EmergencyContactFrame extends JFrame {
    private final Color PRIMARY = new Color(0,153,153);
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtStudentId;
    public static int selectedStudentId = -1; // Update ve Delete için static StudentID

    public EmergencyContactFrame() {
        setTitle("Emergency Contact Management");
        setSize(750, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // ===== HEADER =====
        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(100, 60));
        JLabel title = new JLabel("Emergency Contact Management");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title);

        // ===== SEARCH PANEL =====
        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Emergency Contact by Student ID"));
        txtStudentId = new JTextField(10);
        JButton btnSearch = flatButton("Search");
        searchPanel.add(new JLabel("Student ID:"));
        searchPanel.add(txtStudentId);
        searchPanel.add(btnSearch);

        JPanel top = new JPanel(new BorderLayout());
        top.add(header, BorderLayout.NORTH);
        top.add(searchPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        String[] cols = {
                "Contact Name", "Relationship", "Phone Number", "Address"
        };
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
        JButton btnDelete = deleteButton("Delete");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        add(buttons, BorderLayout.SOUTH);

        // ===== ACTIONS =====

        // SEARCH
        btnSearch.addActionListener(e -> {
            String idStr = txtStudentId.getText().trim();

            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a Student ID.");
                return;
            }

            int studentId;
            try {
                studentId = Integer.parseInt(idStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Student ID must be a number.");
                return;
            }

            // Static değişkene ID'yi kaydet (Update/Delete için)
            selectedStudentId = studentId;

            model.setRowCount(0); // Tabloyu temizle

            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement stmt = conn.prepareCall("{call SP_GetEmergencyContactsByStudent(?)}")) {

                stmt.setInt(1, studentId);
                ResultSet rs = stmt.executeQuery();

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    String contactName = rs.getString("ContactName");
                    String relation = rs.getString("Relation");
                    String phone = rs.getString("ContactPhone");
                    String address = rs.getString("Address");

                    model.addRow(new Object[]{
                            contactName != null ? contactName : "",
                            relation != null ? relation : "",
                            phone != null ? phone : "",
                            address != null ? address : ""
                    });
                }

                if (!found) {
                    JOptionPane.showMessageDialog(this, "No emergency contact found for this student.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // ADD
        btnAdd.addActionListener(e -> new AddEmergencyForm().setVisible(true));

        // UPDATE
        btnUpdate.addActionListener(e -> {
            if (selectedStudentId == -1) {
                JOptionPane.showMessageDialog(this, "Please search for a student first.");
                return;
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No emergency contact data to update.");
                return;
            }

            new UpdateEmergencyContactForm().setVisible(true);
        });

        // DELETE
        btnDelete.addActionListener(e -> {
            if (selectedStudentId == -1) {
                JOptionPane.showMessageDialog(this, "Please search for a student first.");
                return;
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No emergency contact data to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete emergency contact for student " + selectedStudentId + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                     CallableStatement stmt = conn.prepareCall("{call SP_DeleteEmergencyContact(?)}")) {

                    stmt.setInt(1, selectedStudentId);
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        model.setRowCount(0); // Tabloyu temizle
                        JOptionPane.showMessageDialog(this, "Emergency contact deleted successfully.");
                        selectedStudentId = -1; // ID'yi sıfırla
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete emergency contact.");
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    // ===== BUTTON STYLES =====
    private JButton flatButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(PRIMARY));
        btn.setPreferredSize(new Dimension(90, 34));
        return btn;
    }

    private JButton deleteButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.RED);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.RED));
        btn.setPreferredSize(new Dimension(90, 34));
        return btn;
    }
}