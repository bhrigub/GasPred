#include "Gas_Pred.h"
#include <string.h>
#include <iostream>
#include <stdlib.h>
#include <stdio.h>
#include <fstream>
#include <string.h>
#include <vector>
#include <math.h>

using namespace std;

float initial_trend(vector<float> nums, int slen){
  float sum = 0.0;
  for(int i = 0; i < slen; i++){
	sum += (nums[i+slen] - nums[i]) / slen;
  }
  return (sum / slen);
}

vector<float> initial_seasonal_components(vector<float> nums, int slen){
  vector<float> seasonals;
  vector<float> season_averages;
  float sum = 0.0;
  int n_seasons = nums.size() / slen;
  for(int j = 0; j < n_seasons; j++){
	sum = 0.0;
	for(int k = slen*j; k < (slen*j)+slen; k++){
		sum += nums[k];
	}
	season_averages.push_back((sum/slen));
  }
  for(int i = 0; i < slen; i++){
	sum = 0.0;
	for(int j = 0; j < n_seasons; j++){
		sum += nums[(slen*j)+i]-season_averages[j];
	}
	seasonals.push_back((sum/n_seasons));
  }
  return seasonals;
}

vector<float> triple_exponential_smoothing(vector<float> nums, int slen, float alpha, float beta, float gamma, int n_preds){
  vector<float> preds;
  vector<float> seasonals = initial_seasonal_components(nums, slen);
  float last_smooth, smooth, trend, val;
  int m;
  for(int i = 0; i < (nums.size()+n_preds); i++){
	  if(i == 0){
		  smooth = nums[0];
		  trend = initial_trend(nums, slen);
		  //preds.push_back(nums[0]);
		  continue;
	  }
	  if(i >= nums.size()){
		  m = i - nums.size() + 1;
		  preds.push_back((smooth+(m*trend))+seasonals[i%slen]);
	  }
	  else{
		  val = nums[i];
		  last_smooth = smooth;
		  smooth = alpha*(val-seasonals[i%slen]) + (1-alpha)*(smooth+trend);
		  trend = beta * (smooth-last_smooth) + (1-beta)*trend;
		  seasonals[i%slen] = gamma*(val-smooth) + (1-gamma)*seasonals[i%slen];
		  //preds.push_back(smooth+trend+seasonals[i%slen]);
	  }
  }
  return preds;
}

vector<float> train_triple(vector<float> nums, int num_days){
	//cout << nums.size() << endl;
	float sub_alpha = ((float)rand()) / (float)RAND_MAX;
	float sub_beta = ((float)rand()) / (float)RAND_MAX;
	float sub_gamma = ((float)rand()) / (float)RAND_MAX;
	int got_min = 0, slen;
	float sub_err, sub_rmse, rmse = 100.0, alpha, gamma, beta;
	vector<float> training, testing, preds;
	int split = (int)(nums.size()*0.6);
	int sub_slen = rand() % (split/2) + 1; 
	for(int i = 0; i < split; i++){
		training.push_back(nums[i]);
	}
	for(int i = split; i < nums.size(); i++){
		testing.push_back(nums[i]);
	}
	
	while(got_min < 10000){
		//cout << got_min << endl;
		//cout << training.size() << " " << sub_slen << " " << sub_alpha << " " << sub_beta << " " << sub_gamma << endl; 
		preds = triple_exponential_smoothing(training, sub_slen, sub_alpha, sub_beta, sub_gamma, testing.size());
		sub_err = 0.0;
		for(int i = 0; i < testing.size(); i++){
			sub_err += pow((preds[i]-testing[i]), 2); 
		}
		sub_rmse = sqrt(sub_err / testing.size());
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
			sub_slen = rand() % (split/2) + 1; 
			sub_alpha = ((float)rand()) / (float)RAND_MAX;
			sub_beta = ((float)rand()) / (float)RAND_MAX;
			sub_gamma = ((float)rand()) / (float)RAND_MAX;
		}
	}
	preds = triple_exponential_smoothing(nums, slen, alpha, beta, gamma, num_days);
	return preds;
}

