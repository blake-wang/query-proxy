//
//import com.ijunhai.metrics.ActiveUvMetric;
//import com.ijunhai.model.Condition;
//import com.ijunhai.model.Metric;
//
//import com.ijunhai.model.QueryModel;
//import com.ijunhai.model.parsers.ConditionParse;
//import com.ijunhai.util.KylinConnect;
//
//import org.junit.Test;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Path("/")
//public class QueryResource {
//    @Test
//    public void query() throws Exception {
//        QueryModel model = new QueryModel();
////        Condition conditions = model.getConditions();
//        List<String> setReturn_demensionsList = new ArrayList<>();
//        setReturn_demensionsList.add("Cha");
//        List<Metric> metricList = new ArrayList<>();
//        metricList.add(new ActiveUvMetric());
//
//        Condition con = new Condition();
//        List<String> channelId = new ArrayList<>();
//        channelId.add("4");
//        con.setChannelId(channelId);
//        con.setStart("2017-08-07");
//        con.setEnd("2017-08-07");
//
//        model.setReturnDemensions(setReturn_demensionsList);
//        model.setConditions(con);
//        model.setGranularity("day");
//        model.setMetrics(metricList);
//
//        Condition conditions = model.getConditions();
//        List<Metric> metricList2 = model.getMetrics();
//        String Granularity = model.getGranularity();
//        List<String> returnDemensionsList = model.getReturnDemensions();
//
//        ConditionParse ConditionParse = new ConditionParse(conditions);
//        String ConditionSql = ConditionParse.Parse(conditions);
//
//        String returnDemensionsSql = "";
//        String groupByField = "";
//        String finalSql;
//        if (returnDemensionsList.size() != 0 && Granularity != "none"){
//            for (int i = 0; i < returnDemensionsList.size(); i++) {
//                returnDemensionsSql += returnDemensionsList.get(i) + ",";
//            }
//            returnDemensionsSql += "server_date_"+ Granularity +",";
//            groupByField = " group by " + returnDemensionsSql.substring(0 ,returnDemensionsSql.length()-1);
//        } else if (returnDemensionsList.size() != 0 && Granularity == "none"){
//            for (int i = 0; i < returnDemensionsList.size(); i++) {
//                returnDemensionsSql += returnDemensionsList.get(i) + ",";
//            }
//
//            groupByField = "group by " + returnDemensionsSql.substring(0 ,returnDemensionsSql.length()-1);
//        }
//
//        for (int i = 0;i< metricList.size();i++) {
//            String fuction = metricList.get(i).getFuction();
//            String condition = metricList.get(i).getConditions();
//            List<String> tableName = metricList.get(i).getMetricAndTableName();
//            finalSql = "select "  + returnDemensionsSql + fuction+ "from " + tableName.get(1) +
//                    " where " + ConditionSql + condition + groupByField;
//            KylinConnect.connect(finalSql,tableName.get(0));
//            System.out.println(finalSql);
//        }
//
//
//    }
//
//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    @Path("test")
//    public String test() {
//        return "server is ok";
//    }
//
//}