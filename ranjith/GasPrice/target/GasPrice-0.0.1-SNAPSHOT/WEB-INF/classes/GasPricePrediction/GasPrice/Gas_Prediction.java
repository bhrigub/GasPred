package GasPricePrediction.GasPrice;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.util.Random;


public class Gas_Prediction{	
	
	public double initial_trend(Vector<Double> nums, int slen){
		double sum = 0.0;
		for(int i = 0; i < slen; i++){
			sum += (nums.elementAt(i+slen) - nums.elementAt(i)) / slen;
		}
		return (sum / slen);
	}
	
	public Vector<Double> initial_seasonal_components(Vector<Double> nums, int slen){
		Vector<Double> seasonals = new Vector<Double>();
		Vector<Double> season_averages = new Vector<Double>();
		double sum = 0.0;
		int n_seasons = nums.size() / slen;
		for(int j = 0; j < n_seasons; j++){
			sum = 0.0;
			for(int k = slen*j; k < (slen*j)+slen; k++){
				sum += nums.elementAt(k);
			}
			season_averages.addElement((sum/slen));
		}
		for(int i = 0; i < slen; i++){
			sum = 0.0;
			for(int j = 0; j < n_seasons; j++){
				sum += nums.elementAt((slen*j)+i)-season_averages.elementAt(j);
			}
			seasonals.addElement((sum/n_seasons));
		}
		return seasonals;
	}
	
	Vector<Double> triple_exponential_smoothing(Vector<Double> nums, int slen, double alpha, double beta, double gamma, int n_preds){
		Vector<Double> preds = new Vector<Double>();
		Vector<Double> seasonals = initial_seasonal_components(nums, slen);
		double last_smooth = 0, smooth = 0, trend = 0, val = 0;
		int m;
		for(int i = 0; i < (nums.size()+n_preds); i++){
			if(i == 0){
				smooth = nums.elementAt(0);
				trend = initial_trend(nums, slen);
				//preds.push_back(nums[0]);
				continue;
			}
			if(i >= nums.size()){
				m = i - nums.size() + 1;
				preds.addElement((smooth+(m*trend))+seasonals.elementAt(i%slen));
			}
			else{
				val = nums.elementAt(i);
				last_smooth = smooth;
				smooth = alpha*(val-seasonals.elementAt(i%slen)) + (1-alpha)*(smooth+trend);
				trend = beta * (smooth-last_smooth) + (1-beta)*trend;
				seasonals.set(i%slen, gamma*(val-smooth) + (1-gamma)*seasonals.elementAt(i%slen));
				//preds.push_back(smooth+trend+seasonals[i%slen]);
			}
		}
		return preds;
	}

	Vector<Double> train_triple(Vector<Double> nums, int num_days){
		//cout << nums.size() << endl;
		Random rand = new Random();
		double sub_alpha = rand.nextDouble();
		double sub_beta = rand.nextDouble();
		double sub_gamma = rand.nextDouble();
		int got_min = 0, slen = 0;
		double sub_err = 0, sub_rmse = 0, rmse = 100.0, alpha = 0, gamma = 0, beta = 0;
		Vector<Double> training = new Vector<Double>();
		Vector<Double> testing = new Vector<Double>();
		Vector<Double> preds = new Vector<Double>();
		int split = (int)(nums.size()*0.6);
		int sub_slen = Math.abs(rand.nextInt()) % (split/2) + 1; 
		for(int i = 0; i < split; i++){
			training.addElement(nums.elementAt(i));
		}
		for(int i = split; i < nums.size(); i++){
			testing.addElement(nums.elementAt(i));
		}
		
		while(got_min < 10000){
			//cout << got_min << endl;
			//cout << training.size() << " " << sub_slen << " " << sub_alpha << " " << sub_beta << " " << sub_gamma << endl; 
			preds = triple_exponential_smoothing(training, sub_slen, sub_alpha, sub_beta, sub_gamma, testing.size());
			sub_err = 0.0;
			for(int i = 0; i < testing.size(); i++){
				sub_err += Math.pow((preds.elementAt(i)-testing.elementAt(i)), 2); 
			}
			sub_rmse = Math.sqrt(sub_err / testing.size());
			if(sub_rmse < rmse){
				rmse = sub_rmse;
				slen = sub_slen;
				alpha = sub_alpha;
				beta = sub_alpha;
				gamma = sub_gamma;
				got_min = 0;
			}
			else{
				got_min++;
				sub_slen = Math.abs(rand.nextInt()) % (split/2) + 1; 
				sub_alpha = rand.nextDouble();
				sub_beta = rand.nextDouble();
				sub_gamma = rand.nextDouble();
			}
		}
		preds = triple_exponential_smoothing(nums, slen, alpha, beta, gamma, num_days);
		return preds;
	}

