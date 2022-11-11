import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class NSGAII {
    static int fitnessDim = 2;
    static int dim;
    double ub = 1;
    double lb = 0;
    int populationSize = 500;
    int maxGen = 500;
    public NSGAII(){
        int gen = 0;
        dim = 30;
        DrawPointUtil drawPointUtil = new DrawPointUtil();
        List<Individual> pop = init(populationSize);
        HashMap<Integer,List<Individual>>hashMap = noDominateSort(pop);//this function will set the rank and distance;
        setDistance(hashMap);
        drawPointUtil.draw(pop);
        while(gen<maxGen){
            List<Individual> fatherPool = battleSection(pop);//the good Father Pool
            List<Individual> sons = getSons(fatherPool);
            pop.addAll(sons);
            HashMap<Integer,List<Individual>> rankMap = noDominateSort(pop);
            setDistance(rankMap);
            pop = tournamentSection(rankMap,populationSize);//这里的rankMap以及被污染了
            if (gen%1000==0){
                System.out.println(gen);
            }
            drawPointUtil.repaint(pop);
            gen++;
        }
    }

    public static void main(String[] args) {
        new NSGAII();
    }
    private List<Individual> getSons(List<Individual> fatherPool){
        int n = fatherPool.size();//n is the size of the fatherPool.
        List<Individual> sons = new ArrayList<>(populationSize);
        //although the final size of sons may not be 2*n,but inorder to save the time we set 2n;

        while(sons.size()<populationSize){
            if(random()<0.9){
                int f1 = (int)(random()*n);
                int f2 = (int)(random()*n);
                while(f1 == f2){
                    f2 = (int)(random()*n);
                }
                Individual fa = fatherPool.get(f1);
                Individual fb = fatherPool.get(f2);
                Individual[] son = getSBX(fa,fb);
                for (int j = 0; j < son.length; j++) {
                    sons.add(son[j]);
                }
            }else {
                int f = (int)(random()*n);
                Individual son = getPloMutation(fatherPool.get(f));
                sons.add(son);
            }
        }
        return sons;
    }
    //simulated binary crossover
    private Individual[] getSBX(Individual a,Individual b){
        List<Double> aP = new ArrayList<>(dim);//sonA's position
        List<Double> bP = new ArrayList<>(dim);
        List<Double> faP = a.position;
        List<Double> fbP = b.position;
        double etaC = 20;//page6
        for (int j = 0; j < dim; j++) {
            double gama;
            double uj = random();
            if(random()<=0.5){
                gama = Math.pow(2.0*uj,1.0/(etaC+1.0));
            }else {
                gama = Math.pow(1.0/(2*(1.0-uj)),1.0/(etaC+1.0));
            }
            double aPP = 0.5*((1+gama)*faP.get(j)+(1-gama)*fbP.get(j));
            double bPP = 0.5*((1-gama)*faP.get(j)+(1+gama)*fbP.get(j));
            aPP = getSuitLimitX(aPP);
            bPP = getSuitLimitX(bPP);
            aP.add(aPP);
            bP.add(bPP);
        }
        Individual sonA = new Individual(aP);
        Individual sonB = new Individual(bP);
        return new Individual[]{sonA,sonB};
    }
    //select n Individual
    private List<Individual> tournamentSection(HashMap<Integer,List<Individual>> map,int n){
        List<Individual> result = new ArrayList<>(n);
        for (int i = 1; i < map.size(); i++) {
            List<Individual> rankList = map.get(i);
            if(result.size()+rankList.size()<=n){
                result.addAll(rankList);//全部加入
            }else {//将剩余的等级塞进去
                try{
                    rankList.sort(new Comparator<Individual>() {
                        @Override
                        public int compare(Individual o1, Individual o2) {
                            double sub = o1.distance-o2.distance;
                            if (sub==0)return 0;
                            return sub>0?-1:1;//逆序
                        }
                    });
                    result.addAll(rankList.subList(0,n-result.size()));
                }catch (Exception e){
                    System.out.println(e);
                }

            }
        }
        return result;
    }
    //select halfOf individuals
    private List<Individual> battleSection(List<Individual> individuals){
        int n = individuals.size();
        List<Integer> set = new ArrayList<>(n);
        List<Individual> fatherPool = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            set.add(i);
        }
        while(set.size()>1){
            int rand = (int)(random()*set.size());//挑出两个battle
            Individual father1 = individuals.get(set.remove(rand));
            rand = (int)(random()*set.size());
            Individual father2 = individuals.get(set.remove(rand));
            if(father1.getRank()<father2.getRank()){
                fatherPool.add(father1);
            }else if(father1.getRank()==father2.getRank()){
                if (father1.getDistance()>father2.getDistance()){
                    fatherPool.add(father1);
                }else {
                    fatherPool.add(father2);
                }
            }else fatherPool.add(father2);
        }
        return fatherPool;
    }

    //Ploynomial mutation 多项式变异
    private Individual getPloMutation(Individual father){
        List<Double> sonPos = new ArrayList<>(dim);
        List<Double> fatPos = father.position;
        double etaM = 20;
        for (int i = 0; i < dim; i++) {
            double cur = fatPos.get(i);
            if(random()<=1.0/dim){
                double u = random();
                double sigma;
                double sigma1 = (double) (cur-lb)/(ub-lb);
                double sigma2 = (double) (random()-cur)/(ub-lb);
                if(u<=0.5){
                    sigma = Math.pow(2*u+(1-2*u)*Math.pow(1-sigma1,etaM),1.0/etaM)-1;
                }else {
                    sigma = 1 - Math.pow(2*(1-u)+2*(u-0.5)*Math.pow(1-sigma2,etaM),1.0/etaM);
                }
                cur = cur+sigma;
                cur = getSuitLimitX(cur);//获得界内的图
            }
            sonPos.add(cur);
        }
        return new Individual(sonPos);
    }
    //对这个individuals设置distance值
    public void setDistance(HashMap<Integer,List<Individual>> hashMap){
        for (int rank = 1; rank <= hashMap.size() ; rank++) {
            List<Individual> individuals = hashMap.get(rank);
            int len = individuals.size();
            int fSize = individuals.get(0).fitness.length;
            for (Individual i:
                    individuals) {
                i.setDistance(0);//重置dis
            }
            for (int fIndex = 0; fIndex < fSize; fIndex++) {
                final int finalFIndex = fIndex;//保证能够插入内部类里面
                individuals.sort(new Comparator<Individual>() {//根据适应度下标排序
                    @Override
                    public int compare(Individual o1, Individual o2) {
                        double sub = o1.fitness[finalFIndex]-o2.fitness[finalFIndex];
                        if(sub==0)return 0;
                        if (sub>0){
                            return 1;
                        } else return -1;
                    }
                });
                individuals.get(0).setDistance(Double.MAX_VALUE);
                individuals.get(len-1).setDistance(Double.MAX_VALUE);//边界点无穷大
                double fMax = individuals.get(len-1).fitness[fIndex];
                double fMin = individuals.get(0).fitness[fIndex];
                for (int i = 1; i < len-1; i++) {
                    double fDown = individuals.get(i-1).fitness[fIndex];
                    double fUp = individuals.get(i+1).fitness[fIndex];
                    Individual  a = individuals.get(i);
                    a.setDistance(a.getDistance()+(fUp-fDown)/(fMax-fMin));
                }
            }
        }

    }
    public HashMap<Integer,List<Individual>> noDominateSort(List<Individual> individuals){
    //public void setNoDominateRank(List<Individual> individuals){
        for (int i = 0; i < individuals.size(); i++) {
            individuals.get(i).setRank(0);
        }
        int len = individuals.size();
        int n[] = new int[len];//记录个体被多少个人支配
        Set<Integer> S[] = new HashSet[len];
        List<Integer> front = new LinkedList<>();
        int rank[] = new int[len];
        for (int i = 0; i < len; i++) {
            Set<Integer> set = new HashSet();
            S[i] = set;
            for (int j = 0; j < len; j++) {
                if(i==j)continue;//自己和自己比就跳过
                int isDominate = individuals.get(i).isDominate(individuals.get(j));
                if(isDominate==1){//i支配j
                    set.add(j);
                }
                else if (isDominate==-1){//j支配i
                    n[i] = n[i]+1;
                }
            }
            if(n[i]==0){
                rank[i] = 1;
                front.add(i);
            }
        }
        int i = 1;
        while(front.size()!=0){
            List<Integer> Q = new LinkedList<>();
            for (int p:
                    front) {
                for (int q:
                        S[p]) {
                    n[q] = n[q] - 1;
                    if(n[q] == 0){
                        rank[q] = i+1;
                        individuals.get(q).setRank(rank[q]);//设置每个的等级
                        Q.add(q);
                    }
                }
            }
            i = i + 1;
            front = Q;
        }
        //启用这个代码能够直接获得一个hashmap。
        HashMap<Integer,List<Individual>> res = new HashMap<>();
        for (int j = 0; j < len; j++) {
            if (res.get(rank[j])==null){
                List<Individual> list = new ArrayList<>();;
                list.add(individuals.get(j));
                res.put(rank[j],list);
            }else {
                res.get(rank[j]).add(individuals.get(j));
            }
        }

        for (int j = 0; j < len; j++) {
            individuals.get(j).setRank(rank[j]);
        }
        return res;
    }

    public List<Individual> init(int populationSize){
        List<Individual> individuals = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            List<Double> list = new ArrayList<>(dim);
            for (int j = 0; j < dim; j++) {
                list.add(lb+(ub-lb)*random());
            }
            Individual individual = new Individual(list);
            individuals.add(individual);
        }
        return individuals;
    }
    private double getSuitLimitX(double x){
        if (x > ub || x < lb) {//越界判断
            x =  lb + abs(x) % (ub - lb);
        }
        return x;
    }
    class Individual{
        int rank;
        double distance;
        double[] fitness;//越小越好捏
        List<Double> position = new LinkedList<>();
        public Individual(double[] fitness) {
            this.fitness = fitness;
        }
        //will auto set fitness
        public Individual(List<Double> positon){
            this.position = positon;
            this.fitness = culFitness(position);
        }
        // a > b return 1;
        // a >< b return 0;
        // a < b return -1;
        public int isDominate (Individual b){
            double[] bf = b.fitness;
            boolean flag = true;
            boolean notEqualFlag = false;
            //先判断a是否支配b
            for (int i = 0; i < fitness.length; i++) {
                if(fitness[i]>bf[i]){//a不支配b;
                    flag = false;
                    break;
                }
                if (fitness[i]!=bf[i]){
                    notEqualFlag = true;
                }
            }
            if (flag&&notEqualFlag){
                return 1;
            }
            flag = true;
            notEqualFlag = false;
            for (int i = 0; i < fitness.length; i++) {
                if(fitness[i]<bf[i]){//b不支配a;
                    flag = false;
                    break;
                }
                if (fitness[i]!=bf[i]){
                    notEqualFlag = true;
                }
            }
            if (flag&&notEqualFlag){
                return -1;
            }else return 0;
        }

        @Override
        public String toString() {
            return new StringBuffer("rank:"+rank+",distance:"+distance+",fitness:"+Arrays.toString(fitness)).toString();
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }
    }
    public double[] culFitness(List<Double> position){
        return FUtil.ZDT1(position);
    }
    interface ICul{
        double[] culFitness(List<Double> position);
    }
}
