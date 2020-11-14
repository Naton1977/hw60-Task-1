package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

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
            byte[] buff1 = new byte[65507];
            byte[] buff2 = new byte[65507];
            byte[] buff3 = new byte[65507];

            DatagramPacket packet1 = new DatagramPacket(buff1, buff1.length);
            videoClient.receive(packet1);
            DatagramPacket packet2 = new DatagramPacket(buff2, buff2.length);
            videoClient.receive(packet2);
            DatagramPacket packet3;
            byte[] tmp = new byte[packet2.getLength()];
            if (packet2.getLength() < 65507) System.arraycopy(buff2, 0, tmp, 0, packet2.getLength());
            int buffSize = buff1.length + tmp.length;

            byte[] tmp1;
            if (packet2.getLength() == 65507) {
                packet3 = new DatagramPacket(buff3, buff3.length);
                videoClient.receive(packet3);
                tmp1 = new byte[packet3.getLength()];
                if (packet3.getLength() < 65507) System.arraycopy(buff3, 0, tmp1, 0, packet3.getLength());
                buffSize += packet3.getLength();
            }
            byte[] imgBuff = new byte[buffSize];
            int count = 0;
            int count1 = 0;
            if (packet2.getLength() < 65507) {
                for (int i = 0; i < buffSize; i++) {
                    if (i < 65507) {
                        imgBuff[i] = buff1[i];
                    }
                    if (i >= 65507) {
                        imgBuff[i] = tmp[count];
                        count++;
                    }
                }
            }
            count = 0;
            if (packet2.getLength() == 65507) {
                for (int i = 0; i < buffSize; i++) {
                    if (i < 65507) {
                        imgBuff[i] = buff1[i];
                    }
                    if (i >= 65507 && i < 131014) {
                        imgBuff[i] = tmp[count];
                        count++;
                    }
                    if (i >= 131014) {
                        imgBuff[i] = buff3[count1];
                        count1++;
                    }
                }
            }

            InputStream in = new ByteArrayInputStream(imgBuff);
            BufferedImage bImageFromConvert = ImageIO.read(in);
            ImageIO.write(bImageFromConvert, "jpg", new File(
                    "screenshot.jpg"));


            ImageIcon icon = new ImageIcon("screenshot.jpg");
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
