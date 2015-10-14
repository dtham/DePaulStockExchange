package price;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


public class PriceFactory {

  private static Map<String, Price> flyweights = new HashMap<>();

  public static Price makeLimitPrice(long value) {
    String key = value + "";
    Price p = PriceFactory.flyweights.get(key);
    if (p == null) {
      p = new Price(value);
      PriceFactory.flyweights.put(key, p);
    }
    return p;
  }

  public static Price makeLimitPrice(String str)
          throws NumberFormatException, IllegalArgumentException {
    long parsedValue = PriceFactory.parseDollarAmount(str);
    return PriceFactory.makeLimitPrice(parsedValue);
  }

  public static Price makeMarketPrice() {
    Price p = PriceFactory.flyweights.get("MKT");
    if (p == null) {
      p = new Price();
      PriceFactory.flyweights.put("MKT", p);
    }
    return p;
  }

  private static long parseDollarAmount(String str)
          throws NumberFormatException, IllegalArgumentException {
    if (str == null || str.isEmpty()) {
      return 0;
    }
    DecimalFormat formatter = new DecimalFormat("#.00");
    return Long.parseLong(formatter.format(Double.parseDouble(
            str.replaceAll("[^-.0-9]", ""))).replaceAll("\\.", ""));
  }
}