	double get_b1(Vector<Double> nums, Vector<Integer> days){
		int size = nums.size();
		double mean_nums = 0, mean_days = 0;
		double sum_xy = 0, sum_xx = 0;
		double b1;
		for(int i = 0; i < size; i++){
			mean_nums += nums.elementAt(i);
			mean_days += days.elementAt(i);
		}
		mean_nums /= (size);
		mean_days /= (size);
		for(int i = 0; i < size; i++){
			sum_xy += ((days.elementAt(i) - mean_days) * (nums.elementAt(i) - mean_nums));
			sum_xx += (Math.pow((days.elementAt(i) - mean_days), 2));
		}
		b1 = sum_xy / sum_xx;
		return b1;
	}

	double get_b0(Vector<Double> nums, Vector<Integer> days, double b1){
		int size = nums.size();
		double mean_nums = 0, mean_days = 0;
		double b0;
		for(int i = 0; i < size; i++){
			mean_nums += nums.elementAt(i);
			mean_days += days.elementAt(i);
		}
		mean_nums /= (size);
		mean_days /= (size);
		b0 = mean_nums - (b1*mean_days);
		return b0;
	}

	Vector<Double> lin_reg(Vector<Double> nums, Vector<Integer> days, int init_start, int num_days){
		//System.out.println(nums.size());
		double rmse = 100.0, pred, b1 = 0, b0 = 0, sub_b1, sub_b0, sum_err, sub_rmse;
		int count = num_days+1;
		int got_min = 0;
		Vector<Double> returns = new Vector<Double>();
		Vector<Double> predictions = new Vector<Double>();
		Vector<Double> sub_nums = new Vector<Double>();
		Vector<Integer> sub_days = new Vector<Integer>();
		for(int i = 1; i <= num_days; i++){
			sub_nums.addElement(nums.elementAt(nums.size()-i));
			sub_days.addElement(days.elementAt(days.size()-i));
		}
	  
		while(got_min < (nums.size()/2)){
			sub_b1 = get_b1(sub_nums, sub_days);
			sub_b0 = get_b0(sub_nums, sub_days, sub_b1);
			sub_nums.addElement(nums.elementAt(nums.size()-count));
			sub_days.addElement(days.elementAt(days.size()-count));
			sum_err = 0.0;
			for(int i = 1; i <= sub_nums.size(); i++){
				pred = sub_b0+(sub_b1*days.elementAt(days.size()-i));
				sum_err += Math.pow((pred-nums.elementAt(nums.size()-i)), 2); 
			}
			sub_rmse = Math.sqrt(sum_err / sub_nums.size());
			if(sub_rmse < rmse){
				rmse = sub_rmse;
				b0 = sub_b0;
				b1 = sub_b1;
				got_min = 0;
			}
			else{
				got_min++;
			}
			count++;
		}
		//b1 = get_b1(nums, days);
		//b0 = get_b0(nums, days, b1);
		//cout << "   ";
		for(int i = 1; i <= num_days; i++){
			//cout << b0 + b1*(init_start+i) << " ";
			returns.addElement(b0+b1*(init_start+i));
		}
		//cout << endl;
		return returns;
	}

