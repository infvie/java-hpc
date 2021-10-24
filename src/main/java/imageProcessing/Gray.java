package imageProcessing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Gray {

    public static int getRed(int pixel) {
        return (pixel & 0x00FF0000) >> 16;
    }

    public static int getGreen(int pixel) {
        return (pixel & 0x0000FF00) >> 8;
    }

    public static int getBlue(int pixel) {
        return (pixel & 0x000000FF);
    }

    public static boolean isGray(int rgb) {
        int red   = getRed(rgb);
        int green = getGreen(rgb);
        int blue  = getBlue(rgb);
        return Math.abs(red - green) < 30 & Math.abs(green - blue) < 30 & Math.abs(green - blue) < 30;
    }

    public static int rgbFromColors(int red, int green, int blue) {
        int rgb = 0;
        rgb |= blue;
        rgb |= green << 8;
        rgb |= red << 16;
        rgb |= 0xFF000000;
        return rgb;
    }

    public static int rgbFromColors(int gray) {
        int rgb = 0;
        rgb |= gray;
        rgb |= gray << 8;
        rgb |= gray << 16;
        rgb |= 0xFF000000;
        return rgb;
    }


    public static int grayPixel(int rgb) {
        int red   = getRed(rgb);
        int green = getGreen(rgb);
        int blue  = getBlue(rgb);

        int shade = (red + green + blue) / 3;

        return rgbFromColors(shade);
    }

    public static void setRGB(BufferedImage img, int x, int y, int rgb) {
        img.getRaster().setDataElements(x, y, img.getColorModel().getDataElements(rgb, null)) ;
    }

    public static void recolor(BufferedImage img, BufferedImage out, int x, int y) {
        int rgb = img.getRGB(x,y);
        int gray = grayPixel(rgb);
        setRGB(out, x, y, gray);
    }

    public static void recolorImage(BufferedImage img, BufferedImage out, int leftCorner, int topCorner, int width, int height) {
        for (int x = leftCorner; x < leftCorner + width; x++) {
            for (int y = topCorner; y < topCorner + height; y++) {
                recolor(img, out, x, y);
            }
        }
    }

    public static void singleThreadedRecolor(BufferedImage img, BufferedImage out) {
        recolorImage(img, out, 0, 0, img.getWidth(), img.getHeight());
    }

    public static void multiThreadedRecolor(BufferedImage img, BufferedImage out, int numThreads) {
        List<Thread> threads = new ArrayList<>();
        int width = img.getWidth();
        int height = img.getHeight();

        for (int i = 0; i < numThreads; i++) {
            final int multiplier = i;

            Thread thread = new Thread(() -> {
                int leftCorner = 0;
                int topCorner = multiplier * (height / numThreads);
                recolorImage(img, out, leftCorner, topCorner, width, height/numThreads);
            });

            threads.add(thread);
        }

        for (Thread thread : threads) { thread.start(); }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws IOException {
        File file = new File("./images/long.jpeg");
        System.out.println("Reading image in from " + file);

        BufferedImage img = ImageIO.read(file);
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

        // start time


        // single threaded
        long start = System.nanoTime();
        singleThreadedRecolor(img,out);
        long end = System.nanoTime();
        System.out.println(end-start +  " ns taken with 1 thread");

        // multi thread
        start = System.nanoTime();
        multiThreadedRecolor(img,out,2);
        end = System.nanoTime();
        System.out.println(end-start +  " ns taken with 4 threads");

        // write image
        File result = new File("./images/long_out.jpg");
        System.out.println("Writing file to " + result);
        ImageIO.write(out, "jpg", result);

    }
}
