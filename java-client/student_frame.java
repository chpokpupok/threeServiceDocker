import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.*;

public class student_frame extends JFrame {
    private JPanel centerPanel;
    private ArrayList<CourseworkPanel> courseworkPanels = new ArrayList<>();

    // Добавляем поле для логина студента
    private String studentLogin;
    private String studentId;

    // Константы для API
    private static final String BASE_API_URL = "http://localhost:3002";
    private static final String PROCEDURE_API_URL = BASE_API_URL + "/api/procedure";
    private static final String QUERY_API_URL = BASE_API_URL + "/api/query";
    private static final String FUNCTION_API_URL = BASE_API_URL + "/api/function";

    // Процедуры для студента
    private static final String INSERT_COURSEWORK_API_URL = BASE_API_URL + "/api/procedure/insert_coursework";
    private static final String UPDATE_COURSEWORK_STUDENT_API_URL = BASE_API_URL + "/api/procedure/update_coursework_student";
    private static final String DELETE_COURSEWORK_API_URL = BASE_API_URL + "/api/procedure/delete_coursework";
    private static final String SUBMIT_COURSEWORK_API_URL = BASE_API_URL + "/api/procedure/update_coursework_student";

    // Обычные запросы
    private static final String GET_STUDENT_BY_LOGIN_API_URL = QUERY_API_URL + "/student-by-login/";
    private static final String GET_COURSEWORKS_FOR_STUDENT_API_URL = FUNCTION_API_URL + "/get_courseworks_for_student/";

    // Внутренний класс для хранения данных о курсовой
    private class CourseworkPanel {
        String title;
        String subject;
        String supervisorName;
        JPanel panel;
        JTextField topicField;
        JTextField linkField;
        JCheckBox selectCheckBox;
        JLabel statusLabel;
        JLabel gradeLabel;
        JTextArea commentArea;
        JButton submitButton;
        JButton saveButton;
        boolean submitted = false;
        int courseworkId = -1; // ID из базы данных
        String status = "черновик";
        String topicStatus = "-";
        String finalGrade = "-";
        String comment = "";

        CourseworkPanel(String title, String subject, String supervisorName, JPanel panel,
                        JTextField topicField, JTextField linkField, JCheckBox selectCheckBox,
                        JLabel statusLabel, JLabel gradeLabel, JTextArea commentArea,
                        JButton submitButton, JButton saveButton) {
            this.title = title;
            this.subject = subject;
            this.supervisorName = supervisorName;
            this.panel = panel;
            this.topicField = topicField;
            this.linkField = linkField;
            this.selectCheckBox = selectCheckBox;
            this.statusLabel = statusLabel;
            this.gradeLabel = gradeLabel;
            this.commentArea = commentArea;
            this.submitButton = submitButton;
            this.saveButton = saveButton;
        }
    }

    // Конструктор с параметром логина
    public student_frame(String studentLogin) {
        this.studentLogin = studentLogin;

        // Настройка окна
        setTitle("Окно студента - " + studentLogin);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);


        // Создаем интерфейс
        createUI();

        // Получаем student_id по логину
        getStudentId();

