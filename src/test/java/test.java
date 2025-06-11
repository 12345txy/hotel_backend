public class test {
    public static void main(String[] args) {
        double[] test  = {32.01, 28.9998, 30.009, 29.010001, 35.999};
        for (int i = 0; i < test.length; i++) {
            double temp = test[i];
            double rounded = Math.round(temp);
            double diff = Math.abs(rounded - temp);
            if (diff < 0.0101) {
                test[i] = rounded;
            }
            System.out.println(test[i]);
        }
    }
}
