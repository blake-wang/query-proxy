import com.ijunhai.dao.KylinDao;
import com.ijunhai.dao.MysqlDao;
import com.ijunhai.util.PropertiesUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import java.sql.ResultSet;

import static com.ijunhai.contants.ProxyConstants.*;

/**
 * Created by admin on 2017/9/21.
 */
public class test {
    @Test
    public void query() throws Exception {
//        String start = "2017-09-21 15:00:00";
//        String end = "2017-09-22 14:59:00";
//
//        DateTime dateTime = new DateTime();
//        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
//        DateTime startTime = DateTime.parse(start, format);
//        DateTime endTime = DateTime.parse(end, format);
//        int days = Days.daysBetween(startTime, endTime).getDays();
//        startTime.hourOfDay();
//        String key = "123465789";
////        System.out.println(key.substring(0,key.length()-2));
//        String s = "AAA aaa AAA";
//        String a = s.replaceAll("(?i)aaa", "bbb");
//        System.out.println(a);
        KylinDao m = KylinDao.getInstance();
        ResultSet set = m.execQuery("select * from dalan limit 1;");
        while(set.next()){
            System.out.println(set.getString(1));
        }
    }

}