        // Показываем окно
        setVisible(true);
    }

    private void getStudentId() {
        new Thread(() -> {
            try {
                URL url = new URL(GET_STUDENT_BY_LOGIN_API_URL + studentLogin);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject json = new JSONObject(response.toString());
                    studentId = String.valueOf(json.getInt("student_id"));

                    SwingUtilities.invokeLater(() -> {
                        setTitle("Окно студента - " + studentLogin + " (ID: " + studentId + ")");
                        System.out.println(studentLogin);
                        loadCourseworksFromDatabase();
                    });
                } else {
                    System.out.println("Не удалось получить ID студента. Код: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        System.err.println("Ошибка при получении ID студента: " + ex.getMessage())
                );
                ex.printStackTrace();
            }
        }).start();
    }

    private void createUI() {
        // Основная панель
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Верхняя панель с заголовком
        JPanel topPanel = createTopPanel();

        // Центральная панель с курсовыми работами
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Прокрутка если много работ
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Мои курсовые работы"));

        // Нижняя панель с кнопками
        JPanel bottomPanel = createBottomPanel();

        // Собираем всё вместе
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Добавляем основную панель в окно
        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(220, 240, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Личный кабинет студента: " + studentLogin);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Обновить список");
        refreshButton.addActionListener(e -> loadCourseworksFromDatabase());
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton addButton = createStyledButton("+ Добавить курсовую", new Color(46, 125, 50), 14);
        JButton submitSelectedButton = createStyledButton("-> Отправить выбранные", new Color(30, 144, 255), 14);
        JButton submitAllButton = createStyledButton("-> Отправить все", new Color(65, 105, 225), 14);

        addButton.addActionListener(e -> addNewCoursework());
        submitSelectedButton.addActionListener(e -> submitSelectedForReview());
        submitAllButton.addActionListener(e -> submitAllForReview());

        panel.add(addButton);
        panel.add(submitSelectedButton);
        panel.add(submitAllButton);

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

    private void loadCourseworksFromDatabase() {
        if (studentId == null) {
            JOptionPane.showMessageDialog(this, "ID студента не получен. Попробуйте перезайти.");
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(GET_COURSEWORKS_FOR_STUDENT_API_URL + studentId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                
                // Читаем ответ в зависимости от кода
                BufferedReader reader;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                }
                
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();
                connection.disconnect();

                SwingUtilities.invokeLater(() -> {
                    centerPanel.removeAll();
                    courseworkPanels.clear();
                });

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray data = jsonResponse.getJSONArray("data");

                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            String topic = item.getString("topic");
                            String subject = item.optString("discipline_name", "Неизвестная дисциплина");
                            String supervisorName = item.optString("supervisor_name", "Неизвестный преподаватель");
                            int courseworkId = item.getInt("coursework_id");

                            addCourseworkToPanel("Курсовая " + (i + 1), subject, supervisorName, item, courseworkId);
                        }

                        if (data.length() == 0) {
                            JLabel noWorksLabel = new JLabel("У вас пока нет курсовых работ");
                            noWorksLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                            noWorksLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            centerPanel.add(noWorksLabel);
                        }

                        centerPanel.revalidate();
                        centerPanel.repaint();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        // Показываем точный ответ от сервера
                        JOptionPane.showMessageDialog(this,
                                "Ошибка загрузки (HTTP " + responseCode + "):\n" + response.toString(),
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                }

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

    private void addCourseworkToPanel(String title, String subject, String supervisorName,
                                      JSONObject item, int courseworkId) {
        CourseworkPanel cwPanel = createCourseworkPanel(title, subject, supervisorName, item, courseworkId);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(cwPanel.panel);
        courseworkPanels.add(cwPanel);
    }

    private CourseworkPanel createCourseworkPanel(String title, String subject, String supervisorName,
                                                  JSONObject item, int courseworkId) {
        String topic = item.getString("topic");
        String workLink = item.optString("work_link", "");
        String status = item.optString("work_status", "черновик");
        String comment = item.optString("comment", "");
        String finalGrade = item.optString("final_grade", "");
        String topicStatus = item.optString("topic_status", "");

        JLabel gradeLabel = new JLabel("Итоговая оценка:");
        JLabel gradeValueLabel = new JLabel(finalGrade.isEmpty() ? "-" : finalGrade);
        gradeValueLabel.setFont(new Font("Arial", Font.BOLD, 11));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Устанавливаем цвет в зависимости от статуса
        if ("Защищена".equals(status) || "Принята".equals(status)) {
            panel.setBackground(new Color(220, 255, 220)); // Зеленый для сданных
        } else if ("На проверке".equals(status)) {
            panel.setBackground(new Color(255, 255, 200)); // Желтый для проверки
        } else if ("не принята".equals(status) || "Долг".equals(status)) {
            panel.setBackground(new Color(255, 220, 220)); // Красный для отклоненных
        } else if (!finalGrade.isEmpty()) {
            try {
                double grade = Double.parseDouble(finalGrade);
                if (grade >= 3.0) {
                    panel.setBackground(new Color(144, 238, 144)); // Светло-зеленый для сданных (3-5)
                    gradeValueLabel.setForeground(new Color(0, 100, 0)); // Темно-зеленый текст
                    gradeValueLabel.setFont(new Font("Arial", Font.BOLD, 12));
                }
            } catch (NumberFormatException e) {
            }
        } else {
            panel.setBackground(new Color(245, 245, 245));
        }

        panel.setMaximumSize(new Dimension(850, 300));

        // Верхняя панель с чекбоксом и заголовком
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Чекбокс для выбора
        JCheckBox selectCheckBox = new JCheckBox();
        selectCheckBox.setSelected(false);

        // Заголовок
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b><br>" +
                "Дисциплина: " + subject + "<br>" +
                "Преподаватель: " + supervisorName + "<br>" +
                "Статус: " + status + "</html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(0, 51, 102));

        topPanel.add(selectCheckBox, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        // Основная панель с полями
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // Левая панель: поля для ввода
        JPanel leftPanel = new JPanel(new GridLayout(2, 2, 10, 10));

                JLabel topicLabel = new JLabel("Тема работы:");
        JTextField topicField = new JTextField();
        topicField.setText(topic);

        // Панель для темы + кнопки ИИ
        JPanel topicPanel = new JPanel(new BorderLayout(5, 0));
        topicPanel.add(topicField, BorderLayout.CENTER);
        
        JButton generatePlanBtn = new JButton("🤖 План");
        generatePlanBtn.setBackground(new Color(138, 43, 226)); // Фиолетовый
        generatePlanBtn.setForeground(Color.WHITE);
        generatePlanBtn.setFont(new Font("Arial", Font.BOLD, 11));
        generatePlanBtn.setFocusPainted(false);
        generatePlanBtn.setToolTipText("Сгенерировать план курсовой с помощью ИИ");
        
        generatePlanBtn.addActionListener(e -> {
            String currentTopic = topicField.getText().trim();
            if (currentTopic.isEmpty() || currentTopic.equals("Введите тему...")) {
                JOptionPane.showMessageDialog(this, "Сначала введите тему работы!", "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }
            generatePlan(currentTopic, generatePlanBtn);
        });
        topicPanel.add(generatePlanBtn, BorderLayout.EAST);

        JLabel linkLabel = new JLabel("Ссылка на материалы:");
        JTextField linkField = new JTextField();
        linkField.setText(workLink.isEmpty() ? "https://..." : workLink);

        setupTextFieldPlaceholder(topicField, "Введите тему...");
        setupTextFieldPlaceholder(linkField, "https://...");

        leftPanel.add(topicLabel);
        leftPanel.add(topicPanel); // <--- Добавляем панель с кнопкой
        leftPanel.add(linkLabel);
        leftPanel.add(linkField);

        // Правая панель: информация и комментарии
        JPanel rightPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Информация"));

        JLabel statusLabel = new JLabel("Статус:");
        JLabel statusValueLabel = new JLabel(status);
        statusValueLabel.setFont(new Font("Arial", Font.BOLD, 11));

        JLabel topicStatusLabel = new JLabel("Статус темы:");
        JLabel topicStatusValueLabel = new JLabel(topicStatus.isEmpty() ? "-" : topicStatus);
        topicStatusValueLabel.setFont(new Font("Arial", Font.BOLD, 11));

        JLabel commentLabel = new JLabel("Комментарий:");
        JTextArea commentArea = new JTextArea(comment);
        commentArea.setEditable(false);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setRows(3);
        commentArea.setBackground(new Color(255, 255, 225));

        rightPanel.add(statusLabel);
        rightPanel.add(statusValueLabel);
        rightPanel.add(gradeLabel);
        rightPanel.add(gradeValueLabel);
        rightPanel.add(topicStatusLabel);
        rightPanel.add(topicStatusValueLabel);
        rightPanel.add(commentLabel);
        rightPanel.add(new JScrollPane(commentArea));

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton saveButton = new JButton("Сохранить");
        saveButton.setFont(new Font("Arial", Font.PLAIN, 12));
        saveButton.setBackground(new Color(50, 150, 50));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        JButton submitButton = new JButton("Отправить на проверку");
        submitButton.setFont(new Font("Arial", Font.PLAIN, 12));
        submitButton.setBackground(new Color(30, 144, 255));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);

        JButton deleteButton = new JButton("Удалить");
        deleteButton.setFont(new Font("Arial", Font.PLAIN, 12));
        deleteButton.setBackground(new Color(200, 50, 50));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);

        saveButton.addActionListener(e -> saveCourseworkData(courseworkId, topicField, linkField));
        submitButton.addActionListener(e -> submitCourseworkForReview(courseworkId, linkField.getText(), submitButton, panel));
        deleteButton.addActionListener(e -> deleteCoursework(courseworkId, panel));

        buttonPanel.add(saveButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(deleteButton);

        // Создаем объект CourseworkPanel
        CourseworkPanel cwPanel = new CourseworkPanel(title, subject, supervisorName, panel,
                topicField, linkField, selectCheckBox, statusValueLabel,
                gradeValueLabel, commentArea, submitButton, saveButton);
        cwPanel.courseworkId = courseworkId;
        cwPanel.submitted = "На проверке".equals(status) || "Принята".equals(status) || "Защищена".equals(status);
        cwPanel.status = status;
        cwPanel.finalGrade = finalGrade;
        cwPanel.comment = comment;
        cwPanel.topicStatus = topicStatus;

        // Если работа уже отправлена, блокируем редактирование
        if (cwPanel.submitted) {
            topicField.setEnabled(false);
            linkField.setEnabled(false);
            selectCheckBox.setEnabled(false);
            saveButton.setEnabled(false);
            submitButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(mainPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return cwPanel;
    }

    private void setupTextFieldPlaceholder(JTextField field, String placeholder) {
        if (field.getText().equals(placeholder) || field.getText().isEmpty()) {
            field.setForeground(Color.GRAY);
        } else {
            field.setForeground(Color.BLACK);
        }

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void saveCourseworkData(int courseworkId, JTextField topicField, JTextField linkField) {
        String topic = topicField.getText().trim();
        String link = linkField.getText().trim();

        if (topic.isEmpty() || topic.equals("Введите тему...")) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите тему работы",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            topicField.requestFocus();
            return;
        }

        if (!link.isEmpty() && !link.equals("https://...")) {
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                JOptionPane.showMessageDialog(this, "Ссылка должна начинаться с http:// или https://",
                        "Ошибка ссылки", JOptionPane.WARNING_MESSAGE);
                linkField.requestFocus();
                return;
            }
        }

        if (courseworkId == -1) {
            sendInsertCourseworkRequest(topic, link);
        } else {
            // Иначе обновляем существующую
            sendUpdateCourseworkRequest(courseworkId, topic, link);
        }
    }

    private void addNewCoursework() {
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField topicField = new JTextField();
        JTextField subjectField = new JTextField("Программирование");

        inputPanel.add(new JLabel("Тема работы:"));
        inputPanel.add(topicField);
        inputPanel.add(new JLabel("Дисциплина:"));
        inputPanel.add(subjectField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "Добавить новую курсовую", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String topic = topicField.getText().trim();
        String subject = subjectField.getText().trim();

        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Тема не может быть пустой",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (subject.isEmpty()) {
            subject = "Программирование";
        }

        // Создаем JSON объект для новой курсовой
        JSONObject item = new JSONObject();
        item.put("topic", topic);
        item.put("discipline_name", subject);
        item.put("supervisor_name", "prof_sidorov");
        item.put("work_link", "");
        item.put("work_status", "черновик");
        item.put("comment", "");
        item.put("final_grade", "");
        item.put("topic_status", "");

        // Добавляем новую курсовую в интерфейс
        addCourseworkToPanel("Новая курсовая", subject, "prof_sidorov", item, -1);
        centerPanel.revalidate();
        centerPanel.repaint();

        JOptionPane.showMessageDialog(this,
                "Новая курсовая работа добавлена!\n" +
                        "Тема: " + topic + "\n" +
                        "Дисциплина: " + subject + "\n\n" +
                        "Не забудьте сохранить работу!",
                "Добавление курсовой", JOptionPane.INFORMATION_MESSAGE);
    }

    private void submitCourseworkForReview(int courseworkId, String workLink, JButton submitButton, JPanel panel) {
        if (workLink.isEmpty() || workLink.equals("https://...")) {
            JOptionPane.showMessageDialog(this,
                    "Для отправки на проверку необходимо прикрепить ссылку на работу!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!workLink.startsWith("http://") && !workLink.startsWith("https://")) {
            JOptionPane.showMessageDialog(this,
                    "Ссылка должна начинаться с http:// или https://",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Отправить работу на проверку преподавателю?\n\n" +
                        "После отправки редактирование будет заблокировано.",
                "Подтверждение отправки", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            sendSubmitForReviewRequest(courseworkId, workLink, submitButton, panel);
        }
    }

    private void submitSelectedForReview() {
        ArrayList<CourseworkPanel> selectedPanels = new ArrayList<>();

        // Проверяем выбранные работы
        for (CourseworkPanel cw : courseworkPanels) {
            if (cw.selectCheckBox.isSelected()) {
                if (cw.submitted) {
                    JOptionPane.showMessageDialog(this,
                            "Работа \"" + cw.title + "\" уже отправлена на проверку!",
                            "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String topic = cw.topicField.getText().trim();
                String link = cw.linkField.getText().trim();

                if (topic.isEmpty() || topic.equals("Введите тему...")) {
                    JOptionPane.showMessageDialog(this,
                            "Работа \"" + cw.title + "\" не может быть отправлена без темы!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    cw.topicField.requestFocus();
                    return;
                }

                if (link.isEmpty() || link.equals("https://...")) {
                    JOptionPane.showMessageDialog(this,
                            "Работа \"" + cw.title + "\" не может быть отправлена без ссылки!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    cw.linkField.requestFocus();
                    return;
                }

                selectedPanels.add(cw);
            }
        }

        if (selectedPanels.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Выберите работы для отправки (поставьте галочки)",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Показываем список выбранных работ
        StringBuilder worksList = new StringBuilder("Выбранные работы:\n\n");
        for (CourseworkPanel cw : selectedPanels) {
            worksList.append("• ").append(cw.title).append(" (").append(cw.subject).append(")\n");
        }

        int response = JOptionPane.showConfirmDialog(this,
                worksList.toString() + "\n\nОтправить выбранные работы на проверку?",
                "Подтверждение отправки", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            // Отправляем каждую работу через API
            for (CourseworkPanel cw : selectedPanels) {
                String link = cw.linkField.getText().trim();
                if (link.equals("https://...")) link = "";

                if (cw.courseworkId != -1) {
                    sendSubmitForReviewRequest(cw.courseworkId, link, cw.submitButton, cw.panel);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Выбранные работы успешно отправлены на проверку!\n\n" +
                            "Отправлено работ: " + selectedPanels.size() + "\n" +
                            "Статус: На проверке у преподавателя\n" +
                            "Ожидайте обратной связи в течение 3-5 рабочих дней.",
                    "Отправка на проверку", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void submitAllForReview() {
        ArrayList<CourseworkPanel> validPanels = new ArrayList<>();

        for (CourseworkPanel cw : courseworkPanels) {
            if (!cw.submitted) {
                String topic = cw.topicField.getText().trim();
                String link = cw.linkField.getText().trim();

                if (topic.isEmpty() || topic.equals("Введите тему...")) {
                    JOptionPane.showMessageDialog(this,
                            "Работа \"" + cw.title + "\" не может быть отправлена без темы!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    cw.topicField.requestFocus();
                    return;
                }

                if (link.isEmpty() || link.equals("https://...")) {
                    JOptionPane.showMessageDialog(this,
                            "Работа \"" + cw.title + "\" не может быть отправлена без ссылки!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    cw.linkField.requestFocus();
                    return;
                }
                validPanels.add(cw);
            }
        }

        if (validPanels.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Все работы уже отправлены или нет работ для отправки",
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Показываем список
        StringBuilder worksList = new StringBuilder("Все работы:\n\n");
        for (CourseworkPanel cw : validPanels) {
            worksList.append("✓ ").append(cw.title).append(" (").append(cw.subject).append(")\n");
        }

        int response = JOptionPane.showConfirmDialog(this,
                worksList.toString() + "\n\nОтправить все заполненные работы на проверку?",
                "Подтверждение отправки", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            for (CourseworkPanel cw : validPanels) {
                String link = cw.linkField.getText().trim();
                if (link.equals("https://...")) link = "";

                if (cw.courseworkId != -1) {
                    sendSubmitForReviewRequest(cw.courseworkId, link, cw.submitButton, cw.panel);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "✓ Все работы успешно отправлены на проверку!\n\n" +
                            "Отправлено работ: " + validPanels.size(),
                    "Отправка на проверку", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteCoursework(int courseworkId, JPanel panel) {
        if (courseworkId == -1) {
            centerPanel.remove(panel);
            courseworkPanels.removeIf(cw -> cw.panel == panel);
            centerPanel.revalidate();
            centerPanel.repaint();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить эту курсовую работу?",
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            sendDeleteCourseworkRequest(courseworkId);
            centerPanel.remove(panel);
            courseworkPanels.removeIf(cw -> cw.panel == panel);
            centerPanel.revalidate();
            centerPanel.repaint();
        }
    }



    private void sendInsertCourseworkRequest(String topic, String link) {
        new Thread(() -> {
            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("student_login", studentLogin);
                jsonData.put("supervisor_login", "prof_sidorov"); // Используем существующего преподавателя
                jsonData.put("discipline_name", "Программирование"); // Используем существующую дисциплину
                jsonData.put("topic", topic);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String currentDate = sdf.format(new Date());
                jsonData.put("year", currentDate); // Добавляем текущую дату

                System.out.println("Отправка данных для создания курсовой: " + jsonData.toString());

                URL url = new URL(INSERT_COURSEWORK_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonData.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                System.out.println("Код ответа при создании курсовой: " + responseCode);

                // Читаем ответ
                StringBuilder response = new StringBuilder();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }


                    if (!link.isEmpty() && !link.equals("https://...")) {

                    }

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(student_frame.this,
                                "Курсовая работа успешно создана!\n" +
                                        "Статус: Черновик\n" +
                                        "Тема принята на проверку",
                                "Успех",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadCourseworksFromDatabase();
                    });
                } else {
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            response.append(line);
                        }
                    }

                    final String errorMessage = response.toString();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(student_frame.this,
                                "Ошибка при создании курсовой работы:\n" + errorMessage,
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }

                connection.disconnect();

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(student_frame.this,
                            "Ошибка соединения: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void sendUpdateCourseworkRequest(int courseworkId, String topic, String link) {
        new Thread(() -> {
            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("coursework_id", courseworkId);
                jsonData.put("topic", topic);

                if (!link.isEmpty() && !link.equals("https://...")) {
                    jsonData.put("work_link", link);
                }

                URL url = new URL(UPDATE_COURSEWORK_STUDENT_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonData.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        System.out.println("Курсовая успешно обновлена");
                        JOptionPane.showMessageDialog(student_frame.this,
                                "Изменения сохранены!",
                                "Успех", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        System.out.println("Ошибка при обновлении курсовой. Код: " + responseCode);
                        JOptionPane.showMessageDialog(student_frame.this,
                                "Ошибка при сохранении изменений",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    System.err.println("Ошибка при обновлении курсовой: " + ex.getMessage());
                    JOptionPane.showMessageDialog(student_frame.this,
                            "Ошибка соединения: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void sendSubmitForReviewRequest(int courseworkId, String workLink, JButton submitButton, JPanel panel) {
        System.out.println("Отправка курсовой ID: " + courseworkId);
        System.out.println("Ссылка: " + workLink);
        new Thread(() -> {
            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("coursework_id", courseworkId);
                jsonData.put("work_link", workLink);


                System.out.println("Отправка на проверку: " + jsonData.toString());

                URL url = new URL(BASE_API_URL + "/api/procedure/update_coursework_student");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonData.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                System.out.println("Код ответа при отправке на проверку: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Читаем успешный ответ
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
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
                            // Обновляем интерфейс
                            submitButton.setEnabled(false);
                            panel.setBackground(new Color(255, 255, 200)); // Желтый фон - на проверке

                            for (Component comp : panel.getComponents()) {
                                if (comp instanceof JTextField) {
                                    ((JTextField) comp).setEnabled(false);
                                }
                            }

                            for (CourseworkPanel cw : courseworkPanels) {
                                if (cw.courseworkId == courseworkId) {
                                    cw.submitted = true;
                                    cw.status = "На проверке";
                                    cw.statusLabel.setText("На проверке");
                                    break;
                                }
                            }

                            JOptionPane.showMessageDialog(student_frame.this,
                                    "✓ Курсовая работа успешно отправлена на проверку!\n" +
                                            "Статус: На проверке у преподавателя\n" +
                                            "Ожидайте обратной связи в течение 3-5 рабочих дней.",
                                    "Успешная отправка",
                                    JOptionPane.INFORMATION_MESSAGE);

                            loadCourseworksFromDatabase();

                        } else {
                            JOptionPane.showMessageDialog(student_frame.this,
                                    "Ошибка при отправке на проверку",
                                    "Ошибка",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } else {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();

                    final String errorMessage = errorResponse.toString();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(student_frame.this,
                                "Ошибка сервера при отправке на проверку:\n" + errorMessage,
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(student_frame.this,
                            "Ошибка соединения: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void sendDeleteCourseworkRequest(int courseworkId) {
        new Thread(() -> {
            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("coursework_id", courseworkId);

                URL url = new URL(DELETE_COURSEWORK_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonData.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        JOptionPane.showMessageDialog(this, "Курсовая работа удалена", "Успех", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Ошибка при удалении курсовой. Код: " + responseCode, "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении курсовой: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String login = JOptionPane.showInputDialog(null, "Введите логин студента:");
            if (login != null && !login.trim().isEmpty()) {
                new student_frame(login);
            } else {
                JOptionPane.showMessageDialog(null, "Логин не может быть пустым");
            }
        });
    }

        // Метод для запроса плана у GigaChat
    private void generatePlan(String topic, JButton button) {
        button.setEnabled(false);
        button.setText("⏳...");
        
        new Thread(() -> {
            try {
                // Отправляем запрос в coursework-service, который перенаправит его в AI
                URL url = new URL(BASE_API_URL + "/api/ai/generate-plan");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(30000); // ИИ может думать долго

                JSONObject body = new JSONObject();
                body.put("topic", topic);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = connection.getResponseCode();
                StringBuilder response = new StringBuilder();
                
                BufferedReader reader;
                if (code == 200) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                }
                
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                connection.disconnect();

                SwingUtilities.invokeLater(() -> {
                    button.setEnabled(true);
                    button.setText("🤖 План");

                    if (code == 200) {
                        JSONObject json = new JSONObject(response.toString());
                        String plan = json.optString("plan", "План не получен");
                        
                        // Красивое окно для вывода плана
                        JTextArea textArea = new JTextArea(plan);
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
                        textArea.setEditable(false);
                        textArea.setFont(new Font("Serif", Font.PLAIN, 14));
                        
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(500, 400));
                        
                        JOptionPane.showMessageDialog(student_frame.this, scrollPane, 
                                "Сгенерированный план курсовой", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(student_frame.this, 
                                "Ошибка AI-сервиса (HTTP " + code + "):\n" + response.toString(), 
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    button.setEnabled(true);
                    button.setText("🤖 План");
                    JOptionPane.showMessageDialog(student_frame.this, 
                            "Ошибка соединения: " + ex.getMessage(), 
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }

}