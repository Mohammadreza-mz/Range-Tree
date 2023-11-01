import java.io.*;
import java.util.*;

class Point{
    double[] d;
    public Point(int dimension) {
        d= new double[dimension];
    }

    public Point(double[] d) {
    }

    void set(int k, double x){
        d[k]=x;
    }

    double get(int k){
        return d[k];
    }

    void print(){

    }

}

class SortByX implements Comparator<Point>{

    @Override
    public int compare(Point x, Point y){
        if(x.get(0) == y.get(0))
            return 0;
        return (x.get(0) < y.get(0) ? -1 : 1);
    }
}

class SortByLast implements Comparator<Point>{
    //this sorts points by their last dimension then last-1 dimension ....
    //when point are 2D it sorts by Y then X
    @Override
    public int compare(Point x, Point y) {
        for(int i=x.d.length-1;i>=0;i--){
            if(x.get(i)!= y.get(i))
                return (x.get(i) < y.get(i) ? -1 : 1);
        }
        return 0;
    }
}

class Node{
    RangeTree nextDimension= null;
    Node left,right,par;
    double value;
    Point point;

    public Node() {
    }

    public Node(Point point) {
        this.point = point;
    }

    public Node(Node par) {
        this.par = par;
    }

    Node goLeft(){
        Node p=this;
        while(p.left != null)
            p= p.left;
        return p;
    }

    Node next(boolean assign, int k) {
        double x= point.get(k);
        Node p=this;
        while (!p.isLeftChild()){
            p= p.par;
        }

        if(p.par!= null) {
            p = p.par;
            if(assign)
                p.value= x;
            p=p.right;
            return p.goLeft();
        }
        return null;
    }

    boolean isLeftChild(){
        if(par == null || par.left == this)
            return true;
        return false;
    }

    void printSubtree(ArrayList<Point> list){
        if(left != null)
            left.printSubtree(list);
        if(right!= null)
            right.printSubtree(list);
        if(left == null && right == null)
            list.add(point);
    }
}

class RangeTree {
    final int level, dimensions,sz;
    Node root;


    public RangeTree(int dimensions, Point[] points) {
        Arrays.sort(points, new SortByX());

        this.dimensions=dimensions;
        this.sz= points.length;
        this.level=0;
        buildEmpty(sz);

        int ind=0;
        Node p= root.goLeft();

        while(ind < points.length){
            p.point= points[ind];
            p=p.next(true,0);
            ind++;
        }
        if(dimensions!=1)
            buildNext(root);
    }

    void buildEmpty(int sz){
        root= new Node();
        if(sz!= 1){
            root.left= buildEmpty(root, (sz+1)/2);
            root.right= buildEmpty(root, sz/2);
        }
    }
    Node buildEmpty(Node par, int sz){
        Node x= new Node(par);
        if(sz==1)
            return x;
        x.left= buildEmpty(x, (sz+1)/2);
        x.right= buildEmpty(x, sz/2);
        return x;
    }

    void buildNext(Node x){
        if(x.left== null && x.right== null){
            x.nextDimension= new RangeTree(1,dimensions,level+1);
            x.nextDimension.root= new Node(x.point);
        }
        else{
            buildNext(x.left);
            buildNext(x.right);
            x.nextDimension= merge(x.left.nextDimension, x.right.nextDimension);
        }

        if(level+2 < dimensions)
            x.nextDimension.buildNext(x.nextDimension.root);
    }

    public RangeTree(int sz, int dimensions, int level){
        this.sz = sz;
        this.level= level;
        this.dimensions= dimensions;
        buildEmpty(sz);
    }

    public static RangeTree merge(RangeTree l, RangeTree r){
        int nlevel= l.level;
        RangeTree tmp= new RangeTree(l.sz+r.sz, l.dimensions,nlevel);
        tmp.buildEmpty(tmp.sz);

        Node x= l.root.goLeft(), y=r.root.goLeft(), z= tmp.root.goLeft();

        while(x!= null || y!= null){
            if((x!= null && y!= null && x.point.get(nlevel) < y.point.get(nlevel)) || y== null){
                z.point= x.point;
                z=z.next(true, nlevel);
                x=x.next(false, nlevel);
            }
            else{
                z.point= y.point;
                z=z.next(true,nlevel);
                y=y.next(false,nlevel);
            }
        }

        return tmp;
    }

