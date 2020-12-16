package cn.mrxccc.javacv.example;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import javax.swing.*;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * @author mrxccc
 * @create 2020/12/14
 */
public class FaceDetectionTest {
    public static OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

    public static void main(String[] args) throws Exception, InterruptedException {
        faceDetection("D:\\develop\\ideaWorkSpace\\myproject\\javacvExample\\src\\main\\java\\cn\\mrxccc\\javacv\\example\\haarcascade_frontalface_alt.xml",960,540);
    }
    /**
     * 人脸检测-eguid
     * @param cascadeClassifierXml 基于Haar特征的cascade正面人脸分类器
     * @param width 图像宽度
     * @param height 图像高度
     */
    public static void faceDetection(String cascadeClassifierXml,Integer width,Integer height) throws Exception, InterruptedException {
        // 开启摄像头，获取图像（得到的图像为frame类型，需要转换为mat类型进行检测和识别）
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        if(width!=null&&width>1&&height!=null&&height>1) {
            grabber.setImageWidth(width);
            grabber.setImageHeight(height);
        }
        grabber.start();

        if(width==null||height==null) {
            height=grabber.getImageHeight();
            width=grabber.getImageWidth();
        }

        CanvasFrame canvas = new CanvasFrame("人脸检测");// 新建一个预览窗口
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setVisible(true);
        canvas.setFocusable(true);
        //窗口置顶
        if(canvas.isAlwaysOnTopSupported()) {
            canvas.setAlwaysOnTop(true);
        }
        Frame frame =null;

        // 读取opencv人脸检测器
        CascadeClassifier cascade = new CascadeClassifier(cascadeClassifierXml);

        for(;canvas.isVisible()&&(frame=grabber.grab())!=null;) {

            Mat img = converter.convertToMat(grabber.grabFrame());

            Mat grayImg = new Mat();//存放灰度图
            //摄像头色彩模式设置成ImageMode.Gray下不需要再做灰度
            cvtColor(img, grayImg, COLOR_BGRA2GRAY);// 摄像头获取的是彩色图像，所以先灰度化下
            //如果要获取摄像头灰度图，可以直接对FrameGrabber进行设置grabber.setImageMode(ImageMode.GRAY);，grabber.grab()获取的都是灰度图

            equalizeHist(grayImg, grayImg);// 均衡化直方图

            // 检测到的人脸
            RectVector faces = new RectVector();
            cascade.detectMultiScale(grayImg, faces);

            // 遍历人脸
            for (int i = 0; i < faces.size(); i++) {
                Rect face_i = faces.get(i);
                //绘制人脸矩形区域，scalar色彩顺序：BGR(蓝绿红)
                rectangle(img, face_i, new Scalar(0, 255, 0, 1));

                int pos_x = Math.max(face_i.tl().x() - 10, 0);
                int pos_y = Math.max(face_i.tl().y() - 10, 0);
                // 在人脸矩形上方绘制提示文字
                putText(img, "people face", new Point(pos_x, pos_y), FONT_HERSHEY_COMPLEX, 1.0,new Scalar(0, 0, 255, 2.0));
            }
            canvas.showImage(converter.convert(img));// 获取摄像头图像并放到窗口上显示，frame是一帧视频图像
            Thread.sleep(40);// 40毫秒刷新一次图像
        }
        cascade.close();
        canvas.dispose();
        grabber.close();// 停止抓取
    }
}
