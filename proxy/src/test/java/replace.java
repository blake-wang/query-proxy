/**
 * Created by admin on 2017/12/20.
 */
public class replace {
    public static void main(String[] args) {
        String a ="dddd game_channel_id ";
        String b= a.replaceAll(" (?i)gaMe_channel_id "," \"GAME_CHANNEL_ID\" ");
        System.out.println(b);
    }
}
