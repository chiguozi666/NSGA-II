import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DrawPointUtil {
    SolutionFrame solutionFrame;
    public void repaint(List<NSGAII.Individual> list){
        solutionFrame.setData(list);
        solutionFrame.repaint();
    }
    public void draw(List<NSGAII.Individual> list){
        this.solutionFrame = new SolutionFrame(list);
    }

}
class SolutionPanel extends JPanel{
    boolean initFlag = false;
    List<NSGAII.Individual> list;
    SolutionPanel(List<NSGAII.Individual> list){
        this.list = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            this.list.add(list.get(i));
        }
        //prepareFlow(solution);//由于这个时候还没初始化所以不能在这里调用
        setBackground(Color.white);

    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        setBackground(Color.white);
        g.setColor(Color.black);
        g.clearRect(0,0,1000,1000);
        double maxX = 0;
        double minX = Double.MAX_VALUE;
        double maxY = 0;
        double minY = Double.MAX_VALUE;
        for (int i = 0; i < list.size(); i++) {
            double[] curFit = list.get(i).fitness;
            if (maxX<curFit[0]){
                maxX = curFit[0];
            }
            if (minX>curFit[0]){
                minX = curFit[0];
            }
            if (maxY<curFit[1]){
                maxY = curFit[1];
            }
            if (minY>curFit[1]){
                minY = curFit[1];
            }
        }
        double spanX = maxX-minX;
        double spanY = maxY-minY;
        if(!initFlag){
            initFlag = !initFlag;
        }else {

        }
        for (int i = 0; i < list.size(); i++) {
            double curFit[] = list.get(i).fitness;
            g.setColor(Color.black);
//            int x = (int)(2000.0*(curFit[0]-minX)/spanX);
//            int y = (int)(1000.0*(curFit[1]-minY)/spanY);
            int x = (int)(1000.0*(curFit[0]-0)/1);
            int y = (int)(1000-1000.0*(curFit[1]-0)/1);
            g.drawString("x",x,y);
        }
        return;
    }
    public void setData(List<NSGAII.Individual> list){
        this.list = list;
    }
}
class SolutionFrame extends JFrame{
    SolutionPanel solutionPanel;
    public SolutionFrame(List<NSGAII.Individual> list){
        this.solutionPanel = new SolutionPanel(list);
        this.getContentPane().add(solutionPanel);
        setSize(1000, 1000);
        setVisible(true);
    }
    public void setData(List<NSGAII.Individual> list){
        solutionPanel.setData(list);
        solutionPanel.repaint();
    }
}