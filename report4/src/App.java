import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import javax.swing.*;

class MyMouseListener implements MouseMotionListener, MouseListener {
    DrawingPanel pan;

    MyMouseListener(DrawingPanel pan) {
        this.pan = pan;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        pan.list.add(new Point(e.getX(), e.getY(), false));
        pan.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pan.list.add(new Point(e.getX(), e.getY(), true));
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        System.out.println("마우스 릴리즈 발생"); // [cite: 4]
    }

    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
}

class Point {
    int x, y;
    boolean skip; // 선을 끊을지 여부를 결정하는 플래그 

    // 생성자 수정: 좌표와 끊김 여부를 함께 받음
    Point(int x, int y, boolean skip) {
        this.x = x;
        this.y = y;
        this.skip = skip;
    }
}

class DrawingPanel extends JPanel {
    LinkedList<Point> list = new LinkedList<Point>();

    // paint 대신 paintComponent 사용 (Swing 표준)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 배경을 지워 잔상 제거

        Point startPt = null;

        for (Point endPt : list) {
            // skip이 true인 점을 만나면, 그리기를 멈추고 새로운 시작점으로 설정
            if (endPt.skip) {
                startPt = endPt;
                continue; 
            }

            // 시작점이 있고, 현재 점이 연결된 점이라면 선을 그림
            if (startPt != null) {
                g.drawLine(startPt.x, startPt.y, endPt.x, endPt.y); // 
            }
            
            // 현재 점을 다음 선의 시작점으로 업데이트
            startPt = endPt;
        }
    }
}

public class Main3 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("그림판 프로그램");
        frame.setLocation(500, 200); // [cite: 9]
        frame.setPreferredSize(new Dimension(400, 300));
        
        Container contentPane = frame.getContentPane();
        DrawingPanel drawingPanel = new DrawingPanel();
        
        MyMouseListener listener = new MyMouseListener(drawingPanel);
        
        // 리스너 등록
        drawingPanel.addMouseMotionListener(listener);
        drawingPanel.addMouseListener(listener); // [cite: 10]
        
        contentPane.add(drawingPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}