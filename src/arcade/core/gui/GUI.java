package arcade.core.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.LogManager;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import com.formdev.flatlaf.FlatDarculaLaf;
import sim.display.SimApplet;
import arcade.core.ARCADE;

/**
 * Creates a {@code GUI} for selecting setup file and running the simulation.
 *
 * <p>When the model is called with arguments, then the {@code main} method in {@link
 * arcade.core.ARCADE} is called directly and {@code GUI} is not shown.
 */
public final class GUI implements ActionListener {
    /** GUI frame. */
    private static JFrame frame;

    /** Directory for file chooser. */
    private static String chooserDir = null;

    /** Text field for file. */
    private JTextField inputFileField;

    /** Text field for output path. */
    private JTextField outputPathField;

    /** Button for run simulation. */
    private JButton runButton;

    /** Text area for display. */
    private JTextArea displayArea;

    /** Check box for visualization. */
    private JCheckBox visCheck;

    /** Input file. */
    private File inputFile;

    /** Output path. */
    private File outputPath;

    /** Implementation name. */
    private String implementation;

    /**
     * Main function for running ARCADE simulations through the GUI.
     *
     * @param args list of command line arguments
     */
    public static void main(String[] args) throws Exception {
        FlatDarculaLaf.install();

        if (args.length > 0) {
            ARCADE.main(args);
        } else {
            SwingUtilities.invokeLater(GUI::createAndShowGUI);
        }
    }

