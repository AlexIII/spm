package spm;

import javax.swing.*;
import java.awt.*;

public class InputDialog extends Dialog {
    public static final int FIELD_NORMAL = 0;
    public static final int FIELD_PASSWORD = 1 << 0;
    public static final int FIELD_NOTEMPTY = 1 << 1;
    public static final int FIELD_GEN_PASSWORD = 1 << 2;

    public interface FieldValidator {
        String validate(Object[] fields);
    }

    public interface SingleFieldValidator {
        String validate(Object field);
    }

    public static Object show(Frame parent, String title) {
        return show(parent, title, (String) null);
    }

    public static Object show(Frame parent, String title, String labelName) {
        return show(parent, title, labelName, null);
    }

    public static Object show(Frame parent, String title, String labelName, String defaultValue) {
        return show(parent, title, labelName, defaultValue, 0);
    }

    public static Object show(Frame parent, String title, String labelName, String defaultValue, int fieldMode) {
        return show(parent, title, labelName, defaultValue, fieldMode, null);
    }

    public static Object show(Frame parent, String title, String labelName, String defaultValue, int fieldMode, SingleFieldValidator singleFieldValidator) {
        Object[] result = show(parent, title, new String[]{labelName}, new String[]{defaultValue}, new int[]{fieldMode}, null, singleFieldValidator);
        return result != null && result.length > 0 ? result[0] : null;
    }

    public static Object[] show(Frame parent, String title, String[] labelNames) {
        return show(parent, title, labelNames, null);
    }

    public static Object[] show(Frame parent, String title, String[] labelNames, String[] defaultValues) {
        return show(parent, title, labelNames, defaultValues, null);
    }

    public static Object[] show(Frame parent, String title, String[] labelNames, String[] defaultValues, int[] fieldModes) {
        return show(parent, title, labelNames, defaultValues, fieldModes, null);
    }

    public static Object[] show(Frame parent, String title, String[] labelNames, String[] defaultValues, int[] fieldModes, FieldValidator fieldValidator) {
        return show(parent, title, labelNames, defaultValues, fieldModes, fieldValidator, null);
    }

    private static Object[] show(Frame parent, String title, String[] labelNames, String[] defaultValues, int[] fieldModes, FieldValidator fieldValidator, SingleFieldValidator singleFieldValidator) {
        InputDialog dialog = new InputDialog(parent, title, labelNames, defaultValues, fieldModes, fieldValidator, singleFieldValidator);
        dialog.setVisible(true);
        Object[] result = dialog.result;
        dialog.dispose();
        return result;
    }

    private InputDialog(Frame parent, String title, String[] labelNames, String[] defaultValues, int[] fieldModes, FieldValidator fieldValidator, SingleFieldValidator singleFieldValidator) {
        super(parent, true);
        this.fieldModes = fieldModes;
        this.fieldValidator = fieldValidator;
        this.singleFieldValidator = singleFieldValidator;
        initializeComponents(title, labelNames, defaultValues);
    }

