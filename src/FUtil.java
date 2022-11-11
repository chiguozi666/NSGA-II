import java.util.List;

public class FUtil {
    public static double Sphere(List<Double> x){
        double sum = 0;
        for (int i = 0; i < x.size(); i++) {
            sum += x.get(i)*x.get(i);
        }
        return sum;
    }
    public static double[] ZDT1(List<Double> x){
        double a = x.get(0);
        double gX;
        int n = x.size();
        double sum = 0;
        for (int i = 1; i < n; i++) {
            sum += x.get(i);
        }
        gX = 1 + 9.0*sum/(n-1);
        double b = gX*(1-Math.sqrt(a/gX));
        return new double[]{a,b};
    }
    /**  [-500,500]   -2094.9145**/
    public static double F8(List<Double> x){
        int n = x.size();
        double res = 0;
        for (int i = 0; i < n; i++) {
            res+=x.get(i)*Math.sin(Math.sqrt(Math.abs(x.get(i))));
        }
        return res;
    }
}
