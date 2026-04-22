package dnt.localagentapp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dnt.localagentapp.Tool;

import java.sql.Date;
import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class OrdersTool implements Tool
{
    public static final OrderRequest EXAMPLE_REQUEST = new OrderRequest(
            "John",
            "Doe",
            "2000-01-01",
            "12345"
    );
    private final ObjectMapper mapper;
    private final String exampleString;
    private final String description;

    private final Map<String, PublicOrder> orders = new HashMap<>();

    public OrdersTool()
    {
        try
        {
            this.mapper = new ObjectMapper();
            this.mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            this.exampleString = mapper.writeValueAsString(EXAMPLE_REQUEST);
            this.description = """
                    Datastore that contains order information including: 
                    - creation date of order
                    - progress of order
                    - postal tracking number
                    
                    Input should be a JSON object. Here is an example:
                    {example}
                    """.replace("{example}", exampleString);

            {
                PublicOrder order1 = new PublicOrder("PP1000", Date.from(Instant.ofEpochMilli(1766620800000L)), Status.MANUFACTURER_SENT);
                OrderRequest order1owner = new OrderRequest("David", "North", "1978-04-02", order1.orderId);
                orders.put(order1owner.key(), order1);
            }
        }
        catch (JsonProcessingException | ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name()
    {
        return "order_datastore";
    }

    @Override
    public String description()
    {
        return description;
    }

    @Override
    public String execute(String argument)
    {
        final OrderRequest orderRequest;
        try
        {
            orderRequest = mapper.readValue(argument, OrderRequest.class);
        }
        catch (JsonProcessingException e)
        {
            return asJsonString(
                    new ErrorResponse("Error, could not interpret request. Please provide input as a JSON object. Here is an example. " + exampleString));
        }

        final String key;
        try
        {
            key = orderRequest.key();
        }
        catch (ParseException e)
        {
            return asJsonString(new ErrorResponse("Error, invalid date."));
        }

        PublicOrder order = orders.get(key);
        return order == null
                ? asJsonString(new ErrorResponse("Warning, could not find order."))
                : asJsonString(order);
    }

    private <T> String asJsonString(T object)
    {
        try
        {
            return mapper.writeValueAsString(object);
        }
        catch (JsonProcessingException e)
        {
            return """
                    {
                    "error": "{error}"
                    }
                    """.replace("{error}", e.getMessage());
        }
    }
}