    /**
     * Initializes the components of the dialog.
     *
     * @param title        the title of the dialog
     * @param labelNames   the names of the labels
     * @param defaultValues the default values for the fields
     */
    private void initializeComponents(String title, String[] labelNames, String[] defaultValues) {
        okButton = new JButton("OK");
        generatePasswordButton = new JButton("↻");
        Insets buttonMargin = generatePasswordButton.getMargin();
        generatePasswordButton.setMargin(new Insets(buttonMargin.top, 5, buttonMargin.bottom, 5));
        generatePasswordButton.setToolTipText("Generate password and copy to clipboard");
        cancelButton = new JButton("Cancel");
        labels = new JLabel[labelNames.length];
        textFields = new JTextField[labelNames.length];

        for (int i = 0; i < labelNames.length; ++i) {
            labels[i] = new JLabel(labelNames[i]);
            textFields[i] = (fieldModes != null && i < fieldModes.length && (fieldModes[i] & FIELD_PASSWORD) != 0) ? new JPasswordField() : new JTextField();
            textFields[i].setColumns(25);
        }

        if (defaultValues != null) {
            for (int i = 0; i < defaultValues.length && i < textFields.length; ++i) {
                textFields[i].setText(defaultValues[i]);
            }
        }

        setResizable(false);
        setTitle(title);
        setIconImage(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

        cancelButton.addActionListener(evt -> closeDialog());
        okButton.addActionListener(evt -> confirmInput());
        generatePasswordButton.addActionListener(evt -> generatePassword());
        textFields[textFields.length - 1].addActionListener(evt -> confirmInput());

        setLayout(createLayout(labelNames.length > 1));
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Generates a password and copies it to the clipboard.
     */
    private void generatePassword() {
        for (int i = 0; i < textFields.length; ++i) {
            if ((fieldModes[i] & FIELD_GEN_PASSWORD) != 0) {
                String password = new String(Crypto.generatePassword());
                Clipboard.copyToClipboard(password, 30);
                textFields[i].setText(password);
                break;
            }
        }
    }

    /**
     * Creates the layout for the dialog.
     *
     * @param isMulti whether the dialog has multiple fields
     * @return the created layout
     */
    private GroupLayout createLayout(boolean isMulti) {
        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        if (isMulti) {
            createMultiFieldLayout(layout);
        } else {
            createSingleFieldLayout(layout);
        }

        return layout;
    }

    /**
     * Creates the layout for multiple fields.
     *
     * @param layout the layout to be configured
     */
    private void createMultiFieldLayout(GroupLayout layout) {
        GroupLayout.ParallelGroup horizontalLabels = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        GroupLayout.ParallelGroup horizontalFields = layout.createParallelGroup();
        GroupLayout.ParallelGroup horizontalButtons = layout.createParallelGroup();

        for (int i = 0; i < textFields.length; ++i) {
            horizontalLabels.addComponent(labels[i]);
            horizontalFields.addComponent(textFields[i]);
            if ((fieldModes[i] & FIELD_GEN_PASSWORD) != 0) {
                horizontalButtons.addComponent(generatePasswordButton);
            }
        }

        GroupLayout.SequentialGroup horizontalGroup = layout.createSequentialGroup()
                .addGroup(horizontalLabels)
                .addGroup(horizontalFields)
                .addGroup(horizontalButtons);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addGroup(horizontalGroup)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(okButton)
                                .addComponent(cancelButton))
        );

        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        for (int i = 0; i < textFields.length; ++i) {
            GroupLayout.ParallelGroup group = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labels[i])
                    .addComponent(textFields[i]);
            if ((fieldModes[i] & FIELD_GEN_PASSWORD) != 0) {
                group.addComponent(generatePasswordButton);
            }
            verticalGroup.addGroup(group);
        }

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(verticalGroup)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(okButton)
                                .addComponent(cancelButton))
        );
    }

    /**
     * Creates the layout for a single field.
     *
     * @param layout the layout to be configured
     */
    private void createSingleFieldLayout(GroupLayout layout) {
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(labels[0])
                        .addComponent(textFields[0])
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(okButton)
                                .addComponent(cancelButton))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(labels[0])
                        .addComponent(textFields[0])
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(okButton)
                                .addComponent(cancelButton))
        );
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        setVisible(false);
    }

    /**
     * Confirms the input and validates it.
     */
    private void confirmInput() {
        Object[] input = new Object[textFields.length];
        for (int i = 0; i < textFields.length; ++i) {
            input[i] = textFields[i] instanceof JPasswordField ? ((JPasswordField) textFields[i]).getPassword() : textFields[i].getText();
        }

        if (!validateInput(input)) {
            result = null;
            return;
        }
        result = input;
        setVisible(false);
    }

    /**
     * Validates the input fields.
     *
     * @param input the input fields
     * @return true if the input is valid, false otherwise
     */
    private boolean validateInput(Object[] input) {
        for (int i = 0; i < input.length; ++i) {
            if (fieldModes != null && i < fieldModes.length && (fieldModes[i] & FIELD_NOTEMPTY) != 0) {
                if ((input[i] instanceof char[] ? ((char[]) input[i]).length : ((String) input[i]).length()) < 1) {
                    JOptionPane.showMessageDialog(null, "\"" + labels[i].getText() + "\" field cannot be empty",
                            "Warning: Empty field", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
        }

        String errorMessage = input.length > 1 ? (fieldValidator == null ? null : fieldValidator.validate(input)) : (singleFieldValidator == null ? null : singleFieldValidator.validate(input));
        if (errorMessage != null) {
            JOptionPane.showMessageDialog(null, errorMessage, "Warning: Input error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private JButton okButton;
    private JButton generatePasswordButton;
    private JButton cancelButton;
    private JLabel[] labels;
    private JTextField[] textFields;
    private Object[] result;
    private final int[] fieldModes;
    private final FieldValidator fieldValidator;
    private final SingleFieldValidator singleFieldValidator;
}