    /** Creates the GUI and makes it visible. */
    private static void createAndShowGUI() {
        frame = new JFrame();

        // Create and set up the content pane.
        GUI gui = new GUI();
        frame.setContentPane(gui.createContent());

        // Creates an instance of a MASON SimApplet so the doQuit() method of
        // Console does not exit (so that the GUI console is not forced to close).
        new SimApplet();

        // Display the window.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Creates the content pane.
     *
     * @return the content panel
     */
    private JPanel createContent() {
        frame.setSize(1200, 700);

        JPanel screen = new JPanel();
        screen.setLayout(new BorderLayout());

        // Add north panel containing file select and run buttons.
        JPanel northPanel = new JPanel();
        northPanel.setBorder(makeBorder());
        GroupLayout grouping = prepGrouping(northPanel);

        // Label panel.
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.setBorder(new EmptyBorder(10, 0, 20, 0));

        JLabel titleLabel = new JLabel("<html><b><font size=+10>ARCADE</font></b> v3.1</html>");
        labelPanel.add(titleLabel);

        JLabel nameLabel =
                new JLabel(
                        "<html><b>Agent-based Representation "
                                + "of Cells And Dynamic Environments</b></html>");
        labelPanel.add(nameLabel);

        northPanel.add(labelPanel);

        // Select input file panel.
        addATextField("No input file selected", 40, northPanel);
        ((JTextField) northPanel.getComponent(1)).setEditable(false);
        inputFileField = ((JTextField) northPanel.getComponent(1));

        JButton inputSelectButton = addAButton("SELECT INPUT FILE", northPanel);
        inputSelectButton.addActionListener(this);
        inputSelectButton.setActionCommand("INPUT");

        // Select output path panel.
        addATextField("No output path selected", 40, northPanel);
        ((JTextField) northPanel.getComponent(3)).setEditable(false);
        outputPathField = ((JTextField) northPanel.getComponent(3));

        JButton outputSelectButton = addAButton("SELECT OUTPUT PATH", northPanel);
        outputSelectButton.addActionListener(this);
        outputSelectButton.setActionCommand("OUTPUT");

        // Implementation panel.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        implementation = "potts";

        JRadioButton patchImplButton = addARadioButton("patch implementation", buttonPanel);
        patchImplButton.addActionListener(this);
        patchImplButton.setActionCommand("patch");
        patchImplButton.setEnabled(false);

        JRadioButton pottImplButton = addARadioButton("potts implementation", buttonPanel);
        pottImplButton.addActionListener(this);
        pottImplButton.setActionCommand("potts");
        pottImplButton.setSelected(true);

        northPanel.add(buttonPanel);

        ButtonGroup implementations = new ButtonGroup();
        implementations.add(patchImplButton);
        implementations.add(pottImplButton);

        // Run simulation panel.
        runButton = addAButton("RUN SIMULATIONS", northPanel);
        runButton.setEnabled(false);
        runButton.addActionListener(this);
        runButton.setActionCommand("RUN");

        visCheck = addACheckBox("vis", northPanel);
        visCheck.setEnabled(false);
        visCheck.setSelected(false);

        grouping.setHorizontalGroup(
                grouping.createSequentialGroup()
                        .addGroup(
                                grouping.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(northPanel.getComponent(0))
                                        .addComponent(northPanel.getComponent(1))
                                        .addComponent(northPanel.getComponent(3))
                                        .addComponent(northPanel.getComponent(5)))
                        .addGroup(
                                grouping.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(northPanel.getComponent(2))
                                        .addComponent(northPanel.getComponent(4))
                                        .addComponent(northPanel.getComponent(6)))
                        .addGroup(
                                grouping.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(northPanel.getComponent(7))));

        grouping.setVerticalGroup(
                grouping.createSequentialGroup()
                        .addGroup(
                                grouping.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(northPanel.getComponent(0)))
                        .addGroup(
                                grouping.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(northPanel.getComponent(1))
                                        .addComponent(northPanel.getComponent(2)))
                        .addGroup(
                                grouping.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(northPanel.getComponent(3))
                                        .addComponent(northPanel.getComponent(4)))
                        .addGroup(
                                grouping.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(northPanel.getComponent(5))
                                        .addComponent(northPanel.getComponent(6))
                                        .addComponent(northPanel.getComponent(7))));

        screen.add(northPanel, BorderLayout.NORTH);

        // Add center panel containing text area.
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBorder(makeBorder());

        displayArea = addATextArea(centerPanel, "");
        PrintStream printStream = new PrintStream(new CustomOutputStream(displayArea));
        System.setOut(printStream);
        System.setErr(printStream);

        screen.add(centerPanel, BorderLayout.CENTER);

        return screen;
    }

    /**
     * Performs the action for the given event.
     *
     * @param event the action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();

        switch (cmd) {
            case "INPUT":
                inputFile = getFile();
                if (inputFile == null) {
                    inputFileField.setText("No input file selected");
                    runButton.setEnabled(false);
                    visCheck.setEnabled(false);
                } else {
                    displayArea.append("\nselected input xml [ " + inputFile.getName() + " ]\n");
                    inputFileField.setText(inputFile.getAbsolutePath());

                    if (outputPath != null) {
                        runButton.setEnabled(true);
                        visCheck.setEnabled(true);
                    } else {
                        runButton.setEnabled(false);
                        visCheck.setEnabled(false);
                    }
                }
                break;
            case "OUTPUT":
                outputPath = getPath();
                if (outputPath == null) {
                    outputPathField.setText("No output path selected");
                    runButton.setEnabled(false);
                    visCheck.setEnabled(false);
                } else {
                    displayArea.append("\nselected output path [ " + outputPath.getPath() + " ]\n");
                    outputPathField.setText(outputPath.getAbsolutePath());

                    if (inputFile != null) {
                        runButton.setEnabled(true);
                        visCheck.setEnabled(true);
                    } else {
                        runButton.setEnabled(false);
                        visCheck.setEnabled(false);
                    }
                }
                break;
            case "potts":
            case "patch":
                implementation = cmd;
                displayArea.append("\nselected implementation [ " + implementation + " ]\n");
                break;
            case "RUN":
                Thread thread =
                        new Thread(
                                () -> {
                                    try {
                                        displayArea.append("\n\n");
                                        LogManager.getLogManager().reset();

                                        if (visCheck.isSelected()) {
                                            ARCADE.main(
                                                    new String[] {
                                                        implementation,
                                                        inputFile.getAbsolutePath(),
                                                        outputPath.getAbsolutePath(),
                                                        "--vis"
                                                    });
                                        } else {
                                            ARCADE.main(
                                                    new String[] {
                                                        implementation,
                                                        inputFile.getAbsolutePath(),
                                                        outputPath.getAbsolutePath()
                                                    });
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                thread.start();

                break;
            default:
                break;
        }
    }

    /**
     * Opens dialog to select input file.
     *
     * @return the selected file, null if no file is selected
     */
    public File getFile() {
        JFileChooser chooser = new JFileChooser(chooserDir);
        chooser.setDialogTitle("Select input file ...");
        chooser.setCurrentDirectory(new File("."));
        chooser.addChoosableFileFilter(new XMLFileFilter());
        chooser.setAcceptAllFileFilterUsed(false);
        File file;

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            chooserDir = file.getParent();
        } else {
            return null;
        }

        return file;
    }

