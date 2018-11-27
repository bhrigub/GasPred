package GasPricePrediction.GasPrice;

public class GetGasPrices {

	private final String[] regular;
	private final String[] midgrade;
	private final String[] premium;
	private final String[] deisel;

	public GetGasPrices(String[] regular,String[] midgrade,String[] premium,String[] deisel) {
		this.regular = regular;
		this.midgrade = midgrade;
		this.premium = premium;
		this.deisel = deisel;
	}


	public String[] getRegular() {
		return regular;
	}
	public String[] getMidgrade() {
		return midgrade;
	}
	public String[] getPremium() {
		return premium;
	}
	public String[] getDeisel() {
		return deisel;
	}

}