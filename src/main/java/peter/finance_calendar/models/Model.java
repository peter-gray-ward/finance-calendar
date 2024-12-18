package peter.finance_calendar.models;

import java.lang.reflect.Field;

public class Model {
	public String view() {
		Class<?> cls = this.getClass();
		StringBuilder log = new StringBuilder(cls.getName() + "\n");
		Field[] fields = cls.getDeclaredFields();

		for (Field field : fields) {
			field.setAccessible(true); // Allows access to private fields
			try {
				Object val = field.get(this); // Get the field value for 'this' instance
					log.append("\t").append(field.getName()).append(": ")
					.append(val == null ? "null" : val.toString()).append("\n");
	        } catch (IllegalAccessException e) {
	            log.append("\t").append(field.getName()).append(": <access denied>\n");
	        }
	    }
	    return log.toString();
	}

}

