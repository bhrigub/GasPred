package GasPricePrediction.GasPrice;

public class TestRest {
	
    private final String days;
    private final String name;
    private final String isdn;
    private final String plan;
    private final String address;
    private final String ssn;
    private final String family;
    

    public TestRest(String days, String name, String isdn, String plan, String address, String ssn, String family) {
        this.days = days;
        this.name = name;
        this.isdn = isdn;
        this.plan = plan;
        this.address = address;
        this.ssn = ssn;
        this.family = family;
    }

    public String getDays() {
        return days;
    }
    
    public String getName() {
        return name;
    }
    
    public String getIsdn() {
        return isdn;
    }
    
    public String getplan() {
        return plan;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getSsn() {
        return ssn;
    }
    public String getFamily() {
        return family;
    }
    

}
