package peter.finance_calendar.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UTCDateDeserializer extends JsonDeserializer<Date> {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        try {
            System.out.println("OMG OMG OMG");
            System.out.println(jsonParser.getText());
            Date parsedDate = sdf.parse(jsonParser.getText());

            // Verify the date by printing it in UTC
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            System.out.println("Parsed Date in UTC: " + utcFormat.format(parsedDate));

            return parsedDate;
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date in UTC", e);
        }
    }

}

