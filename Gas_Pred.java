public class Gas_Pred{	
	public native String GasPredition(String date, int num_days);
	
	public static void main(String[] args){
		
		//Date argument provided MUST be in format: "MMM DD, YYYY" i.e. "Aug 01, 1992"  
		String date = "Sep 02, 2018";
		int num_days = 6;
		System.loadLibrary("Gas_Pred");
		Gas_Pred predictor = new Gas_Pred();
		String predictions = predictor.GasPredition(date, num_days);
		System.out.println("Predictions: " + predictions);
		
		//Parsing goes here
	}
}