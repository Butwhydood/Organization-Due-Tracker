package gui;

import exceptions.DuplicateIdException;
import exceptions.InvalidInputException;
import exceptions.MemberNotFoundException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import model.Member;
import model.Officer;
import model.RegularMember;
import model.Validator;
import storage.MemberStore;

/**
 * Modal dialog for adding a new {@link RegularMember} or {@link Officer}, or
 * editing an existing member's editable fields.
 */
public class MemberDialog extends JDialog {

    private final MemberStore store;
    private final Member existingMember;
    private final JComboBox<String> typeCombo;
    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField courseField;
    private final JTextField yearField;
    private final JLabel positionLabel;
    private final JTextField positionField;
    private boolean saved;

    /**
     * Creates an Add or Edit member dialog.
     *
     * @param owner          parent frame
     * @param store          member store
     * @param existingMember {@code null} to add a new member, or an existing member to edit
     */
    public MemberDialog(Frame owner, MemberStore store, Member existingMember) {
        super(owner, existingMember == null ? "Add Member" : "Edit Member", true);
        this.store = store;
        this.existingMember = existingMember;

        setSize(440, 340);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(MainFrame.BG_APP);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBackground(MainFrame.BG_CARD);
        form.setBorder(new EmptyBorder(16, 18, 12, 18));

        Font labelFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        Font inputFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

        this.typeCombo = new JComboBox<>(new String[]{"Regular", "Officer"});
        this.typeCombo.setFont(inputFont);

        this.idField = new JTextField();
        this.idField.setFont(inputFont);

        this.nameField = new JTextField();
        this.nameField.setFont(inputFont);

        this.courseField = new JTextField();
        this.courseField.setFont(inputFont);

        this.yearField = new JTextField();
        this.yearField.setFont(inputFont);

        this.positionLabel = new JLabel("Position:");
        this.positionLabel.setFont(labelFont);

        this.positionField = new JTextField();
        this.positionField.setFont(inputFont);

        form.add(styledLabel("Member Type:", labelFont));
        form.add(typeCombo);
        form.add(styledLabel("Student ID:", labelFont));
        form.add(idField);
        form.add(styledLabel("Full Name:", labelFont));
        form.add(nameField);
        form.add(styledLabel("Course:", labelFont));
        form.add(courseField);
        form.add(styledLabel("Year Level (1-6):", labelFont));
        form.add(yearField);
        form.add(positionLabel);
        form.add(positionField);

        add(form, BorderLayout.CENTER);

        typeCombo.addActionListener(e -> updatePositionVisibility());

        if (existingMember != null) {
            idField.setText(existingMember.getStudentId());
            idField.setEditable(false);
            typeCombo.setSelectedItem(existingMember.getMemberType());
            typeCombo.setEnabled(false);
            nameField.setText(existingMember.getFullName());
            courseField.setText(existingMember.getCourse());
            yearField.setText(String.valueOf(existingMember.getYearLevel()));
            if (existingMember instanceof Officer officer) {
                positionField.setText(officer.getPosition());
            }
        }
        updatePositionVisibility();

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setBackground(MainFrame.BG_APP);
        JButton saveBtn = MainFrame.createPrimaryButton("Save");
        JButton cancelBtn = MainFrame.createSecondaryButton("Cancel");

        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(saveBtn);
        buttons.add(cancelBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    /**
     * Returns whether a member was added or updated.
     *
     * @return {@code true} if saved
     */
    public boolean isSaved() {
        return saved;
    }

    private JLabel styledLabel(String text, Font font) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(MainFrame.NAVY_DARK);
        return lbl;
    }

    private void updatePositionVisibility() {
        boolean isOfficer = "Officer".equals(typeCombo.getSelectedItem());
        positionLabel.setVisible(isOfficer);
        positionField.setVisible(isOfficer);
    }

    private void onSave() {
        try {
            int year = Validator.parseInt("Year level", yearField.getText());
            String name = nameField.getText();
            String course = courseField.getText();

            if (existingMember == null) {
                String id = idField.getText();
                String type = (String) typeCombo.getSelectedItem();
                Member created = "Officer".equals(type)
                        ? new Officer(id, name, course, year, positionField.getText())
                        : new RegularMember(id, name, course, year);
                store.add(created);
            } else {
                existingMember.setFullName(name);
                existingMember.setCourse(course);
                existingMember.setYearLevel(year);
                if (existingMember instanceof Officer officer) {
                    officer.setPosition(positionField.getText());
                }
                store.update(existingMember);
            }

            saved = true;
            dispose();
        } catch (InvalidInputException | DuplicateIdException | MemberNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}