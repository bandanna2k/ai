package dnt.localagentapp.tools;

import java.util.Date;

public class PublicOrder
{
    public final String orderId;
    public final Date creationTime;
    public final Status status;

    public PublicOrder(
            String orderId,
            Date creationTime,
            Status status)

    {
        this.orderId = orderId;
        this.creationTime = creationTime;
        this.status = status;
    }
}
