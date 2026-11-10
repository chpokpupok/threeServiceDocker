import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthApplication {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Константы для API
    private static final String SURVISOR_LOGIN_API_URL = "http://localhost:3001/home/login_supervisor";
    private static final String STUDENT_LOGIN_API_URL = "http://localhost:3001/home/login_student";
    private static final String REGISTER_API_URL = "http://localhost:3001/home/register";
    private static final String supervisor_password = "secret123";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new AuthApplication().createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void createAndShowGUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Создаем панели
        mainPanel.add(createWelcomePanel(), "WELCOME");
        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createRegisterPanel(), "REGISTER");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("База курсовых работ");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JButton loginButton = new JButton("Вход");
        loginButton.setFont(new Font("Serif", Font.PLAIN, 16));
        loginButton.setPreferredSize(new Dimension(150, 40));
        loginButton.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));

        JButton registerButton = new JButton("Регистрация");
        registerButton.setFont(new Font("Serif", Font.PLAIN, 16));
        registerButton.setPreferredSize(new Dimension(150, 40));
        registerButton.addActionListener(e -> {
            showPasswordDialog();
        });

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(loginButton, gbc);

        gbc.gridx = 1;
        panel.add(registerButton, gbc);
        return panel;
    }

    private void showPasswordDialog() {
        // Создаем диалоговое окно
        JDialog passwordDialog = new JDialog(frame, "Введите пароль");
        passwordDialog.setLayout(new BorderLayout(10, 10));
        passwordDialog.setSize(300, 150);
        passwordDialog.setLocationRelativeTo(frame);

        // Панель для ввода пароля
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JLabel label = new JLabel("Пароль:");
        JPasswordField passwordField = new JPasswordField(15);
        inputPanel.add(label);
        inputPanel.add(passwordField);

        // Панель для кнопки
        JPanel buttonPanel = new JPanel();
        JButton continueButton = new JButton("Продолжить");

        // Обработчик кнопки "Продолжить"
        continueButton.addActionListener(e -> {
            char[] password = passwordField.getPassword();
            String enteredPassword = new String(password);

            // Проверка пароля (пример)
            if (checkPassword(enteredPassword)) {
                passwordDialog.dispose(); // Закрываем диалоговое окно
                cardLayout.show(mainPanel, "REGISTER"); // Показываем основное окно
            } else {
                JOptionPane.showMessageDialog(passwordDialog,
                        "Неверный пароль!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        });

        // Обработка нажатия Enter в поле пароля
        passwordField.addActionListener(e -> continueButton.doClick());

        buttonPanel.add(continueButton);

        // Добавляем компоненты в диалог
        passwordDialog.add(inputPanel, BorderLayout.CENTER);
        passwordDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Показываем диалоговое окно
        passwordDialog.setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Вход в систему");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Логин:"), gbc);

        JTextField loginField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(loginField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Пароль:"), gbc);

        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        String[] items = {
                "Научный руководитель",
                "Студент"
        };
        JComboBox login_type = new JComboBox(items);
        login_type.setSelectedIndex(1);

        JLabel selectedLabel = new JLabel("Студент");
        selectedLabel.setFont(new Font("Serif", Font.PLAIN, 14));

        // Добавляем слушатель событий для обработки выбора
        login_type.addActionListener(e -> {
            String selectedRole = (String) login_type.getSelectedItem();
            if (selectedRole != null) {
                selectedLabel.setText(selectedRole);
            }
        });
        gbc.gridwidth = 1;
        gbc.gridy = -1;
        gbc.gridx = 2;
        panel.add(new JLabel("Выберите роль: "));
        panel.add(login_type, gbc);

        JButton loginButton = new JButton("Войти");
        loginButton.setFont(new Font("Serif", Font.PLAIN, 14));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String login = loginField.getText();
                String password = new String(passwordField.getPassword());

                if (login.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Заполните все поля!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if(login_type.getSelectedItem() == "Научный руководитель") {
                    sendSupervisorLoginRequest(login, password);
                } else {
                    // Отправка запроса на сервер
                    sendStudentLoginRequest(login, password);
                }
            }
        });

        JButton backButton = new JButton("Назад");
        backButton.setFont(new Font("Serif", Font.PLAIN, 14));
        backButton.addActionListener(e -> {
            loginField.setText("");
            passwordField.setText("");
            cardLayout.show(mainPanel, "WELCOME");
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(backButton);
        buttonPanel.add(loginButton);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    // Вспомогательный метод для обработки необязательных полей
    private String processOptionalField(JTextField field) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            return "null";
        } else {
            return "\"" + text.trim() + "\"";
        }
    }

    private boolean checkPassword(String password) {
        return supervisor_password.equals(password); // Пример пароля
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Регистрация");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        String[] labels = {"Логин:", "Пароль:", "Фамилия:", "Имя:", "Отчество:", "Телефон:", "Email научного руководителя:", "Должность:"};
        JTextField[] fields = new JTextField[labels.length-1];
        fields[1] = new JPasswordField(20); // Парольное поле
        String[] items = {
                "преподаватель",
                "старший преподаватель",
                "ассистент",
                "зав. кафедрой",
                "доцент",
                "профессор"
        };
        JComboBox position_type = new JComboBox(items);
        position_type.setSelectedIndex(0);

        JLabel selectedLabel = new JLabel("преподаватель");
        selectedLabel.setFont(new Font("Serif", Font.PLAIN, 14));

        // Добавляем слушатель событий для обработки выбора
        position_type.addActionListener(e -> {
            String selectedRole = (String) position_type.getSelectedItem();
            if (selectedRole != null) {
                selectedLabel.setText(selectedRole);
            }
        });

        gbc.gridwidth = 1;
        gbc.gridy = 8;
        gbc.gridx = 0;
        panel.add(new JLabel(labels[7]), gbc);

        gbc.gridx = 1;
        panel.add(position_type, gbc);

        for (int i = 0; i < labels.length-1; i++) {
            if (i != 1) {
                fields[i] = new JTextField(20);
            }
            gbc.gridwidth = 1;
            gbc.gridy = i + 1;
            gbc.gridx = 0;
            panel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            panel.add(fields[i], gbc);
        }

        JButton registerButton = new JButton("Зарегистрироваться");
        registerButton.setFont(new Font("Serif", Font.PLAIN, 14));
        registerButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Проверка заполнения всех полей
                for (int i = 0; i < fields.length-1; i++) {
                    String text = (i == 1) ? new String(((JPasswordField)fields[1]).getPassword()) : fields[i].getText();
                    if ((text == null || text.trim().isEmpty()) && i != 4) {
                        JOptionPane.showMessageDialog(frame, "Заполните все обязательные поля!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // Сбор данных
                String login = fields[0].getText();
                String password = new String(((JPasswordField)fields[1]).getPassword());
                String last_name = fields[2].getText();
                String first_name = fields[3].getText();
                String middle_name = processOptionalField(fields[4]);
                String phone = processOptionalField(fields[5]);
                String supervisorEmail = processOptionalField(fields[6]);
                String position = "\"" + position_type.getSelectedItem().toString() + "\"";

                // Отправка запроса на сервер
                sendRegisterRequest(login, password, last_name, first_name, middle_name, position, phone, supervisorEmail);
            }
        });

        JButton backButton = new JButton("Назад");
        backButton.setFont(new Font("Serif", Font.PLAIN, 14));
        backButton.addActionListener(e -> {
            for (JTextField field : fields) {
                if (field != null) field.setText("");
            }
            cardLayout.show(mainPanel, "WELCOME");
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(backButton);
        buttonPanel.add(registerButton);

        gbc.gridy = labels.length + 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void sendSupervisorLoginRequest(String username, String password) {
        new Thread(() -> {
            try {
                URL url = new URL(SURVISOR_LOGIN_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String jsonInputString = String.format("{\"login\":\"%s\",\"password\":\"%s\"}", username, password);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                
                // Читаем ответ СРАЗУ, до закрытия соединения
                StringBuilder response = new StringBuilder();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }
                } else {
                     try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                        String inputLine;
                        while ((inputLine = errorReader.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }
                }
                
                connection.disconnect(); // Теперь можно безопасно закрывать

                // Обновляем интерфейс
                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try {
                            // Парсим JSON, чтобы достать реальный ID преподавателя
                            org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                            String realId = String.valueOf(jsonResponse.getInt("userId"));

                            JOptionPane.showMessageDialog(frame, "Успешный вход преподавателя!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                            frame.dispose();
                            
                            // Передаем РЕАЛЬНЫЙ ID в TeacherApp
                            new TeacherApp(username, realId);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(frame, "Ошибка чтения ответа: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Неверный логин или пароль\n" + response.toString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ошибка соединения", JOptionPane.ERROR_MESSAGE));
                ex.printStackTrace();
            }
        }).start();
    }


    private void sendStudentLoginRequest(String username, String password) {
        new Thread(() -> {
            try {
                URL url = new URL(STUDENT_LOGIN_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String jsonInputString = String.format("{\"login\":\"%s\",\"password\":\"%s\"}", username, password);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                
                StringBuilder response = new StringBuilder();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                     try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }
                } else {
                     try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                        String inputLine;
                        while ((inputLine = errorReader.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }
                }
                
                connection.disconnect();

                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        JOptionPane.showMessageDialog(frame, "Успешный вход студента!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                        frame.dispose();
                        new student_frame(username);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Неверный логин или пароль\n" + response.toString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ошибка соединения", JOptionPane.ERROR_MESSAGE)
                );
                ex.printStackTrace();
            }
        }).start();
    }


    private void sendRegisterRequest(String login, String password, String last_name,
                                     String first_name,String middle_name,
                                     String position, String phone, String supervisorEmail) {
        new Thread(() -> {
            try {
                URL url = new URL(REGISTER_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Формирование JSON
                String jsonInputString = String.format(
                        "{\"login\": \"%s\", \"password\": \"%s\", \"last_name\": \"%s\", " +
                                "\"first_name\": \"%s\", \"middle_name\": %s, " +
                                "\"position\": %s, \"phone_number\": %s, \"email\": %s}",
                        login, password, last_name, first_name, middle_name, position, phone, supervisorEmail
                );

                System.out.println(jsonInputString);
                // Отправка данных
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Получение ответа
                int responseCode = connection.getResponseCode();

                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        JOptionPane.showMessageDialog(frame, "Регистрация прошла успешно!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                        cardLayout.show(mainPanel, "WELCOME");
                    } else {
                        try {
                            JOptionPane.showMessageDialog(frame, connection.getResponseMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                connection.disconnect();

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Ошибка соединения: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }
}
