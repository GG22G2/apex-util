package hsb.game.util.demo;

/**
 * @author 胡帅博
 * @date 2023/10/8 20:00
 */
public class MathTan {
    public static void main(String[] args) {
        //double tan = Math.tan(Math.toRadians(60));
        //System.out.println(tan);

       // double radians = Math.atan(-Double.MAX_VALUE);
        //System.out.println(radians);
        //  System.out.println(Math.toDegrees(radians));


        double asin = Math.cos(Math.toRadians(45));

      //  System.out.println(asin);
        //System.out.println(Math.toDegrees(asin));

// [4.578689990600971, -0.023554315003135717, 1654.4156880363144, -0.007574885100830877, 4.598515868353865, 756.6599129357995, -9.673828577892999E-6, -1.0705006679635081E-5, 1.0]


        double rotate = Math.atan2(-0.023554315003135717, -0.007574885100830877);
        System.out.println(Math.toDegrees(rotate));

    }
}
