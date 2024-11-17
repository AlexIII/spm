package spm;

import java.util.List;
import java.util.Collection;
import javax.swing.*;
import java.awt.*;

public class InputDialog extends Dialog {
    public static final int FIELD_NORMAL = 0;
    public static final int FIELD_PASSWORD = 1 << 0;
    public static final int FIELD_NOTEMPTY = 1 << 1;
    public static final int FIELD_GEN_PASSWORD = 1 << 2;

    private JButton okButton;
    private JButton cancelButton;

    /**
     * Interface for validating a single field.
     */
    public static interface FieldValidator {
        /**
         * Validates a single field.
         * 
         * @param value String or char[] field
         * @return error message or null if valid
         */
        String validate(Object value);
    }

    /**
     * Interface for validating multiple fields.
     */
    public static interface FieldsValidator {
        /**
         * Validates multiple fields.
         * 
         * @param value String[] or char[][] field
         * @return error message or null if valid
         */
        String validate(Object[] value);
    }

    public static class Field {
        public final String label;
        public final String defaultValue;
        public final int mode;
        public final FieldValidator validator;
        
        public Field(String label) {
            this(label, "", 0, null);
        }

        public Field(String label, String defaultValue) {
            this(label, defaultValue, 0, null);
        }

        public Field(String label, String defaultValue, int mode) {
            this(label, defaultValue, mode, null);
        }

        public Field(String label, int mode) {
            this(label, null, mode, null);
        }

        public Field(String label, int mode, FieldValidator validator) {
            this(label, null, mode, validator);
        }

        public Field(String label, String defaultValue, int mode, FieldValidator validator) {
            this.label = label;
            this.defaultValue = defaultValue;
            this.mode = mode;
            if ((mode & FIELD_NOTEMPTY) != 0) {
                this.validator = (value) -> {
                    final boolean empty = value instanceof char[] ? ((char[]) value).length < 1 : ((String) value).isBlank();
                    if(empty) return "Field " + label + " cannot be empty";
                    if(validator != null) return validator.validate(value);
                    return null;
                };
            } else {
                this.validator = validator;
            }
        }
    }

    private static class FieldInt extends Field {
        public Object result; // String or char[] field
        public JLabel uiLabel;
        public JTextField uiTextField;
        public JButton uiButton;

        public FieldInt(Field field) {
            super(field.label, field.defaultValue, field.mode, field.validator);
            uiLabel = new JLabel(label);
            uiTextField = (mode & FIELD_PASSWORD) != 0 ? new JPasswordField() : new JTextField();
            uiTextField.setColumns(25);
            if (defaultValue != null) {
                uiTextField.setText(defaultValue);
            }
            uiButton = (mode & FIELD_GEN_PASSWORD) != 0 ? new JButton("↻") : null;
        }
    }

    private final List<FieldInt> fields;
    private final FieldsValidator validator;
    private boolean isConfirmed = false;

    /**
     * Shows a dialog with a single input field.
     * 
     * @param parent the parent frame
     * @param title the title of the dialog
     * @param field the field to be displayed
     * @return the input value (String or char[] for password) or null if cancelled
     */
    public static Object show(Frame parent, String title, Field field) {
        Object[] result = show(parent, title, List.of(field));
        return result != null ? result[0] : null;
    }

    /**
     * Shows a dialog with multiple input fields.
     * 
     * @param parent the parent frame
     * @param title the title of the dialog
     * @param fields the collection of fields to be displayed
     * @return an array of input values (String or char[] for password) or null if cancelled
     */
    public static Object[] show(Frame parent, String title, Collection<Field> fields) {
        return show(parent, title, fields, null);
    }
    