    /**
     * Opens dialog to select output path.
     *
     * @return the selected path, null if no path is selected
     */
    public File getPath() {
        JFileChooser chooser = new JFileChooser(chooserDir);
        chooser.setDialogTitle("Select output path ...");
        chooser.setCurrentDirectory(new File("."));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        File file;

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getCurrentDirectory();
            chooserDir = file.getParent();
        } else {
            return null;
        }

        return file;
    }

    /** Custom file filter for XML files. */
    private static class XMLFileFilter extends FileFilter {
        /**
         * Determines if the file is an XML file.
         *
         * @param file the file
         * @return {@code true} if the file is an XML, {@code false} otherwise
         */
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            String[] filename = file.getName().split("[.]");
            String extension = filename[filename.length - 1];
            return extension.equalsIgnoreCase("xml");
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    /** Custom output stream for console text. */
    public static class CustomOutputStream extends OutputStream {
        /** Text area for output. */
        private final JTextArea textArea;

        /**
         * Creates an output stream for the text area.
         *
         * @param textArea the text area
         */
        CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        /** Writes to the text area. */
        @Override
        public void write(int b) {
            // Redirects data to the text area.
            textArea.append(String.valueOf((char) b));

            // Scrolls the text area to the end of data.
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    /**
     * Adds a button to the container.
     *
     * @param text the text for the button
     * @param container the container to add to
     * @return the button object
     */
    private static JButton addAButton(String text, Container container) {
        JButton button = new JButton(text);
        container.add(button);
        return button;
    }

    /**
     * Adds a text field to the container.
     *
     * @param text the text for the field
     * @param length the length of the text field
     * @param container the container to add to
     * @return the text field object
     */
    private static JTextField addATextField(String text, int length, Container container) {
        JTextField textField = new JTextField(text, length);
        container.add(textField);
        return textField;
    }

    /**
     * Adds a check box to the container.
     *
     * @param text the text for the check box
     * @param container the container to add to
     * @return the check box object
     */
    private static JCheckBox addACheckBox(String text, Container container) {
        JCheckBox checkBox = new JCheckBox(text);
        container.add(checkBox);
        return checkBox;
    }

    /**
     * Adds a radio button to the container.
     *
     * @param text the text for the radio button
     * @param container the container to add to
     * @return the radio button object
     */
    private static JRadioButton addARadioButton(String text, Container container) {
        JRadioButton radioButton = new JRadioButton(text);
        container.add(radioButton);
        return radioButton;
    }

    /**
     * Adds a text area to the container.
     *
     * @param container the container to add to
     * @param text the text to add
     * @return the text area object
     */
    private static JTextArea addATextArea(Container container, String text) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        textArea.setText(text);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        container.add(scrollPane);
        return textArea;
    }

    /**
     * Prepares a grouping layout.
     *
     * @param pane the container
     * @return the group layout
     */
    private static GroupLayout prepGrouping(Container pane) {
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);
        return gl;
    }

    /**
     * Creates a border with just padding.
     *
     * @return the border
     */
    private static Border makeBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("test").getBorder(),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }
}
