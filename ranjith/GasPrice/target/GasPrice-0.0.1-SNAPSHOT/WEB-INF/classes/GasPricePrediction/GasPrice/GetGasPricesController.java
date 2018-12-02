package GasPricePrediction.GasPrice;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetGasPricesController {
	private static Map<String, String[]> map;
	private static Map<String, String> monthMap;

	static
	{
		map = new HashMap<String, String[]>();
		String[] info1 = {"123","Marcus","443529xxx","Monthly","5003 Westland Blvd","29300XX","NA"};
		map.put("123",info1 );
		String[] info2 = {"456","Magnus","261829xxx","Anual","4766 Drayton Blvd","560400XX","NA"};
		map.put("456", info2);
		monthMap = new HashMap<String, String>();
		monthMap.put("01", "Jan");
		monthMap.put("02", "Feb");
		monthMap.put("03", "Mar");
		monthMap.put("04", "Apr");
		monthMap.put("05", "May");
		monthMap.put("06", "Jun");
		monthMap.put("07", "Jul");
		monthMap.put("08", "Aug");
		monthMap.put("09", "Sep");
		monthMap.put("10", "Oct");
		monthMap.put("11", "Nov");
		monthMap.put("12", "Dec");

	}

	@RequestMapping("/gasPrice")
	public GetGasPrices greeting(@RequestParam(value="startDate") String startDate, @RequestParam(value="noOfDays") String noOfDays ) {

		char[] dateArray = startDate. toCharArray();
		String date = monthMap.get((dateArray[0]+""+dateArray[1]))+" "+dateArray[2]+dateArray[3]+", "+dateArray[4]+dateArray[5]+dateArray[6]+dateArray[7];
		//String date = "Sep 02, 2018";
		int num_days = Integer.parseInt(noOfDays);
		Gas_Prediction pediction = new Gas_Prediction();
		String[] regular = new String[num_days];
		String[] midgrade = new String[num_days];
		String[] premium = new String[num_days];
		String[] deisel = new String[num_days];

		String[][] prices = new String[4][num_days];
		String predict_dates = pediction.predict_dates(date, num_days);
		String[] predict_split = predict_dates.split(";");

		updateArray(regular, predict_split,0);
		updateArray(midgrade, predict_split,1);
		updateArray(premium, predict_split,2);
		updateArray(deisel, predict_split,3);

		for(int i = 0; i< predict_split.length; i++)
		{
			String[] pre = predict_split[i].split(",");
			for(int j = 0; j < pre.length; j++)
			{
				prices[i][j] = pre[j].toString();
			}
		}
		return new GetGasPrices(regular, midgrade, premium, deisel);
	}

	private String[] updateArray(String[] regular, String[] predict_split, int index) {
		String[] pre = predict_split[index].split(",");
		for(int i=0 ; i< pre.length; i++ )
		{
			regular[i] = pre[i];
		}
		return pre;
	}

	@RequestMapping("/testRest/{customerId}/{type}")
	public TestRest testRest(@PathVariable(value="customerId") String custId, @PathVariable(value="type") String type ) {
		String[] strings = map.get(custId);
		return new TestRest(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6]);
	}

}