    /**
     * Shows a dialog with multiple input fields and a custom validator.
     * 
     * @param parent the parent frame
     * @param title the title of the dialog
     * @param fields the collection of fields to be displayed
     * @param validator the custom validator for the fields
     * @return an array of input values (String or char[] for password) or null if cancelled
     */
    public static Object[] show(Frame parent, String title, Collection<Field> fields, FieldsValidator validator) {
        final InputDialog dialog = new InputDialog(parent, title, fields, validator);
        dialog.setVisible(true);
        final Object[] results = dialog.fields.stream().map(f -> f.result).toArray();
        dialog.dispose();
        return dialog.isConfirmed ? results : null;
    }

    private InputDialog(Frame parent, String title, Collection<Field> fields, FieldsValidator validator) {
        super(parent, true);
        this.fields = fields.stream().map(FieldInt::new).toList();
        this.validator = validator;
        initializeComponents(title);
    }

    /**
     * Initializes the components of the dialog.
     *
     * @param title the title of the dialog
     */
    private void initializeComponents(String title) {
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");

        setResizable(false);
        setTitle(title);
        setIconImage(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

        fields.stream().forEach(f -> {
            if ((f.mode & FIELD_GEN_PASSWORD) != 0) {
                Insets buttonMargin = f.uiButton.getMargin();
                f.uiButton.setMargin(new Insets(buttonMargin.top, 5, buttonMargin.bottom, 5));
                f.uiButton.setToolTipText("Generate password and copy to clipboard");
                f.uiButton.addActionListener(evt -> {
                    String password = new String(Crypto.generatePassword());
                    Clipboard.copyToClipboard(password, 30);
                    f.uiTextField.setText(password);
                });
            }
        });

        cancelButton.addActionListener(evt -> closeDialog());
        okButton.addActionListener(evt -> confirmInput());
        fields.get(fields.size() - 1).uiTextField.addActionListener(evt -> confirmInput());

        setLayout(createLayout());
        pack();
        setLocationRelativeTo(null);
    }


    /**
     * Creates the layout for the dialog.
     *
     * @return the created layout
     */
    private GroupLayout createLayout() {
        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        createFieldLayout(layout);
        return layout;
    }

    /**
     * Creates the layout for the fields.
     *
     * @param layout the layout to be configured
     */
    private void createFieldLayout(GroupLayout layout) {
        GroupLayout.ParallelGroup horizontalLabels = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        GroupLayout.ParallelGroup horizontalFields = layout.createParallelGroup();
        GroupLayout.ParallelGroup horizontalButtons = layout.createParallelGroup();

        fields.stream().forEach(f -> {
            horizontalLabels.addComponent(f.uiLabel);
            horizontalFields.addComponent(f.uiTextField);
            if ((f.mode & FIELD_GEN_PASSWORD) != 0) {
                horizontalButtons.addComponent(f.uiButton);
            }
        });

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
        fields.stream().forEach(f -> {
            GroupLayout.ParallelGroup group = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(f.uiLabel)
                .addComponent(f.uiTextField);
            if ((f.mode & FIELD_GEN_PASSWORD) != 0) {
                group.addComponent(f.uiButton);
            }
            verticalGroup.addGroup(group);
        });

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(verticalGroup)
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
        fields.stream().forEach(f -> {
            f.result = (f.uiTextField instanceof JPasswordField)
                ? ((JPasswordField) f.uiTextField).getPassword()
                : f.uiTextField.getText();
        });
        if (!validateResults()) return;
        isConfirmed = true;
        setVisible(false);
    }

    /**
     * Validates the input fields.
     *
     * @return true if the input is valid, false otherwise
     */
    private boolean validateResults() {
        String errorMessage = null;
        for (FieldInt field : fields) {
            if (field.validator != null) {
                errorMessage = field.validator.validate(field.result);
                if (errorMessage != null) {
                    break;
                }
            }
        }
        if (errorMessage == null && validator != null) {
            errorMessage = validator.validate(fields.stream().map(f -> f.result).toArray());
        }
        if (errorMessage != null) {
            JOptionPane.showMessageDialog(null, errorMessage, "Input error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