    void search(Point l, Point r, ArrayList<Point> list){
        Node x= root;
        while (true){
            if(x.left== null && x.right ==null){
                if(l.get(level) <= x.point.get(level) && x.point.get(level)<=r.get(level)) {
                    if(level+1 == dimensions)
                        list.add(x.point);
                    else
                        x.nextDimension.search(l, r, list);
                }
                break;
            }

            if(r.get(level) <x.value)
                x=x.left;
            else if(x.value < l.get(level))
                x=x.right;
            else if(l.get(level)<= x.value && x.value<= r.get(level))
                break;
        }

        //prefix and suffix:
        Node le=x.left,ri=x.right;
        while(le!= null){
            //~while true
            if(le.left==null && le.right==null){
                if(l.get(level) <= le.point.get(level) && le.point.get(level)<=r.get(level)) {
                    if(level+1 == dimensions) {
                        list.add(le.point);
                    }
                    else {
                        le.nextDimension.search(l, r, list);
                    }
                }
                break;
            }

            if(le.value< l.get(level)) {
                le= le.right;
            }
            else{
                if(level+1 == dimensions)
                    le.right.printSubtree(list);
                else
                    le.right.nextDimension.search(l, r, list);
                le= le.left;
            }
        }

        while(ri!= null){
            //~while true
            if(ri.left==null && ri.right==null){
                if(l.get(level) <= ri.point.get(level) && ri.point.get(level)<=r.get(level)) {
                    if(level+1 == dimensions)
                        list.add(ri.point);
                    else
                        ri.nextDimension.search(l, r, list);
                }
                break;
            }

            if(r.get(level)<ri.value)
                ri= ri.left;
            else{
                if(level+1 == dimensions)
                    ri.left.printSubtree(list);
                else
                    ri.left.nextDimension.search(l,r, list);
                ri= ri.right;
            }
        }
    }

}

public class Main {
    /*

        Reading input from IO. in the first line it gets the n which is the number of point.
        Fast reader is used because default Scanner is slow when we have large scale IO.
        After reading the points we will read q which is the number of queries.
        Each query is a hypercube in kD dimension. In the testcase for these are only 2D points but it could be modified to read kD points.

     */

    public static void main(String[] args) {
        PrintStream out = new PrintStream(new BufferedOutputStream(System.out));

        //Scanner sc = new Scanner(System.in);
        FastReader sc= new FastReader();

        int n = sc.nextInt();
        Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            points[i] = new Point(2);
            points[i].set(0, sc.nextDouble());
        }
        for (int i = 0; i < n; i++)
            points[i].set(1, sc.nextDouble());

        RangeTree rangeTree = new RangeTree(2, points);

        int q = sc.nextInt();
        for (int i = 0; i < q; i++) {
            Point l = new Point(2), r = new Point(2);
            l.set(0, sc.nextDouble());
            l.set(1, sc.nextDouble());
            r.set(0, sc.nextDouble());
            r.set(1, sc.nextDouble());

            ArrayList<Point> list = new ArrayList<>();
            rangeTree.search(l, r, list);

            if (list.size() == 0) {
                out.println("None");
                continue;
            }

            list.sort(new SortByLast());

            for (Point x : list)
                out.print(x.get(0) + " ");
            out.println();
            for (Point x : list)
                out.print(x.get(1) + " ");
            out.println();
        }
        out.flush();
    }


    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() {
            br = new BufferedReader(new
                    InputStreamReader(System.in));
        }

        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }
        int nextInt() {
            return Integer.parseInt(next());
        }

        double nextDouble() {
            return Double.parseDouble(next());
        }
    }

}
