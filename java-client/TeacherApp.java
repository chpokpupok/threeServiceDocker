import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TeacherApp extends JFrame {
    private JButton checkButton;
    private JButton gradeButton;
    private JButton commentButton;
    private JButton editButton;
    private JButton openLinkButton;

    private String supervisorId;
    private String supervisorLogin;
    private JTabbedPane tabbedPane;

    // Константы для API
    private static final String BASE_API_URL = "http://localhost:3002";
    private static final String PROCEDURE_API_URL = BASE_API_URL + "/api/procedure";
    private static final String FUNCTION_API_URL = BASE_API_URL + "/api/function";

    // API endpoints
    private static final String GET_COURSEWORKS_FOR_TEACHER_API_URL = FUNCTION_API_URL + "/get-courseworks";
    private static final String UPDATE_COURSEWORK_TEACHER_API_URL = PROCEDURE_API_URL + "/update_coursework";
    private static final String GET_STUDENT_CREDENTIALS_API_URL = FUNCTION_API_URL + "/student-credentials";
    private static final String INSERT_COURSEWORK_API_URL = PROCEDURE_API_URL + "/insert_coursework";
    private static final String GET_COURSEWORK_REPORT_API_URL = FUNCTION_API_URL + "/coursework-report";
    //
    private static final String IMPORT_STUDENTS_API_URL = PROCEDURE_API_URL + "/import_students_from_text";
    private static final String EXPORT_CREDENTIALS_API_URL = PROCEDURE_API_URL + "/export_student_credentials";
    private static final String MASS_CREATE_COURSEWORKS_API_URL = BASE_API_URL + "/api/procedure/insert_courseworks_for_students_from_file";
    //
    // Таблицы
    private JTable courseworkTable;
    private DefaultTableModel courseworkTableModel;
    private JTable studentsTable;
    private DefaultTableModel studentsTableModel;

    public TeacherApp(String supervisorLogin, String supervisorId) {
        this.supervisorLogin = supervisorLogin;
        this.supervisorId = supervisorId;

        setTitle("Личный кабинет преподавателя - " + supervisorLogin);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        createUI();
        loadCourseworks();
        loadStudentCredentials();

        setVisible(true);
    }

    private void createUI() {
        // Основная панель с вкладками
        tabbedPane = new JTabbedPane();

        // 1. Вкладка курсовых работ
        JPanel courseworkPanel = createCourseworkPanel();
        tabbedPane.addTab("Курсовые работы", courseworkPanel);

        // 2. Вкладка студентов
        JPanel studentsPanel = createStudentsPanel();
        tabbedPane.addTab("Студенты", studentsPanel);

        // 3. Вкладка создания курсовых
        JPanel createCourseworkPanel = createCreateCourseworkPanel();
        tabbedPane.addTab("Создать курсовую", createCourseworkPanel);

        add(tabbedPane);

        // Панель сверху с информацией
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(220, 240, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Преподаватель: " + supervisorLogin);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Обновить данные");
        refreshButton.addActionListener(e -> refreshAllData());
        topPanel.add(refreshButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    private JPanel createCourseworkPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Курсовые работы для проверки");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Таблица курсовых работ
        String[] columnNames = {"ID", "Студент", "Группа", "Дисциплина", "Тема", "Статус темы",
                "Статус работы", "Дата сдачи", "Дата защиты", "Итоговая оценка", "Комментарий", "Ссылка на работу"};
        courseworkTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseworkTable = new JTable(courseworkTableModel);
        courseworkTable.setRowHeight(25);

        // Слушатель выбора строки
        courseworkTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = courseworkTable.getSelectedRow();
                checkButton.setEnabled(selectedRow >= 0);
            }
        });

        JScrollPane scrollPane = new JScrollPane(courseworkTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок - ТОЛЬКО ОДНА КНОПКА
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Одна главная кнопка для проверки
        checkButton = createStyledButton("Проверить курсовую",
                new Color(30, 144, 255), 14);
        checkButton.setEnabled(false); // изначально выключена

        openLinkButton = createStyledButton("Открыть ссылку", new Color(52, 152, 219), 14);
        openLinkButton.setEnabled(false);

        checkButton.addActionListener(e -> checkSelectedCoursework());
        openLinkButton.addActionListener(e -> openWorkLink());

        buttonPanel.add(checkButton);
        buttonPanel.add(openLinkButton);

        // Кнопки импорта/экспорта (можно оставить)
        JButton importFileButton = createStyledButton("Импорт из файла",
                new Color(30, 144, 255), 14);
        JButton exportTxtButton = createStyledButton("Экспорт в TXT",
                new Color(46, 125, 50), 14);
        JButton exportCredentialsButton = createStyledButton("Экспорт учетных данных",
                new Color(255, 140, 0), 14);

        importFileButton.addActionListener(e -> importStudentsFromFile());
        exportTxtButton.addActionListener(e -> exportCredentialsToTxtFile());
        exportCredentialsButton.addActionListener(e -> exportCredentialsToFile());

        buttonPanel.add(importFileButton);
        buttonPanel.add(exportTxtButton);
        buttonPanel.add(exportCredentialsButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Студенты и учетные данные");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Таблица студентов
        String[] columnNames = {"ID", "Фамилия", "Имя", "Отчество", "Логин", "Пароль", "Группа", "Зачетка"};
        studentsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentsTable = new JTable(studentsTableModel);
        studentsTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(studentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JButton generateCredentialsButton = createStyledButton("Сгенерировать учетные данные",
                new Color(46, 125, 50), 14);
        JButton exportCredentialsButton = createStyledButton("Экспорт в файл",
                new Color(30, 144, 255), 14);
        JButton addStudentButton = createStyledButton("Добавить студента",
                new Color(255, 140, 0), 14);

        generateCredentialsButton.addActionListener(e -> generateStudentCredentials());
        exportCredentialsButton.addActionListener(e -> exportCredentialsToFile());
        addStudentButton.addActionListener(e -> showAddStudentDialog());

        buttonPanel.add(generateCredentialsButton);
        buttonPanel.add(exportCredentialsButton);
        buttonPanel.add(addStudentButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCreateCourseworkPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Заголовок
        JLabel titleLabel = new JLabel("Массовое создание курсовых работ из файла");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // 1. Кнопка загрузки файла
        gbc.gridwidth = 1;
        gbc.gridy++;
        panel.add(new JLabel("Файл со студентами:"), gbc);

        gbc.gridx = 1;
        JButton loadFileButton = new JButton("Загрузить файл");
        panel.add(loadFileButton, gbc);

        // Область для отображения содержимого файла
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JTextArea fileContentArea = new JTextArea(10, 40);
        fileContentArea.setEditable(false);
        fileContentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(fileContentArea);
        panel.add(scrollPane, gbc);

        // 2. Дисциплина
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Дисциплина:"), gbc);

        gbc.gridx = 1;
        JTextField disciplineField = new JTextField(20);
        disciplineField.setText("Программирование");
        panel.add(disciplineField, gbc);

        // 3. Тема курсовой
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Тема курсовой:"), gbc);

        gbc.gridx = 1;
        JTextField topicField = new JTextField(20);
        topicField.setText("Разработка веб-приложения");
        panel.add(topicField, gbc);

        // 4. Учебный год
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Учебный год:"), gbc);

        gbc.gridx = 1;
        JTextField yearField = new JTextField(20);
        yearField.setText("2024-09-01");
        panel.add(yearField, gbc);

        // 5. Кнопка создания курсовых
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton createButton = createStyledButton("Создать курсовые работы для всех студентов",
                new Color(46, 125, 50), 16);

        createButton.addActionListener(e -> {
            createCourseworksFromFile(
                    fileContentArea.getText(),
                    disciplineField.getText(),
                    topicField.getText(),
                    yearField.getText()
            );
        });
        panel.add(createButton, gbc);

        // Обработчик кнопки загрузки файла
        loadFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Выберите файл со списком студентов");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Текстовые файлы", "txt"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    int studentCount = 0;

                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                        studentCount++;
                    }

                    fileContentArea.setText(content.toString());

                    JOptionPane.showMessageDialog(this,
                            "Файл успешно загружен!\n" +
                                    "Имя файла: " + selectedFile.getName() + "\n" +
                                    "Найдено студентов: " + studentCount,
                            "Успех", JOptionPane.INFORMATION_MESSAGE);

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка при чтении файла: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private JButton createStyledButton(String text, Color color, int fontSize) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void loadCourseworks() {
        new Thread(() -> {
            try {
                URL url = new URL(GET_COURSEWORKS_FOR_TEACHER_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Отправляем supervisor_id
                String jsonInputString = String.format("{\"supervisor_id\": %s}", supervisorId);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray courseworks = jsonResponse.getJSONArray("data");

                    SwingUtilities.invokeLater(() -> {
                        courseworkTableModel.setRowCount(0);
                        for (int i = 0; i < courseworks.length(); i++) {
                            JSONObject cw = courseworks.getJSONObject(i);
                            Object[] row = {
                                    cw.getInt("coursework_id"),
                                    cw.getString("student_name"),
                                    cw.getString("group_name"),
                                    cw.getString("discipline_name"),
                                    cw.getString("topic"),
                                    cw.getString("topic_status"),
                                    cw.getString("work_status"),
                                    cw.optString("submission_date", ""),
                                    cw.optString("defense_date", ""),
                                    cw.optDouble("final_grade", 0),
                                    cw.optString("comment", ""),
                                    cw.optString("worklink", "")
                            };
                            courseworkTableModel.addRow(row);
                        }
                    });
                }

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка при загрузке курсовых работ: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void loadStudentCredentials() {
        new Thread(() -> {
            try {
                URL url = new URL(GET_STUDENT_CREDENTIALS_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray students = jsonResponse.getJSONArray("data");

                    SwingUtilities.invokeLater(() -> {
                        studentsTableModel.setRowCount(0);
                        for (int i = 0; i < students.length(); i++) {
                            JSONObject student = students.getJSONObject(i);
                            // Здесь нужно добавить запрос для получения дополнительной информации о студенте
                            // (группа, зачетка и т.д.)
                            Object[] row = {
                                    student.getInt("student_id"),
                                    student.getString("last_name"),
                                    student.getString("first_name"),
                                    "", // Отчество (нужно добавить в запрос)
                                    student.getString("generated_login"),
                                    student.getString("generated_password"),
                                    "", // Группа (нужно добавить в запрос)
                                    ""  // Зачетка (нужно добавить в запрос)
                            };
                            studentsTableModel.addRow(row);
                        }
                    });
                }

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    System.err.println("Ошибка при загрузке учетных данных: " + ex.getMessage());
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void enableCourseworkButtons() {
        // Включаем кнопки, если выбрана курсовая работа
        int selectedRow = courseworkTable.getSelectedRow();
        if (selectedRow != -1) {
            // Можно добавить логику проверки статуса для активации/деактивации кнопок
            Component[] components = ((JPanel)tabbedPane.getComponentAt(0)).getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    if (panel.getLayout() instanceof BorderLayout) {
                        Component southComp = ((BorderLayout)panel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
                        if (southComp instanceof JPanel) {
                            for (Component btn : ((JPanel)southComp).getComponents()) {
                                if (btn instanceof JButton) {
                                    btn.setEnabled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkSelectedCoursework() {
        int selectedRow = courseworkTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите курсовую работу для проверки", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseworkId = (int) courseworkTableModel.getValueAt(selectedRow, 0);
        String studentName = (String) courseworkTableModel.getValueAt(selectedRow, 1);
        String topic = (String) courseworkTableModel.getValueAt(selectedRow, 4);
        String currentTopicStatus = (String) courseworkTableModel.getValueAt(selectedRow, 5);
        String currentWorkStatus = (String) courseworkTableModel.getValueAt(selectedRow, 6);
        
        // Получаем текущие даты. Если они в формате ГГГГ-ММ-ДД, переводим их в ДД.ММ.ГГГГ для отображения
        String currentSubDateRaw = (String) courseworkTableModel.getValueAt(selectedRow, 7);
        String currentDefDateRaw = (String) courseworkTableModel.getValueAt(selectedRow, 8);
        
        String currentSubDate = formatDateForDisplay(currentSubDateRaw);
        String currentDefDate = formatDateForDisplay(currentDefDateRaw);
        
        String currentComment = (String) courseworkTableModel.getValueAt(selectedRow, 10);

        JDialog checkDialog = new JDialog(this, "Проверка курсовой работы: " + studentName, true);
        checkDialog.setLayout(new BorderLayout(10, 10));
        checkDialog.setSize(600, 650);
        checkDialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        contentPanel.add(new JLabel("Студент:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(new JLabel(studentName), gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Тема:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(new JLabel("<html>" + topic + "</html>"), gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Статус темы:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> topicStatusCombo = new JComboBox<>(new String[]{"-", "На проверке", "Принята", "Не принята"});
        topicStatusCombo.setSelectedItem(currentTopicStatus);
        contentPanel.add(topicStatusCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Статус работы:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> workStatusCombo = new JComboBox<>(
                new String[]{"-", "Черновик", "На проверке", "Принята", "Не принята", "Защищена", "Долг"});
        workStatusCombo.setSelectedItem(currentWorkStatus);
        contentPanel.add(workStatusCombo, gbc);

        // НОВЫЕ ПОЛЯ (Формат ДД.ММ.ГГГГ)
        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Дата сдачи (ДД.ММ.ГГГГ):"), gbc);
        gbc.gridx = 1;
        JTextField submissionDateField = new JTextField(currentSubDate, 15);
        contentPanel.add(submissionDateField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Дата защиты (ДД.ММ.ГГГГ):"), gbc);
        gbc.gridx = 1;
        JTextField defenseDateField = new JTextField(currentDefDate, 15);
        contentPanel.add(defenseDateField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        contentPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Теоретическая часть (0-5):"), gbc);
        gbc.gridx = 1;
        JSpinner theorySpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 5.0, 0.5));
        contentPanel.add(theorySpinner, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Практическая часть (0-5):"), gbc);
        gbc.gridx = 1;
        JSpinner practiceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 5.0, 0.5));
        contentPanel.add(practiceSpinner, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Оформление (0-5):"), gbc);
        gbc.gridx = 1;
        JSpinner designSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 5.0, 0.5));
        contentPanel.add(designSpinner, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Защита (0-5):"), gbc);
        gbc.gridx = 1;
        JSpinner defenseSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 5.0, 0.5));
        contentPanel.add(defenseSpinner, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Уровень антиплагиата (0-100):"), gbc);
        gbc.gridx = 1;
        JSpinner antiplagiatSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
        contentPanel.add(antiplagiatSpinner, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        contentPanel.add(new JLabel("Комментарий для студента:"), gbc);

        gbc.gridy++; gbc.gridheight = 3;
        JTextArea commentArea = new JTextArea(currentComment, 4, 40);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(commentArea);
        contentPanel.add(scrollPane, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveButton = new JButton("Сохранить изменения");
        saveButton.setBackground(new Color(46, 125, 50));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));

        JButton cancelButton = new JButton("Отмена");
        cancelButton.setBackground(new Color(200, 50, 50));
        cancelButton.setForeground(Color.WHITE);

        saveButton.addActionListener(e -> {
            String topicStatus = (String) topicStatusCombo.getSelectedItem();
            String workStatus = (String) workStatusCombo.getSelectedItem();
            String subDateStr = submissionDateField.getText().trim();
            String defDateStr = defenseDateField.getText().trim();
            
            // Проверка формата ДД.ММ.ГГГГ
            if (!subDateStr.isEmpty() && !subDateStr.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$")) {
                JOptionPane.showMessageDialog(checkDialog, "Неверный формат даты сдачи. Используйте ДД.ММ.ГГГГ", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!defDateStr.isEmpty() && !defDateStr.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$")) {
                JOptionPane.showMessageDialog(checkDialog, "Неверный формат даты защиты. Используйте ДД.ММ.ГГГГ", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // КОНВЕРТАЦИЯ: Переводим ДД.ММ.ГГГГ в ГГГГ-ММ-ДД для базы данных
            String subDateIso = convertDateToISO(subDateStr);
            String defDateIso = convertDateToISO(defDateStr);

            double theoryGrade = (double) theorySpinner.getValue();
            double practiceGrade = (double) practiceSpinner.getValue();
            double designGrade = (double) designSpinner.getValue();
            double defenseGrade = (double) defenseSpinner.getValue();
            double antiplagiatLevel = (double) antiplagiatSpinner.getValue();
            String comment = commentArea.getText();

            updateCourseworkWithAllData(courseworkId, topic, topicStatus, workStatus, subDateIso, defDateIso,
                    theoryGrade, practiceGrade, designGrade, defenseGrade, antiplagiatLevel, comment);
            checkDialog.dispose();
        });

        cancelButton.addActionListener(e -> checkDialog.dispose());
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        checkDialog.add(contentPanel, BorderLayout.CENTER);
        checkDialog.add(buttonPanel, BorderLayout.SOUTH);
        checkDialog.setVisible(true);
    }

    // Вспомогательный метод: ДД.ММ.ГГГГ -> ГГГГ-ММ-ДД
    private String convertDateToISO(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "";
        String[] parts = dateStr.split("\\.");
        if (parts.length == 3) {
            return parts[2] + "-" + parts[1] + "-" + parts[0]; 
        }
        return dateStr; 
    }

    // Вспомогательный метод: ГГГГ-ММ-ДД -> ДД.ММ.ГГГГ (для отображения в интерфейсе)
    private String formatDateForDisplay(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || dateStr.equals("null")) return "";
        String[] parts = dateStr.split("-");
        if (parts.length == 3) {
            // Если база вернула время, обрезаем его (например, 2024-05-12 00:00:00)
            if (parts[2].length() > 2) {
                parts[2] = parts[2].substring(0, 2);
            }
            return parts[2] + "." + parts[1] + "." + parts[0];
        }
        return dateStr;
    }

    private void gradeSelectedCoursework() {
        int selectedRow = courseworkTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите курсовую работу для выставления оценок",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseworkId = (int) courseworkTableModel.getValueAt(selectedRow, 0);

        // Диалог для выставления оценок
        JDialog gradeDialog = new JDialog(this, "Выставление оценок", true);
        gradeDialog.setLayout(new BorderLayout(10, 10));
        gradeDialog.setSize(400, 350);
        gradeDialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel.add(new JLabel("Теоретическая часть (0-5):"));
        JSpinner theorySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 0.5));
        contentPanel.add(theorySpinner);

        contentPanel.add(new JLabel("Практическая часть (0-5):"));
        JSpinner practiceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 0.5));
        contentPanel.add(practiceSpinner);

        contentPanel.add(new JLabel("Оформление (0-5):"));
        JSpinner designSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 0.5));
        contentPanel.add(designSpinner);

        contentPanel.add(new JLabel("Защита (0-5):"));
        JSpinner defenseSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 0.5));
        contentPanel.add(defenseSpinner);

        contentPanel.add(new JLabel("Уровень антиплагиата (0-100):"));
        JSpinner antiplagiatSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        contentPanel.add(antiplagiatSpinner);

        JButton saveButton = new JButton("Сохранить оценки");
        saveButton.addActionListener(e -> {
            double theoryGrade = (double) theorySpinner.getValue();
            double practiceGrade = (double) practiceSpinner.getValue();
            double designGrade = (double) designSpinner.getValue();
            double defenseGrade = (double) defenseSpinner.getValue();
            double antiplagiatLevel = (double) antiplagiatSpinner.getValue();

            updateCourseworkGrades(courseworkId, theoryGrade, practiceGrade,
                    designGrade, defenseGrade, antiplagiatLevel);
            gradeDialog.dispose();
        });

        gradeDialog.add(contentPanel, BorderLayout.CENTER);
        gradeDialog.add(saveButton, BorderLayout.SOUTH);

        gradeDialog.setVisible(true);
    }

    private void commentSelectedCoursework() {
        int selectedRow = courseworkTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите курсовую работу для добавления комментария",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseworkId = (int) courseworkTableModel.getValueAt(selectedRow, 0);
        String currentComment = (String) courseworkTableModel.getValueAt(selectedRow, 10);

        // Диалог для комментария
        String comment = JOptionPane.showInputDialog(this,
                "Введите комментарий к курсовой работе:", currentComment);

        if (comment != null && !comment.trim().isEmpty()) {
            updateCourseworkComment(courseworkId, comment.trim());
        }
    }

    private void updateCoursework(int courseworkId, String topicStatus, String workStatus, String comment) {
        new Thread(() -> {
            try {
                URL url = new URL(UPDATE_COURSEWORK_TEACHER_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("coursework_id", courseworkId);
                json.put("topic_status", topicStatus);
                json.put("work_status", workStatus);
                json.put("comment", comment);

                System.out.println("Отправка данных на обновление курсовой: " + json.toString());

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                System.out.println("Код ответа при обновлении курсовой: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean success = jsonResponse.getBoolean("success");

                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(this,
                                    "Курсовая работа успешно обновлена!",
                                    "Успех", JOptionPane.INFORMATION_MESSAGE);
                            loadCourseworks();
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Ошибка при обновлении курсовой работы",
                                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } else {
                    BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "Ошибка сервера: " + errorResponse.toString(),
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                }

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                });
            }
        }).start();
    }

    private void updateCourseworkGrades(int courseworkId, double theoryGrade, double practiceGrade,
                                        double designGrade, double defenseGrade, double antiplagiatLevel) {
        new Thread(() -> {
            try {
                URL url = new URL(UPDATE_COURSEWORK_TEACHER_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("coursework_id", courseworkId);
                json.put("theory_grade", theoryGrade);
                json.put("practice_grade", practiceGrade);
                json.put("design_grade", designGrade);
                json.put("defense_grade", defenseGrade);
                json.put("antiplagiat_level", antiplagiatLevel);
                json.put("work_status", "Защищена"); // После выставления оценок работа считается защищенной

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        JOptionPane.showMessageDialog(this,
                                "Оценки успешно сохранены!",
                                "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadCourseworks();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Ошибка при сохранении оценок",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void updateCourseworkComment(int courseworkId, String comment) {
        new Thread(() -> {
            try {
                URL url = new URL(UPDATE_COURSEWORK_TEACHER_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("coursework_id", courseworkId);
                json.put("comment", comment);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        JOptionPane.showMessageDialog(this,
                                "Комментарий успешно сохранен!",
                                "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadCourseworks();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Ошибка при сохранении комментария",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void generateStudentCredentials() {
        // Эта функция вызывает процедуру на сервере для генерации учетных данных
        // В реальной системе это может быть отдельная процедура или использование существующего триггера
        JOptionPane.showMessageDialog(this,
                "Учетные данные генерируются автоматически при добавлении студентов.\n" +
                        "Для просмотра существующих учетных данных используйте кнопку 'Обновить данные'.",
                "Информация", JOptionPane.INFORMATION_MESSAGE);

        loadStudentCredentials();
    }

    private void exportCredentialsToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить учетные данные");
        fileChooser.setSelectedFile(new File("student_credentials.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                for (int i = 0; i < studentsTableModel.getRowCount(); i++) {
                    writer.write(String.format("Студент: %s %s %s\n",
                            studentsTableModel.getValueAt(i, 1),
                            studentsTableModel.getValueAt(i, 2),
                            studentsTableModel.getValueAt(i, 3)));
                    writer.write(String.format("Логин: %s\n", studentsTableModel.getValueAt(i, 4)));
                    writer.write(String.format("Пароль: %s\n", studentsTableModel.getValueAt(i, 5)));
                    writer.write(String.format("Группа: %s\n", studentsTableModel.getValueAt(i, 6)));
                    writer.write(String.format("Зачетная книжка: %s\n\n", studentsTableModel.getValueAt(i, 7)));
                }

                JOptionPane.showMessageDialog(this,
                        "Учетные данные успешно экспортированы в файл!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при экспорте в файл: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCourseworkReport() {
        new Thread(() -> {
            try {
                URL url = new URL(GET_COURSEWORK_REPORT_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray reportData = jsonResponse.getJSONArray("data");

                    SwingUtilities.invokeLater(() -> {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setDialogTitle("Сохранить отчет по курсовым работам");
                        fileChooser.setSelectedFile(new File("coursework_report.csv"));

                        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                                // Заголовок CSV
                                writer.write("Студент;Тема работы;Дата сдачи;Дата защиты;Статус работы\n");

                                // Данные
                                for (int i = 0; i < reportData.length(); i++) {
                                    JSONObject row = reportData.getJSONObject(i);
                                    writer.write(String.format("%s;%s;%s;%s;%s\n",
                                            row.getString("student_name"),
                                            row.getString("topic"),
                                            row.optString("submission_date", ""),
                                            row.optString("defense_date", ""),
                                            row.getString("work_status")));
                                }

                                JOptionPane.showMessageDialog(this,
                                        "Отчет успешно экспортирован в файл!",
                                        "Успех", JOptionPane.INFORMATION_MESSAGE);

                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(this,
                                        "Ошибка при экспорте отчета: " + ex.getMessage(),
                                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                }

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка при получении отчета: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void createCoursework(String studentLogin, String discipline, String topic, String year) {
        if (studentLogin.isEmpty() || discipline.isEmpty() || topic.isEmpty() || year.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните все поля!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(INSERT_COURSEWORK_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("student_login", studentLogin);
                json.put("supervisor_login", supervisorLogin);
                json.put("discipline_name", discipline);
                json.put("topic", topic);
                json.put("year", year);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                        JOptionPane.showMessageDialog(this,
                                "Курсовая работа успешно создана!",
                                "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadCourseworks();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Ошибка при создании курсовой работы",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void showAddStudentDialog() {
        JDialog addStudentDialog = new JDialog(this, "Добавление студента", true);
        addStudentDialog.setLayout(new BorderLayout(10, 10));
        addStudentDialog.setSize(400, 400);
        addStudentDialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel.add(new JLabel("Фамилия:"));
        JTextField lastNameField = new JTextField();
        contentPanel.add(lastNameField);

        contentPanel.add(new JLabel("Имя:"));
        JTextField firstNameField = new JTextField();
        contentPanel.add(firstNameField);

        contentPanel.add(new JLabel("Отчество:"));
        JTextField middleNameField = new JTextField();
        contentPanel.add(middleNameField);

        contentPanel.add(new JLabel("Номер зачетной книжки:"));
        JTextField recordBookField = new JTextField();
        contentPanel.add(recordBookField);

        contentPanel.add(new JLabel("Группа:"));
        JTextField groupField = new JTextField();
        contentPanel.add(groupField);

        contentPanel.add(new JLabel("Телефон:"));
        JTextField phoneField = new JTextField();
        contentPanel.add(phoneField);

        contentPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        contentPanel.add(emailField);

        JButton saveButton = new JButton("Добавить студента");
        saveButton.addActionListener(e -> {
            // Здесь должен быть вызов API для добавления студента
            // Поскольку у вас уже есть триггер для генерации логина/пароля,
            // нужно использовать соответствующую процедуру или прямой INSERT
            JOptionPane.showMessageDialog(addStudentDialog,
                    "Функция добавления студента требует реализации соответствующего API\n" +
                            "Используйте процедуру add_students_to_group для массового добавления",
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
        });

        addStudentDialog.add(contentPanel, BorderLayout.CENTER);
        addStudentDialog.add(saveButton, BorderLayout.SOUTH);

        addStudentDialog.setVisible(true);
    }

    private void refreshAllData() {
        loadCourseworks();
        loadStudentCredentials();
        JOptionPane.showMessageDialog(this, "Данные обновлены!",
                "Обновление", JOptionPane.INFORMATION_MESSAGE);
    }
    //
        private void importStudentsFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл со списком студентов");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Текстовые файлы", "txt"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            JTextField groupField = new JTextField(10);
            JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
            panel.add(new JLabel("Название группы:"));
            panel.add(groupField);
            panel.add(new JLabel("Формат файла:"));
            panel.add(new JLabel("Фамилия Имя Отчество Номер_зачетки"));

            int result = JOptionPane.showConfirmDialog(this, panel, "Введите название группы", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String groupName = groupField.getText().trim();
                if (groupName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Введите название группы", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                new Thread(() -> {
                    try {
                        // Читаем файл
                        StringBuilder fileContent = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                fileContent.append(line).append("\n");
                            }
                        }

                        // Отправляем на сервер
                        URL url = new URL(IMPORT_STUDENTS_API_URL);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setDoOutput(true);

                        JSONObject json = new JSONObject();
                        json.put("group_name", groupName);
                        json.put("students_text", fileContent.toString());

                        try (OutputStream os = connection.getOutputStream()) {
                            byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                            os.write(input, 0, input.length);
                        }

                        int responseCode = connection.getResponseCode();
                        
                        // ЧИТАЕМ ОТВЕТ СРАЗУ, ДО DISCONNECT
                        StringBuilder response = new StringBuilder();
                        if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = in.readLine()) != null) {
                                    response.append(line);
                                }
                            }
                        } else {
                            InputStream errorStream = connection.getErrorStream();
                            if (errorStream != null) {
                                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                                    String line;
                                    while ((line = errorReader.readLine()) != null) {
                                        response.append(line);
                                    }
                                }
                            }
                        }
                        
                        connection.disconnect(); // Закрываем соединение

                        // Обновляем интерфейс
                        SwingUtilities.invokeLater(() -> {
                            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                                JOptionPane.showMessageDialog(TeacherApp.this,
                                        "Студенты успешно импортированы из файла!\n" +
                                                "Файл: " + selectedFile.getName() + "\n" +
                                                "Группа: " + groupName,
                                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                                loadStudentCredentials(); // Обновляем список
                            } else {
                                // Показываем РЕАЛЬНУЮ ошибку от сервера
                                JOptionPane.showMessageDialog(TeacherApp.this,
                                        "Ошибка сервера (HTTP " + responseCode + "):\n" + response.toString(),
                                        "Ошибка импорта", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(TeacherApp.this,
                                    "Ошибка при обработке файла: " + ex.getMessage(),
                                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                        });
                        ex.printStackTrace();
                    }
                }).start();
            }
        }
    }

    private void generatePlan(String topic) {
    new Thread(() -> {
        try {
            URL url = new URL(BASE_API_URL + "/api/ai/generate-plan");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("topic", topic);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = connection.getResponseCode();

            if (code == 200) {
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8")
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                JSONObject json = new JSONObject(response.toString());
                String plan = json.optString("plan", "План не получен");

                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, plan, "План курсовой", JOptionPane.INFORMATION_MESSAGE)
                );
            } else {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, "Ошибка AI-сервиса. Код: " + code)
                );
            }

            connection.disconnect();
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage())
            );
        }
    }).start();
}

    private void exportCredentialsToTxtFile() {
        // Диалог для выбора группы
        JTextField groupField = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
        panel.add(new JLabel("Название группы:"));
        panel.add(groupField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Экспорт учетных данных", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String groupName = groupField.getText().trim();
            if (groupName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите название группы",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new Thread(() -> {
                try {
                    URL url = new URL(EXPORT_CREDENTIALS_API_URL + "?group_name=" +
                            URLEncoder.encode(groupName, "UTF-8"));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        JSONArray credentials = jsonResponse.getJSONArray("data");

                        SwingUtilities.invokeLater(() -> {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setDialogTitle("Сохранить учетные данные");
                            fileChooser.setSelectedFile(new File("credentials_" + groupName + ".txt"));

                            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                                try (BufferedWriter writer = new BufferedWriter(
                                        new FileWriter(fileChooser.getSelectedFile()))) {

                                    writer.write("=".repeat(60) + "\n");
                                    writer.write("УЧЕТНЫЕ ДАННЫЕ СТУДЕНТОВ\n");
                                    writer.write("Группа: " + groupName + "\n");
                                    writer.write("Дата экспорта: " + new java.util.Date() + "\n");
                                    writer.write("=".repeat(60) + "\n\n");

                                    for (int i = 0; i < credentials.length(); i++) {
                                        JSONObject student = credentials.getJSONObject(i);
                                        writer.write(String.format("СТУДЕНТ %d\n", i + 1));
                                        writer.write(String.format("ФИО: %s %s %s\n",
                                                student.getString("last_name"),
                                                student.getString("first_name"),
                                                student.optString("middle_name", "")));
                                        writer.write(String.format("Номер зачетки: %s\n",
                                                student.getString("record_book")));
                                        writer.write(String.format("Логин: %s\n",
                                                student.getString("login")));
                                        writer.write(String.format("Пароль: %s\n",
                                                student.getString("password")));
                                        writer.write(String.format("Группа: %s\n",
                                                student.getString("group_name")));
                                        writer.write("-".repeat(40) + "\n\n");
                                    }

                                    writer.write("\nВсего студентов: " + credentials.length() + "\n");

                                    JOptionPane.showMessageDialog(this,
                                            "Учетные данные успешно экспортированы в файл!\n" +
                                                    "Файл: " + fileChooser.getSelectedFile().getName() + "\n" +
                                                    "Студентов: " + credentials.length(),
                                            "Успех", JOptionPane.INFORMATION_MESSAGE);

                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(this,
                                            "Ошибка при записи файла: " + ex.getMessage(),
                                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        });
                    }

                    connection.disconnect();

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "Ошибка при получении данных: " + ex.getMessage(),
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    ex.printStackTrace();
                }
            }).start();
        }
    }

    private void createCourseworksFromFile(String studentsText, String discipline,
                                           String topic, String year) {
        if (studentsText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Сначала загрузите файл со студентами!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (discipline.isEmpty() || topic.isEmpty() || year.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Заполните все поля: дисциплина, тема и учебный год!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Подсчет количества строк (студентов)
        int studentCount = studentsText.trim().split("\n").length;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Создать курсовые работы для " + studentCount + " студентов?\n\n" +
                        "Дисциплина: " + discipline + "\n" +
                        "Тема: " + topic + "\n" +
                        "Учебный год: " + year + "\n\n" +
                        "Курсовые работы будут добавлены в базу данных и станут доступны студентам.",
                "Подтверждение создания", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            sendMassCreateCourseworkRequest(studentsText, discipline, topic, year);
        }
    }

    // Метод отправки запроса на массовое создание курсовых
    private void sendMassCreateCourseworkRequest(String studentsText, String discipline,
                                                 String topic, String year) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_API_URL + "/api/procedure/insert_courseworks_for_students_from_file");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000); // Увеличиваем таймаут для больших файлов
                connection.setReadTimeout(10000);

                JSONObject json = new JSONObject();
                json.put("students_text", studentsText);
                json.put("supervisor_login", supervisorLogin); // Используем текущего преподавателя
                json.put("discipline_name", discipline);
                json.put("topic", topic);
                json.put("year", year);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_CREATED ||
                            responseCode == HttpURLConnection.HTTP_OK) {

                        // Подсчет успешно созданных курсовых
                        int createdCount = studentsText.trim().split("\n").length;

                        JOptionPane.showMessageDialog(this,
                                "✓ Курсовые работы успешно созданы!\n\n" +
                                        "Создано курсовых работ: " + createdCount + "\n" +
                                        "Дисциплина: " + discipline + "\n" +
                                        "Тема: " + topic + "\n" +
                                        "Учебный год: " + year + "\n\n" +
                                        "Студенты могут увидеть курсовые работы в своих личных кабинетах.",
                                "Успех", JOptionPane.INFORMATION_MESSAGE);

                    } else {
                        // Чтение сообщения об ошибке
                        String errorMessage;
                        try {
                            BufferedReader errorReader = new BufferedReader(
                                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                            StringBuilder errorResponse = new StringBuilder();
                            String line;
                            while ((line = errorReader.readLine()) != null) {
                                errorResponse.append(line);
                            }
                            errorReader.close();

                            try {
                                JSONObject errorJson = new JSONObject(errorResponse.toString());
                                errorMessage = errorJson.optString("error", errorJson.optString("message", "Неизвестная ошибка"));
                            } catch (JSONException e) {
                                errorMessage = errorResponse.toString();
                            }
                        } catch (Exception e) {
                            errorMessage = "Ошибка сервера: " + responseCode;
                        }

                        JOptionPane.showMessageDialog(this,
                                "Ошибка при создании курсовых работ:\n" + errorMessage,
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка соединения: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }
    private void enableCourseworkButtons(boolean enabled) {
        checkButton.setEnabled(enabled);
        gradeButton.setEnabled(enabled);
        commentButton.setEnabled(enabled);
        editButton.setEnabled(enabled);
        openLinkButton.setEnabled(enabled);

        // Дополнительная логика: оценку можно ставить только если работа "На проверке" или "Принята"
        int selectedRow = courseworkTable.getSelectedRow();
        if (enabled && selectedRow >= 0) {
            String workStatus = (String) courseworkTableModel.getValueAt(selectedRow, 6);
            String topicStatus = (String) courseworkTableModel.getValueAt(selectedRow, 5);

            // Можно выставлять оценки только если работа на проверке или принята
            gradeButton.setEnabled("На проверке".equals(workStatus) || "Принята".equals(workStatus));

            // Редактировать можно всегда
            editButton.setEnabled(true);
        }
    }

    private void editSelectedCoursework() {
        int selectedRow = courseworkTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите курсовую работу для редактирования",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseworkId = (int) courseworkTableModel.getValueAt(selectedRow, 0);
        String currentTopic = (String) courseworkTableModel.getValueAt(selectedRow, 4);
        String currentTopicStatus = (String) courseworkTableModel.getValueAt(selectedRow, 5);
        String currentWorkStatus = (String) courseworkTableModel.getValueAt(selectedRow, 6);
        String currentSubmissionDate = (String) courseworkTableModel.getValueAt(selectedRow, 7);
        String currentDefenseDate = (String) courseworkTableModel.getValueAt(selectedRow, 8);
        double currentFinalGrade = (double) courseworkTableModel.getValueAt(selectedRow, 9);
        String currentComment = (String) courseworkTableModel.getValueAt(selectedRow, 10);

        // Диалог для полного редактирования курсовой
        JDialog editDialog = new JDialog(this, "Редактирование курсовой работы", true);
        editDialog.setLayout(new BorderLayout(10, 10));
        editDialog.setSize(500, 500);
        editDialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Тема
        contentPanel.add(new JLabel("Тема работы:"), gbc);
        gbc.gridx = 1;
        JTextField topicField = new JTextField(currentTopic, 30);
        contentPanel.add(topicField, gbc);

        // Статус темы
        gbc.gridx = 0;
        gbc.gridy++;
        contentPanel.add(new JLabel("Статус темы:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> topicStatusCombo = new JComboBox<>(
                new String[]{"-", "На проверке", "Принята", "Не принята"});
        topicStatusCombo.setSelectedItem(currentTopicStatus);
        contentPanel.add(topicStatusCombo, gbc);

        // Статус работы
        gbc.gridx = 0;
        gbc.gridy++;
        contentPanel.add(new JLabel("Статус работы:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> workStatusCombo = new JComboBox<>(
                new String[]{"-", "Черновик", "На проверке", "Принята", "Не принята", "Защищена", "Долг"});
        workStatusCombo.setSelectedItem(currentWorkStatus);
        contentPanel.add(workStatusCombo, gbc);

        // Дата сдачи
        gbc.gridx = 0;
        gbc.gridy++;
        contentPanel.add(new JLabel("Дата сдачи (ГГГГ-ММ-ДД):"), gbc);
        gbc.gridx = 1;
        JTextField submissionDateField = new JTextField(currentSubmissionDate, 15);
        contentPanel.add(submissionDateField, gbc);

        // Дата защиты
        gbc.gridx = 0;
        gbc.gridy++;
        contentPanel.add(new JLabel("Дата защиты (ГГГГ-ММ-ДД):"), gbc);
        gbc.gridx = 1;
        JTextField defenseDateField = new JTextField(currentDefenseDate, 15);
        contentPanel.add(defenseDateField, gbc);

        // Итоговая оценка
        gbc.gridx = 0;
        gbc.gridy++;
        contentPanel.add(new JLabel("Итоговая оценка:"), gbc);
        gbc.gridx = 1;
        JTextField gradeField = new JTextField(String.valueOf(currentFinalGrade), 10);
        contentPanel.add(gradeField, gbc);

        // Комментарий
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        contentPanel.add(new JLabel("Комментарий:"), gbc);

        gbc.gridy++;
        gbc.gridheight = 3;
        JTextArea commentArea = new JTextArea(currentComment, 5, 40);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(commentArea);
        contentPanel.add(scrollPane, gbc);

        JButton saveButton = new JButton("Сохранить изменения");
        saveButton.addActionListener(e -> {
            String topic = topicField.getText();
            String topicStatus = (String) topicStatusCombo.getSelectedItem();
            String workStatus = (String) workStatusCombo.getSelectedItem();
            String submissionDate = submissionDateField.getText();
            String defenseDate = defenseDateField.getText();
            String gradeText = gradeField.getText();
            String comment = commentArea.getText();

            // Валидация дат
            if (!submissionDate.isEmpty() && !submissionDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(editDialog,
                        "Неверный формат даты сдачи. Используйте ГГГГ-ММ-ДД",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!defenseDate.isEmpty() && !defenseDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(editDialog,
                        "Неверный формат даты защиты. Используйте ГГГГ-ММ-ДД",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            updateCourseworkFull(courseworkId, topic, topicStatus, workStatus,
                    submissionDate.isEmpty() ? null : submissionDate,
                    defenseDate.isEmpty() ? null : defenseDate,
                    gradeText.isEmpty() ? null : Double.parseDouble(gradeText),
                    comment);
            editDialog.dispose();
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> editDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        editDialog.add(contentPanel, BorderLayout.CENTER);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);

        editDialog.setVisible(true);
    }

    private void updateCourseworkFull(int courseworkId, String topic, String topicStatus,
                                      String workStatus, String submissionDate,
                                      String defenseDate, Double finalGrade, String comment) {
        new Thread(() -> {
            try {
                URL url = new URL(UPDATE_COURSEWORK_TEACHER_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("coursework_id", courseworkId);
                json.put("topic", topic);
                json.put("topic_status", topicStatus);
                json.put("work_status", workStatus);

                if (submissionDate != null && !submissionDate.isEmpty()) {
                    json.put("submission_date", submissionDate);
                }

                if (defenseDate != null && !defenseDate.isEmpty()) {
                    json.put("defense_date", defenseDate);
                }

                if (finalGrade != null) {
                    json.put("final_grade", finalGrade);
                }

                json.put("comment", comment);

                System.out.println("Полное обновление курсовой: " + json.toString());

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                System.out.println("Код ответа при полном обновлении: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean success = jsonResponse.getBoolean("success");

                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(this,
                                    "Курсовая работа успешно обновлена!",
                                    "Успех", JOptionPane.INFORMATION_MESSAGE);
                            loadCourseworks();
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Ошибка при обновлении курсовой работы",
                                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } else {
                    BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "Ошибка сервера: " + errorResponse.toString(),
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                }

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                });
            }
        }).start();
    }
    private void openWorkLink() {
        int selectedRow = courseworkTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите курсовую работу!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Предполагаем, что worklink возвращается из API как 11-й столбец (после comment)
        String workLink = (String) courseworkTableModel.getValueAt(selectedRow, 11);
        if (workLink == null || workLink.trim().isEmpty() || workLink.equals("https://...")) {
            JOptionPane.showMessageDialog(this, "Ссылка на работу не прикреплена!", "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(workLink));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Не удалось открыть ссылку: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

        private void updateCourseworkWithAllData(int courseworkId, String topic, String topicStatus,
                                             String workStatus, String submissionDate, String defenseDate,
                                             double theoryGrade, double practiceGrade,
                                             double designGrade, double defenseGrade,
                                             double antiplagiatLevel, String comment) {
        new Thread(() -> {
            try {
                URL url = new URL(UPDATE_COURSEWORK_TEACHER_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("coursework_id", courseworkId);
                json.put("topic", topic);
                json.put("topic_status", topicStatus);
                json.put("work_status", workStatus);

                // Отправляем даты, если они заполнены
                if (submissionDate != null && !submissionDate.isEmpty()) {
                    json.put("submission_date", submissionDate);
                }
                if (defenseDate != null && !defenseDate.isEmpty()) {
                    json.put("defense_date", defenseDate);
                }

                if (theoryGrade > 0) json.put("theory_grade", theoryGrade);
                if (practiceGrade > 0) json.put("practice_grade", practiceGrade);
                if (designGrade > 0) json.put("design_grade", designGrade);
                if (defenseGrade > 0) json.put("defense_grade", defenseGrade);
                if (antiplagiatLevel > 0) json.put("antiplagiat_level", antiplagiatLevel);

                json.put("comment", comment);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                
                BufferedReader reader;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();

                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        JOptionPane.showMessageDialog(this, "Изменения успешно сохранены!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadCourseworks(); // Обновляем таблицу
                    } else {
                        JOptionPane.showMessageDialog(this, "Ошибка сервера: " + response.toString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }





}