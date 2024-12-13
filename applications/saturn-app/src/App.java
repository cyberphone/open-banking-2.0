import org.webpki.cbor.*;
import org.webpki.ob2.common.COM2;
import org.webpki.saturn.common.BaseProperties;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println(new CBORArray().add(
            new CBORString(BaseProperties.ACCOUNT_ID_JSON)).toString());
        COM2.doit();
    }
}
