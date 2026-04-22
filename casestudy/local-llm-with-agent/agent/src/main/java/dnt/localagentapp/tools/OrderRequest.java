package dnt.localagentapp.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderRequest
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    public String firstName;
    public String lastName;
    public String dateOfBirth;

    public String orderId;

    public OrderRequest() {
    }

    public OrderRequest(String firstName, String lastName, String dateOfBirth, String orderId)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;

        this.orderId = orderId;
    }

    public String key() throws ParseException
    {
        return firstName + "|" + lastName +  "|" + SDF.format(dateOfBirth());
    }

    public Date dateOfBirth() throws ParseException
    {
        return SDF.parse(this.dateOfBirth);
    }
}
