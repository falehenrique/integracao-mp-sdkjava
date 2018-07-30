package sdk;

import com.google.gson.JsonObject;
import com.mercadopago.*;
import com.mercadopago.core.MPApiResponse;
import com.mercadopago.core.annotations.rest.PayloadType;
import com.mercadopago.exceptions.MPConfException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.exceptions.MPRestException;
import com.mercadopago.net.HttpMethod;
import com.mercadopago.net.MPRestClient;
import com.mercadopago.resources.Payment;
import com.mercadopago.resources.datastructures.payment.Payer;

import java.util.Random;

public class TesteIntegracao {

    public static void main(String[] args)throws MPException, MPConfException {
        String accessToken = "";
        String publicKey = "";

        if (System.getenv("ACCESS_TOKEN_TEST") == null && args.length == 0) {
            throw new MPConfException("Invalid parameter, send access_token and public_key with parameter or set in environment variables");
        } else if (System.getenv("ACCESS_TOKEN_TEST") == null && args.length > 0) {
            accessToken = args[0];
            publicKey =  args[1];
        } else {
            accessToken = System.getenv("ACCESS_TOKEN_TEST");
            publicKey =  System.getenv("PUBLIC_KEY_TEST");
        }

        MercadoPago.SDK.setAccessToken(accessToken);
        Payment payment = new Payment()
                .setTransactionAmount(100f)
                .setToken(getCardToken(CardResultExpected.APPROVED, publicKey))
                .setDescription("description")
                .setInstallments(1)
                .setPaymentMethodId("visa")
                .setPayer(new Payer()
                        .setEmail("test_user_7523855@testuser.com"));

        payment.save();
        System.out.println(payment.getStatus());
    }

    private static String getCardToken(CardResultExpected result, String publicKey) throws MPException {

        JsonObject jsonPayload = new JsonObject();
        Random rnd = new Random();

        int expiration_year = rnd.nextInt(20) + 2019;
        int expiration_month = 1 + rnd.nextInt(10) + 1;
        int security_code = rnd.nextInt(900) + 100;

        jsonPayload.addProperty("card_number", "4075595716483764");
        jsonPayload.addProperty("security_code", String.valueOf(security_code));
        jsonPayload.addProperty("expiration_year", expiration_year);
        jsonPayload.addProperty("expiration_month", expiration_month);

        JsonObject identification = new JsonObject();
        identification.addProperty("type", "Otro");
        identification.addProperty("number", "12345678");

        JsonObject cardHolder = new JsonObject();

        cardHolder.addProperty("name", result.getCardHolderName());
        cardHolder.add("identification", identification);

        jsonPayload.add("cardholder", cardHolder);

        MPApiResponse response;
        try {
            MPRestClient client = new MPRestClient();
            response = client.executeRequest(
                    HttpMethod.POST,
                    MercadoPago.SDK.getBaseUrl() + "/v1/card_tokens?public_key=" + publicKey ,
                    PayloadType.JSON,
                    jsonPayload,
                    null);
        } catch (MPRestException rex) {
            throw new MPException(rex);
        }
        return ((JsonObject) response.getJsonElementResponse()).get("id").getAsString();
    }

    private enum CardResultExpected {
        APPROVED("APRO"),
        PENDING("CONT");

        private String CardHolderName;

        private CardResultExpected(String cardHolderName){
            this.CardHolderName = cardHolderName;
        }

        public String getCardHolderName(){
            return this.CardHolderName;
        }

    }

}
