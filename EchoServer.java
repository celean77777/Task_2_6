import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer extends JFrame {
    private JTextField msgInputField;
    private JTextArea chatArea;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket = null;


    public EchoServer() {

        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        prepareGUI();
    }


    public void startServer () throws IOException {

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(8189)) {
                socket = serverSocket.accept();
                chatArea.setBackground(Color.GREEN);
                System.out.println("Клиент подключился" + socket.getInetAddress().toString());
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                while (true) {
                    String str = in.readUTF();
                    if (str.equals("/end")) {
                        chatArea.setBackground(Color.darkGray);
                        break;
                    }
                    chatArea.append(str);
                    chatArea.append("\n");
                    out.writeUTF("Я: " + str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    public void closeServer() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage() {
        if (!msgInputField.getText().trim().isEmpty()) {
            try {
                out.writeUTF(msgInputField.getText());
                msgInputField.setText("");
                msgInputField.grabFocus();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения");
            }
        }
    }


    public void prepareGUI() {
// Параметры окна
        setBounds(600, 300, 500, 500);
        setTitle("Сервер");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
// Текстовое поле для вывода сообщений
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setBackground(Color.darkGray);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
// Нижняя панель с полем для ввода сообщений и кнопкой отправки сообщений

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton btnSendMsg = new JButton("Отправить");
        bottomPanel.add(btnSendMsg, BorderLayout.EAST);
        msgInputField = new JTextField();
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(msgInputField, BorderLayout.CENTER);

        btnSendMsg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        msgInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
// Настраиваем действие на закрытие окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    out.writeUTF("/end");
                    closeServer();
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
        });
        setVisible(true);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EchoServer());
    }
}