float get_b1(vector<float> nums, vector<int> days){
  int size = nums.size();
  float mean_nums = 0, mean_days = 0;
  float sum_xy = 0, sum_xx = 0;
  float b1;
  for(int i = 0; i < size; i++){
    mean_nums += nums[i];
    mean_days += days[i];
  }
  mean_nums /= (size);
  mean_days /= (size);
  for(int i = 0; i < size; i++){
    sum_xy += ((days[i] - mean_days) * (nums[i] - mean_nums));
    sum_xx += (pow((days[i] - mean_days), 2));
  }
  b1 = sum_xy / sum_xx;
  return b1;
}

float get_b0(vector<float> nums, vector<int> days, float b1){
  int size = nums.size();
  float mean_nums = 0, mean_days = 0;
  float b0;
  for(int i = 0; i < size; i++){
    mean_nums += nums[i];
    mean_days += days[i];
  }
  mean_nums /= (size);
  mean_days /= (size);
  b0 = mean_nums - (b1*mean_days);
  return b0;
}

vector<float> lin_reg(vector<float> nums, vector<int> days, int init_start, int num_days){
  float rmse = 100.0, pred, b1, b0, sub_b1, sub_b0, sum_err, sub_rmse;
  int count = num_days+1;
  int got_min = 0;
  vector<float> returns;
  vector<float> predictions;
  vector<float> sub_nums;
  vector<int> sub_days;
  for(int i = 1; i <= num_days; i++){
	  sub_nums.push_back(nums[nums.size()-i]);
	  sub_days.push_back(days[days.size()-i]);
  }
  
  while(got_min < (nums.size()/2)){
    sub_b1 = get_b1(sub_nums, sub_days);
    sub_b0 = get_b0(sub_nums, sub_days, sub_b1);
    sub_nums.push_back(nums[nums.size()-count]);
    sub_days.push_back(days[days.size()-count]);
	sum_err = 0.0;
	for(int i = 1; i <= sub_nums.size(); i++){
		pred = sub_b0+(sub_b1*days[days.size()-i]);
		sum_err += pow((pred-nums[nums.size()-i]), 2); 
	}
	sub_rmse = sqrt(sum_err / sub_nums.size());
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
    returns.push_back(b0+b1*(init_start+i));
  }
  //cout << endl;
  return returns;
}

int month_value(string month){
  if(month.compare("Jan") == 1) return 0;
  if(month.compare("Feb") == 1) return 1;
  if(month.compare("Mar") == 1) return 2;
  if(month.compare("Apr") == 1) return 3;
  if(month.compare("May") == 1) return 4;
  if(month.compare("Jun") == 1) return 5;
  if(month.compare("Jul") == 1) return 6;
  if(month.compare("Aug") == 1) return 7;
  if(month.compare("Sep") == 1) return 8;
  if(month.compare("Oct") == 1) return 9;
  if(month.compare("Nov") == 1) return 10;
  if(month.compare("Dec") == 1) return 11;
  return 12;
}

void print_vector(vector<float> items){
	for(int i = 0; i < items.size(); i++){
		cout << items[i] << " ";
	}
	cout << endl;
}

vector<float> average_vectors(vector<float> lin, vector<float> exp){
	vector<float> avg;
	for(int i = 0; i < lin.size(); i++){
		avg.push_back((lin[i]+exp[i])/2);
	}	
	return avg;
}

string return_prices(vector<float> reg, vector<float> mid, vector<float> prem, vector<float> diesel){
	string prices = "";
	for(int i = 0; i < reg.size()-1; i++){
		prices += to_string(reg[i]) + ",";
	}
	prices += to_string(reg[reg.size()-1]);
	prices += ";";
	for(int i = 0; i < mid.size()-1; i++){
		prices += to_string(mid[i]) + ",";
	}
	prices += to_string(mid[mid.size()-1]);
	prices += ";";
	for(int i = 0; i < prem.size()-1; i++){
		prices += to_string(prem[i]) + ",";
	}
	prices += to_string(prem[prem.size()-1]);
	prices += ";";
	for(int i = 0; i < diesel.size()-1; i++){
		prices += to_string(diesel[i]) + ",";
	}
	prices += to_string(diesel[diesel.size()-1]);
	prices += ";";
	return prices;
}

