package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class VideoStreamer {
    public static final String MULTICAST_ADR = "230.0.0.1";
    public static final int PORT = 10_000;

    public static void main(String[] args) {
        try {
            Robot robot = new Robot();
            String format = "jpg";
            String fileName = "screenshot." + format;

            // получить скриншот
            while (true) {
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage screenFullImage = robot.createScreenCapture(screenRect);

                // сохраняем в файл (для примера)
//            ImageIO.write(screenFullImage, format, new File(fileName));
//            System.out.println("Сохранен!");

                // получаем мессив байт для отравки по сети
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(screenFullImage, format, out);

                byte[] bytes = out.toByteArray();
//                System.out.println("Image size: " + bytes.length);
                out.close();
                // TODO: здесь код для отправки изображения по сети

                ArrayList<byte[]> bytesArr = getList(bytes);
                for (int i = 0; i < bytesArr.size(); i++) {
                    DatagramSocket videoStream = new DatagramSocket();
                    byte[] path = bytesArr.get(i);
                    videoStream.send(new DatagramPacket(path, path.length, new InetSocketAddress(MULTICAST_ADR, PORT)));
                }
            }

        } catch (AWTException | IOException ex) {
            System.err.println(ex);
        }
    }


    public static ArrayList<byte[]> getList(byte[] bytes) {
        ArrayList<byte[]> list = new ArrayList<>();
        int start_position = 0;
        int end_position = 65507;
        int countPath = 0;
        int length = bytes.length;
        while (length > 0) {
            length -= 65507;
            countPath++;
        }

        for (int i = 0; i < countPath; i++) {
            list.add(Arrays.copyOfRange(bytes, start_position, end_position));
            if (bytes.length - end_position < 65507) {
                start_position += 65507;
                end_position = bytes.length;
                list.add(Arrays.copyOfRange(bytes, start_position, end_position));
               break;
            }
            start_position += 65507;
            end_position += 65507;
        }
        return list;
    }
}
