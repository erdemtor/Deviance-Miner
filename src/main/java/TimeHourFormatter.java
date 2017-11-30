import java.util.*;

public class TimeHourFormatter {


    public static void main(String[] args) {
        System.out.println(get(1, 8, 3, 9, 7, 4));
    }


    public static String get(int a, int b, int c, int d, int e, int f) {
        List<Integer> ints = Arrays.asList(a, b, c, d, e, f);
        Collections.sort(ints);
        Collections.reverse(ints);

        int[] result = new int[6];
        Arrays.fill(result, -1);
        boolean isDoable = ints.stream().allMatch(num -> feed(result, num));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2 ; j++) {
               builder.append(result[2*i + j]);
            }
            builder.append(":");
        }
        builder.deleteCharAt(builder.lastIndexOf(":"));
        return isDoable ? builder.toString() : "NOT POSSIBLE";
    }

    public static boolean updateResultAndReturnTrue(int[] result, int i, int number) {
        result[i] = number;
        return true;
    }

    public static boolean feed(int[] result, int number) {
        for (int i = 5; i >= 0; i--) {
            if (result[i] != -1) continue;
            if (i % 2 == 1) {
                return updateResultAndReturnTrue(result, i, number);
            }
            if (i == 4 || i == 2) {
                if (number <= 6) {
                    return updateResultAndReturnTrue(result, i, number);
                } else {
                    continue;
                }
            }
            if (i == 0) {
                if (result[1] > 3) {
                    if (number > 1) {
                        continue;
                    } else {
                        return updateResultAndReturnTrue(result, i, number);
                    }
                } else {
                    if (number > 2) {
                        continue;
                    } else {
                        return updateResultAndReturnTrue(result, i, number);
                    }
                }
            }

        }
        return false;
    }
}