	int month_value(String month){
		if(month.equals("Jan")) return 0;
		if(month.equals("Feb")) return 1;
		if(month.equals("Mar")) return 2;
		if(month.equals("Apr")) return 3;
		if(month.equals("May")) return 4;
		if(month.equals("Jun")) return 5;
		if(month.equals("Jul")) return 6;
		if(month.equals("Aug")) return 7;
		if(month.equals("Sep")) return 8;
		if(month.equals("Oct")) return 9;
		if(month.equals("Nov")) return 10;
		if(month.equals("Dec")) return 11;
		return 12;
	}

	void print_vector(Vector<Double> items){
		for(int i = 0; i < items.size(); i++){
			System.out.print(items.elementAt(i) + " ");
		}
		System.out.println();
	}

	Vector<Double> average_vectors(Vector<Double> lin, Vector<Double> exp){
		Vector<Double> avg = new Vector<Double>();
		for(int i = 0; i < lin.size(); i++){
			avg.addElement((lin.elementAt(i)+exp.elementAt(i))/2);
		}	
		return avg;
	}

	String return_prices(Vector<Double> reg, Vector<Double> mid, Vector<Double> prem, Vector<Double> diesel){
		String prices = "";
		for(int i = 0; i < reg.size()-1; i++){
			prices += Double.toString(reg.elementAt(i)) + ",";
		}
		prices += Double.toString(reg.elementAt(reg.size()-1));
		prices += ";";
		for(int i = 0; i < mid.size()-1; i++){
			prices += Double.toString(mid.elementAt(i)) + ",";
		}
		prices += Double.toString(mid.elementAt(mid.size()-1));
		prices += ";";
		for(int i = 0; i < prem.size()-1; i++){
			prices += Double.toString(prem.elementAt(i)) + ",";
		}
		prices += Double.toString(prem.elementAt(prem.size()-1));
		prices += ";";
		for(int i = 0; i < diesel.size()-1; i++){
			prices += Double.toString(diesel.elementAt(i)) + ",";
		}
		prices += Double.toString(diesel.elementAt(diesel.size()-1));
		prices += ";";
		return prices;
	}

