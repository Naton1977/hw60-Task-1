package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class VideoClient {

    public static final int WIDTH = 1000;
    public static final int HEIGHT = 600;

    public static void main(String[] args) throws IOException {
        // Создаем окно нужного размера
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(WIDTH, HEIGHT);

        // Добавляем метку
        JLabel lbl = new JLabel();
        // Считываем изображение из файла
        // TODO: считать из сети используя перегруженный конструктор ImageIcon
        while (true) {
            MulticastSocket videoClient = new MulticastSocket(VideoStreamer.PORT);
            InetAddress inetAddress = InetAddress.getByName(VideoStreamer.MULTICAST_ADR);
            videoClient.joinGroup(new InetSocketAddress(inetAddress, VideoStreamer.PORT), NetworkInterface.getByIndex(0));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (int i = 0; i < 10; i++) {
                byte[] buff = new byte[65507];
                DatagramPacket packet = new DatagramPacket(buff, buff.length);
                videoClient.receive(packet);
                outputStream.write(buff);
                if (packet.getLength() < 65507) {
                    outputStream.close();
                    break;
                }
            }
            byte[] imgBuff = outputStream.toByteArray();


            ImageIcon icon = new ImageIcon(imgBuff);
            int h = icon.getIconHeight();
            int w = icon.getIconWidth();
            float scale = (float) WIDTH / w; // масштабируем
            icon = new ImageIcon(icon.getImage().getScaledInstance((int) (w * scale), (int) (h * scale), Image.SCALE_SMOOTH));
            lbl.setIcon(icon); // устанавливаем изображение для метки

            // Добавляем метку в окно
            frame.add(lbl);
            frame.setVisible(true);
            // Зкарывать окно при выходе
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }
}
