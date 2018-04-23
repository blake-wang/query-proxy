import java.sql.*;

public class GPtest {

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection db = DriverManager.getConnection("jdbc:mysql://10.13.113.203:3306/jh_data", "data_work", "data_work#171012@#876");
            Statement st = db.createStatement();
            ResultSet rs = st.executeQuery("select distinct game_channel_id from agent_game_channel where game_id<128 and game_channel_name like '%IOS独代%';");
            ResultSetMetaData metaData = rs.getMetaData();
            String tableName = metaData.getColumnLabel(1);

//            ResultSet rs1 = st.executeQuery("select sum(active_uv) as active_uv, date  from rpt_day_login where date between '2017-07-01' and '2017-07-31'   group by  date ");
//            ResultSetMetaData metaData1 = rs1.getMetaData();
//            String tableName1 = metaData1.getTableName(2);
//            String schemaName1 = metaData1.getCatalogName(2);
            StringBuffer sb = new StringBuffer();
            while (rs.next()) {
                sb.append(rs.getString(1) + ",");
            }
            System.out.println(sb);
//            System.out.println("1:"+ tableName1+";  schemaName1:"+schemaName1);

            rs.close();
            st.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