	String predict_dates(String init_date, int num_days){
		String date, line;
		Vector<Double> all_form = new Vector<Double>(), all_conv = new Vector<Double>(), all_reform = new Vector<Double>();
		Vector<Double> reg_form = new Vector<Double>(), reg_conv = new Vector<Double>(), reg_reform = new Vector<Double>();
		Vector<Double> mid_form = new Vector<Double>(), mid_conv = new Vector<Double>(), mid_reform = new Vector<Double>();
		Vector<Double> prem_form = new Vector<Double>(), prem_conv = new Vector<Double>(), prem_reform = new Vector<Double>();
		Vector<Double> diesel_form = new Vector<Double>(), diesel_conv = new Vector<Double>(), diesel_reform = new Vector<Double>();
		Vector<Integer> days = new Vector<Integer>();
		int month, day, year, copy_days, init_start;
		double true_value;

		month = month_value(init_date.subSequence(0,3).toString());
		day = Integer.parseInt(init_date.subSequence(4,6).toString());
		year = Integer.parseInt(init_date.subSequence(8,12).toString());
		init_start = day+(month*30)+((year-1990)*365);
	  
		try{
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("PET_PRI_GND_DCUS_NUS_W.csv");
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream ));
			br.readLine();
			br.readLine();
			line = br.readLine();
			//System.out.println(value);
			//getline(file, value, '\n');
			while(((line = br.readLine()) != null)){
				String value[] = line.split(",");
				//getline(file, value, ',');
				for(int i = 0; i < value.length; i++){
					if(value[i] != null && !value[i].isEmpty()){
						if(i == 0){
							date = value[i];
							month = month_value(date.subSequence(1,4).toString());
							day = Integer.parseInt(date.subSequence(5,7).toString());
							i++;
							year = Integer.parseInt(value[i].subSequence(1,5).toString());
							days.addElement(day+(month*30)+((year-1990)*365));
						}		
						if(i == 2){
							all_form.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 3){
							all_conv.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 4){
							all_reform.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 5){
							reg_form.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 6){
							reg_conv.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 7){
							reg_reform.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 8){
							mid_form.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 9){
							mid_conv.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 10){
							mid_reform.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 11){
							prem_form.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 12){
							prem_conv.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 13){
							prem_reform.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 14){
							diesel_form.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 15){
							diesel_conv.addElement(Double.parseDouble(value[i].toString()));
						}
						if(i == 16){
							diesel_reform.addElement(Double.parseDouble(value[i].toString()));
						}
					}
				}
			}
		} 
		catch(Exception ee){
			ee.printStackTrace();
		}
	  
		Vector<Double> reg_lin, mid_lin, prem_lin, diesel_lin;
		Vector<Double> reg_exp, mid_exp, prem_exp, diesel_exp; 
		Vector<Double> reg_avg, mid_avg, prem_avg, diesel_avg;
	  
		//cout << "Predictions: " << endl;
		//cout << " " << labels[4] << ": " << endl;
		reg_lin = lin_reg(reg_form, days, init_start, num_days);
		//print_vector(reg_lin);
		//cout << " " << labels[7] << ": " << endl;
		mid_lin = lin_reg(mid_form, days, init_start, num_days);
		//print_vector(mid_lin);
		//cout << " " << labels[10] << ": " << endl;
		prem_lin = lin_reg(prem_form, days, init_start, num_days);
		//print_vector(prem_lin);
		//cout << " " << labels[13] << ": " << endl;
		diesel_lin = lin_reg(diesel_form, days, init_start, num_days);
		//print_vector(diesel_lin);
	  
		//cout << endl;
	  
		//cout << " " << labels[4] << ": " << endl;
		reg_exp = train_triple(reg_form, num_days);
		//print_vector(reg_exp);
		//cout << " " << labels[7] << ": " << endl;
		mid_exp = train_triple(mid_form, num_days);
		//print_vector(mid_exp);
		//cout << " " << labels[10] << ": " << endl;
		prem_exp = train_triple(prem_form, num_days);
		//print_vector(prem_exp);
		//cout << " " << labels[13] << ": " << endl;
		diesel_exp = train_triple(diesel_form, num_days);
		//print_vector(diesel_exp);
	  
		//cout << endl;
	  
		//cout << " " << labels[4] << ": " << endl;
		reg_avg = average_vectors(reg_lin, reg_exp);
		//print_vector(reg_avg);
		//cout << " " << labels[7] << ": " << endl;
		mid_avg = average_vectors(mid_lin, mid_exp);
		//print_vector(mid_avg);
		//cout << " " << labels[10] << ": " << endl;
		prem_avg = average_vectors(prem_lin, prem_exp);
		//print_vector(prem_avg);
		//cout << " " << labels[13] << ": " << endl;
		diesel_avg = average_vectors(diesel_lin, diesel_exp);
		//print_vector(diesel_avg);
	  
		//cout << endl;
		String prices = return_prices(reg_avg, mid_avg, prem_avg, diesel_avg);
		//cout << prices << endl;
		return prices;
	}

	public static void main(String[] args){
		
		//Date argument provided MUST be in format: "MMM DD, YYYY" i.e. "Aug 01, 1992"  
		String date = "Sep 02, 2018";
		int num_days = 6;
		String prediction = "";
		Gas_Prediction predictor = new Gas_Prediction();
		prediction = predictor.predict_dates(date, num_days);
		System.out.println(prediction);
		
		//Parsing goes here
	}
}