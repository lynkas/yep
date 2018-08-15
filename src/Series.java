public class Series {
    protected String shortName;
    protected String fullName;

    Series(String shortName,String fullName){
        this.shortName=shortName;
        this.fullName=fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
