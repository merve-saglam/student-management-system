import javax.swing.*;
import java.awt.*;
import java.sql.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class UpdateStudentForm extends JFrame {
    private final Color PRIMARY = new Color(0,153,153);
    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtSurname;
    private JTextField txtPhone;
    private JComboBox<String> cmbGender;
    private JTextField txtBirth;
    private JTextField txtAge;

    public UpdateStudentForm() {
        setTitle("Update Student");
        setSize(480, 380);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // ===== HEADER =====
        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(100, 55));
        JLabel title = new JLabel("Update Student");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // ===== FORM =====
        JPanel form = new JPanel(new GridLayout(7,2,12,12));
        form.setBorder(BorderFactory.createEmptyBorder(25, 60, 25, 60));

        form.add(new JLabel("Student ID:"));
        txtId = new JTextField();
        txtId.setEditable(false); // StudentID değiştirilemez (primary key)
        form.add(txtId);

        form.add(new JLabel("Name:"));
        txtName = new JTextField();
        form.add(txtName);

        form.add(new JLabel("Surname:"));
        txtSurname = new JTextField();
        form.add(txtSurname);

        form.add(new JLabel("Phone:"));
        txtPhone = new JTextField();
        form.add(txtPhone);

        form.add(new JLabel("Gender:"));
        cmbGender = new JComboBox<>(new String[]{"Male", "Female"});
        form.add(cmbGender);

        form.add(new JLabel("Birth-Date"));
        txtBirth = new JTextField();
        form.add(txtBirth);

        form.add(new JLabel("Age:"));
        txtAge = new JTextField();
        form.add(txtAge);

        add(form, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel buttons = new JPanel();
        JButton btnSave = flatButton("Save");
        JButton btnCancel = flatButton("Cancel");

        btnCancel.addActionListener(e -> dispose());

        btnSave.addActionListener(e -> {
            String idStr = txtId.getText().trim();
            String name = txtName.getText().trim();
            String surname = txtSurname.getText().trim();
            String phone = txtPhone.getText().trim();
            String gender = (String) cmbGender.getSelectedItem();
            String birthDate = txtBirth.getText().trim();
            String ageStr = txtAge.getText().trim();

            if (idStr.isEmpty() || name.isEmpty() || surname.isEmpty() || birthDate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Required fields cannot be empty (ID, Name, Surname, Birth-Date).");
                return;
            }

            int studentId;
            try {
                studentId = Integer.parseInt(idStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Student ID must be a valid number.");
                return;
            }

            int age = 0;
            if (!ageStr.isEmpty()) {
                try {
                    age = Integer.parseInt(ageStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Age must be a valid number.");
                    return;
                }
            }

            // Basit tarih format kontrolü
            if (!birthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Birth-Date must be in yyyy-MM-dd format.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE Student SET name = ?, surname = ?, Phone = ?, Gender = ?, BirthDate = ?, Age = ? WHERE StudentID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, surname);
                stmt.setString(3, phone.isEmpty() ? null : phone);
                stmt.setString(4, gender.equals("Male") ? "M" : "F");
                stmt.setString(5, birthDate);
                stmt.setObject(6, age == 0 ? null : age);
                stmt.setInt(7, studentId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Student updated successfully.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No student found with this ID or no changes made.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        buttons.add(btnSave);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // Form açıldığında seçili öğrencinin verilerini yükle
        loadSelectedStudentData();
    }

    // StudentListFrame'den seçili öğrencinin verilerini yükle
    private void loadSelectedStudentData() {
        JTable table = getTableFromStudentListFrame();
        if (table == null) {
            JOptionPane.showMessageDialog(this, "Unable to load data: Student list not found.");
            dispose();
            return;
        }

        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the main list first.");
            dispose();
            return;
        }

        txtId.setText(table.getValueAt(selectedRow, 0).toString());
        txtName.setText((String) table.getValueAt(selectedRow, 1));
        txtSurname.setText((String) table.getValueAt(selectedRow, 2));
        txtPhone.setText((String) table.getValueAt(selectedRow, 3));
        
        String gender = (String) table.getValueAt(selectedRow, 4);
        cmbGender.setSelectedItem(gender.equals("M") ? "Male" : "Female");
        
        Object birthObj = table.getValueAt(selectedRow, 5);
        txtBirth.setText(birthObj != null ? birthObj.toString() : "");
        
        Object ageObj = table.getValueAt(selectedRow, 6);
        txtAge.setText(ageObj != null ? ageObj.toString() : "");
    }

    // StudentListFrame'deki tabloyu reflection ile al
    private JTable getTableFromStudentListFrame() {
        Frame[] frames = Frame.getFrames();
        for (Frame frame : frames) {
            if (frame instanceof StudentListFrame && frame.isVisible()) {
                try {
                    java.lang.reflect.Field field = StudentListFrame.class.getDeclaredField("table");
                    field.setAccessible(true);
                    return (JTable) field.get(frame);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
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