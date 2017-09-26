package controllers.askde;

public class Address {

    private String stateOrRegion;
    private String city;
    private String countryCode;
    private String postalCode;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String districtOrCounty;

    private Address() {

    }

    public String getStateOrRegion() {
        return stateOrRegion;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public String getCity() {
        return city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getDistrictOrCounty() {
        return districtOrCounty;
    }
}