string predict_dates(string init_date, int num_days){
  ifstream file("PET_PRI_GND_DCUS_NUS_W.csv");
  string value, date;
  vector<float> all_form, all_conv, all_reform;
  vector<float> reg_form, reg_conv, reg_reform;
  vector<float> mid_form, mid_conv, mid_reform;
  vector<float> prem_form, prem_conv, prem_reform;
  vector<float> diesel_form, diesel_conv, diesel_reform;
  vector<int> days;
  int month, day, year, i=0, copy_days, init_start;
  size_t pos;
  float true_value;
  string labels[16];

  month = month_value(init_date.substr(0,3));
  day = atoi(init_date.substr(4,6).c_str());
  year = atoi(init_date.substr(8,12).c_str());
  init_start = day+(month*30)+((year-1990)*365);
  
  getline(file, value);
  getline(file, value);
  for(int i =0; i <= 15; i++){
    getline(file, value, ',');
    labels[i] = value;
  }
  getline(file, value, '\n');
  while(file.good() && i<16){
    getline(file, value, ',');
    if(value != ""){
      if(i == 0){
	date = value;
	month = month_value(date.substr(1,4));
	day = atoi(date.substr(5,7).c_str());
	getline(file, value, ',');
	year = atoi(value.substr(1,5).c_str());
	days.push_back(day+(month*30)+((year-1990)*365));
      }
      if(i == 1){
	all_form.push_back(atof(value.c_str()));
      }
      if(i == 2){
	all_conv.push_back(atof(value.c_str()));
      }
      if(i == 3){
        all_reform.push_back(atof(value.c_str()));
      }
      if(i == 4){
        reg_form.push_back(atof(value.c_str()));
      }
      if(i == 5){
        reg_conv.push_back(atof(value.c_str()));
      }
      if(i == 6){
        reg_reform.push_back(atof(value.c_str()));
      }
      if(i == 7){
        mid_form.push_back(atof(value.c_str()));
      }
      if(i == 8){
        mid_conv.push_back(atof(value.c_str()));
      }
      if(i == 9){
        mid_reform.push_back(atof(value.c_str()));
      }
      if(i == 10){
        prem_form.push_back(atof(value.c_str()));
      }
      if(i == 11){
        prem_conv.push_back(atof(value.c_str()));
      }
      if(i == 12){
        prem_reform.push_back(atof(value.c_str()));
      }
      if(i == 13){
        diesel_form.push_back(atof(value.c_str()));
      }
      if(i == 14){
        diesel_conv.push_back(atof(value.c_str()));
      }
      if(i == 15){
        diesel_reform.push_back(atof(value.c_str()));
      }
    }
    i++;
    if(i == 16){
      getline(file, value, '\n');
      i = 0;
    }
  }
  
  vector<float> reg_lin, mid_lin, prem_lin, diesel_lin;
  vector<float> reg_exp, mid_exp, prem_exp, diesel_exp; 
  vector<float> reg_avg, mid_avg, prem_avg, diesel_avg;
  
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
  
  cout << endl;
  
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
  string prices = return_prices(reg_avg, mid_avg, prem_avg, diesel_avg);
  //cout << prices << endl;
  return prices;
}

JNIEXPORT jstring JNICALL Java_Gas_1Pred_GasPredition
  (JNIEnv *env, jobject obj, jstring date, jint num_days){
	string predictions = predict_dates(env->GetStringUTFChars(date, 0), num_days);
	return env->NewStringUTF(predictions.c_str());
  }
  
int main(){
	return 0;
}
